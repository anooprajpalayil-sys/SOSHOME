package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_events")
data class EmergencyEvent(
    @PrimaryKey
    val id: String, // UUID
    val timestamp: Long = System.currentTimeMillis(),
    val triggerMethod: String, // "VOICE_TRIGGER", "SOS_BUTTON", "SHAKE_TRIGGER", "TEST_MODE"
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val address: String,
    val status: String, // "ACTIVE", "RESOLVED", "CANCELLED", "TEST_RUN"
    val audioFilePath: String? = null,
    val audioDurationSeconds: Int = 0,
    val contactsNotifiedCount: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "trusted_contacts")
data class TrustedContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val relationship: String, // "Mother", "Father", "Spouse", "Sister", "Brother", "Friend", "Helpline"
    val isPrimary: Boolean = false,
    val sendSms: Boolean = true,
    val sendWhatsApp: Boolean = true,
    val directCall: Boolean = false
)

@Entity(tableName = "safety_circle_members")
data class SafetyCircleMember(
    @PrimaryKey
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String,
    val batteryPercent: Int = 85,
    val latitude: Double = 12.9716,
    val longitude: Double = 77.5946,
    val lastLocationTime: Long = System.currentTimeMillis(),
    val status: String = "Safe", // "Safe", "In Transit", "SOS Active", "Responding"
    val isResponding: Boolean = false
)
