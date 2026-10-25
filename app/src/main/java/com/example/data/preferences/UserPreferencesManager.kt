package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("safevoice_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString("user_name", "Sarah Mathew") ?: "Sarah Mathew"
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userPhone: String
        get() = prefs.getString("user_phone", "+91 98450 12345") ?: "+91 98450 12345"
        set(value) = prefs.edit().putString("user_phone", value).apply()

    var secretPhrase: String
        get() = prefs.getString("secret_phrase", "Save me God") ?: "Save me God"
        set(value) = prefs.edit().putString("secret_phrase", value.trim()).apply()

    var secretLanguage: String
        get() = prefs.getString("secret_language", "en-US") ?: "en-US" // "en-US" or "ml-IN"
        set(value) = prefs.edit().putString("secret_language", value).apply()

    var cancellationCountdownSeconds: Int
        get() = prefs.getInt("countdown_seconds", 5)
        set(value) = prefs.edit().putInt("countdown_seconds", value).apply()

    var shakeToSosEnabled: Boolean
        get() = prefs.getBoolean("shake_enabled", true)
        set(value) = prefs.edit().putBoolean("shake_enabled", value).apply()

    var shakeThreshold: Float
        get() = prefs.getFloat("shake_threshold", 14.5f)
        set(value) = prefs.edit().putFloat("shake_threshold", value).apply()

    var isGuardianListeningActive: Boolean
        get() = prefs.getBoolean("guardian_listening", false)
        set(value) = prefs.edit().putBoolean("guardian_listening", value).apply()

    var autoRecordEvidence: Boolean
        get() = prefs.getBoolean("auto_record_evidence", true)
        set(value) = prefs.edit().putBoolean("auto_record_evidence", value).apply()

    var maxRecordDurationMinutes: Int
        get() = prefs.getInt("max_record_minutes", 10)
        set(value) = prefs.edit().putInt("max_record_minutes", value).apply()

    var strobeFlashlightOnSos: Boolean
        get() = prefs.getBoolean("strobe_on_sos", false)
        set(value) = prefs.edit().putBoolean("strobe_on_sos", value).apply()

    var loudSirenOnSos: Boolean
        get() = prefs.getBoolean("siren_on_sos", false)
        set(value) = prefs.edit().putBoolean("siren_on_sos", value).apply()

    var pinLockEnabled: Boolean
        get() = prefs.getBoolean("pin_lock_enabled", false)
        set(value) = prefs.edit().putBoolean("pin_lock_enabled", value).apply()

    var securityPin: String
        get() = prefs.getString("security_pin", "1234") ?: "1234"
        set(value) = prefs.edit().putString("security_pin", value).apply()

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean("onboarding_completed", false)
        set(value) = prefs.edit().putBoolean("onboarding_completed", value).apply()

    var familyCircleCode: String
        get() = prefs.getString("circle_code", "SV-7821") ?: "SV-7821"
        set(value) = prefs.edit().putString("circle_code", value).apply()

    var fakeCallerName: String
        get() = prefs.getString("fake_caller_name", "Dad (Home)") ?: "Dad (Home)"
        set(value) = prefs.edit().putString("fake_caller_name", value).apply()

    var fakeCallDelaySeconds: Int
        get() = prefs.getInt("fake_call_delay", 10)
        set(value) = prefs.edit().putInt("fake_call_delay", value).apply()
}
