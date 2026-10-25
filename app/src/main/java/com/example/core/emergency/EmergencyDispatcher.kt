package com.example.core.emergency

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import com.example.data.model.TrustedContact
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmergencyDispatcher(private val context: Context) {

    fun formatEmergencyMessage(
        userName: String,
        latitude: Double,
        longitude: Double,
        address: String,
        isTest: Boolean = false
    ): String {
        val mapLink = "https://maps.google.com/?q=$latitude,$longitude"
        val timeFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())

        val prefix = if (isTest) "[SAFEVOICE TEST ALERT]" else "🚨 EMERGENCY ALERT"

        return """
            $prefix: An emergency alert has been activated by $userName.
            Please check on them immediately!

            📍 Live Location: $mapLink
            🏠 Near: $address
            ⏰ Time: $timeFormatted
            🔴 Status: ${if (isTest) "Test Simulation" else "ACTIVE EMERGENCY"}

            Please call $userName or alert local emergency services (112).
        """.trimIndent()
    }

    @SuppressLint("MissingPermission")
    fun sendDirectSms(phoneNumber: String, message: String): Boolean {
        return try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            true
        } catch (e: Exception) {
            Log.e("EmergencyDispatcher", "Direct SMS failed: ${e.message}")
            false
        }
    }

    fun createSmsIntent(phoneNumber: String, message: String): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${phoneNumber.replace(" ", "")}")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createWhatsAppIntent(phoneNumber: String, message: String): Intent {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
        val encodedMsg = URLEncoder.encode(message, "UTF-8")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg")
        return Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun createCallIntent(phoneNumber: String): Intent {
        val cleanPhone = phoneNumber.replace(" ", "")
        return Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
