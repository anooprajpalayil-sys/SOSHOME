package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SafeVoiceApp
import com.example.core.audio.EvidenceAudioPlayer
import com.example.core.audio.EvidenceAudioRecorder
import com.example.core.audio.PlayerStatus
import com.example.core.audio.RecordingStatus
import com.example.core.auth.GoogleAuthManager
import com.example.core.emergency.EmergencyDispatcher
import com.example.core.hardware.SafetyToolsManager
import com.example.core.hardware.SafetyToolsState
import com.example.core.location.LocationData
import com.example.core.location.LocationTracker
import com.example.core.sensor.ShakeDetector
import com.example.core.voice.VoiceActivationManager
import com.example.data.firestore.FirestoreEmergencyAlert
import com.example.data.firestore.FirestoreEmergencyRepository
import com.example.data.model.EmergencyEvent
import com.example.data.model.SafetyCircleMember
import com.example.data.model.TrustedContact
import com.example.data.preferences.UserPreferencesManager
import com.example.data.repository.SafeVoiceRepository
import com.example.service.EmergencyGuardianService
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class UiState(
    val activeEvent: EmergencyEvent? = null,
    val activeCloudAlert: FirestoreEmergencyAlert? = null,
    val incomingAlert: FirestoreEmergencyAlert? = null,
    val isSosActive: Boolean = false,
    val isCountdownActive: Boolean = false,
    val countdownSecondsLeft: Int = 5,
    val pendingTriggerMethod: String = "",
    val isTestMode: Boolean = false,
    val contactsAlertedCount: Int = 0,
    val deliveryStatusSummary: Map<String, String> = emptyMap(),
    val responderAcknowledgments: Map<String, String> = emptyMap(),
    val fakeCallActive: Boolean = false,
    val fakeCallCountdown: Int = 0,
    val snackbarMessage: String? = null
)

class SafeVoiceViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SafeVoiceApp
    val repository: SafeVoiceRepository = app.repository
    val prefs: UserPreferencesManager = app.preferences

    val locationTracker = LocationTracker(application)
    val audioRecorder = EvidenceAudioRecorder(application)
    val audioPlayer = EvidenceAudioPlayer(application)
    val safetyTools = SafetyToolsManager(application)
    val dispatcher = EmergencyDispatcher(application)
    val authManager = GoogleAuthManager(application)
    val firestoreRepository = FirestoreEmergencyRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser

    val contacts: StateFlow<List<TrustedContact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val circleMembers: StateFlow<List<SafetyCircleMember>> = repository.allCircleMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyEvents: StateFlow<List<EmergencyEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val location: StateFlow<LocationData> = locationTracker.currentLocation
    val recordingStatus: StateFlow<RecordingStatus> = audioRecorder.status
    val playerStatus: StateFlow<PlayerStatus> = audioPlayer.status
    val safetyToolsState: StateFlow<SafetyToolsState> = safetyTools.state

    lateinit var voiceManager: VoiceActivationManager
        private set
    lateinit var shakeDetector: ShakeDetector
        private set

    private var countdownJob: Job? = null
    private var fakeCallJob: Job? = null
    private var cloudLocationStreamJob: Job? = null
    private var cloudAlertObserverJob: Job? = null
    private var incomingAlertsJob: Job? = null

    init {
        authManager.attemptSilentAutoSignIn(viewModelScope)

        voiceManager = VoiceActivationManager(application) { spokenText ->
            triggerSosCountdown("VOICE_TRIGGER: \"$spokenText\"")
        }
        voiceManager.updateConfig(prefs.secretPhrase, prefs.secretLanguage)

        shakeDetector = ShakeDetector(application) {
            if (prefs.shakeToSosEnabled && !_uiState.value.isSosActive && !_uiState.value.isCountdownActive) {
                triggerSosCountdown("SHAKE_TRIGGER")
            }
        }
        shakeDetector.thresholdG = prefs.shakeThreshold

        if (prefs.shakeToSosEnabled) {
            shakeDetector.start()
        }

        if (prefs.isGuardianListeningActive) {
            startGuardianListening()
        }

        // Listen for incoming alerts from connected family circle / contacts
        viewModelScope.launch {
            authManager.currentUser.collect { user ->
                if (user != null) {
                    startListeningForIncomingAlerts(user.uid)
                }
            }
        }
    }

    private fun startListeningForIncomingAlerts(userId: String) {
        incomingAlertsJob?.cancel()
        incomingAlertsJob = viewModelScope.launch {
            firestoreRepository.observeIncomingEmergencyAlerts(userId).collect { incomingList ->
                val activeIncoming = incomingList.firstOrNull { it.senderId != userId }
                if (activeIncoming != null && activeIncoming.alertId != _uiState.value.incomingAlert?.alertId) {
                    // High-priority emergency notification for recipient
                    safetyTools.triggerSosVibration()
                    _uiState.value = _uiState.value.copy(incomingAlert = activeIncoming)
                } else if (activeIncoming == null && _uiState.value.incomingAlert != null) {
                    _uiState.value = _uiState.value.copy(incomingAlert = null)
                }
            }
        }
    }

    fun acknowledgeIncomingAlert(message: String) {
        val incoming = _uiState.value.incomingAlert ?: return
        val user = authManager.currentUser.value
        val myName = user?.displayName ?: prefs.userName

        viewModelScope.launch {
            try {
                firestoreRepository.acknowledgeAlert(
                    alertId = incoming.alertId,
                    responderUid = user?.uid ?: "user_${System.currentTimeMillis()}",
                    responderName = myName,
                    message = message
                )
                showSnackbar("Acknowledgment sent to ${incoming.senderName}!")
            } catch (e: Exception) {
                showSnackbar("Failed to send acknowledgment: ${e.message}")
            }
        }
    }

    fun dismissIncomingAlert() {
        _uiState.value = _uiState.value.copy(incomingAlert = null)
    }

    // --- Voice Guardian Mode ---
    fun toggleGuardianListening() {
        val newState = !prefs.isGuardianListeningActive
        prefs.isGuardianListeningActive = newState
        if (newState) {
            startGuardianListening()
            showSnackbar("Guardian Voice Protection activated")
        } else {
            stopGuardianListening()
            showSnackbar("Guardian Voice Protection paused")
        }
    }

    private fun startGuardianListening() {
        voiceManager.updateConfig(prefs.secretPhrase, prefs.secretLanguage)
        voiceManager.startListening(continuous = true, testOnly = false)
        EmergencyGuardianService.startGuardian(getApplication())
    }

    private fun stopGuardianListening() {
        voiceManager.stopListening()
        EmergencyGuardianService.stopGuardian(getApplication())
    }

    fun startVoiceTest() {
        voiceManager.updateConfig(prefs.secretPhrase, prefs.secretLanguage)
        voiceManager.startListening(continuous = false, testOnly = true)
    }

    fun stopVoiceTest() {
        voiceManager.stopListening()
    }

    // --- Emergency Trigger & Cancellation Countdown ---
    fun triggerSosCountdown(triggerMethod: String, isTest: Boolean = false) {
        // Prevent duplicate alerts from repeated taps while already active
        if (_uiState.value.isSosActive) {
            showSnackbar("Emergency is already ACTIVE and broadcasting!")
            return
        }

        val seconds = if (isTest) 3 else prefs.cancellationCountdownSeconds
        _uiState.value = _uiState.value.copy(
            isCountdownActive = true,
            countdownSecondsLeft = seconds,
            pendingTriggerMethod = triggerMethod,
            isTestMode = isTest
        )

        safetyTools.triggerSosVibration()

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _uiState.value = _uiState.value.copy(countdownSecondsLeft = remaining)
                safetyTools.triggerSosVibration()
            }
            if (isActive) {
                _uiState.value = _uiState.value.copy(isCountdownActive = false)
                startActiveEmergencyImmediate(triggerMethod, isTest)
            }
        }
    }

    fun cancelSosCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        safetyTools.cancelVibration()
        _uiState.value = _uiState.value.copy(
            isCountdownActive = false,
            pendingTriggerMethod = "",
            isTestMode = false
        )
        showSnackbar("SOS cancelled. You are safe.")
    }

    private fun startActiveEmergencyImmediate(triggerMethod: String, isTest: Boolean) {
        viewModelScope.launch {
            locationTracker.startLiveTracking()
            val currentLoc = locationTracker.currentLocation.value

            // 1. Create local Room event
            val event = repository.createEmergencyEvent(
                triggerMethod = triggerMethod,
                latitude = currentLoc.latitude,
                longitude = currentLoc.longitude,
                accuracyMeters = currentLoc.accuracy,
                address = currentLoc.address,
                isTestRun = isTest
            )

            // 2. Start audio evidence recording
            var recordedPath: String? = null
            if (prefs.autoRecordEvidence) {
                recordedPath = audioRecorder.startRecording(event.id)
            }

            // 3. Start foreground service
            EmergencyGuardianService.startGuardian(getApplication())

            // 4. Sound Siren / Strobe if configured
            if (prefs.loudSirenOnSos && !isTest) {
                safetyTools.startSiren()
            }
            if (prefs.strobeFlashlightOnSos && !isTest) {
                safetyTools.startStrobe()
            }

            // 5. Gather authorized recipients
            val contactList = contacts.value
            val circleList = circleMembers.value
            val authorizedUids = mutableListOf<String>()
            val deliveryMap = mutableMapOf<String, String>()

            for (c in contactList) {
                val key = c.phone.replace(" ", "")
                deliveryMap[key] = "PENDING"
            }
            for (m in circleList) {
                authorizedUids.add(m.id)
                deliveryMap[m.id] = "DELIVERED_CLOUD"
            }

            val user = authManager.currentUser.value
            val verifiedSenderId = user?.uid ?: "user_${System.currentTimeMillis()}"
            val verifiedSenderName = user?.displayName ?: prefs.userName
            val verifiedSenderPhone = prefs.userPhone

            // 6. Create backend Firestore emergency alert
            val cloudAlert = FirestoreEmergencyAlert(
                id = event.id,
                alertId = event.id,
                senderId = verifiedSenderId,
                senderName = verifiedSenderName,
                senderPhone = verifiedSenderPhone,
                circleId = prefs.familyCircleCode,
                status = "ACTIVE",
                latitude = currentLoc.latitude,
                longitude = currentLoc.longitude,
                accuracy = currentLoc.accuracy.toDouble(),
                address = currentLoc.address,
                triggerMethod = triggerMethod,
                authorizedUids = authorizedUids,
                deliveryStatuses = deliveryMap
            )

            if (!isTest) {
                try {
                    firestoreRepository.publishEmergencyAlert(cloudAlert)
                    startCloudLocationStream(event.id)
                    observeCloudAlertStatus(event.id)
                } catch (e: Exception) {
                    // Fallback to local SMS/offline queue
                }
            }

            // 7. Dispatch SMS fallback
            var notifiedCount = 0
            val msg = dispatcher.formatEmergencyMessage(
                userName = verifiedSenderName,
                latitude = currentLoc.latitude,
                longitude = currentLoc.longitude,
                address = currentLoc.address,
                isTest = isTest
            )

            for (contact in contactList) {
                if (contact.sendSms) {
                    val sent = dispatcher.sendDirectSms(contact.phone, msg)
                    if (sent) {
                        notifiedCount++
                        deliveryMap[contact.phone.replace(" ", "")] = "SMS_DISPATCHED"
                    }
                }
            }

            val updatedEvent = event.copy(
                audioFilePath = recordedPath,
                contactsNotifiedCount = notifiedCount
            )
            repository.updateEvent(updatedEvent)

            _uiState.value = _uiState.value.copy(
                activeEvent = updatedEvent,
                activeCloudAlert = cloudAlert,
                isSosActive = true,
                contactsAlertedCount = notifiedCount,
                deliveryStatusSummary = deliveryMap,
                isTestMode = isTest
            )
        }
    }

    private fun startCloudLocationStream(alertId: String) {
        cloudLocationStreamJob?.cancel()
        cloudLocationStreamJob = viewModelScope.launch {
            locationTracker.currentLocation.collect { loc ->
                if (_uiState.value.isSosActive) {
                    firestoreRepository.updateLiveLocation(
                        alertId = alertId,
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracy = loc.accuracy.toDouble(),
                        address = loc.address
                    )
                }
            }
        }
    }

    private fun observeCloudAlertStatus(alertId: String) {
        cloudAlertObserverJob?.cancel()
        cloudAlertObserverJob = viewModelScope.launch {
            firestoreRepository.observeActiveAlert(alertId).collect { cloudAlert ->
                if (cloudAlert != null) {
                    val acks = mutableMapOf<String, String>()
                    cloudAlert.acknowledgments.forEach { (uid, map) ->
                        val name = map["name"] as? String ?: "Responder"
                        val msg = map["message"] as? String ?: "Acknowledged"
                        acks[name] = msg
                    }

                    _uiState.value = _uiState.value.copy(
                        activeCloudAlert = cloudAlert,
                        deliveryStatusSummary = cloudAlert.deliveryStatuses,
                        responderAcknowledgments = acks
                    )
                }
            }
        }
    }

    fun resolveActiveEmergency(notes: String = "Resolved safely by user") {
        viewModelScope.launch {
            val active = _uiState.value.activeEvent
            val recordedFilePath = audioRecorder.stopRecording()
            val recStatus = audioRecorder.status.value

            locationTracker.stopLiveTracking()
            safetyTools.release()
            cloudLocationStreamJob?.cancel()
            cloudLocationStreamJob = null
            cloudAlertObserverJob?.cancel()
            cloudAlertObserverJob = null

            if (!prefs.isGuardianListeningActive) {
                EmergencyGuardianService.stopGuardian(getApplication())
            }

            if (active != null) {
                // Update Cloud status to RESOLVED
                try {
                    firestoreRepository.resolveEmergency(active.id)
                } catch (e: Exception) {
                    // Local fallback
                }

                val updated = active.copy(
                    status = "RESOLVED",
                    audioFilePath = recordedFilePath ?: active.audioFilePath,
                    audioDurationSeconds = recStatus.elapsedSeconds,
                    notes = notes
                )
                repository.updateEvent(updated)
            }

            _uiState.value = _uiState.value.copy(
                activeEvent = null,
                activeCloudAlert = null,
                isSosActive = false,
                isTestMode = false,
                responderAcknowledgments = emptyMap(),
                deliveryStatusSummary = emptyMap()
            )
            showSnackbar("Emergency resolved. All responders notified that you are safe.")
        }
    }

    // --- Safety Tools ---
    fun scheduleFakeCall(delaySeconds: Int = prefs.fakeCallDelaySeconds, callerName: String = prefs.fakeCallerName) {
        fakeCallJob?.cancel()
        _uiState.value = _uiState.value.copy(fakeCallActive = false, fakeCallCountdown = delaySeconds)
        showSnackbar("Fake call scheduled from $callerName in $delaySeconds seconds")

        fakeCallJob = viewModelScope.launch {
            var left = delaySeconds
            while (left > 0 && isActive) {
                delay(1000L)
                left--
                _uiState.value = _uiState.value.copy(fakeCallCountdown = left)
            }
            if (isActive) {
                _uiState.value = _uiState.value.copy(fakeCallActive = true)
                safetyTools.triggerSosVibration()
            }
        }
    }

    fun dismissFakeCall() {
        fakeCallJob?.cancel()
        fakeCallJob = null
        safetyTools.cancelVibration()
        _uiState.value = _uiState.value.copy(fakeCallActive = false, fakeCallCountdown = 0)
    }

    fun toggleSiren() = safetyTools.toggleSiren()
    fun toggleStrobe() = safetyTools.toggleStrobe()
    fun toggleFlashlight() = safetyTools.toggleFlashlight()

    // --- Contacts & Circle ---
    fun addContact(contact: TrustedContact) {
        viewModelScope.launch {
            repository.addContact(contact)
            showSnackbar("Added contact ${contact.name}")
        }
    }

    fun updateContact(contact: TrustedContact) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

    fun deleteContact(contact: TrustedContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            showSnackbar("Deleted contact")
        }
    }

    fun addCircleMember(member: SafetyCircleMember) {
        viewModelScope.launch {
            repository.addCircleMember(member)
            showSnackbar("Added ${member.name} to Safety Circle")
        }
    }

    fun respondToCircleEmergency(member: SafetyCircleMember) {
        viewModelScope.launch {
            val updated = member.copy(
                status = "Responding",
                isResponding = true
            )
            repository.updateCircleMember(updated)
            showSnackbar("Response beacon sent to ${member.name}!")
        }
    }

    fun updateSecretPhrase(phrase: String, language: String) {
        prefs.secretPhrase = phrase
        prefs.secretLanguage = language
        voiceManager.updateConfig(phrase, language)
        showSnackbar("Secret voice phrase updated to: \"$phrase\"")
    }

    fun deleteEvent(event: EmergencyEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            showSnackbar("Evidence record deleted")
        }
    }

    fun showSnackbar(message: String) {
        _uiState.value = _uiState.value.copy(snackbarMessage = message)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        shakeDetector.stop()
        voiceManager.stopListening()
        audioRecorder.stopRecording()
        audioPlayer.stop()
        safetyTools.release()
        locationTracker.stopLiveTracking()
        cloudLocationStreamJob?.cancel()
        cloudAlertObserverJob?.cancel()
        incomingAlertsJob?.cancel()
    }
}
