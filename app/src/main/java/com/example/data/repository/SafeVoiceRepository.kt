package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.EmergencyEvent
import com.example.data.model.SafetyCircleMember
import com.example.data.model.TrustedContact
import com.example.data.preferences.UserPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class SafeVoiceRepository(
    private val context: Context,
    private val database: AppDatabase,
    val prefs: UserPreferencesManager
) {
    private val eventDao = database.emergencyEventDao()
    private val contactDao = database.trustedContactDao()
    private val circleDao = database.safetyCircleDao()

    val allEvents: Flow<List<EmergencyEvent>> = eventDao.getAllEvents()
    val activeEvent: Flow<EmergencyEvent?> = eventDao.getActiveEvent()
    val allContacts: Flow<List<TrustedContact>> = contactDao.getAllContacts()
    val allCircleMembers: Flow<List<SafetyCircleMember>> = circleDao.getAllMembers()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultsIfEmpty()
        }
    }

    private suspend fun seedDefaultsIfEmpty() {
        if (contactDao.getContactCount() == 0) {
            contactDao.insertContact(
                TrustedContact(
                    name = "Mom (Priya)",
                    phone = "+91 98450 11223",
                    relationship = "Mother",
                    isPrimary = true,
                    sendSms = true,
                    sendWhatsApp = true,
                    directCall = true
                )
            )
            contactDao.insertContact(
                TrustedContact(
                    name = "Dad (Mathew)",
                    phone = "+91 98450 44556",
                    relationship = "Father",
                    isPrimary = false,
                    sendSms = true,
                    sendWhatsApp = true,
                    directCall = false
                )
            )
            contactDao.insertContact(
                TrustedContact(
                    name = "Women Helpline",
                    phone = "1091",
                    relationship = "Emergency Support",
                    isPrimary = false,
                    sendSms = true,
                    sendWhatsApp = false,
                    directCall = false
                )
            )
            contactDao.insertContact(
                TrustedContact(
                    name = "National Emergency",
                    phone = "112",
                    relationship = "Police / EMS",
                    isPrimary = false,
                    sendSms = true,
                    sendWhatsApp = false,
                    directCall = false
                )
            )
        }

        if (circleDao.getMemberCount() == 0) {
            circleDao.insertMember(
                SafetyCircleMember(
                    id = "mem_1",
                    name = "Priya Mathew (Mom)",
                    relationship = "Mother",
                    phone = "+91 98450 11223",
                    batteryPercent = 92,
                    latitude = 12.9716,
                    longitude = 77.5946,
                    lastLocationTime = System.currentTimeMillis() - 1000 * 60 * 4,
                    status = "Safe"
                )
            )
            circleDao.insertMember(
                SafetyCircleMember(
                    id = "mem_2",
                    name = "Mathew Thomas (Dad)",
                    relationship = "Father",
                    phone = "+91 98450 44556",
                    batteryPercent = 64,
                    latitude = 12.9750,
                    longitude = 77.5990,
                    lastLocationTime = System.currentTimeMillis() - 1000 * 60 * 12,
                    status = "In Transit"
                )
            )
            circleDao.insertMember(
                SafetyCircleMember(
                    id = "mem_3",
                    name = "Rahul (Brother)",
                    relationship = "Brother",
                    phone = "+91 98450 77889",
                    batteryPercent = 48,
                    latitude = 12.9680,
                    longitude = 77.5890,
                    lastLocationTime = System.currentTimeMillis() - 1000 * 60 * 2,
                    status = "Safe"
                )
            )
        }
    }

    suspend fun createEmergencyEvent(
        triggerMethod: String,
        latitude: Double,
        longitude: Double,
        accuracyMeters: Float,
        address: String,
        isTestRun: Boolean = false
    ): EmergencyEvent {
        val event = EmergencyEvent(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            triggerMethod = triggerMethod,
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracyMeters,
            address = address,
            status = if (isTestRun) "TEST_RUN" else "ACTIVE",
            audioFilePath = null,
            audioDurationSeconds = 0,
            contactsNotifiedCount = 0,
            notes = if (isTestRun) "SafeVoice Test Workflow Verification" else ""
        )
        eventDao.insertEvent(event)
        return event
    }

    suspend fun updateEvent(event: EmergencyEvent) {
        eventDao.updateEvent(event)
    }

    suspend fun resolveActiveEmergency(eventId: String, resolutionNotes: String = "Resolved safely by user") {
        val event = eventDao.getEventById(eventId) ?: return
        val updated = event.copy(
            status = "RESOLVED",
            notes = resolutionNotes
        )
        eventDao.updateEvent(updated)
    }

    suspend fun cancelActiveEmergency(eventId: String) {
        val event = eventDao.getEventById(eventId) ?: return
        val updated = event.copy(status = "CANCELLED", notes = "Accidental trigger cancelled by user")
        eventDao.updateEvent(updated)
    }

    suspend fun deleteEvent(event: EmergencyEvent) {
        eventDao.deleteEvent(event)
    }

    suspend fun addContact(contact: TrustedContact): Long {
        return contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: TrustedContact) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: TrustedContact) {
        contactDao.deleteContact(contact)
    }

    suspend fun addCircleMember(member: SafetyCircleMember) {
        circleDao.insertMember(member)
    }

    suspend fun updateCircleMember(member: SafetyCircleMember) {
        circleDao.updateMember(member)
    }

    suspend fun deleteCircleMember(member: SafetyCircleMember) {
        circleDao.deleteMember(member)
    }
}
