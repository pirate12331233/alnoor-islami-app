package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    const val CHANNEL_ID_BROADCASTS = "alnoor_community_broadcasts_v3"
    const val CHANNEL_NAME_BROADCASTS = "Alnoor Community Broadcasts"
    const val CHANNEL_DESC_BROADCASTS = "Real-time alerts for events, mosque notices, live updates, and community announcements"

    const val TOPIC_ALL_MEMBERS = "alnoor_all_members"
    const val TOPIC_LIVE_BROADCASTS = "alnoor_live_broadcasts"
    const val TOPIC_COMMUNITY_EVENTS = "alnoor_community_events"
    const val TOPIC_NOTICES = "alnoor_notices"

    /**
     * Wakes up the screen temporarily so the heads-up notification banner is immediately visible
     * even when the device is locked, identical to WhatsApp and SMS alerts.
     */
    private fun wakeUpScreen(context: Context) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null && !powerManager.isInteractive) {
                @Suppress("DEPRECATION")
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                    "Alnoor:NotificationWakeLock"
                )
                wakeLock.acquire(4000L) // Keep screen on for 4 seconds so notification popup is seen
                Log.d("NotificationHelper", "Screen woken up for incoming community notification.")
            }
        } catch (e: Exception) {
            Log.w("NotificationHelper", "Could not wake screen: ${e.message}")
        }
    }

    fun initNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return
        createHighPriorityChannel(context, notificationManager)
    }

    fun showHeadsUpNotification(
        context: Context,
        title: String,
        body: String,
        targetTab: String? = null,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            createHighPriorityChannel(context, notificationManager)

            // Turn screen on if phone is locked
            wakeUpScreen(context)

            // Intent to open MainActivity when user taps the notification
            val tapIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (!targetTab.isNullOrBlank()) {
                    putExtra("target_tab", targetTab)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val vibrationPattern = longArrayOf(0, 450, 200, 450)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_BROADCASTS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setSound(soundUri)
                .setVibrate(vibrationPattern)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE)
                .setAutoCancel(true)
                .setFullScreenIntent(pendingIntent, false) // Enables heads-up popup over locked or active screen
                .setContentIntent(pendingIntent)

            notificationManager.notify(notificationId, builder.build())
            Log.d("NotificationHelper", "High-priority heads-up notification posted with default sound: $title")
        } catch (e: Exception) {
            Log.w("NotificationHelper", "Failed to display notification: ${e.message}", e)
        }
    }

    private fun createHighPriorityChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID_BROADCASTS)
            if (existing == null) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID_BROADCASTS,
                    CHANNEL_NAME_BROADCASTS,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC_BROADCASTS
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 450, 200, 450)
                    enableLights(true)
                    lightColor = 0xFF059669.toInt() // Emerald green
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setShowBadge(true)
                    setSound(soundUri, audioAttributes)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d("NotificationHelper", "Created high-priority notification channel: $CHANNEL_ID_BROADCASTS")
            }
        }
    }
}
