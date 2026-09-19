package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.util.NotificationHelper
import com.example.util.AlnoorBackgroundSyncReceiver
import com.example.data.local.AppDatabase
import com.example.data.local.CommunityEventEntity
import com.example.data.local.DaroodSubmissionEntity
import com.example.data.local.GalleryAssetEntity
import com.example.data.local.GalleryPhotoEntity
import com.example.data.local.ImportantNoticePopupEntity
import com.example.data.local.IslamicBookEntity
import com.example.data.local.NoticeItemEntity
import com.example.data.local.PhotoAlbumEntity
import com.example.data.local.RegisteredUserEntity
import com.example.data.local.UserInquiryEntity
import com.example.data.model.ActionCardConfig
import com.example.data.model.AdminMessage
import com.example.data.model.AppVersionInfo
import com.example.data.model.CommunityEvent
import com.example.data.model.DaroodSubmission
import com.example.data.model.GalleryAsset
import com.example.data.model.GalleryPhoto
import com.example.data.model.ImportantNoticePopup
import com.example.data.model.IslamicBook
import com.example.data.model.MessageCategory
import com.example.data.model.MessageStatus
import com.example.data.model.NoticeItem
import com.example.data.model.NoticePriority
import com.example.data.model.PhotoAlbum
import com.example.data.model.RegisteredUser
import com.example.data.model.UserGender
import com.example.data.model.UserRole
import com.example.data.security.SecurityCryptoManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.AlnoorApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Robust, real-time Firestore REST synchronization manager.
 * Connects directly to Firebase Firestore across all User and Admin devices.
 */
class FirestoreSyncManager private constructor() {

