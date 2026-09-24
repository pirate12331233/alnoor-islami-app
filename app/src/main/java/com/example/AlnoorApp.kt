package com.example

import android.app.Application
import android.util.Log
import com.example.util.AlnoorBackgroundSyncReceiver
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Custom Application class for Alnoor Islamic App.
 * Manages app-wide initialization, notification channel registration,
 * and starts the high-reliability background synchronization receiver.
 */
class AlnoorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "AlnoorApp initialized.")

        // 1. Initialize notification channel with high priority, default sound and vibration
        NotificationHelper.initNotificationChannel(this)

        // 2. Start high-reliability background sync alarm (operates independently of Google Play Services)
        AlnoorBackgroundSyncReceiver.schedule(this)

        // 3. Asynchronously ensure the 15,000+ Sahihain Hadith database is unpacked to local storage
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                com.example.data.local.HadithDatabaseManager.ensureDatabaseExtracted(this@AlnoorApp)
            } catch (e: Exception) {
                Log.w(TAG, "Early Hadith database extraction note: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "AlnoorApp"

        var instance: AlnoorApp? = null
            private set
    }
}
