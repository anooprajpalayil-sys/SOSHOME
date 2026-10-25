package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.preferences.UserPreferencesManager
import com.example.data.repository.SafeVoiceRepository

class SafeVoiceApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var preferences: UserPreferencesManager
        private set
    lateinit var repository: SafeVoiceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        preferences = UserPreferencesManager(this)
        repository = SafeVoiceRepository(this, database, preferences)
    }
}