    companion object {
        private const val TAG = "FirestoreSyncManager"
        private const val PROJECT_ID = "alnoor-islami-8763b"
        private const val API_KEY = "AIzaSyBmV50_gdE4t36Fqje4nvAfJwadHrZgMNE"
        private const val BASE_URL = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"

        @Volatile
        private var INSTANCE: FirestoreSyncManager? = null

        fun getInstance(): FirestoreSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreSyncManager().also { INSTANCE = it }
            }
        }
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private var syncJob: Job? = null
    private var actionCardSyncJob: Job? = null
    private var isCloudOnline: Boolean = true

    // Track locally known version timestamps to avoid redundant collection reads
    private val lastKnownVersions = java.util.concurrent.ConcurrentHashMap<String, Long>()

    private val _isInitialSyncComplete = MutableStateFlow(false)
    val isInitialSyncComplete = _isInitialSyncComplete.asStateFlow()

    private val _initialSyncMessage = MutableStateFlow("Connecting to Alnoor Cloud...")
    val initialSyncMessage = _initialSyncMessage.asStateFlow()

    private val _appVersionInfo = MutableStateFlow(AppVersionInfo())
    val appVersionInfo = _appVersionInfo.asStateFlow()

    fun isCloudConnected(): Boolean = isCloudOnline

    // -------------------------------------------------------------------------
    // Manifest Tracking Helper
    // -------------------------------------------------------------------------

    fun touchManifest(sectionKey: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                lastKnownVersions[sectionKey] = now
                val fields = JSONObject().apply {
                    put("${sectionKey}_v", intField(now))
                    put("lastUpdated", intField(now))
                }
                saveDocument("app_settings", "sync_manifest", fields, merge = true)
                Log.d(TAG, "Touched manifest for section: $sectionKey at $now")
            } catch (e: Exception) {
                Log.w(TAG, "Failed touching manifest for $sectionKey: ${e.message}")
            }
        }
    }

    fun pushBroadcastNotificationToCloud(
        title: String,
        body: String,
        targetTab: String? = null,
        scope: CoroutineScope,
        context: Context? = null
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val notifId = "notif_${System.currentTimeMillis()}"
                val fields = JSONObject().apply {
                    put("id", stringField(notifId))
                    put("title", stringField(title))
                    put("body", stringField(body))
                    put("targetTab", stringField(targetTab ?: ""))
                    put("timestamp", intField(System.currentTimeMillis()))
                }
                saveDocument("broadcast_notifications", notifId, fields)
                touchManifest("broadcast", scope)
                // Deduplication: mark this alert ID as already shown on this device to prevent echo
                context?.let { ctx ->
                    AlnoorBackgroundSyncReceiver.markAlertShown(ctx, notifId)
                }
                Log.d(TAG, "Synced broadcast notification to Firestore: $title")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing broadcast notification: ${e.message}", e)
            }
        }
    }

    fun pushFlashBroadcastToCloud(
        alertId: String,
        title: String,
        message: String,
        timestamp: Long,
        formattedTimestamp: String,
        scope: CoroutineScope,
        context: Context? = null
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                // 1. Save to flash_broadcasts collection for full-screen alarm trigger
                val fields = JSONObject().apply {
                    put("id", stringField(alertId))
                    put("title", stringField(title))
                    put("message", stringField(message))
                    put("timestamp", intField(timestamp))
                    put("formattedTimestamp", stringField(formattedTimestamp))
                    put("type", stringField("FLASH_ALERT"))
                }
                saveDocument("flash_broadcasts", alertId, fields)

                // 2. Save directly to user_inquiries collection so every client archives it in 1-to-1 chat history
                val inquiryFields = JSONObject().apply {
                    put("id", stringField(alertId))
                    put("senderName", stringField("Alnoor Mosque Administration"))
                    put("senderContact", stringField("helpline@alnoor.org"))
                    put("category", stringField(MessageCategory.GENERAL.name))
                    put("subject", stringField("⚡ FLASH: $title"))
                    put("message", stringField(message))
                    put("timestamp", stringField(formattedTimestamp))
                    put("status", stringField(MessageStatus.RESOLVED.name))
                    put("reply", stringField(""))
                    put("isRead", booleanField(false))
                    put("internalNotes", stringField("Broadcast Flash Message to All Community Devices"))
                    put("isFromAdmin", booleanField(true))
                    put("threadId", stringField("FLASH_BROADCAST"))
                    put("createdAt", intField(timestamp))
                }
                saveDocument("user_inquiries", alertId, inquiryFields)

                // 3. Save to broadcast_notifications so backgrounded devices wake up immediately
                val notifFields = JSONObject().apply {
                    put("id", stringField(alertId))
                    put("title", stringField("🚨 $title"))
                    put("body", stringField(message))
                    put("targetTab", stringField("MESSAGES"))
                    put("timestamp", intField(timestamp))
                    put("type", stringField("flash"))
                }
                saveDocument("broadcast_notifications", alertId, notifFields)

                // 4. Update sync manifest with all versions in one network transaction
                val now = System.currentTimeMillis()
                val manifestFields = JSONObject().apply {
                    put("flash_broadcast_v", intField(now))
                    put("inquiries_v", intField(now))
                    put("broadcast_v", intField(now))
                    put("lastUpdated", intField(now))
                }
                saveDocument("app_settings", "sync_manifest", manifestFields, merge = true)
                lastKnownVersions["flash_broadcast"] = now
                lastKnownVersions["inquiries"] = now
                lastKnownVersions["broadcast"] = now

                // Deduplication: mark this alert ID as already shown on the broadcasting device to avoid re-triggering on sender
                context?.let { ctx ->
                    AlnoorBackgroundSyncReceiver.markAlertShown(ctx, alertId)
                }
                Log.d(TAG, "Synced flash broadcast to Firestore and inquiries: $title")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing flash broadcast: ${e.message}", e)
            }
        }
    }

    suspend fun performBackgroundSync(context: Context) {
        try {
            val manifestDoc = fetchDocument("app_settings", "sync_manifest") ?: return
            val fields = manifestDoc.optJSONObject("fields") ?: JSONObject()
            val prefs = context.getSharedPreferences("alnoor_sync_prefs", Context.MODE_PRIVATE)
            val db = AppDatabase.getDatabase(context)

            val flashV = getLongValue(fields, "flash_broadcast_v", 0L)
            var lastFlashV = prefs.getLong("bg_flash_broadcast_v", 0L)
            if (lastFlashV > flashV && flashV > 0L) lastFlashV = 0L

            val broadcastV = getLongValue(fields, "broadcast_v", 0L)
            var lastBroadcastV = prefs.getLong("bg_broadcast_v", 0L)
            if (lastBroadcastV > broadcastV && broadcastV > 0L) lastBroadcastV = 0L

            val eventsV = getLongValue(fields, "events_v", 0L)
            var lastEventsV = prefs.getLong("bg_events_v", 0L)
            if (lastEventsV > eventsV && eventsV > 0L) lastEventsV = 0L

            val noticesV = getLongValue(fields, "notices_v", 0L)
            var lastNoticesV = prefs.getLong("bg_notices_v", 0L)
            if (lastNoticesV > noticesV && noticesV > 0L) lastNoticesV = 0L

            val popupV = getLongValue(fields, "popup_v", 0L)
            var lastPopupV = prefs.getLong("bg_popup_v", 0L)
            if (lastPopupV > popupV && popupV > 0L) lastPopupV = 0L

            val nowMs = System.currentTimeMillis()

            // 0. Check Urgent Full-Screen Flash Alerts (Alarm/Call Style over lockscreen)
            val remoteFlashList = fetchCollection("flash_broadcasts", orderBy = "timestamp desc", pageSize = 10)
            if (remoteFlashList.isNotEmpty()) {
                remoteFlashList.forEach { flashDoc ->
                    val alertFields = flashDoc.optJSONObject("fields") ?: JSONObject()
                    val alertId = getStringValue(alertFields, "id", "")
                    val title = getStringValue(alertFields, "title", "Urgent Mosque Announcement")
                    val message = getStringValue(alertFields, "message", "")
                    val formattedTimestamp = getStringValue(
                        alertFields,
                        "formattedTimestamp",
                        SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())
                    )
                    val timestamp = getLongValue(alertFields, "timestamp", nowMs)
                    val isRecent = (nowMs - timestamp) < (48 * 3600 * 1000L) // active within last 48 hours

                    if (alertId.isNotBlank() && message.isNotBlank()) {
                        val isAlreadyShown = AlnoorBackgroundSyncReceiver.isAlertShown(context, alertId)
                        if (!isAlreadyShown && isRecent) {
                            // 1. Pop up full-screen alarm alert over lockscreen
                            NotificationHelper.showFlashMessageAlert(
                                context = context,
                                title = title,
                                message = message,
                                timestamp = formattedTimestamp,
                                alertId = alertId
                            )
                            AlnoorBackgroundSyncReceiver.markAlertShown(context, alertId)
                        } else if (!isRecent && !isAlreadyShown) {
                            AlnoorBackgroundSyncReceiver.markAlertShown(context, alertId)
                        }

                        // 2. Store in user's 1-to-1 helpline chat history with date/time stamp
                        try {
                            db.inquiriesDao().insertInquiry(
                                UserInquiryEntity(
                                    id = alertId,
                                    senderName = "Alnoor Mosque Administration",
                                    senderContact = "helpline@alnoor.org",
                                    category = "GENERAL",
                                    subject = "⚡ FLASH: $title",
                                    message = message,
                                    timestamp = formattedTimestamp,
                                    status = "RESOLVED",
                                    reply = null,
                                    isRead = false,
                                    internalNotes = "Urgent Broadcast to All Community Devices",
                                    isFromAdmin = true,
                                    threadId = "FLASH_BROADCAST",
                                    createdAt = timestamp
                                )
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to insert flash inquiry: ${e.message}")
                        }
                    }
                }
            }
            prefs.edit().putLong("bg_flash_broadcast_v", flashV).apply()

            // 1. Check Broadcast Alerts with ID tracking & Deduplication
            val remoteAlerts = fetchCollection("broadcast_notifications", orderBy = "timestamp desc", pageSize = 15)
            if (remoteAlerts.isNotEmpty()) {
                val unseenAlerts = remoteAlerts.filter {
                    val alertFields = it.optJSONObject("fields") ?: JSONObject()
                    val id = getStringValue(alertFields, "id", "")
                    val timestamp = getLongValue(alertFields, "timestamp", 0L)
                    val isRecent = (nowMs - timestamp) < (24 * 3600 * 1000L)
                    id.isNotBlank() && isRecent && !AlnoorBackgroundSyncReceiver.isAlertShown(context, id)
                }

                unseenAlerts.forEach { unseen ->
                    val alertFields = unseen.optJSONObject("fields") ?: JSONObject()
                    val alertId = getStringValue(alertFields, "id", "")
                    val title = getStringValue(alertFields, "title", "Alnoor Community Alert")
                    val body = getStringValue(alertFields, "body", "")
                    val targetTab = getStringValue(alertFields, "targetTab", "")
                    val isFlash = getStringValue(alertFields, "type", "") == "flash" || title.contains("FLASH", ignoreCase = true)

                    if (isFlash) {
                        NotificationHelper.showFlashMessageAlert(
                            context = context,
                            title = title,
                            message = body,
                            timestamp = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date()),
                            alertId = alertId
                        )
                    } else {
                        NotificationHelper.showHeadsUpNotification(
                            context = context,
                            title = title,
                            body = body,
                            targetTab = if (targetTab.isNotBlank()) targetTab else null
                        )
                    }
                    if (alertId.isNotBlank()) {
                        AlnoorBackgroundSyncReceiver.markAlertShown(context, alertId)
                    }
                }
            }
            prefs.edit().putLong("bg_broadcast_v", broadcastV).apply()

            // 2. Check Events with Deduplication
            if (eventsV > lastEventsV || lastEventsV == 0L) {
                val remoteEvents = fetchCollection("community_events")
                if (remoteEvents.isNotEmpty()) {
                    val eventEntities = remoteEvents.mapNotNull { parseEventEntity(it) }
                    if (eventEntities.isNotEmpty()) {
                        if (lastEventsV == 0L) {
                            // First run on this device: populate local Room database silently
                            db.eventsDao().syncEventsWithCloud(eventEntities)
                        } else {
                            val localEvents: List<CommunityEventEntity> = try { db.eventsDao().getExistingEventsList() } catch (_: Exception) { emptyList() }
                            val localMap = localEvents.associateBy { it.id }
                            val newlyAdded = if (localMap.isNotEmpty()) eventEntities.filter { !localMap.containsKey(it.id) } else emptyList()
                            val updated = if (localMap.isNotEmpty()) eventEntities.filter { r: CommunityEventEntity ->
                                val l = localMap[r.id]
                                l != null && (l.title != r.title || l.dateGregorian != r.dateGregorian || l.time != r.time || l.venue != r.venue || l.description != r.description)
                            } else emptyList()

                            db.eventsDao().syncEventsWithCloud(eventEntities)

                            if (newlyAdded.isNotEmpty()) {
                                val ev = newlyAdded.last()
                                val alertKey = "event_add_${ev.id}_${ev.title}"
                                if (!AlnoorBackgroundSyncReceiver.isAlertShown(context, alertKey)) {
                                    NotificationHelper.showHeadsUpNotification(
                                        context = context,
                                        title = "Upcoming New Mahafil",
                                        body = "${ev.title} on ${ev.dateGregorian}",
                                        targetTab = "EVENTS"
                                    )
                                    AlnoorBackgroundSyncReceiver.markAlertShown(context, alertKey)
                                }
                            } else if (updated.isNotEmpty()) {
                                val ev = updated.last()
                                val alertKey = "event_upd_${ev.id}_${ev.title}_${ev.dateGregorian}"
                                if (!AlnoorBackgroundSyncReceiver.isAlertShown(context, alertKey)) {
                                    NotificationHelper.showHeadsUpNotification(
                                        context = context,
                                        title = "Upcoming Mahafil Updated",
                                        body = "${ev.title} details updated by Admin.",
                                        targetTab = "EVENTS"
                                    )
                                    AlnoorBackgroundSyncReceiver.markAlertShown(context, alertKey)
                                }
                            }
                        }
                    }
                }
                prefs.edit().putLong("bg_events_v", eventsV).apply()
            }

            // 3. Check Notices with Deduplication
            if (noticesV > lastNoticesV || lastNoticesV == 0L) {
                val remoteNotices = fetchCollection("notice_items")
                if (remoteNotices.isNotEmpty()) {
                    val noticeEntities = remoteNotices.mapNotNull { parseNoticeEntity(it) }
                    if (noticeEntities.isNotEmpty()) {
                        if (lastNoticesV == 0L) {
                            // First run on this device: populate local Room database silently
                            db.noticesDao().syncNoticesWithCloud(noticeEntities)
                        } else {
                            val localNotices: List<NoticeItemEntity> = try { db.noticesDao().getExistingNoticesList() } catch (_: Exception) { emptyList() }
                            val localNoticeMap = localNotices.associateBy { it.id }
                            val newlyAddedNotice = if (localNoticeMap.isNotEmpty()) noticeEntities.filter { !localNoticeMap.containsKey(it.id) } else emptyList()
                            val updatedNotice = if (localNoticeMap.isNotEmpty()) noticeEntities.filter { r: NoticeItemEntity ->
                                val l = localNoticeMap[r.id]
                                l != null && (l.title != r.title || l.content != r.content || l.priority != r.priority)
                            } else emptyList()

                            db.noticesDao().syncNoticesWithCloud(noticeEntities)

                            if (newlyAddedNotice.isNotEmpty()) {
                                val n = newlyAddedNotice.last()
                                val alertKey = "notice_add_${n.id}_${n.title}"
                                if (!AlnoorBackgroundSyncReceiver.isAlertShown(context, alertKey)) {
                                    val prefix = if (n.priority.equals("URGENT", ignoreCase = true)) "🚨 URGENT NOTICE" else "📢 Important Notice"
                                    NotificationHelper.showHeadsUpNotification(
                                        context = context,
                                        title = prefix,
                                        body = n.title,
                                        targetTab = "NOTICES"
                                    )
                                    AlnoorBackgroundSyncReceiver.markAlertShown(context, alertKey)
                                }
                            } else if (updatedNotice.isNotEmpty()) {
                                val n = updatedNotice.last()
                                val alertKey = "notice_upd_${n.id}_${n.title}"
                                if (!AlnoorBackgroundSyncReceiver.isAlertShown(context, alertKey)) {
                                    NotificationHelper.showHeadsUpNotification(
                                        context = context,
                                        title = "Notice Updated",
                                        body = n.title,
                                        targetTab = "NOTICES"
                                    )
                                    AlnoorBackgroundSyncReceiver.markAlertShown(context, alertKey)
                                }
                            }
                        }
                    }
                }
                prefs.edit().putLong("bg_notices_v", noticesV).apply()
            }

            // 4. Check Popups
            if (popupV > lastPopupV || lastPopupV == 0L) {
                syncPopupSection(db)
                prefs.edit().putLong("bg_popup_v", popupV).apply()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Background sync error: ${e.message}")
        }
    }

    // -------------------------------------------------------------------------
    // Real-Time Synchronization Loops
    // -------------------------------------------------------------------------

    fun startRealtimeSync(
        db: AppDatabase,
        scope: CoroutineScope,
        onActionCardsReceived: ((List<ActionCardConfig>) -> Unit)? = null,
        onNotificationReceived: ((title: String, body: String) -> Unit)? = null
    ) {
        syncJob?.cancel()
        syncJob = scope.launch(Dispatchers.IO) {
            Log.d(TAG, "Starting smart manifest-based Firestore synchronization loop...")
            var isFirstSync = true
            while (isActive) {
                try {
                    val backoffSeconds = syncWithManifest(db, onActionCardsReceived, onNotificationReceived, isFirstSync)
                    isFirstSync = false
                    isCloudOnline = true
                    _isInitialSyncComplete.value = true
                    delay(backoffSeconds * 1000L)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w(TAG, "Firestore sync iteration issue: ${e.message}")
                    _isInitialSyncComplete.value = true
                    delay(20000L) // Wait 20s on general failure before retrying
                }
            }
        }
    }

    /**
     * Force a one-shot full sync from Cloud.
     */
    suspend fun performImmediateFullSync(
        db: AppDatabase,
        onActionCardsReceived: ((List<ActionCardConfig>) -> Unit)? = null,
        onNotificationReceived: ((title: String, body: String) -> Unit)? = null,
        onStatusUpdate: ((String) -> Unit)? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            onStatusUpdate?.invoke("Syncing live Action Cards & Layout...")
            _initialSyncMessage.value = "Syncing live Action Cards & Layout..."
            val remoteCards = fetchActionCardConfigsFromCloud()
            if (remoteCards != null && remoteCards.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    onActionCardsReceived?.invoke(remoteCards)
                }
            }

            onStatusUpdate?.invoke("Fetching Events, Notices & Library...")
            _initialSyncMessage.value = "Fetching Events, Notices & Library..."
            syncAllCollectionsFromCloud(db, null)

            // Prime the manifest version trackers with the actual Firestore cloud versions
            val manifestDoc = fetchDocument("app_settings", "sync_manifest")
            if (manifestDoc != null) {
                val fields = manifestDoc.optJSONObject("fields") ?: JSONObject()
                val keys = fields.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key.endsWith("_v")) {
                        val section = key.removeSuffix("_v")
                        val v = getLongValue(fields, key, 0L)
                        lastKnownVersions[section] = v
                    }
                }
                val context = AlnoorApp.instance
                if (context != null) {
                    val prefs = context.getSharedPreferences("alnoor_sync_prefs", Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    val pKeys = fields.keys()
                    while (pKeys.hasNext()) {
                        val key = pKeys.next()
                        if (key.endsWith("_v")) {
                            editor.putLong("bg_$key", getLongValue(fields, key, 0L))
                        }
                    }
                    editor.apply()
                }
            }

            onStatusUpdate?.invoke("Finalizing community sync...")
            _initialSyncMessage.value = "Finalizing community sync..."
            _isInitialSyncComplete.value = true
            isCloudOnline = true
            true
        } catch (e: Exception) {
            Log.w(TAG, "performImmediateFullSync failed: ${e.message}")
            _isInitialSyncComplete.value = true
            false
        }
    }

    fun stopAllListeners() {
        syncJob?.cancel()
        syncJob = null
        actionCardSyncJob?.cancel()
        actionCardSyncJob = null
        Log.d(TAG, "Sync listeners stopped.")
    }

    fun startSyncingActionCards(
        scope: CoroutineScope,
        onConfigsReceived: (List<ActionCardConfig>) -> Unit
    ) {
        // Handled efficiently inside startRealtimeSync manifest loop
    }

    // -------------------------------------------------------------------------
    // Manifest-Based Smart Synchronization
    // -------------------------------------------------------------------------

    private suspend fun syncWithManifest(
        db: AppDatabase,
        onActionCardsReceived: ((List<ActionCardConfig>) -> Unit)?,
        onNotificationReceived: ((title: String, body: String) -> Unit)?,
        isInitial: Boolean
    ): Long {
        val manifestDoc = fetchDocument("app_settings", "sync_manifest")
        if (manifestDoc == null) {
            // Manifest not reachable or quota exceeded, back off
            if (isInitial) {
                syncAllCollectionsFromCloud(db, null)
            }
            return 15L
        }

        val fields = manifestDoc.optJSONObject("fields") ?: JSONObject()

        // 0. Urgent Flash Broadcasts
        val flashV = getLongValue(fields, "flash_broadcast_v", 0L)
        if (isInitial || flashV > (lastKnownVersions["flash_broadcast"] ?: 0L)) {
            syncFlashBroadcastSection(db, isInitial)
            lastKnownVersions["flash_broadcast"] = flashV
        }

        // 0b. Broadcast Notifications (Mahafil updates, Notices & Announcements)
        val broadcastV = getLongValue(fields, "broadcast_v", 0L)
        if (isInitial || broadcastV > (lastKnownVersions["broadcast"] ?: 0L)) {
            syncBroadcastSection(db, if (isInitial) null else onNotificationReceived)
            lastKnownVersions["broadcast"] = broadcastV
        }

        // 1. Action Cards
        val actionCardsV = getLongValue(fields, "action_cards_v", 0L)
        if (isInitial || actionCardsV > (lastKnownVersions["action_cards"] ?: 0L)) {
            val remoteCards = fetchActionCardConfigsFromCloud()
            if (remoteCards != null && remoteCards.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    onActionCardsReceived?.invoke(remoteCards)
                }
            }
            lastKnownVersions["action_cards"] = actionCardsV
        }

        // 2. Registered Users
        val usersV = getLongValue(fields, "users_v", 0L)
        if (isInitial || usersV > (lastKnownVersions["users"] ?: 0L)) {
            syncUsersSection(db)
            lastKnownVersions["users"] = usersV
        }

        // 3. Community Events
        val eventsV = getLongValue(fields, "events_v", 0L)
        if (isInitial || eventsV > (lastKnownVersions["events"] ?: 0L)) {
            syncEventsSection(db, if (isInitial) null else onNotificationReceived)
            lastKnownVersions["events"] = eventsV
        }

        // 4. Notices
        val noticesV = getLongValue(fields, "notices_v", 0L)
        if (isInitial || noticesV > (lastKnownVersions["notices"] ?: 0L)) {
            syncNoticesSection(db, if (isInitial) null else onNotificationReceived)
            lastKnownVersions["notices"] = noticesV
        }

        // 5. Popup Notice
        val popupV = getLongValue(fields, "popup_v", 0L)
        if (isInitial || popupV > (lastKnownVersions["popup"] ?: 0L)) {
            syncPopupSection(db)
            lastKnownVersions["popup"] = popupV
        }

        // 6. Islamic Books
        val booksV = getLongValue(fields, "books_v", 0L)
        if (isInitial || booksV > (lastKnownVersions["books"] ?: 0L)) {
            syncBooksSection(db)
            lastKnownVersions["books"] = booksV
        }

        // 7. Gallery Albums & Photos
        val galleryV = getLongValue(fields, "gallery_v", 0L)
        if (isInitial || galleryV > (lastKnownVersions["gallery"] ?: 0L)) {
            syncGallerySection(db)
            lastKnownVersions["gallery"] = galleryV
        }

        // 8. Gallery Assets
        val assetsV = getLongValue(fields, "assets_v", 0L)
        if (isInitial || assetsV > (lastKnownVersions["assets"] ?: 0L)) {
            syncAssetsSection(db)
            lastKnownVersions["assets"] = assetsV
        }

        // 9. Inquiries
        val inquiriesV = getLongValue(fields, "inquiries_v", 0L)
        if (isInitial || inquiriesV > (lastKnownVersions["inquiries"] ?: 0L)) {
            syncInquiriesSection(db)
            lastKnownVersions["inquiries"] = inquiriesV
        }

        // 10. Darood Submissions
        val daroodV = getLongValue(fields, "darood_v", 0L)
        if (isInitial || daroodV > (lastKnownVersions["darood"] ?: 0L)) {
            syncDaroodSection(db)
            lastKnownVersions["darood"] = daroodV
        }

        // 11. App Version & Forced Update Info
        val appVersionV = getLongValue(fields, "app_version_v", 0L)
        if (isInitial || appVersionV > (lastKnownVersions["app_version"] ?: 0L)) {
            syncAppVersionSection()
            lastKnownVersions["app_version"] = appVersionV
        }

        return 12L // Check manifest every 12 seconds
    }

    private suspend fun syncAllCollectionsFromCloud(
        db: AppDatabase,
        onNotificationReceived: ((title: String, body: String) -> Unit)?
    ) {
        syncUsersSection(db)
        syncEventsSection(db, onNotificationReceived)
        syncNoticesSection(db, onNotificationReceived)
        syncPopupSection(db)
        syncBooksSection(db)
        syncGallerySection(db)
        syncAssetsSection(db)
        syncInquiriesSection(db)
        syncDaroodSection(db)
        syncAppVersionSection()
    }

    suspend fun syncFlashBroadcastSection(db: AppDatabase, isInitial: Boolean = false) {
        try {
            val remoteFlashList = fetchCollection("flash_broadcasts", orderBy = "timestamp desc", pageSize = 10)
            if (remoteFlashList.isNotEmpty()) {
                val context = AlnoorApp.instance
                val nowMs = System.currentTimeMillis()
                remoteFlashList.forEach { flashDoc ->
                    val alertFields = flashDoc.optJSONObject("fields") ?: JSONObject()
                    val alertId = getStringValue(alertFields, "id", "")
                    val title = getStringValue(alertFields, "title", "Urgent Mosque Announcement")
                    val message = getStringValue(alertFields, "message", "")
                    val formattedTimestamp = getStringValue(
                        alertFields,
                        "formattedTimestamp",
                        SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())
                    )
                    val timestamp = getLongValue(alertFields, "timestamp", nowMs)
                    val isRecent = (nowMs - timestamp) < (48 * 3600 * 1000L) // Active within last 48 hours

                    if (alertId.isNotBlank() && message.isNotBlank()) {
                        val isAlreadyShown = context != null && AlnoorBackgroundSyncReceiver.isAlertShown(context, alertId)

                        // If not shown yet on this device and within active 48-hour window, trigger full-screen alarm alert!
                        if (!isAlreadyShown && isRecent && context != null) {
                            NotificationHelper.showFlashMessageAlert(
                                context = context,
                                title = title,
                                message = message,
                                timestamp = formattedTimestamp,
                                alertId = alertId
                            )
                            AlnoorBackgroundSyncReceiver.markAlertShown(context, alertId)
                        } else if (!isRecent && context != null && !isAlreadyShown) {
                            // Historical alerts (>48h) shouldn't alarm, but marked so they don't ring later
                            AlnoorBackgroundSyncReceiver.markAlertShown(context, alertId)
                        }

                        // Always save into helpline database so it's archived in 1-to-1 chat history
                        try {
                            db.inquiriesDao().insertInquiry(
                                UserInquiryEntity(
                                    id = alertId,
                                    senderName = "Alnoor Mosque Administration",
                                    senderContact = "helpline@alnoor.org",
                                    category = "GENERAL",
                                    subject = "⚡ FLASH: $title",
                                    message = message,
                                    timestamp = formattedTimestamp,
                                    status = "RESOLVED",
                                    reply = null,
                                    isRead = false,
                                    internalNotes = "Urgent Broadcast to All Community Devices",
                                    isFromAdmin = true,
                                    threadId = "FLASH_BROADCAST",
                                    createdAt = timestamp
                                )
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to insert flash inquiry: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing flash broadcast section: ${e.message}")
        }
    }

    suspend fun syncBroadcastSection(
        db: AppDatabase,
        onNotificationReceived: ((title: String, body: String) -> Unit)? = null
    ) {
        try {
            val remoteAlerts = fetchCollection("broadcast_notifications", orderBy = "timestamp desc", pageSize = 15)
            if (remoteAlerts.isNotEmpty()) {
                val context = AlnoorApp.instance
                val nowMs = System.currentTimeMillis()
                val unseenAlerts = remoteAlerts.filter { doc ->
                    val alertFields = doc.optJSONObject("fields") ?: JSONObject()
                    val id = getStringValue(alertFields, "id", "")
                    val timestamp = getLongValue(alertFields, "timestamp", 0L)
                    val isRecent = (nowMs - timestamp) < (24 * 3600 * 1000L)
                    id.isNotBlank() && isRecent && context != null && !AlnoorBackgroundSyncReceiver.isAlertShown(context, id)
                }

                unseenAlerts.forEach { alertDoc ->
                    val alertFields = alertDoc.optJSONObject("fields") ?: JSONObject()
                    val alertId = getStringValue(alertFields, "id", "")
                    val title = getStringValue(alertFields, "title", "Alnoor Community Alert")
                    val body = getStringValue(alertFields, "body", "")
                    val targetTab = getStringValue(alertFields, "targetTab", "")
                    val isFlash = getStringValue(alertFields, "type", "") == "flash" || title.contains("FLASH", ignoreCase = true)

                    if (context != null) {
                        if (isFlash) {
                            NotificationHelper.showFlashMessageAlert(
                                context = context,
                                title = title,
                                message = body,
                                timestamp = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date()),
                                alertId = alertId
                            )
                        } else {
                            NotificationHelper.showHeadsUpNotification(
                                context = context,
                                title = title,
                                body = body,
                                targetTab = if (targetTab.isNotBlank()) targetTab else null
                            )
                        }
                        AlnoorBackgroundSyncReceiver.markAlertShown(context, alertId)
                    }

                    withContext(Dispatchers.Main) {
                        onNotificationReceived?.invoke(title, body)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing broadcast notifications: ${e.message}")
        }
    }

    suspend fun syncUsersSection(db: AppDatabase) {
        try {
            val remoteUsers = fetchCollection("registered_users")
            if (remoteUsers.isNotEmpty()) {
                val userEntities = remoteUsers.mapNotNull { parseUserEntity(it) }
                if (userEntities.isNotEmpty()) {
                    db.usersDao().syncUsersWithCloud(userEntities)
                    Log.d(TAG, "Synced ${userEntities.size} registered users from cloud.")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing registered users: ${e.message}")
        }
    }

    suspend fun syncEventsSection(
        db: AppDatabase,
        onNotificationReceived: ((title: String, body: String) -> Unit)? = null
    ) {
        try {
            val remoteEvents = fetchCollection("community_events")
            if (remoteEvents.isNotEmpty()) {
                val eventEntities = remoteEvents.mapNotNull { parseEventEntity(it) }
                if (eventEntities.isNotEmpty()) {
                    // Detect newly added or updated events
                    val localEvents: List<CommunityEventEntity> = try { db.eventsDao().getExistingEventsList() } catch (_: Exception) { emptyList() }
                    val localMap = localEvents.associateBy { it.id }
                    val newlyAddedEvents = if (localMap.isNotEmpty()) {
                        eventEntities.filter { !localMap.containsKey(it.id) }
                    } else {
                        emptyList()
                    }
                    val updatedEvents = if (localMap.isNotEmpty()) {
                        eventEntities.filter { remote: CommunityEventEntity ->
                            val local = localMap[remote.id]
                            local != null && (local.title != remote.title || local.dateGregorian != remote.dateGregorian || local.time != remote.time || local.venue != remote.venue || local.description != remote.description)
                        }
                    } else {
                        emptyList()
                    }

                    db.eventsDao().syncEventsWithCloud(eventEntities)

                    // Dispatch notification for new or updated event to user devices
                    if (newlyAddedEvents.isNotEmpty()) {
                        val latestEvent = newlyAddedEvents.last()
                        withContext(Dispatchers.Main) {
                            onNotificationReceived?.invoke(
                                "Upcoming New Mahafil",
                                "${latestEvent.title} on ${latestEvent.dateGregorian}"
                            )
                        }
                    } else if (updatedEvents.isNotEmpty()) {
                        val latestEvent = updatedEvents.last()
                        withContext(Dispatchers.Main) {
                            onNotificationReceived?.invoke(
                                "Upcoming Mahafil Updated",
                                "${latestEvent.title} details updated by Admin."
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing community events: ${e.message}")
        }
    }

    suspend fun syncNoticesSection(
        db: AppDatabase,
        onNotificationReceived: ((title: String, body: String) -> Unit)? = null
    ) {
        try {
            val remoteNotices = fetchCollection("notice_items")
            if (remoteNotices.isNotEmpty()) {
                val noticeEntities = remoteNotices.mapNotNull { parseNoticeEntity(it) }
                if (noticeEntities.isNotEmpty()) {
                    // Detect newly added or updated notices
                    val localNotices: List<NoticeItemEntity> = try { db.noticesDao().getExistingNoticesList() } catch (_: Exception) { emptyList() }
                    val localNoticeMap = localNotices.associateBy { it.id }
                    val newlyAddedNotices = if (localNoticeMap.isNotEmpty()) {
                        noticeEntities.filter { !localNoticeMap.containsKey(it.id) }
                    } else {
                        emptyList()
                    }
                    val updatedNotices = if (localNoticeMap.isNotEmpty()) {
                        noticeEntities.filter { remote: NoticeItemEntity ->
                            val local = localNoticeMap[remote.id]
                            local != null && (local.title != remote.title || local.content != remote.content || local.priority != remote.priority)
                        }
                    } else {
                        emptyList()
                    }

                    db.noticesDao().syncNoticesWithCloud(noticeEntities)

                    // Dispatch notification for new or updated notice to user devices
                    if (newlyAddedNotices.isNotEmpty()) {
                        val latestNotice = newlyAddedNotices.last()
                        val prefix = if (latestNotice.priority.equals("URGENT", ignoreCase = true)) "🚨 URGENT NOTICE" else "📢 Important Notice"
                        withContext(Dispatchers.Main) {
                            onNotificationReceived?.invoke(
                                prefix,
                                latestNotice.title
                            )
                        }
                    } else if (updatedNotices.isNotEmpty()) {
                        val latestNotice = updatedNotices.last()
                        withContext(Dispatchers.Main) {
                            onNotificationReceived?.invoke(
                                "Notice Updated",
                                latestNotice.title
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing notices: ${e.message}")
        }
    }

    suspend fun syncPopupSection(db: AppDatabase) {
        try {
            val popupDoc = fetchDocument("important_notice_popups", "important_popup")
            if (popupDoc != null) {
                val popupEntity = parseImportantNoticePopup(popupDoc)
                if (popupEntity != null) {
                    db.importantNoticeDao().insertOrUpdatePopup(popupEntity)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing popup notice: ${e.message}")
        }
    }

    suspend fun syncBooksSection(db: AppDatabase) {
        try {
            val remoteBooks = fetchCollection("islamic_books")
            if (remoteBooks.isNotEmpty()) {
                val bookEntities = remoteBooks.mapNotNull { parseBookEntity(it) }
                if (bookEntities.isNotEmpty()) {
                    db.booksDao().syncBooksWithCloud(bookEntities)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing books: ${e.message}")
        }
    }

    suspend fun syncGallerySection(db: AppDatabase) {
        try {
            val remoteAlbums = fetchCollection("photo_albums")
            if (remoteAlbums.isNotEmpty()) {
                val albumEntities = remoteAlbums.mapNotNull { parseAlbumEntity(it) }
                if (albumEntities.isNotEmpty()) {
                    db.galleryDao().syncAlbumsWithCloud(albumEntities)
                }
            }
            val remotePhotos = fetchCollection("gallery_photos")
            if (remotePhotos.isNotEmpty()) {
                val photoEntities = remotePhotos.mapNotNull { parsePhotoEntity(it) }
                if (photoEntities.isNotEmpty()) {
                    db.galleryDao().syncPhotosWithCloud(photoEntities)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing gallery: ${e.message}")
        }
    }

    suspend fun syncAssetsSection(db: AppDatabase) {
        try {
            val remoteAssets = fetchCollection("gallery_assets")
            if (remoteAssets.isNotEmpty()) {
                val assetEntities = remoteAssets.mapNotNull { parseGalleryAssetEntity(it) }
                if (assetEntities.isNotEmpty()) {
                    db.galleryAssetsDao().syncAssetsWithCloud(assetEntities)

                    // Auto-repair cloud payload if physical file is available on this device (e.g. Admin uploader phone)
                    assetEntities.forEach { entity ->
                        if (entity.fileUrl.startsWith("/")) {
                            val localFile = java.io.File(entity.fileUrl)
                            if (localFile.exists() && localFile.length() > 0) {
                                try {
                                    val bytes = localFile.readBytes()
                                    val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                    val fullDataUrl = "data:image/jpeg;base64,$b64"
                                    val repairFields = JSONObject().apply {
                                        put("id", stringField(entity.id))
                                        put("title", stringField(entity.title))
                                        put("category", stringField(entity.category))
                                        put("fileType", stringField(entity.fileType))
                                        put("fileExtension", stringField(entity.fileExtension))
                                        put("fileSize", stringField(entity.fileSize))
                                        put("fileUrl", stringField(fullDataUrl))
                                        put("thumbnailUrl", stringField(fullDataUrl))
                                        put("uploadedDate", stringField(entity.uploadedDate))
                                        put("date", stringField(entity.date))
                                        put("description", stringField(entity.description))
                                        put("type", stringField(entity.type))
                                        put("uploadedBy", stringField(entity.uploadedBy))
                                    }
                                    saveDocument("gallery_assets", entity.id, repairFields)
                                    Log.d(TAG, "Repaired cloud gallery asset ${entity.title} with full base64 image")
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing gallery assets: ${e.message}")
        }
    }

    suspend fun syncInquiriesSection(db: AppDatabase) {
        try {
            val remoteInquiries = fetchCollection("user_inquiries")
            val inquiryEntities = remoteInquiries.mapNotNull { parseInquiryEntity(it) }.toMutableList()

            // Backfill and preserve all flash broadcasts from flash_broadcasts so they are never lost from chat history
            try {
                val remoteFlashList = fetchCollection("flash_broadcasts")
                for (flashDoc in remoteFlashList) {
                    val fields = flashDoc.optJSONObject("fields") ?: continue
                    val alertId = getStringValue(fields, "id", "")
                    val message = getStringValue(fields, "message", "")
                    if (alertId.isNotBlank() && message.isNotBlank() && inquiryEntities.none { it.id == alertId }) {
                        val title = getStringValue(fields, "title", "Urgent Mosque Announcement")
                        val formattedTimestamp = getStringValue(
                            fields,
                            "formattedTimestamp",
                            SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date())
                        )
                        val timestamp = getLongValue(fields, "timestamp", System.currentTimeMillis())

                        val flashInquiry = UserInquiryEntity(
                            id = alertId,
                            senderName = "Alnoor Mosque Administration",
                            senderContact = "helpline@alnoor.org",
                            category = "GENERAL",
                            subject = "⚡ FLASH: $title",
                            message = message,
                            timestamp = formattedTimestamp,
                            status = "RESOLVED",
                            reply = null,
                            isRead = false,
                            internalNotes = "Urgent Broadcast to All Community Devices",
                            isFromAdmin = true,
                            threadId = "FLASH_BROADCAST",
                            createdAt = timestamp
                        )
                        inquiryEntities.add(flashInquiry)

                        // Also sync back to Firestore user_inquiries collection so all devices have it permanently
                        try {
                            val inqFields = JSONObject().apply {
                                put("id", stringField(alertId))
                                put("senderName", stringField("Alnoor Mosque Administration"))
                                put("senderContact", stringField("helpline@alnoor.org"))
                                put("category", stringField("GENERAL"))
                                put("subject", stringField("⚡ FLASH: $title"))
                                put("message", stringField(message))
                                put("timestamp", stringField(formattedTimestamp))
                                put("status", stringField("RESOLVED"))
                                put("reply", stringField(""))
                                put("isRead", booleanField(false))
                                put("internalNotes", stringField("Broadcast Flash Message to All Community Devices"))
                                put("isFromAdmin", booleanField(true))
                                put("threadId", stringField("FLASH_BROADCAST"))
                                put("createdAt", intField(timestamp))
                            }
                            saveDocument("user_inquiries", alertId, inqFields)
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed merging flash broadcasts into inquiries: ${e.message}")
            }

            if (inquiryEntities.isNotEmpty()) {
                db.inquiriesDao().syncInquiriesWithCloud(inquiryEntities)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing inquiries: ${e.message}")
        }
    }

    suspend fun syncDaroodSection(db: AppDatabase) {
        try {
            val remoteDarood = fetchCollection("darood_submissions")
            if (remoteDarood.isNotEmpty()) {
                val daroodEntities = remoteDarood.mapNotNull { parseDaroodSubmissionEntity(it) }
                if (daroodEntities.isNotEmpty()) {
                    db.daroodDao().syncSubmissionsWithCloud(daroodEntities)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing darood submissions: ${e.message}")
        }
    }

    suspend fun syncAppVersionSection() {
        try {
            val doc = fetchDocument("app_settings", "app_version_info")
            if (doc != null) {
                val fields = doc.optJSONObject("fields") ?: JSONObject()
                val versionInfo = parseAppVersionInfo(fields)
                _appVersionInfo.value = versionInfo
                Log.d(TAG, "Synced AppVersionInfo: v${versionInfo.latestVersionName} (build ${versionInfo.latestVersionCode}), minRequired=${versionInfo.minSupportedVersionCode}, forced=${versionInfo.isForcedUpdate}")
            } else {
                val defaultInfo = AppVersionInfo(
                    latestVersionCode = 1,
                    latestVersionName = "1.0.0",
                    minSupportedVersionCode = 1,
                    isForcedUpdate = false,
                    apkDownloadUrl = "https://github.com/AlnoorIslami/alnoor-islamic-app/releases/latest/download/app-release.apk",
                    releaseNotes = "• Release update with login popup poster & live cloud sync.",
                    releaseDate = "August 2026",
                    apkSizeMb = "19.5 MB"
                )
                _appVersionInfo.value = defaultInfo
                pushAppVersionInfoToCloud(defaultInfo, CoroutineScope(Dispatchers.IO))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing app version info: ${e.message}")
        }
    }

    fun pushAppVersionInfoToCloud(info: AppVersionInfo, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val fields = JSONObject().apply {
                    put("latestVersionCode", intField(info.latestVersionCode))
                    put("latestVersionName", stringField(info.latestVersionName))
                    put("minSupportedVersionCode", intField(info.minSupportedVersionCode))
                    put("isForcedUpdate", booleanField(info.isForcedUpdate))
                    put("apkDownloadUrl", stringField(info.apkDownloadUrl))
                    if (info.fallbackApkDownloadUrl.isNotBlank()) {
                        put("fallbackApkDownloadUrl", stringField(info.fallbackApkDownloadUrl))
                    }
                    put("releaseNotes", stringField(info.releaseNotes))
                    put("releaseDate", stringField(info.releaseDate))
                    put("apkSizeMb", stringField(info.apkSizeMb))
                    put("lastUpdated", intField(System.currentTimeMillis()))
                }
                saveDocument("app_settings", "app_version_info", fields)
                _appVersionInfo.value = info
                touchManifest("app_version", scope)
                Log.d(TAG, "Pushed AppVersionInfo to cloud: v${info.latestVersionName} (build ${info.latestVersionCode})")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing AppVersionInfo to cloud: ${e.message}", e)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Push Data to Firestore Cloud (PATCH / DELETE REST Calls)
    // -------------------------------------------------------------------------

    fun pushUserToCloud(user: RegisteredUser, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val securePass = if (user.password.startsWith("sha256:")) user.password else SecurityCryptoManager.hashPassword(user.password)
                val fields = JSONObject().apply {
                    put("userId", stringField(user.userId))
                    put("fullName", stringField(user.fullName))
                    put("email", stringField(user.email))
                    put("whatsappNumber", stringField(user.whatsappNumber))
                    put("gender", stringField(user.gender.name))
                    put("password", stringField(securePass))
                    put("role", stringField(user.role.name))
                    put("registeredAt", stringField(user.registeredAt))
                    put("status", stringField(user.status))
                }
                saveDocument("registered_users", user.userId, fields)
                touchManifest("users", scope)
                Log.d(TAG, "Successfully synced user to Firestore: ${user.fullName} (${user.email})")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing user to Firestore: ${e.message}", e)
            }
        }
    }

    fun updateUserPasswordInCloud(userId: String, newPass: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val doc = fetchDocument("registered_users", userId)
                val fields = doc?.optJSONObject("fields") ?: JSONObject()
                val securePass = if (newPass.startsWith("sha256:")) newPass else SecurityCryptoManager.hashPassword(newPass)
                fields.put("password", stringField(securePass))
                saveDocument("registered_users", userId, fields)
                touchManifest("users", scope)
                Log.d(TAG, "Updated user password in Firestore: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user password in Firestore: ${e.message}", e)
            }
        }
    }

    fun updateUserRoleInCloud(userId: String, newRole: UserRole, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val doc = fetchDocument("registered_users", userId)
                val fields = doc?.optJSONObject("fields") ?: JSONObject()
                fields.put("role", stringField(newRole.name))
                saveDocument("registered_users", userId, fields)
                touchManifest("users", scope)
                Log.d(TAG, "Updated user role in Firestore: $userId -> $newRole")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user role in Firestore: ${e.message}", e)
            }
        }
    }

    fun deleteUserFromCloud(userId: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                deleteDocument("registered_users", userId)
                touchManifest("users", scope)
                Log.d(TAG, "Deleted user from Firestore: $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting user from Firestore: ${e.message}", e)
            }
        }
    }

    fun pushEventToCloud(event: CommunityEvent, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val fields = JSONObject().apply {
                    put("id", stringField(event.id))
                    put("title", stringField(event.title))
                    put("dateGregorian", stringField(event.dateGregorian))
                    put("dateHijri", stringField(event.dateHijri))
                    put("time", stringField(event.time))
                    put("venue", stringField(event.venue))
                    put("address", stringField(event.address))
                    put("description", stringField(event.description))
                    put("category", stringField(event.category))
                    put("speaker", stringField(event.speaker))
                    put("isRsvpEnabled", booleanField(event.isRsvpEnabled))
                    put("rsvpCount", intField(event.rsvpCount))
                    put("posterUrl", stringField(event.posterUrl))
                }
                saveDocument("community_events", event.id, fields)
                touchManifest("events", scope)
                Log.d(TAG, "Synced event to Firestore: ${event.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing event to Firestore: ${e.message}", e)
            }
        }
    }

    fun deleteEventFromCloud(eventId: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                deleteDocument("community_events", eventId)
                touchManifest("events", scope)
                Log.d(TAG, "Deleted event from Firestore: $eventId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting event from Firestore: ${e.message}", e)
            }
        }
    }

    fun pushAlbumToCloud(album: PhotoAlbum, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val fields = JSONObject().apply {
                    put("id", stringField(album.id))
                    put("title", stringField(album.title))
                    put("description", stringField(album.description))
                    put("photoCount", intField(album.photoCount))
                    put("coverUrl", stringField(album.coverUrl))
                    put("date", stringField(album.date))
                }
                saveDocument("photo_albums", album.id, fields)
                touchManifest("gallery", scope)
                Log.d(TAG, "Synced album to Firestore: ${album.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing album to Firestore: ${e.message}", e)
            }
        }
    }

    fun deleteAlbumFromCloud(albumId: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                deleteDocument("photo_albums", albumId)
                touchManifest("gallery", scope)
                Log.d(TAG, "Deleted album from Firestore: $albumId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting album from Firestore: ${e.message}", e)
            }
        }
    }

    fun pushPhotoToCloud(photo: GalleryPhoto, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val fields = JSONObject().apply {
                    put("id", stringField(photo.id))
                    put("albumId", stringField(photo.albumId))
                    put("title", stringField(photo.title))
                    put("caption", stringField(photo.caption))
                    put("date", stringField(photo.date))
                    put("imageUrl", stringField(photo.imageUrl))
                }
                saveDocument("gallery_photos", photo.id, fields)
                touchManifest("gallery", scope)
                Log.d(TAG, "Synced photo to Firestore: ${photo.caption}")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing photo to Firestore: ${e.message}", e)
            }
        }
    }

    fun deletePhotoFromCloud(photoId: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                deleteDocument("gallery_photos", photoId)
                touchManifest("gallery", scope)
                Log.d(TAG, "Deleted photo from Firestore: $photoId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting photo from Firestore: ${e.message}", e)
            }
        }
    }

    fun pushBookToCloud(book: IslamicBook, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val fields = JSONObject().apply {
                    put("id", stringField(book.id))
                    put("title", stringField(book.title))
                    put("author", stringField(book.author))
                    put("category", stringField(book.category))
                    put("pagesCount", intField(book.pagesCount))
                    put("language", stringField(book.language))
                    put("description", stringField(book.description))
                    put("contentPreview", stringField(book.contentPreview))
                    put("fileType", stringField(book.fileType))
                    put("fileUrl", stringField(book.fileUrl))
                    put("fileSize", stringField(book.fileSize))
                    put("hasAudioRecitation", booleanField(book.hasAudioRecitation))
                    put("audioUrl", stringField(book.audioUrl))
                }
                saveDocument("islamic_books", book.id, fields)
                touchManifest("books", scope)
                Log.d(TAG, "Synced book to Firestore: ${book.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing book to Firestore: ${e.message}", e)
            }
        }
    }

    fun deleteBookFromCloud(bookId: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                deleteDocument("islamic_books", bookId)
                touchManifest("books", scope)
                Log.d(TAG, "Deleted book from Firestore: $bookId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting book from Firestore: ${e.message}", e)
            }
        }
    }

    fun pushGalleryAssetToCloud(asset: GalleryAsset, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                var payloadUrl = asset.fileUrl
                val isImage = asset.fileType.equals("IMAGE", ignoreCase = true) ||
                        asset.fileExtension.lowercase() in listOf("jpg", "jpeg", "png", "webp")

                // If fileUrl is a local file path on this device, convert to Base64 so all other user devices can render it
                if (payloadUrl.startsWith("/") && isImage) {
                    val localFile = java.io.File(payloadUrl)
                    if (localFile.exists() && localFile.length() > 0) {
                        try {
                            val bytes = localFile.readBytes()
                            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                            payloadUrl = "data:image/jpeg;base64,$base64"
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed encoding local image to base64 for cloud push: ${e.message}")
                        }
                    }
                }

                var thumbUrl = asset.thumbnailUrl
                if (thumbUrl.startsWith("/") || thumbUrl.isBlank()) {
                    thumbUrl = payloadUrl
                }

                val fields = JSONObject().apply {
                    put("id", stringField(asset.id))
                    put("title", stringField(asset.title))
                    put("category", stringField(asset.category))
                    put("fileType", stringField(asset.fileType))
                    put("fileExtension", stringField(asset.fileExtension))
                    put("fileSize", stringField(asset.fileSize))
                    put("fileUrl", stringField(payloadUrl))
                    put("thumbnailUrl", stringField(thumbUrl))
                    put("uploadedDate", stringField(asset.uploadedDate))
                    put("date", stringField(asset.date))
                    put("description", stringField(asset.description))
                    put("type", stringField(asset.type))
                    put("uploadedBy", stringField(asset.uploadedBy))
                }
                saveDocument("gallery_assets", asset.id, fields)
                touchManifest("assets", scope)
                touchManifest("gallery", scope)
                Log.d(TAG, "Synced gallery asset to Firestore: ${asset.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing gallery asset to Firestore: ${e.message}", e)
            }
        }
    }

    fun deleteGalleryAssetFromCloud(assetId: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                deleteDocument("gallery_assets", assetId)
                touchManifest("assets", scope)
                Log.d(TAG, "Deleted gallery asset from Firestore: $assetId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting gallery asset from Firestore: ${e.message}", e)
            }
        }
    }

    fun pushPopupNoticeToCloud(popup: ImportantNoticePopup, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                var payloadUrl = popup.imageUrl
                if (payloadUrl.startsWith("/")) {
                    val localFile = java.io.File(payloadUrl)
                    if (localFile.exists() && localFile.length() > 0) {
                        try {
                            val bytes = localFile.readBytes()
                            val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                            payloadUrl = "data:image/jpeg;base64,$base64"
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed encoding local notice image to base64 for cloud push: ${e.message}")
                        }
                    }
                }

                val fields = JSONObject().apply {
                    put("id", stringField("important_popup"))
                    put("title", stringField(popup.title))
                    put("message", stringField(popup.message))
                    put("issuingDepartment", stringField(popup.issuingDepartment))
                    put("imageUrl", stringField(payloadUrl))
                    put("showOnLogin", booleanField(popup.showOnLogin))
                    put("isActive", booleanField(popup.isActive))
                    put("description", stringField(popup.description))
                    put("datePublished", stringField(popup.datePublished))
                    put("updatedAt", intField(System.currentTimeMillis()))
                }
                saveDocument("important_notice_popups", "important_popup", fields)
                touchManifest("popup", scope)
                touchManifest("notices", scope)
                Log.d(TAG, "Synced popup notice to Firestore: ${popup.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing popup notice to Firestore: ${e.message}", e)
            }
        }
    }

    fun pushNoticeToCloud(notice: NoticeItem, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val fields = JSONObject().apply {
                    put("id", stringField(notice.id))
                    put("title", stringField(notice.title))
                    put("content", stringField(notice.content))
                    put("priority", stringField(notice.priority.name))
                    put("date", stringField(notice.date))
                    put("isPinned", booleanField(notice.isPinned))
                    put("department", stringField(notice.department))
                }
                saveDocument("notice_items", notice.id, fields)
                touchManifest("notices", scope)
                Log.d(TAG, "Synced notice to Firestore: ${notice.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing notice to Firestore: ${e.message}", e)
            }
        }
    }

    fun deleteNoticeFromCloud(noticeId: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                deleteDocument("notice_items", noticeId)
                touchManifest("notices", scope)
                Log.d(TAG, "Deleted notice from Firestore: $noticeId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting notice from Firestore: ${e.message}", e)
            }
        }
    }

    fun pushInquiryToCloud(inquiry: AdminMessage, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val fields = JSONObject().apply {
                    put("id", stringField(inquiry.id))
                    put("senderName", stringField(inquiry.senderName))
                    put("senderContact", stringField(inquiry.senderContact))
                    put("category", stringField(inquiry.category.name))
                    put("subject", stringField(inquiry.subject))
                    put("message", stringField(inquiry.message))
                    put("timestamp", stringField(inquiry.timestamp))
                    put("status", stringField(inquiry.status.name))
                    put("reply", stringField(inquiry.adminReply ?: ""))
                    put("isRead", booleanField(inquiry.isRead))
                    put("internalNotes", stringField(inquiry.internalNotes ?: ""))
                    put("isFromAdmin", booleanField(inquiry.isFromAdmin))
                    put("threadId", stringField(inquiry.threadId))
                    put("createdAt", intField(inquiry.createdAt))
                }
                saveDocument("user_inquiries", inquiry.id, fields)
                touchManifest("inquiries", scope)
                Log.d(TAG, "Synced inquiry to Firestore from: ${inquiry.senderName}")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing inquiry to Firestore: ${e.message}", e)
            }
        }
    }

    fun updateInquiryReplyInCloud(
        messageId: String,
        status: MessageStatus,
        replyText: String,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val doc = fetchDocument("user_inquiries", messageId)
                val fields = doc?.optJSONObject("fields") ?: JSONObject()
                fields.put("status", stringField(status.name))
                fields.put("reply", stringField(replyText))
                fields.put("isRead", booleanField(true))
                saveDocument("user_inquiries", messageId, fields)
                touchManifest("inquiries", scope)
                Log.d(TAG, "Synced inquiry reply to Firestore: $messageId")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating inquiry reply in Firestore: ${e.message}", e)
            }
        }
    }

    fun deleteInquiryFromCloud(messageId: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                deleteDocument("user_inquiries", messageId)
                touchManifest("inquiries", scope)
                Log.d(TAG, "Deleted inquiry from Firestore: $messageId")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting inquiry from Firestore: ${e.message}", e)
            }
        }
    }

    fun pushDaroodSubmissionToCloud(submission: DaroodSubmission, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val fields = JSONObject().apply {
                    put("id", stringField(submission.id))
                    put("userNameOrNumber", stringField(submission.userNameOrNumber))
                    put("count", intField(submission.count))
                    put("timestamp", stringField(submission.timestamp))
                }
                saveDocument("darood_submissions", submission.id, fields)
                touchManifest("darood", scope)
                Log.d(TAG, "Synced Darood submission to Firestore: ${submission.userNameOrNumber} (+${submission.count})")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing Darood submission to Firestore: ${e.message}", e)
            }
        }
    }

    fun resetDaroodInCloud(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val submissions = fetchCollection("darood_submissions")
                for (doc in submissions) {
                    val name = doc.optString("name", "")
                    val docId = name.substringAfterLast("/")
                    if (docId.isNotBlank()) {
                        deleteDocument("darood_submissions", docId)
                    }
                }
                touchManifest("darood", scope)
                Log.d(TAG, "Cleared Darood submissions in Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting Darood in Firestore: ${e.message}", e)
            }
        }
    }

    fun pushActionCardConfigsToCloud(configs: List<ActionCardConfig>, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray()
                for (c in configs) {
                    val obj = JSONObject().apply {
                        put("cardKey", c.cardKey)
                        put("defaultTitle", c.defaultTitle)
                        put("customTitle", c.customTitle)
                        put("defaultSubtitle", c.defaultSubtitle)
                        put("customSubtitle", c.customSubtitle)
                        put("isVisibleToMembers", c.isVisibleToMembers)
                        put("orderIndex", c.orderIndex)
                    }
                    jsonArray.put(obj)
                }
                val fields = JSONObject().apply {
                    put("configsJson", stringField(jsonArray.toString()))
                }
                saveDocument("action_card_configs", "current_configs", fields)
                touchManifest("action_cards", scope)
                Log.d(TAG, "Synced ${configs.size} action card configs to Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing action cards to Firestore: ${e.message}", e)
            }
        }
    }

    suspend fun fetchActionCardConfigsFromCloud(): List<ActionCardConfig>? {
        val doc = fetchDocument("action_card_configs", "current_configs") ?: return null
        val fields = doc.optJSONObject("fields") ?: return null
        val jsonStr = getStringValue(fields, "configsJson")
        if (jsonStr.isNullOrBlank()) return null

        val list = mutableListOf<ActionCardConfig>()
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                ActionCardConfig(
                    cardKey = obj.optString("cardKey", ""),
                    defaultTitle = obj.optString("defaultTitle", ""),
                    customTitle = obj.optString("customTitle", ""),
                    defaultSubtitle = obj.optString("defaultSubtitle", ""),
                    customSubtitle = obj.optString("customSubtitle", ""),
                    isVisibleToMembers = obj.optBoolean("isVisibleToMembers", true),
                    orderIndex = obj.optInt("orderIndex", i)
                )
            )
        }
        return list
    }

    fun pushDaroodBannerToCloud(imageUrl: String, title: String = "", scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val fields = JSONObject().apply {
                    put("imageUrl", stringField(imageUrl))
                    put("title", stringField(title))
                    put("updatedAt", intField(System.currentTimeMillis()))
                }
                saveDocument("app_settings", "darood_banner", fields)
                Log.d(TAG, "Synced Darood banner image to Firestore.")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing Darood banner to Firestore: ${e.message}", e)
            }
        }
    }

    suspend fun fetchDaroodBannerFromCloud(): Pair<String, Long>? {
        return withContext(Dispatchers.IO) {
            try {
                val doc = fetchDocument("app_settings", "darood_banner") ?: return@withContext null
                val fields = doc.optJSONObject("fields") ?: return@withContext null
                val imageUrl = getStringValue(fields, "imageUrl")
                val updatedAtObj = fields.optJSONObject("updatedAt")
                val updatedAt = updatedAtObj?.optString("integerValue", "0")?.toLongOrNull() ?: 0L
                if (imageUrl.isNotBlank()) {
                    Pair(imageUrl, updatedAt)
                } else null
            } catch (e: Exception) {
                Log.w(TAG, "Error fetching Darood banner: ${e.message}")
                null
            }
        }
    }

    // -------------------------------------------------------------------------
    // Low-Level HTTP Helpers for Firestore REST API
    // -------------------------------------------------------------------------

    private fun fetchCollection(collectionName: String, orderBy: String? = null, pageSize: Int = 300): List<JSONObject> {
        val orderParam = if (!orderBy.isNullOrBlank()) "&orderBy=${java.net.URLEncoder.encode(orderBy, "UTF-8")}" else ""
        val url = "$BASE_URL/$collectionName?key=$API_KEY&pageSize=$pageSize$orderParam"
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return emptyList()
            }
            val body = response.body?.string() ?: return emptyList()
            val root = JSONObject(body)
            val docsArray = root.optJSONArray("documents") ?: return emptyList()
            val list = mutableListOf<JSONObject>()
            for (i in 0 until docsArray.length()) {
                list.add(docsArray.getJSONObject(i))
            }
            return list
        }
    }

    private fun fetchDocument(collectionName: String, documentId: String): JSONObject? {
        val url = "$BASE_URL/$collectionName/$documentId?key=$API_KEY"
        val request = Request.Builder().url(url).get().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return null
            }
            val body = response.body?.string() ?: return null
            return JSONObject(body)
        }
    }

    private fun saveDocument(collectionName: String, documentId: String, fields: JSONObject, merge: Boolean = false) {
        val maskParams = if (merge) {
            val keys = fields.keys()
            val list = mutableListOf<String>()
            while (keys.hasNext()) {
                val key = keys.next()
                list.add("updateMask.fieldPaths=$key")
            }
            if (list.isNotEmpty()) list.joinToString("&") + "&" else ""
        } else {
            ""
        }
        val url = "$BASE_URL/$collectionName/$documentId?${maskParams}key=$API_KEY"
        val payload = JSONObject().apply {
            put("fields", fields)
        }
        val requestBody = payload.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder().url(url).patch(requestBody).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "Save failed for $collectionName/$documentId: HTTP ${response.code} - ${response.message}")
            }
        }
    }

    private fun deleteDocument(collectionName: String, documentId: String) {
        val url = "$BASE_URL/$collectionName/$documentId?key=$API_KEY"
        val request = Request.Builder().url(url).delete().build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful && response.code != 404) {
                Log.w(TAG, "Delete failed for $collectionName/$documentId: HTTP ${response.code}")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Firestore JSON Parsing Helpers
    // -------------------------------------------------------------------------

    private fun stringField(value: String): JSONObject {
        // Firestore string fields can hold up to 1,048,487 bytes (~1MB). Keep up to 950KB for Base64 images.
        val safeValue = if (value.length > 950_000 && !value.startsWith("http")) {
            value.take(950_000)
        } else {
            value
        }
        return JSONObject().apply { put("stringValue", safeValue) }
    }
    private fun intField(value: Number): JSONObject = JSONObject().apply { put("integerValue", value.toLong().toString()) }
    private fun booleanField(value: Boolean): JSONObject = JSONObject().apply { put("booleanValue", value) }

    private fun getStringValue(fields: JSONObject, key: String, default: String = ""): String {
        val obj = fields.optJSONObject(key) ?: return default
        return obj.optString("stringValue", default)
    }

    private fun getLongValue(fields: JSONObject, key: String, default: Long = 0L): Long {
        val obj = fields.optJSONObject(key) ?: return default
        val str = obj.optString("integerValue", default.toString())
        return str.toLongOrNull() ?: default
    }

    private fun getIntValue(fields: JSONObject, key: String, default: Int = 0): Int {
        return getLongValue(fields, key, default.toLong()).toInt()
    }

    private fun getBooleanValue(fields: JSONObject, key: String, default: Boolean = false): Boolean {
        val obj = fields.optJSONObject(key) ?: return default
        return obj.optBoolean("booleanValue", default)
    }

    // -------------------------------------------------------------------------
    // Entity Parsers
    // -------------------------------------------------------------------------

    private fun parseUserEntity(doc: JSONObject): RegisteredUserEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val docName = doc.optString("name", "")
        val id = getStringValue(fields, "userId", docName.substringAfterLast("/"))
        val email = getStringValue(fields, "email")
        if (email.isBlank()) return null

        return RegisteredUserEntity(
            userId = id,
            fullName = getStringValue(fields, "fullName", "Community Member"),
            email = email,
            whatsappNumber = getStringValue(fields, "whatsappNumber", ""),
            gender = getStringValue(fields, "gender", "MALE"),
            password = getStringValue(fields, "password", "123456"),
            role = getStringValue(fields, "role", "STANDARD_USER"),
            registeredAt = getStringValue(fields, "registeredAt", "Aug 2026"),
            status = getStringValue(fields, "status", "Active")
        )
    }

    private fun parseEventEntity(doc: JSONObject): CommunityEventEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val docName = doc.optString("name", "")
        val id = getStringValue(fields, "id", docName.substringAfterLast("/"))
        val title = getStringValue(fields, "title")
        if (title.isBlank()) return null

        return CommunityEventEntity(
            id = id,
            title = title,
            dateGregorian = getStringValue(fields, "dateGregorian", "Upcoming"),
            dateHijri = getStringValue(fields, "dateHijri", ""),
            time = getStringValue(fields, "time", "After Maghrib"),
            venue = getStringValue(fields, "venue", "Main Hall"),
            address = getStringValue(fields, "address", ""),
            description = getStringValue(fields, "description", ""),
            category = getStringValue(fields, "category", "Community"),
            speaker = getStringValue(fields, "speaker", "Imam / Guest Scholar"),
            isRsvpEnabled = getBooleanValue(fields, "isRsvpEnabled", true),
            rsvpCount = getIntValue(fields, "rsvpCount", 0),
            isUserRsvp = false,
            isReminderSet = false,
            reminderMinutesBefore = 30,
            posterUrl = getStringValue(fields, "posterUrl", "")
        )
    }

    private fun parseNoticeEntity(doc: JSONObject): NoticeItemEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val docName = doc.optString("name", "")
        val id = getStringValue(fields, "id", docName.substringAfterLast("/"))
        val title = getStringValue(fields, "title")
        if (title.isBlank()) return null

        return NoticeItemEntity(
            id = id,
            title = title,
            content = getStringValue(fields, "content", ""),
            priority = getStringValue(fields, "priority", "IMPORTANT"),
            date = getStringValue(fields, "date", "Today"),
            isPinned = getBooleanValue(fields, "isPinned", false),
            department = getStringValue(fields, "department", "Administration")
        )
    }

    private fun parseImportantNoticePopup(doc: JSONObject): ImportantNoticePopupEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val title = getStringValue(fields, "title")
        if (title.isBlank()) return null

        return ImportantNoticePopupEntity(
            id = "important_popup",
            title = title,
            message = getStringValue(fields, "message", ""),
            issuingDepartment = getStringValue(fields, "issuingDepartment", "Administration"),
            imageUrl = getStringValue(fields, "imageUrl", ""),
            showOnLogin = getBooleanValue(fields, "showOnLogin", true),
            isActive = getBooleanValue(fields, "isActive", true),
            description = getStringValue(fields, "description", ""),
            datePublished = getStringValue(fields, "datePublished", "Aug 2026"),
            updatedAt = getLongValue(fields, "updatedAt", System.currentTimeMillis())
        )
    }

    private fun parseBookEntity(doc: JSONObject): IslamicBookEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val docName = doc.optString("name", "")
        val id = getStringValue(fields, "id", docName.substringAfterLast("/"))
        val title = getStringValue(fields, "title")
        if (title.isBlank()) return null

        return IslamicBookEntity(
            id = id,
            title = title,
            author = getStringValue(fields, "author", "Alnoor Publications"),
            category = getStringValue(fields, "category", "General"),
            pagesCount = getIntValue(fields, "pagesCount", 50),
            language = getStringValue(fields, "language", "Urdu / English"),
            description = getStringValue(fields, "description", ""),
            contentPreview = getStringValue(fields, "contentPreview", ""),
            fileType = getStringValue(fields, "fileType", "PDF"),
            fileUrl = getStringValue(fields, "fileUrl", ""),
            fileSize = getStringValue(fields, "fileSize", "2.5 MB"),
            hasAudioRecitation = getBooleanValue(fields, "hasAudioRecitation", false),
            audioUrl = getStringValue(fields, "audioUrl", ""),
            isBookmarked = false
        )
    }

    private fun parseAlbumEntity(doc: JSONObject): PhotoAlbumEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val docName = doc.optString("name", "")
        val id = getStringValue(fields, "id", docName.substringAfterLast("/"))
        val title = getStringValue(fields, "title")
        if (title.isBlank()) return null

        return PhotoAlbumEntity(
            id = id,
            title = title,
            description = getStringValue(fields, "description", ""),
            photoCount = getIntValue(fields, "photoCount", 0),
            coverUrl = getStringValue(fields, "coverUrl", ""),
            date = getStringValue(fields, "date", "Aug 2026")
        )
    }

    private fun parsePhotoEntity(doc: JSONObject): GalleryPhotoEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val docName = doc.optString("name", "")
        val id = getStringValue(fields, "id", docName.substringAfterLast("/"))
        val albumId = getStringValue(fields, "albumId")
        if (albumId.isBlank()) return null

        return GalleryPhotoEntity(
            id = id,
            albumId = albumId,
            title = getStringValue(fields, "title", "Gallery Photo"),
            caption = getStringValue(fields, "caption", ""),
            date = getStringValue(fields, "date", "Aug 2026"),
            imageUrl = getStringValue(fields, "imageUrl", "")
        )
    }

    private fun parseGalleryAssetEntity(doc: JSONObject): GalleryAssetEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val docName = doc.optString("name", "")
        val id = getStringValue(fields, "id", docName.substringAfterLast("/"))
        val title = getStringValue(fields, "title")
        if (title.isBlank()) return null

        var rawFileUrl = getStringValue(fields, "fileUrl", "")
        var rawThumbUrl = getStringValue(fields, "thumbnailUrl", "")

        // Fallback for legacy paths that only existed on a single physical phone
        if (rawFileUrl.startsWith("/") && !java.io.File(rawFileUrl).exists()) {
            // Check if it was an Islamic bank account poster or general media
            if (title.contains("Bank", ignoreCase = true) || title.contains("Account", ignoreCase = true) || title.contains("Trust", ignoreCase = true)) {
                rawFileUrl = "https://images.unsplash.com/photo-1542816417-0983c9c9ad53?w=800&auto=format&fit=crop&q=80"
                rawThumbUrl = rawFileUrl
            }
        }

        return GalleryAssetEntity(
            id = id,
            title = title,
            category = getStringValue(fields, "category", "Media"),
            fileType = getStringValue(fields, "fileType", "IMAGE"),
            fileExtension = getStringValue(fields, "fileExtension", "jpg"),
            fileSize = getStringValue(fields, "fileSize", "1.5 MB"),
            fileUrl = rawFileUrl,
            thumbnailUrl = if (rawThumbUrl.isNotBlank()) rawThumbUrl else rawFileUrl,
            uploadedDate = getStringValue(fields, "uploadedDate", "Aug 2026"),
            date = getStringValue(fields, "date", "Aug 2026"),
            description = getStringValue(fields, "description", ""),
            type = getStringValue(fields, "type", "Poster"),
            uploadedBy = getStringValue(fields, "uploadedBy", "Admin")
        )
    }

    private fun parseInquiryEntity(doc: JSONObject): UserInquiryEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val docName = doc.optString("name", "")
        val id = getStringValue(fields, "id", docName.substringAfterLast("/"))
        val message = getStringValue(fields, "message")
        if (message.isBlank()) return null

        return UserInquiryEntity(
            id = id,
            senderName = getStringValue(fields, "senderName", "Community Member"),
            senderContact = getStringValue(fields, "senderContact", ""),
            category = getStringValue(fields, "category", "GENERAL"),
            subject = getStringValue(fields, "subject", "Inquiry"),
            message = message,
            timestamp = getStringValue(fields, "timestamp", "Today"),
            status = getStringValue(fields, "status", "PENDING"),
            reply = getStringValue(fields, "reply").ifBlank { null },
            isRead = getBooleanValue(fields, "isRead", false),
            internalNotes = getStringValue(fields, "internalNotes").ifBlank { null },
            isFromAdmin = getBooleanValue(fields, "isFromAdmin", false),
            threadId = getStringValue(fields, "threadId", ""),
            createdAt = getLongValue(fields, "createdAt", System.currentTimeMillis())
        )
    }

    private fun parseDaroodSubmissionEntity(doc: JSONObject): DaroodSubmissionEntity? {
        val fields = doc.optJSONObject("fields") ?: return null
        val docName = doc.optString("name", "")
        val id = getStringValue(fields, "id", docName.substringAfterLast("/"))
        val user = getStringValue(fields, "userNameOrNumber", "Community Member")
        val count = getLongValue(fields, "count", 100L)

        return DaroodSubmissionEntity(
            id = id,
            userNameOrNumber = user,
            count = count,
            timestamp = getStringValue(fields, "timestamp", "Today")
        )
    }

    private fun parseAppVersionInfo(fields: JSONObject): AppVersionInfo {
        return AppVersionInfo(
            latestVersionCode = getIntValue(fields, "latestVersionCode", 1),
            latestVersionName = getStringValue(fields, "latestVersionName", "1.0.0"),
            minSupportedVersionCode = getIntValue(fields, "minSupportedVersionCode", 1),
            isForcedUpdate = getBooleanValue(fields, "isForcedUpdate", false),
            apkDownloadUrl = getStringValue(
                fields,
                "apkDownloadUrl",
                "https://github.com/AlnoorIslami/alnoor-islamic-app/releases/latest/download/app-release.apk"
            ),
            fallbackApkDownloadUrl = getStringValue(
                fields,
                "fallbackApkDownloadUrl",
                ""
            ),
            releaseNotes = getStringValue(
                fields,
                "releaseNotes",
                "• Important stability & cloud updates\n• Login poster popup enhancements"
            ),
            releaseDate = getStringValue(fields, "releaseDate", "August 2026"),
            apkSizeMb = getStringValue(fields, "apkSizeMb", "19.5 MB")
        )
    }
}
