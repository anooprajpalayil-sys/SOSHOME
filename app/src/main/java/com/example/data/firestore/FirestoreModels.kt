package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUser(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val circleId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreEmergencyAlert(
    @DocumentId val id: String = "",
    val alertId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhone: String = "",
    val circleId: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "RESOLVED"
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Double = 0.0,
    val address: String = "",
    val triggerMethod: String = "MANUAL_SOS", // "MANUAL_SOS", "VOICE_TRIGGER", "SHAKE_TRIGGER", "TEST_DRILL"
    val authorizedUids: List<String> = emptyList(),
    val acknowledgments: Map<String, Map<String, Any>> = emptyMap(),
    val deliveryStatuses: Map<String, String> = emptyMap(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class AlertAcknowledgment(
    val responderUid: String = "",
    val responderName: String = "",
    val message: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)
