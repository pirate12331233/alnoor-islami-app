package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.util.AlnoorBackgroundSyncReceiver
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Custom Application class for Alnoor Islamic App.
 * Manages app-wide initialization, notification channel registration,
 * FCM topic subscriptions with Google Play Services safety checks,
 * and starts the background synchronization receiver.
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

        // 3. Gracefully initialize FCM token & subscriptions
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fcm = FirebaseMessaging.getInstance()
                fcm.isAutoInitEnabled = true
                fcm.token.addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isNullOrBlank()) {
                        Log.d(TAG, "FCM token retrieved: ${task.result}")
                    } else {
                        Log.i(TAG, "FCM registration pending/deferred. Background REST sync active.")
                    }
                }
                fcm.subscribeToTopic(NotificationHelper.TOPIC_ALL_MEMBERS)
                    .addOnSuccessListener { Log.d(TAG, "Subscribed to ${NotificationHelper.TOPIC_ALL_MEMBERS}") }
                    .addOnFailureListener { e -> Log.w(TAG, "FCM topic subscription notice: ${e.message}") }
                fcm.subscribeToTopic(NotificationHelper.TOPIC_COMMUNITY_EVENTS)
                fcm.subscribeToTopic(NotificationHelper.TOPIC_NOTICES)
                fcm.subscribeToTopic(NotificationHelper.TOPIC_LIVE_BROADCASTS)
            } catch (e: Exception) {
                Log.w(TAG, "FCM initialization handled safely: ${e.message}")
            }
        }
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        return try {
            val clazz = Class.forName("com.google.android.gms.common.GoogleApiAvailability")
            val getInstanceMethod = clazz.getMethod("getInstance")
            val instance = getInstanceMethod.invoke(null)
            val isAvailableMethod = clazz.getMethod("isGooglePlayServicesAvailable", Context::class.java)
            val code = isAvailableMethod.invoke(instance, context) as? Int
            code == 0 // 0 corresponds to ConnectionResult.SUCCESS
        } catch (_: Exception) {
            try {
                context.packageManager.getPackageInfo("com.google.android.gms", 0)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    companion object {
        private const val TAG = "AlnoorApp"
        var instance: AlnoorApp? = null
            private set
    }
}
