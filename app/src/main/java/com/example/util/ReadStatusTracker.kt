package com.example.util

import android.content.Context
import android.util.Log

/**
 * Robust local persistence for message read/unread statuses.
 * Guarantees that once a user or administrator reads a message on their device,
 * subsequent cloud sync operations, manifest touch polling, or periodic background
 * synchronizations will never revert the message status back to unread.
 */
object ReadStatusTracker {
    private const val TAG = "ReadStatusTracker"
    private const val PREFS_NAME = "alnoor_read_status_prefs"
    private const val KEY_READ_IDS = "read_message_ids"

    fun markRead(context: Context, id: String) {
        if (id.isBlank()) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getStringSet(KEY_READ_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
            if (current.add(id)) {
                prefs.edit().putStringSet(KEY_READ_IDS, current).apply()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error marking message read: ${e.message}")
        }
    }

    fun markMultipleRead(context: Context, ids: Collection<String>) {
        if (ids.isEmpty()) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getStringSet(KEY_READ_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
            var modified = false
            for (id in ids) {
                if (id.isNotBlank() && current.add(id)) {
                    modified = true
                }
            }
            if (modified) {
                prefs.edit().putStringSet(KEY_READ_IDS, current).apply()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error marking multiple read: ${e.message}")
        }
    }

    fun isRead(context: Context, id: String): Boolean {
        if (id.isBlank()) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_READ_IDS, emptySet())?.contains(id) == true
    }

    fun getReadIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_READ_IDS, emptySet()) ?: emptySet()
    }
}
