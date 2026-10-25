package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.SafeVoiceViewModel
import com.example.ui.screens.ActiveSosScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.CountdownCancelDialog
import com.example.ui.screens.EvidenceVaultScreen
import com.example.ui.screens.FakeCallScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncomingEmergencyAlertScreen
import com.example.ui.screens.PermissionsPrivacyScreen
import com.example.ui.screens.SafetyCircleScreen
import com.example.ui.screens.SafetyToolsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceActivationScreen
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GuardianGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "SafeVoice")
    object Voice : Screen("voice", "Voice Activation")
    object Contacts : Screen("contacts", "Contacts")
    object Circle : Screen("circle", "Safety Circle")
    object Vault : Screen("vault", "Evidence Vault")
    object Tools : Screen("tools", "Safety Tools")
    object Permissions : Screen("permissions", "Permissions")
    object Settings : Screen("settings", "Settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: SafeVoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestInitialPermissions()

        setContent {
            MyApplicationTheme {
                val currentUser by viewModel.currentUser.collectAsState()

                if (currentUser == null) {
                    AuthScreen(
                        authManager = viewModel.authManager,
                        onAuthSuccess = { }
                    )
                } else {
                    SafeVoiceMainApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun requestInitialPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permsCheck(permissions, Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
            launcher.launch(missing.toTypedArray())
        }
    }

    private fun permsCheck(list: MutableList<String>, perm: String) {
        list.add(perm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeVoiceMainApp(viewModel: SafeVoiceViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val locationData by viewModel.location.collectAsState()
    val recordingStatus by viewModel.recordingStatus.collectAsState()
    val playerStatus by viewModel.playerStatus.collectAsState()
    val safetyToolsState by viewModel.safetyToolsState.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val circleMembers by viewModel.circleMembers.collectAsState()
    val events by viewModel.emergencyEvents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Handle back button when not on Home
    if (currentScreen != Screen.Home) {
        BackHandler {
            currentScreen = Screen.Home
        }
    }

    // Handle snackbar messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = currentScreen.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = PureWhite
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Navy900,
                        titleContentColor = PureWhite,
                        actionIconContentColor = PureWhite
                    ),
                    actions = {
                        IconButton(onClick = { currentScreen = Screen.Tools }) {
                            Icon(Icons.Default.Security, contentDescription = "Safety Tools")
                        }
                        IconButton(onClick = { currentScreen = Screen.Permissions }) {
                            Icon(Icons.Default.Lock, contentDescription = "Permissions")
                        }
                        IconButton(onClick = { currentScreen = Screen.Settings }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { viewModel.authManager.signOut(scope) }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = PureWhite.copy(alpha = 0.8f))
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Navy900,
                    contentColor = PureWhite,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.Home,
                        onClick = { currentScreen = Screen.Home },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("SOS", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Voice,
                        onClick = { currentScreen = Screen.Voice },
                        icon = { Icon(Icons.Default.Mic, contentDescription = "Voice") },
                        label = { Text("Voice", fontSize = 11.sp) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_voice")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Contacts,
                        onClick = { currentScreen = Screen.Contacts },
                        icon = { Icon(Icons.Default.People, contentDescription = "Contacts") },
                        label = { Text("Contacts", fontSize = 11.sp) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_contacts")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Circle,
                        onClick = { currentScreen = Screen.Circle },
                        icon = { Icon(Icons.Default.Group, contentDescription = "Circle") },
                        label = { Text("Circle", fontSize = 11.sp) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_circle")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Vault,
                        onClick = { currentScreen = Screen.Vault },
                        icon = { Icon(Icons.Default.Lock, contentDescription = "Vault") },
                        label = { Text("Vault", fontSize = 11.sp) },
                        colors = navItemColors(),
                        modifier = Modifier.testTag("nav_vault")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen(
                        isGuardianActive = viewModel.prefs.isGuardianListeningActive,
                        secretPhrase = viewModel.prefs.secretPhrase,
                        secretLanguage = viewModel.prefs.secretLanguage,
                        locationData = locationData,
                        contacts = contacts,
                        circleMembers = circleMembers,
                        safetyToolsState = safetyToolsState,
                        onTriggerSos = {
                            viewModel.triggerSosCountdown("MANUAL_SOS_BUTTON", isTest = false)
                        },
                        onTriggerTestMode = {
                            viewModel.triggerSosCountdown("TEST_SOS_DRILL", isTest = true)
                        },
                        onToggleGuardian = { viewModel.toggleGuardianListening() },
                        onNavigateToVoiceConfig = { currentScreen = Screen.Voice },
                        onNavigateToContacts = { currentScreen = Screen.Contacts },
                        onNavigateToCircle = { currentScreen = Screen.Circle },
                        onNavigateToSafetyTools = { currentScreen = Screen.Tools },
                        onToggleSiren = { viewModel.toggleSiren() },
                        onToggleStrobe = { viewModel.toggleStrobe() },
                        onScheduleFakeCall = { viewModel.scheduleFakeCall() }
                    )

                    Screen.Voice -> VoiceActivationScreen(
                        voiceManager = viewModel.voiceManager,
                        isGuardianActive = viewModel.prefs.isGuardianListeningActive,
                        currentPhrase = viewModel.prefs.secretPhrase,
                        currentLanguage = viewModel.prefs.secretLanguage,
                        onSavePhrase = { phrase, lang ->
                            viewModel.updateSecretPhrase(phrase, lang)
                        },
                        onToggleGuardian = { viewModel.toggleGuardianListening() },
                        onStartTest = { viewModel.startVoiceTest() },
                        onStopTest = { viewModel.stopVoiceTest() }
                    )

                    Screen.Contacts -> ContactsScreen(
                        contacts = contacts,
                        dispatcher = viewModel.dispatcher,
                        userName = currentUser?.displayName ?: viewModel.prefs.userName,
                        onAddContact = { viewModel.addContact(it) },
                        onUpdateContact = { viewModel.updateContact(it) },
                        onDeleteContact = { viewModel.deleteContact(it) }
                    )

                    Screen.Circle -> SafetyCircleScreen(
                        members = circleMembers,
                        circleCode = viewModel.prefs.familyCircleCode,
                        onAddMember = { viewModel.addCircleMember(it) },
                        onRespondToEmergency = { viewModel.respondToCircleEmergency(it) }
                    )

                    Screen.Vault -> EvidenceVaultScreen(
                        events = events,
                        playerStatus = playerStatus,
                        onPlayAudio = { viewModel.audioPlayer.playAudio(it) },
                        onPauseAudio = { viewModel.audioPlayer.pause() },
                        onSeekAudio = { viewModel.audioPlayer.seekTo(it) },
                        onDeleteEvent = { viewModel.deleteEvent(it) }
                    )

                    Screen.Tools -> SafetyToolsScreen(
                        prefs = viewModel.prefs,
                        safetyToolsState = safetyToolsState,
                        onToggleSiren = { viewModel.toggleSiren() },
                        onToggleStrobe = { viewModel.toggleStrobe() },
                        onToggleFlashlight = { viewModel.toggleFlashlight() },
                        onScheduleFakeCall = { delay, name -> viewModel.scheduleFakeCall(delay, name) },
                        onTriggerTestDrill = {
                            viewModel.triggerSosCountdown("TEST_SOS_DRILL", isTest = true)
                        },
                        onNavigateToPermissions = { currentScreen = Screen.Permissions }
                    )

                    Screen.Permissions -> PermissionsPrivacyScreen()

                    Screen.Settings -> SettingsScreen(
                        prefs = viewModel.prefs,
                        onSaveProfile = { name, phone ->
                            viewModel.showSnackbar("Profile updated for $name")
                        }
                    )
                }
            }
        }

        // --- Active Emergency Fullscreen Dashboard Overlay ---
        if (uiState.isSosActive) {
            ActiveSosScreen(
                event = uiState.activeEvent,
                locationData = locationData,
                recordingStatus = recordingStatus,
                contactsAlertedCount = uiState.contactsAlertedCount,
                contacts = contacts,
                safetyToolsState = safetyToolsState,
                isTestMode = uiState.isTestMode,
                responderAcknowledgments = uiState.responderAcknowledgments,
                deliveryStatusSummary = uiState.deliveryStatusSummary,
                onResolveEmergency = { notes ->
                    viewModel.resolveActiveEmergency(notes)
                },
                onToggleSiren = { viewModel.toggleSiren() },
                onToggleStrobe = { viewModel.toggleStrobe() }
            )
        }

        // --- Accidental Trigger Cancellation Countdown Overlay ---
        if (uiState.isCountdownActive) {
            CountdownCancelDialog(
                secondsRemaining = uiState.countdownSecondsLeft,
                triggerMethod = uiState.pendingTriggerMethod,
                isTestMode = uiState.isTestMode,
                onCancel = { viewModel.cancelSosCountdown() }
            )
        }

        // --- Recipient Experience: Prominent Incoming Emergency Alert Screen ---
        uiState.incomingAlert?.let { incoming ->
            IncomingEmergencyAlertScreen(
                alert = incoming,
                onAcknowledge = { msg ->
                    viewModel.acknowledgeIncomingAlert(msg)
                },
                onDismiss = {
                    viewModel.dismissIncomingAlert()
                }
            )
        }

        // --- Fake Call Simulator Overlay ---
        if (uiState.fakeCallActive) {
            FakeCallScreen(
                callerName = viewModel.prefs.fakeCallerName,
                onDismiss = { viewModel.dismissFakeCall() }
            )
        }
    }
}

@Composable
fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PureWhite,
    selectedTextColor = PureWhite,
    unselectedIconColor = Color.LightGray,
    unselectedTextColor = Color.LightGray,
    indicatorColor = CrimsonRed
)
