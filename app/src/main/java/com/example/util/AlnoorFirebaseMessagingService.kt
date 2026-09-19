package com.example.util

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.UserInquiryEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        val isFlash = remoteMessage.data["type"] == "flash" ||
                remoteMessage.data["is_flash"] == "true" ||
                remoteMessage.data["flash"] == "true" ||
                title.contains("FLASH", ignoreCase = true)

        if (isFlash) {
            val alertId = remoteMessage.data["id"] ?: "flash_${System.currentTimeMillis()}"
            val timestamp = remoteMessage.data["timestamp_formatted"]
                ?: SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())

            NotificationHelper.showFlashMessageAlert(
                context = applicationContext,
                title = title,
                message = body,
                timestamp = timestamp,
                alertId = alertId
            )
            AlnoorBackgroundSyncReceiver.markAlertShown(applicationContext, alertId)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    db.inquiriesDao().insertInquiry(
                        UserInquiryEntity(
                            id = alertId,
                            senderName = "Alnoor Mosque Administration",
                            senderContact = "helpline@alnoor.org",
                            category = "GENERAL",
                            subject = "⚡ FLASH: $title",
                            message = body,
                            timestamp = timestamp,
                            status = "RESOLVED",
                            reply = null,
                            isRead = false,
                            internalNotes = "FCM Flash Broadcast",
                            isFromAdmin = true,
                            threadId = "FLASH_BROADCAST",
                            createdAt = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to store FCM flash alert: ${e.message}")
                }
            }
        } else {
            // 2. Display high-priority heads-up banner with sound, vibration, and screen wakeup
            NotificationHelper.showHeadsUpNotification(
                context = applicationContext,
                title = title,
                body = body,
                targetTab = targetTab
            )
        }
    }

    companion object {
        private const val TAG = "AlnoorFCM"
    }
}
