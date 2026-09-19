package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.FlashMessageActivity
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID_BROADCASTS = "alnoor_community_broadcasts_v3"
    const val CHANNEL_NAME_BROADCASTS = "Alnoor Community Broadcasts"
    const val CHANNEL_DESC_BROADCASTS = "Real-time alerts for events, mosque notices, live updates, and community announcements"

    const val CHANNEL_ID_FLASH = "alnoor_urgent_flash_v1"
    const val CHANNEL_NAME_FLASH = "Alnoor Urgent Flash Broadcasts"
    const val CHANNEL_DESC_FLASH = "Mandatory full-screen emergency alerts and important mosque announcements"

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
        notificationId: Int? = null
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            createHighPriorityChannel(context, notificationManager)

            // Turn screen on if phone is locked
            wakeUpScreen(context)

            // Mask/transform event title as requested
            val displayTitle = when {
                title.equals("New Community Event", ignoreCase = true) ||
                title.contains("New Community Event", ignoreCase = true) ->
                    title.replace("New Community Event", "Upcoming New Mahafil", ignoreCase = true)

                title.equals("Community Event Updated", ignoreCase = true) ||
                title.contains("Community Event Updated", ignoreCase = true) ->
                    title.replace("Community Event Updated", "Upcoming Mahafil Updated", ignoreCase = true)

                title.equals("Community Event", ignoreCase = true) ->
                    "Upcoming New Mahafil"

                else -> title
            }

            // Deduplicate notification ID by content so duplicate identical pushes do not show duplicate cards
            val finalNotificationId = notificationId ?: (displayTitle.trim() + "_" + body.trim()).hashCode()

            // Intent to open MainActivity when user taps the notification
            val tapIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (!targetTab.isNullOrBlank()) {
                    putExtra("target_tab", targetTab)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                finalNotificationId,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val vibrationPattern = longArrayOf(0, 450, 200, 450)

            // Decode Alnoor Islamic App icon for display in notification header & large icon
            val appLogoBitmap: Bitmap? = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
                    ?: BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
            } catch (_: Exception) {
                null
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_BROADCASTS)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(displayTitle)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setColor(0xFF059669.toInt()) // Signature Alnoor Islamic Emerald Green
                .setSound(soundUri)
                .setVibrate(vibrationPattern)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE)
                .setAutoCancel(true)
                .setFullScreenIntent(pendingIntent, false) // Enables heads-up popup over locked or active screen
                .setContentIntent(pendingIntent)
                .apply {
                    if (appLogoBitmap != null) {
                        setLargeIcon(appLogoBitmap)
                    }
                }

            notificationManager.notify(finalNotificationId, builder.build())
            Log.d("NotificationHelper", "High-priority heads-up notification posted with default sound: $displayTitle")
        } catch (e: Exception) {
            Log.w("NotificationHelper", "Failed to display notification: ${e.message}", e)
        }
    }

    /**
     * Triggers Option 1: Full-Screen Alarm/Call Style Flash Alert.
     *
     * - Wakes up screen immediately when locked or turned off.
     * - Fires full-screen intent directly into FlashMessageActivity over lockscreen.
     * - Posts sticky high-priority alarm notification that cannot be missed.
     */
    fun showFlashMessageAlert(
        context: Context,
        title: String,
        message: String,
        timestamp: String,
        alertId: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            createFlashPriorityChannel(context, notificationManager)

            // 1. Wake screen up with strong wake lock
            wakeUpScreen(context)

            val flashIntent = Intent(context, FlashMessageActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                putExtra(FlashMessageActivity.EXTRA_TITLE, title)
                putExtra(FlashMessageActivity.EXTRA_MESSAGE, message)
                putExtra(FlashMessageActivity.EXTRA_TIMESTAMP, timestamp)
                putExtra(FlashMessageActivity.EXTRA_ALERT_ID, alertId)
            }

            // Directly launch the activity so it pops up over active app or lockscreen immediately
            try {
                context.startActivity(flashIntent)
                Log.d("NotificationHelper", "FlashMessageActivity started directly.")
            } catch (e: Exception) {
                Log.w("NotificationHelper", "Direct startActivity deferred to fullScreenIntent: ${e.message}")
            }

            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                alertId.hashCode(),
                flashIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val vibrationPattern = longArrayOf(0, 800, 300, 800, 300, 800)

            val appLogoBitmap: Bitmap? = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
                    ?: BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
            } catch (_: Exception) {
                null
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID_FLASH)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("🚨 $title")
                .setContentText(message.take(120))
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setColor(0xFFDC2626.toInt()) // Red accent for urgent flash alert
                .setSound(soundUri)
                .setVibrate(vibrationPattern)
                .setOngoing(true) // Cannot be swiped away accidentally until handled
                .setAutoCancel(true)
                .setFullScreenIntent(fullScreenPendingIntent, true) // Launches immediately over lockscreen
                .setContentIntent(fullScreenPendingIntent)
                .apply {
                    if (appLogoBitmap != null) {
                        setLargeIcon(appLogoBitmap)
                    }
                }

            notificationManager.notify(alertId.hashCode(), builder.build())
            Log.d("NotificationHelper", "Urgent Flash Alert full-screen notification posted: $title")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to trigger Flash Alert: ${e.message}", e)
        }
    }

    private fun createFlashPriorityChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existing = notificationManager.getNotificationChannel(CHANNEL_ID_FLASH)
            if (existing == null) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID_FLASH,
                    CHANNEL_NAME_FLASH,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC_FLASH
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 800, 300, 800, 300, 800)
                    enableLights(true)
                    lightColor = 0xFFDC2626.toInt()
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setShowBadge(true)
                    setSound(soundUri, audioAttributes)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d("NotificationHelper", "Created flash alarm channel: $CHANNEL_ID_FLASH")
            }
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
