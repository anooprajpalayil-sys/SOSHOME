package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class EmergencyGuardianService : Service() {

    companion object {
        const val CHANNEL_ID = "safevoice_guardian_channel"
        const val NOTIFICATION_ID = 911
        const val ACTION_START_GUARDIAN = "ACTION_START_GUARDIAN"
        const val ACTION_STOP_GUARDIAN = "ACTION_STOP_GUARDIAN"
        const val ACTION_ACTIVE_SOS = "ACTION_ACTIVE_SOS"

        fun startGuardian(context: Context) {
            val intent = Intent(context, EmergencyGuardianService::class.java).apply {
                action = ACTION_START_GUARDIAN
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopGuardian(context: Context) {
            val intent = Intent(context, EmergencyGuardianService::class.java).apply {
                action = ACTION_STOP_GUARDIAN
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_GUARDIAN -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_ACTIVE_SOS -> {
                val notification = buildNotification(
                    title = "🚨 SafeVoice SOS ACTIVE",
                    content = "Live audio recording & GPS tracking are in progress.",
                    isEmergency = true
                )
                startForegroundWithTypes(notification)
            }
            else -> {
                val notification = buildNotification(
                    title = "🛡️ SafeVoice Guardian Active",
                    content = "Listening for your secret trigger phrase. Tap to open app.",
                    isEmergency = false
                )
                startForegroundWithTypes(notification)
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithTypes(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(title: String, content: String, isEmergency: Boolean): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(if (isEmergency) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_LOW)
            .setCategory(if (isEmergency) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeVoice Guardian & Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows active background listening and live emergency monitoring status"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
