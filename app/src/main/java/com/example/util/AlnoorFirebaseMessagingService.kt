package com.example.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service for Alnoor Islamic App.
 * Delivers real-time push alerts even when the application is completely closed,
 * in the background, or when the screen is locked — similar to WhatsApp.
 */
class AlnoorFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM registration token generated: $token")
        // Store locally or sync token with Firestore if needed
        val prefs = applicationContext.getSharedPreferences("alnoor_fcm_prefs", MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        // 1. Extract notification title and body
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Alnoor Community Alert"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "New announcement from Alnoor Mosque."

        val targetTab = remoteMessage.data["target_tab"]
            ?: remoteMessage.data["targetTab"]

        // 2. Display high-priority heads-up banner with sound, vibration, and screen wakeup
        NotificationHelper.showHeadsUpNotification(
            context = applicationContext,
            title = title,
            body = body,
            targetTab = targetTab
        )
    }

    companion object {
        private const val TAG = "AlnoorFCM"
    }
}
