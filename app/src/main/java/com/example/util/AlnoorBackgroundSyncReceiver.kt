package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import com.example.data.remote.FirestoreSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * High-reliability Background Sync Receiver for Alnoor Islamic App.
 * Ensures notifications for newly added or updated Events and Notices are delivered
 * instantly when the application is closed, backgrounded, or when the screen is locked,
 * identical to WhatsApp / messaging apps.
 */
class AlnoorBackgroundSyncReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Background sync broadcast received with action: $action")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Alnoor:BackgroundSyncReceiverWakeLock"
        )
        try {
            wakeLock?.acquire(15_000L) // 15 seconds max CPU lock to complete sync check
        } catch (e: Exception) {
            Log.w(TAG, "Could not acquire partial wakelock: ${e.message}")
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirestoreSyncManager.getInstance().performBackgroundSync(context)
            } catch (e: Exception) {
                Log.w(TAG, "Background sync execution failed: ${e.message}", e)
            } finally {
                // Ensure the next background sync check is always scheduled
                schedule(context)
                try {
                    if (wakeLock?.isHeld == true) {
                        wakeLock.release()
                    }
                } catch (_: Exception) {}
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "AlnoorSyncReceiver"
        const val ACTION_BACKGROUND_SYNC = "com.example.alnoor.ACTION_BACKGROUND_SYNC"
        private const val REQUEST_CODE = 4092

        // Fast, battery-efficient check interval (60 seconds)
        private const val DEFAULT_INTERVAL_MS = 60_000L
        private const val PREFS_NAME = "alnoor_sync_prefs"
        private const val KEY_SHOWN_ALERT_IDS = "shown_alert_ids"

        fun isAlertShown(context: Context, alertId: String): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val shownSet = prefs.getStringSet(KEY_SHOWN_ALERT_IDS, emptySet()) ?: emptySet()
            return shownSet.contains(alertId)
        }

        fun markAlertShown(context: Context, alertId: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val shownSet = prefs.getStringSet(KEY_SHOWN_ALERT_IDS, emptySet()) ?: emptySet()
            val updated = HashSet(shownSet).apply { add(alertId) }
            prefs.edit().putStringSet(KEY_SHOWN_ALERT_IDS, updated).apply()
        }

        fun markAlertsShown(context: Context, alertIds: Collection<String>) {
            if (alertIds.isEmpty()) return
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val shownSet = prefs.getStringSet(KEY_SHOWN_ALERT_IDS, emptySet()) ?: emptySet()
            val updated = HashSet(shownSet).apply { addAll(alertIds) }
            prefs.edit().putStringSet(KEY_SHOWN_ALERT_IDS, updated).apply()
        }

        fun schedule(context: Context, intervalMs: Long = DEFAULT_INTERVAL_MS) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    ?: return

                val intent = Intent(context, AlnoorBackgroundSyncReceiver::class.java).apply {
                    action = ACTION_BACKGROUND_SYNC
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                var scheduled = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        val alarmClockInfo = AlarmManager.AlarmClockInfo(
                            System.currentTimeMillis() + intervalMs,
                            pendingIntent
                        )
                        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                        scheduled = true
                        Log.d(TAG, "Scheduled unthrottled AlarmClock background sync in ${intervalMs / 1000}s.")
                    } catch (e: Exception) {
                        Log.w(TAG, "setAlarmClock not permitted, falling back to idle alarm: ${e.message}")
                    }
                }

                if (!scheduled) {
                    val triggerAt = SystemClock.elapsedRealtime() + intervalMs
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                triggerAt,
                                pendingIntent
                            )
                        } else {
                            alarmManager.setAndAllowWhileIdle(
                                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                triggerAt,
                                pendingIntent
                            )
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerAt,
                            pendingIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            triggerAt,
                            pendingIntent
                        )
                    }
                }
                Log.d(TAG, "Next background sync alarm scheduled in ${intervalMs / 1000}s.")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to schedule background sync alarm: ${e.message}")
            }
        }

        fun cancel(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    ?: return
                val intent = Intent(context, AlnoorBackgroundSyncReceiver::class.java).apply {
                    action = ACTION_BACKGROUND_SYNC
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to cancel sync alarm: ${e.message}")
            }
        }
    }
}
