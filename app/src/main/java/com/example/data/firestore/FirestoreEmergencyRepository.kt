package com.example.data.firestore

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreEmergencyRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before accessing emergency backend.")
    }

    suspend fun publishEmergencyAlert(alert: FirestoreEmergencyAlert): String {
        val uid = requireUserId()
        val docRef = db.collection("emergency_alerts").document(alert.alertId)

        val payload = hashMapOf<String, Any>(
            "alertId" to alert.alertId,
            "senderId" to uid,
            "senderName" to alert.senderName,
            "senderPhone" to alert.senderPhone,
            "circleId" to alert.circleId,
            "status" to "ACTIVE",
            "latitude" to alert.latitude,
            "longitude" to alert.longitude,
            "accuracy" to alert.accuracy,
            "address" to alert.address,
            "triggerMethod" to alert.triggerMethod,
            "authorizedUids" to alert.authorizedUids,
            "acknowledgments" to alert.acknowledgments,
            "deliveryStatuses" to alert.deliveryStatuses,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            return alert.alertId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateLiveLocation(
        alertId: String,
        latitude: Double,
        longitude: Double,
        accuracy: Double,
        address: String
    ) {
        val docRef = db.collection("emergency_alerts").document(alertId)
        val updates = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "accuracy" to accuracy,
            "address" to address,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    suspend fun updateDeliveryStatus(alertId: String, contactKey: String, status: String) {
        val docRef = db.collection("emergency_alerts").document(alertId)
        try {
            docRef.update("deliveryStatuses.$contactKey", status, "updatedAt", FieldValue.serverTimestamp()).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    suspend fun resolveEmergency(alertId: String) {
        val docRef = db.collection("emergency_alerts").document(alertId)
        val updates = mapOf(
            "status" to "RESOLVED",
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun acknowledgeAlert(
        alertId: String,
        responderUid: String,
        responderName: String,
        message: String
    ) {
        val docRef = db.collection("emergency_alerts").document(alertId)
        val ackData = mapOf(
            "name" to responderName,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            docRef.update(
                "acknowledgments.$responderUid", ackData,
                "deliveryStatuses.$responderUid", "ACKNOWLEDGED",
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeActiveAlert(alertId: String): Flow<FirestoreEmergencyAlert?> {
        val docRef = db.collection("emergency_alerts").document(alertId)
        return docRef.snapshots()
            .map { it.toObject(FirestoreEmergencyAlert::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, docRef.path)
                emit(null)
            }
    }

    fun observeIncomingEmergencyAlerts(currentUserId: String): Flow<List<FirestoreEmergencyAlert>> {
        val query = db.collection("emergency_alerts")
            .whereArrayContains("authorizedUids", currentUserId)
            .whereEqualTo("status", "ACTIVE")

        return query.snapshots()
            .map { snapshot -> snapshot.toObjects(FirestoreEmergencyAlert::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "emergency_alerts")
                emit(emptyList())
            }
    }
}
