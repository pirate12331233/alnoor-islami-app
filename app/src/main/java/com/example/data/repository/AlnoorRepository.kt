package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.CommunityEventEntity
import com.example.data.local.DaroodRecordEntity
import com.example.data.local.DaroodSubmissionEntity
import com.example.data.local.GalleryAssetEntity
import com.example.data.local.GalleryPhotoEntity
import com.example.data.local.ImportantNoticePopupEntity
import com.example.data.local.IslamicBookEntity
import com.example.data.local.NoticeItemEntity
import com.example.data.local.PhotoAlbumEntity
import com.example.data.local.RegisteredUserEntity
import com.example.data.local.SavedBookmarkEntity
import com.example.data.local.UserInquiryEntity
import com.example.data.local.YouTubePlaylistEntity
import com.example.data.auth.FirebaseAuthManager
import com.example.data.remote.FirestoreSyncManager
import com.example.data.security.SecurityCryptoManager
import com.example.data.model.ActionCardConfig
import com.example.data.model.AdminMessage
import com.example.data.model.AppThemeMode
import com.example.data.model.AppVersionInfo
import com.example.data.model.CommunityEvent
import com.example.data.model.DaroodState
import com.example.data.model.DaroodSubmission
import com.example.data.model.GalleryAsset
import com.example.data.model.GalleryPhoto
import com.example.data.model.ImportantNoticePopup
import com.example.data.model.IslamicBook
import com.example.data.model.LiveStreamItem
import com.example.data.model.MediaArchiveItem
import com.example.data.model.MessageCategory
import com.example.data.model.MessageStatus
import com.example.data.model.NoticeItem
import com.example.data.model.NoticePriority
import com.example.data.model.PhotoAlbum
import com.example.data.model.PrayerTimesData
import com.example.data.model.RegisteredUser
import com.example.data.model.UserGender
import com.example.data.model.UserRole
import com.example.data.model.YouTubePlaylist
import com.example.util.PrayerLocationService
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AlnoorRepository private constructor(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val firestoreSync = FirestoreSyncManager.getInstance()

    // Current Role (Admin vs Standard User)
    private val _currentUserRole = MutableStateFlow(UserRole.STANDARD_USER)
    val currentUserRole = _currentUserRole.asStateFlow()

    // Notification broadcast feed (Simulated FCM push notifications)
    private val _latestNotification = MutableStateFlow<String?>(null)
    val latestNotification = _latestNotification.asStateFlow()

    // Live Streams
    private val _liveStreams = MutableStateFlow(getInitialLiveStreams())
    val liveStreams = _liveStreams.asStateFlow()

    // Upcoming Events
    private val _events = MutableStateFlow<List<CommunityEvent>>(emptyList())
    val events = _events.asStateFlow()

    // Photo Gallery
    private val _albums = MutableStateFlow<List<PhotoAlbum>>(emptyList())
    val albums = _albums.asStateFlow()
    val photoAlbums = _albums.asStateFlow()

    private val _photos = MutableStateFlow<List<GalleryPhoto>>(emptyList())
    val photos = _photos.asStateFlow()

    // Islamic Library (PDFs and Images only)
    private val _books = MutableStateFlow<List<IslamicBook>>(emptyList())
    val books = _books.asStateFlow()

    // Prayer Times (Loaded from persistent storage / auto-detected)
    private val _prayerTimes = MutableStateFlow(PrayerLocationService.loadPrayerTimes(context))
    val prayerTimes = _prayerTimes.asStateFlow()

    // Media & Mahafil Playlists
    private val _mediaArchives = MutableStateFlow(getInitialMediaArchives())
    val mediaArchives = _mediaArchives.asStateFlow()

    private val _playlists = MutableStateFlow<List<YouTubePlaylist>>(emptyList())
    val playlists = _playlists.asStateFlow()

    // Gallery Assets (Images, PDFs, Documents)
    private val _galleryAssets = MutableStateFlow<List<GalleryAsset>>(emptyList())
    val galleryAssets = _galleryAssets.asStateFlow()

    // Important Notice Popup (Login Popup)
    private val _importantNoticePopup = MutableStateFlow(getInitialImportantNoticePopup())
    val importantNoticePopup = _importantNoticePopup.asStateFlow()

    private val _isImportantNoticeDismissed = MutableStateFlow(false)
    val isImportantNoticeDismissed = _isImportantNoticeDismissed.asStateFlow()

    // Important Notices
    private val _notices = MutableStateFlow<List<NoticeItem>>(emptyList())
    val notices = _notices.asStateFlow()

    // Messages / Inquiries
    private val _messages = MutableStateFlow<List<AdminMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    // Registered Users (Community Members & Admins)
    private val _registeredUsers = MutableStateFlow<List<RegisteredUser>>(emptyList())
    val registeredUsers = _registeredUsers.asStateFlow()

    // Darood Counter State & Submissions
    private val _daroodSubmissions = MutableStateFlow<List<DaroodSubmission>>(emptyList())
    val daroodSubmissions = _daroodSubmissions.asStateFlow()

    private val _daroodState = MutableStateFlow(
        DaroodState(
            submissions = emptyList(),
            grandTotal = 1425890L
        )
    )
    val daroodState = _daroodState.asStateFlow()

    // Darood Banner Image State (Stored in local device storage and synced weekly with Cloud)
    private val daroodBannerPrefs = context.getSharedPreferences("alnoor_darood_banner_prefs", Context.MODE_PRIVATE)
    private val _daroodBannerImageUrl = MutableStateFlow(loadCachedDaroodBannerImage())
    val daroodBannerImageUrl = _daroodBannerImageUrl.asStateFlow()

    private val _daroodBannerLastUpdated = MutableStateFlow(daroodBannerPrefs.getLong("darood_banner_updated_at", 0L))
    val daroodBannerLastUpdated = _daroodBannerLastUpdated.asStateFlow()

    // Active Audio Player State
    private val _currentlyPlayingMedia = MutableStateFlow<MediaArchiveItem?>(null)
    val currentlyPlayingMedia = _currentlyPlayingMedia.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio = _isPlayingAudio.asStateFlow()

    private val _audioProgressSeconds = MutableStateFlow(0)
    val audioProgressSeconds = _audioProgressSeconds.asStateFlow()

    // Dynamic Action Cards Configurations (Card Names & Member Visibility)
    private val actionCardsPrefs = context.getSharedPreferences("alnoor_action_cards_prefs", Context.MODE_PRIVATE)
    private val _actionCardConfigs = MutableStateFlow(loadActionCardConfigsFromPrefs())
    val actionCardConfigs = _actionCardConfigs.asStateFlow()

    // Dynamic Theme Customization (Emerald & Gold default, Dark Navy Blue, Pure White, OLED Black)
    private val appThemePrefs = context.getSharedPreferences("alnoor_theme_prefs", Context.MODE_PRIVATE)
    private val _currentThemeMode = MutableStateFlow(loadAppThemeMode())
    val currentThemeMode = _currentThemeMode.asStateFlow()

    private fun loadAppThemeMode(): AppThemeMode {
        val savedId = appThemePrefs.getString("active_theme_id", AppThemeMode.EMERALD_GREEN.id)
        return AppThemeMode.fromId(savedId)
    }

    fun setAppThemeMode(mode: AppThemeMode) {
        _currentThemeMode.value = mode
        appThemePrefs.edit().putString("active_theme_id", mode.id).apply()
    }

    // App Version & Forced Update State
    val appVersionInfo = firestoreSync.appVersionInfo

    fun updateAppVersionInfo(info: AppVersionInfo) {
        firestoreSync.pushAppVersionInfoToCloud(info, repositoryScope)
    }

    // Real-time Cloud First-Sync Status for New and Existing Users
    val isInitialSyncComplete = firestoreSync.isInitialSyncComplete
    val initialSyncMessage = firestoreSync.initialSyncMessage

    /**
     * Trigger immediate real-time sync when a user logs in or registers.
     * Ensures all Action Cards, Events, Notices, Books, and Gallery material
     * are refreshed directly from the Administrator's cloud database before dashboard display.
     */
    suspend fun performInitialFullSync(onStatusUpdate: ((String) -> Unit)? = null): Boolean {
        val result = firestoreSync.performImmediateFullSync(
            db = db,
            onActionCardsReceived = { remoteConfigs ->
                _actionCardConfigs.value = remoteConfigs
                saveActionCardConfigsToPrefs(remoteConfigs)
            },
            onNotificationReceived = { title, body ->
                // Incoming sync notifications are local to this device and must not re-broadcast to Firestore cloud
                triggerFcmPushNotification(title, body, isBroadcast = false)
            },
            onStatusUpdate = onStatusUpdate
        )
        // Refresh weekly Darood banner if due
        syncDaroodBannerFromCloud(force = false)
        return result
    }

    init {
        // Initialize persistent Room database and bind reactive flows
        repositoryScope.launch {
            try {
                seedDatabaseIfEmpty()
                bindDatabaseFlows()
                // Start smart manifest-based real-time Firestore synchronization across all connected client & admin devices
                firestoreSync.startRealtimeSync(
                    db = db,
                    scope = repositoryScope,
                    onActionCardsReceived = { remoteConfigs ->
                        val initialList = getInitialActionCardConfigs()
                        val merged = remoteConfigs.toMutableList()
                        for (defaultCard in initialList) {
                            if (merged.none { it.cardKey == defaultCard.cardKey }) {
                                merged.add(defaultCard)
                            }
                        }
                        _actionCardConfigs.value = merged
                        saveActionCardConfigsToPrefs(merged)
                    },
                    onNotificationReceived = { title, body ->
                        // Incoming sync notifications are local to this device and must not re-broadcast to Firestore cloud
                        triggerFcmPushNotification(title, body, isBroadcast = false)
                    }
                )

                // Automatic weekly Darood banner sync
                syncDaroodBannerFromCloud(force = false)

                // Automatic 24-hour location & prayer timings check on launch
                if (PrayerLocationService.shouldSyncLocationDaily(context)) {
                    Log.d("AlnoorRepository", "Daily 24-hour sync threshold met on startup. Updating device location & prayer timings...")
                    syncDeviceLocationAndPrayerTimes(force = false)
                }
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Error initializing database and cloud sync: ${e.message}", e)
            }
        }

        // Periodic background 24-hour location sync loop
        repositoryScope.launch(Dispatchers.IO) {
            while (true) {
                delay(30 * 60 * 1000L) // Check every 30 minutes
                try {
                    if (PrayerLocationService.shouldSyncLocationDaily(context)) {
                        Log.d("AlnoorRepository", "24 hours elapsed. Auto-syncing location and prayer times in background...")
                        syncDeviceLocationAndPrayerTimes(force = false)
                    }
                } catch (e: Exception) {
                    Log.w("AlnoorRepository", "Periodic 24h location sync check error: ${e.message}")
                }
            }
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        // 1. Seed Registered Users
        if (db.usersDao().getUsersCount() == 0) {
            val initialUsers = getInitialRegisteredUsers().map { user ->
                RegisteredUserEntity(
                    userId = user.userId,
                    fullName = user.fullName,
                    email = user.email,
                    whatsappNumber = user.whatsappNumber,
                    gender = user.gender.name,
                    password = SecurityCryptoManager.hashPassword(user.password),
                    role = user.role.name,
                    registeredAt = user.registeredAt,
                    status = user.status
                )
            }
            db.usersDao().insertUsers(initialUsers)
        }

        // 2. Seed Islamic Books
        if (db.booksDao().getBooksCount() == 0) {
            val initialBooks = getInitialBooks().map { book ->
                IslamicBookEntity(
                    id = book.id,
                    title = book.title,
                    author = book.author,
                    category = book.category,
                    pagesCount = book.pagesCount,
                    language = book.language,
                    description = book.description,
                    contentPreview = book.contentPreview,
                    fileType = book.fileType,
                    fileUrl = book.fileUrl,
                    fileSize = book.fileSize,
                    hasAudioRecitation = book.hasAudioRecitation,
                    audioUrl = book.audioUrl,
                    isBookmarked = book.isBookmarked
                )
            }
            db.booksDao().insertBooks(initialBooks)
        }

        // 3. Seed Photo Albums and Photos
        if (db.galleryDao().getAlbumsCount() == 0) {
            val initialAlbums = getInitialAlbums().map { album ->
                PhotoAlbumEntity(
                    id = album.id,
                    title = album.title,
                    description = album.description,
                    photoCount = album.photoCount,
                    coverUrl = album.coverUrl,
                    date = album.date
                )
            }
            db.galleryDao().insertAlbums(initialAlbums)

            val initialPhotos = getInitialPhotos().map { photo ->
                GalleryPhotoEntity(
                    id = photo.id,
                    albumId = photo.albumId,
                    title = photo.title,
                    caption = photo.caption,
                    date = photo.date,
                    imageUrl = photo.imageUrl
                )
            }
            db.galleryDao().insertPhotos(initialPhotos)
        }

        // 4. Seed Gallery Assets (Documents, PDFs, Images)
        if (db.galleryAssetsDao().getAssetsCount() == 0) {
            val initialAssets = getInitialGalleryAssets().map { asset ->
                GalleryAssetEntity(
                    id = asset.id,
                    title = asset.title,
                    category = asset.category,
                    fileType = asset.fileType,
                    fileExtension = asset.fileExtension,
                    fileSize = asset.fileSize,
                    fileUrl = asset.fileUrl,
                    thumbnailUrl = asset.thumbnailUrl,
                    uploadedDate = asset.uploadedDate,
                    date = asset.date,
                    description = asset.description,
                    type = asset.type,
                    uploadedBy = asset.uploadedBy
                )
            }
            db.galleryAssetsDao().insertAssets(initialAssets)
        }

        // 5. Seed Community Events
        val eventsPrefs = context.getSharedPreferences("alnoor_events_prefs", Context.MODE_PRIVATE)
        val hasSeededEventsV2 = eventsPrefs.getBoolean("has_seeded_events_v2", false)
        if (!hasSeededEventsV2 || db.eventsDao().getEventsCount() == 0) {
            val initialEvents = getInitialEvents().map { evt ->
                CommunityEventEntity(
                    id = evt.id,
                    title = evt.title,
                    dateGregorian = evt.dateGregorian,
                    dateHijri = evt.dateHijri,
                    time = evt.time,
                    venue = evt.venue,
                    address = evt.address,
                    description = evt.description,
                    category = evt.category,
                    speaker = evt.speaker,
                    isRsvpEnabled = evt.isRsvpEnabled,
                    rsvpCount = evt.rsvpCount,
                    isUserRsvp = evt.isUserRsvp,
                    isReminderSet = evt.isReminderSet,
                    reminderMinutesBefore = evt.reminderMinutesBefore,
                    posterUrl = evt.posterUrl
                )
            }
            db.eventsDao().insertEventsReplacing(initialEvents)
            eventsPrefs.edit().putBoolean("has_seeded_events_v2", true).apply()
        }

        // 6. Seed Darood Submissions (Seed only on very first install, do not re-seed after Admin reset)
        val daroodPrefs = context.getSharedPreferences("alnoor_darood_prefs", Context.MODE_PRIVATE)
        val hasSeededDarood = daroodPrefs.getBoolean("has_seeded_darood_v1", false)
        if (!hasSeededDarood && db.daroodDao().getSubmissionsCount() == 0) {
            val initialSubs = getInitialDaroodSubmissions().map { sub: DaroodSubmission ->
                DaroodSubmissionEntity(
                    id = sub.id,
                    userNameOrNumber = sub.userNameOrNumber,
                    count = sub.count,
                    timestamp = sub.timestamp
                )
            }
            db.daroodDao().insertSubmissions(initialSubs)
            daroodPrefs.edit().putBoolean("has_seeded_darood_v1", true).apply()
        }

        // 7. Seed Inquiries
        if (db.inquiriesDao().getInquiriesCount() == 0) {
            val initialMsgs = getInitialMessages().map { msg ->
                UserInquiryEntity(
                    id = msg.id,
                    senderName = msg.senderName,
                    senderContact = msg.senderContact,
                    category = msg.category.name,
                    subject = msg.subject,
                    message = msg.message,
                    timestamp = msg.timestamp,
                    status = msg.status.name,
                    reply = msg.adminReply,
                    isRead = msg.isRead,
                    internalNotes = msg.internalNotes
                )
            }
            db.inquiriesDao().insertInquiries(initialMsgs)
        }

        // 8. Seed Notices
        if (db.noticesDao().getNoticesCount() == 0) {
            val initialNotices = getInitialNotices().map { notice ->
                NoticeItemEntity(
                    id = notice.id,
                    title = notice.title,
                    content = notice.content,
                    priority = notice.priority.name,
                    date = notice.date,
                    isPinned = notice.isPinned,
                    department = notice.department
                )
            }
            db.noticesDao().insertNotices(initialNotices)
        }

        // 9. Seed Playlists
        if (db.playlistsDao().getPlaylistsCount() == 0) {
            val initialPlaylists = getInitialPlaylists().map { pl ->
                YouTubePlaylistEntity(
                    id = pl.id,
                    title = pl.title,
                    playlistUrl = pl.playlistUrl,
                    playlistId = pl.playlistId,
                    videoCount = pl.videoCount,
                    description = pl.description,
                    dateAdded = pl.dateAdded,
                    channelTitle = pl.channelTitle,
                    channelHandle = pl.channelHandle
                )
            }
            db.playlistsDao().insertPlaylists(initialPlaylists)
        }

        // 10. Seed Important Notice Popup (Login Popup Message)
        if (db.importantNoticeDao().getCount() == 0) {
            val initial = getInitialImportantNoticePopup()
            db.importantNoticeDao().insertOrUpdatePopup(
                ImportantNoticePopupEntity(
                    id = "important_popup",
                    title = initial.title,
                    message = initial.message,
                    issuingDepartment = initial.issuingDepartment,
                    imageUrl = initial.imageUrl,
                    showOnLogin = initial.showOnLogin,
                    isActive = initial.isActive,
                    description = initial.description,
                    datePublished = initial.datePublished,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun sanitizeFileUrlForStorage(url: String, prefix: String = "doc"): String {
        if (url.startsWith("data:") && url.length > 5000) {
            try {
                val uploadDir = java.io.File(context.filesDir, "uploaded_files").apply { mkdirs() }
                val ext = if (url.contains("pdf")) "pdf" else if (url.contains("png")) "png" else "jpg"
                val file = java.io.File(uploadDir, "${prefix}_${System.currentTimeMillis()}.$ext")
                val base64Data = if (url.contains("base64,")) url.substringAfter("base64,") else url.substringAfter(",")
                val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                java.io.FileOutputStream(file).use { it.write(bytes) }
                return file.absolutePath
            } catch (e: Exception) {
                Log.w("AlnoorRepository", "Failed saving base64 file to storage: ${e.message}")
            }
        }
        return url
    }

    private fun bindDatabaseFlows() {
        // Users Flow
        repositoryScope.launch {
            db.usersDao().getAllUsers()
                .catch { e -> Log.e("AlnoorRepository", "Error in users flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    RegisteredUser(
                        userId = entity.userId,
                        fullName = entity.fullName,
                        email = entity.email,
                        whatsappNumber = entity.whatsappNumber,
                        gender = try { UserGender.valueOf(entity.gender) } catch (_: Exception) { UserGender.MALE },
                        password = entity.password,
                        role = try { UserRole.valueOf(entity.role) } catch (_: Exception) { UserRole.STANDARD_USER },
                        registeredAt = entity.registeredAt,
                        status = entity.status
                    )
                }
                _registeredUsers.value = list
            }
        }

        // Islamic Books Flow
        repositoryScope.launch {
            db.booksDao().getAllBooks()
                .catch { e -> Log.e("AlnoorRepository", "Error in books flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    IslamicBook(
                        id = entity.id,
                        title = entity.title,
                        author = entity.author,
                        category = entity.category,
                        pagesCount = entity.pagesCount,
                        language = entity.language,
                        description = entity.description,
                        contentPreview = entity.contentPreview,
                        fileType = entity.fileType,
                        fileUrl = entity.fileUrl,
                        fileSize = entity.fileSize,
                        hasAudioRecitation = entity.hasAudioRecitation,
                        audioUrl = entity.audioUrl,
                        isBookmarked = entity.isBookmarked
                    )
                }
                _books.value = list
            }
        }

        // Albums Flow
        repositoryScope.launch {
            db.galleryDao().getAllAlbums()
                .catch { e -> Log.e("AlnoorRepository", "Error in albums flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    PhotoAlbum(
                        id = entity.id,
                        title = entity.title,
                        description = entity.description,
                        photoCount = entity.photoCount,
                        coverUrl = entity.coverUrl,
                        date = entity.date
                    )
                }
                _albums.value = list
            }
        }

        // Photos Flow
        repositoryScope.launch {
            db.galleryDao().getAllPhotos()
                .catch { e -> Log.e("AlnoorRepository", "Error in photos flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    GalleryPhoto(
                        id = entity.id,
                        albumId = entity.albumId,
                        title = entity.title,
                        caption = entity.caption,
                        date = entity.date,
                        imageUrl = entity.imageUrl
                    )
                }
                _photos.value = list
            }
        }

        // Gallery Assets Flow
        repositoryScope.launch {
            db.galleryAssetsDao().getAllAssets()
                .catch { e -> Log.e("AlnoorRepository", "Error in assets flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    GalleryAsset(
                        id = entity.id,
                        title = entity.title,
                        category = entity.category,
                        fileType = entity.fileType,
                        fileExtension = entity.fileExtension,
                        fileSize = entity.fileSize,
                        fileUrl = entity.fileUrl,
                        thumbnailUrl = entity.thumbnailUrl,
                        uploadedDate = entity.uploadedDate,
                        date = entity.date,
                        description = entity.description,
                        type = entity.type,
                        uploadedBy = entity.uploadedBy
                    )
                }
                _galleryAssets.value = list
            }
        }

        // Community Events Flow
        repositoryScope.launch {
            db.eventsDao().getAllEvents()
                .catch { e -> Log.e("AlnoorRepository", "Error in events flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    CommunityEvent(
                        id = entity.id,
                        title = entity.title,
                        dateGregorian = entity.dateGregorian,
                        dateHijri = entity.dateHijri,
                        time = entity.time,
                        venue = entity.venue,
                        address = entity.address,
                        description = entity.description,
                        category = entity.category,
                        speaker = entity.speaker,
                        isRsvpEnabled = entity.isRsvpEnabled,
                        rsvpCount = entity.rsvpCount,
                        isUserRsvp = entity.isUserRsvp,
                        isReminderSet = entity.isReminderSet,
                        reminderMinutesBefore = entity.reminderMinutesBefore,
                        posterUrl = entity.posterUrl
                    )
                }
                _events.value = list
            }
        }

        // Darood Submissions Flow
        repositoryScope.launch {
            db.daroodDao().getAllSubmissions()
                .catch { e -> Log.e("AlnoorRepository", "Error in darood flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    DaroodSubmission(
                        id = entity.id,
                        userNameOrNumber = entity.userNameOrNumber,
                        count = entity.count,
                        timestamp = entity.timestamp
                    )
                }
                _daroodSubmissions.value = list
                val totalSubmissions = list.sumOf { it.count }
                _daroodState.update { state ->
                    state.copy(
                        submissions = list,
                        grandTotal = totalSubmissions,
                        monthlyCommunityTotal = totalSubmissions
                    )
                }
            }
        }

        // Inquiries Flow
        repositoryScope.launch {
            db.inquiriesDao().getAllInquiries()
                .catch { e -> Log.e("AlnoorRepository", "Error in inquiries flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    AdminMessage(
                        id = entity.id,
                        senderName = entity.senderName,
                        senderContact = entity.senderContact,
                        category = try { MessageCategory.valueOf(entity.category) } catch (_: Exception) { MessageCategory.GENERAL },
                        subject = entity.subject,
                        message = entity.message,
                        timestamp = entity.timestamp,
                        status = try { MessageStatus.valueOf(entity.status) } catch (_: Exception) { MessageStatus.PENDING },
                        isRead = entity.isRead,
                        adminReply = entity.reply,
                        internalNotes = entity.internalNotes,
                        isFromAdmin = entity.isFromAdmin,
                        threadId = entity.threadId,
                        createdAt = entity.createdAt
                    )
                }
                _messages.value = list
            }
        }

        // Notices Flow
        repositoryScope.launch {
            db.noticesDao().getAllNotices()
                .catch { e -> Log.e("AlnoorRepository", "Error in notices flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    NoticeItem(
                        id = entity.id,
                        title = entity.title,
                        content = entity.content,
                        priority = try { NoticePriority.valueOf(entity.priority) } catch (_: Exception) { NoticePriority.GENERAL },
                        date = entity.date,
                        isPinned = entity.isPinned,
                        department = entity.department
                    )
                }
                _notices.value = list
            }
        }

        // Playlists Flow
        repositoryScope.launch {
            db.playlistsDao().getAllPlaylists()
                .catch { e -> Log.e("AlnoorRepository", "Error in playlists flow: ${e.message}", e) }
                .collectLatest { entities ->
                val list = entities.map { entity ->
                    val defaultPlaylist = getInitialPlaylists().find { it.id == entity.id }
                    YouTubePlaylist(
                        id = entity.id,
                        title = entity.title,
                        playlistUrl = entity.playlistUrl,
                        playlistId = entity.playlistId,
                        videoCount = entity.videoCount,
                        description = entity.description,
                        dateAdded = entity.dateAdded,
                        channelTitle = entity.channelTitle,
                        channelHandle = entity.channelHandle,
                        videos = defaultPlaylist?.videos ?: emptyList(),
                        items = defaultPlaylist?.items ?: emptyList()
                    )
                }
                _playlists.value = list
            }
        }

        // Important Notice Popup Flow
        repositoryScope.launch {
            db.importantNoticeDao().getPopupFlow()
                .catch { e -> Log.e("AlnoorRepository", "Error in popup flow: ${e.message}", e) }
                .collectLatest { entity ->
                if (entity != null) {
                    _importantNoticePopup.value = ImportantNoticePopup(
                        id = entity.id,
                        title = entity.title,
                        message = entity.message,
                        issuingDepartment = entity.issuingDepartment,
                        imageUrl = entity.imageUrl,
                        showOnLogin = entity.showOnLogin,
                        isActive = entity.isActive,
                        description = entity.description,
                        datePublished = entity.datePublished
                    )
                }
            }
        }
    }

    // Role Management
    fun setUserRole(role: UserRole) {
        _currentUserRole.value = role
    }

    fun triggerFcmPushNotification(title: String, body: String, targetTab: String? = null, isBroadcast: Boolean = true) {
        _latestNotification.value = "$title: $body"
        com.example.util.NotificationHelper.showHeadsUpNotification(
            context = context,
            title = title,
            body = body,
            targetTab = targetTab
        )
        // Broadcast notification to Firestore cloud so all closed/backgrounded devices receive it (only if isBroadcast is true)
        if (isBroadcast) {
            firestoreSync.pushBroadcastNotificationToCloud(title, body, targetTab, repositoryScope, context)
        }
    }

    fun clearNotification() {
        _latestNotification.value = null
    }

    /**
     * Broadcasts Option 1 Full-Screen Alarm/Call Style Flash Alert to all users and devices.
     * Wakes screens, displays over lockscreen, cannot be skipped without clicking OK,
     * supports 1,000+ characters, and archives automatically in the 1-to-1 Helpline chat.
     */
    fun broadcastFlashMessage(title: String, message: String): Result<Unit> {
        val cleanTitle = title.trim().ifBlank { "URGENT OFFICIAL ANNOUNCEMENT" }
        val cleanMessage = message.trim()
        if (cleanMessage.isBlank()) {
            return Result.failure(IllegalArgumentException("Announcement text cannot be empty."))
        }

        val alertId = "flash_${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()
        val formattedTimestamp = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(now))

        // 1. Push to Firestore Cloud flash_broadcasts collection so all devices receive it
        firestoreSync.pushFlashBroadcastToCloud(
            alertId = alertId,
            title = cleanTitle,
            message = cleanMessage,
            timestamp = now,
            formattedTimestamp = formattedTimestamp,
            scope = repositoryScope,
            context = context
        )

        // 2. Immediately archive in local 1-to-1 Helpline chat database with date/time stamp
        repositoryScope.launch(Dispatchers.IO) {
            try {
                db.inquiriesDao().insertInquiry(
                    UserInquiryEntity(
                        id = alertId,
                        senderName = "Alnoor Mosque Administration",
                        senderContact = "helpline@alnoor.org",
                        category = "GENERAL",
                        subject = "⚡ FLASH: $cleanTitle",
                        message = cleanMessage,
                        timestamp = formattedTimestamp,
                        status = "RESOLVED",
                        reply = null,
                        isRead = false,
                        internalNotes = "Broadcast Flash Message to All Community Devices",
                        isFromAdmin = true,
                        threadId = "FLASH_BROADCAST",
                        createdAt = now
                    )
                )
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed to archive flash message to helpline inquiries: ${e.message}", e)
            }
        }

        // 3. Trigger local Full-Screen Flash Alert on the admin device as immediate confirmation
        NotificationHelper.showFlashMessageAlert(
            context = context,
            title = cleanTitle,
            message = cleanMessage,
            timestamp = formattedTimestamp,
            alertId = alertId
        )

        return Result.success(Unit)
    }

    // --- Live Streams ---
    fun toggleLiveStatus(streamId: String) {
        _liveStreams.update { list ->
            list.map { stream ->
                if (stream.id == streamId) stream.copy(isLiveNow = !stream.isLiveNow) else stream
            }
        }
        val isNowLive = _liveStreams.value.find { it.id == streamId }?.isLiveNow == true
        if (isNowLive) {
            triggerFcmPushNotification(
                "🔴 Alnoor Live Broadcast Started",
                "Tune in now to the live Mahafil stream.",
                targetTab = "LIVE_STREAMS"
            )
        }
    }

    fun updateLiveStream(stream: LiveStreamItem) {
        _liveStreams.update { list ->
            list.map { if (it.id == stream.id) stream else it }
        }
        triggerFcmPushNotification("Live Stream Updated", stream.title, targetTab = "LIVE_STREAMS")
    }

    // --- Events Management (Admin CRUD & User RSVP/Reminder with Real-time Cloud Sync) ---
    fun addEvent(event: CommunityEvent) {
        repositoryScope.launch {
            db.eventsDao().insertEvent(
                CommunityEventEntity(
                    id = event.id,
                    title = event.title,
                    dateGregorian = event.dateGregorian,
                    dateHijri = event.dateHijri,
                    time = event.time,
                    venue = event.venue,
                    address = event.address,
                    description = event.description,
                    category = event.category,
                    speaker = event.speaker,
                    isRsvpEnabled = event.isRsvpEnabled,
                    rsvpCount = event.rsvpCount,
                    isUserRsvp = event.isUserRsvp,
                    isReminderSet = event.isReminderSet,
                    reminderMinutesBefore = event.reminderMinutesBefore,
                    posterUrl = event.posterUrl
                )
            )
        }
        // Broadcast immediately to Firestore Cloud so all connected users receive the event in real-time
        firestoreSync.pushEventToCloud(event, repositoryScope)
        triggerFcmPushNotification(
            "Upcoming New Mahafil",
            "${event.title} on ${event.dateGregorian}",
            targetTab = "EVENTS"
        )
    }

    fun updateEvent(event: CommunityEvent) {
        repositoryScope.launch {
            db.eventsDao().updateEvent(
                CommunityEventEntity(
                    id = event.id,
                    title = event.title,
                    dateGregorian = event.dateGregorian,
                    dateHijri = event.dateHijri,
                    time = event.time,
                    venue = event.venue,
                    address = event.address,
                    description = event.description,
                    category = event.category,
                    speaker = event.speaker,
                    isRsvpEnabled = event.isRsvpEnabled,
                    rsvpCount = event.rsvpCount,
                    isUserRsvp = event.isUserRsvp,
                    isReminderSet = event.isReminderSet,
                    reminderMinutesBefore = event.reminderMinutesBefore,
                    posterUrl = event.posterUrl
                )
            )
        }
        firestoreSync.pushEventToCloud(event, repositoryScope)
        triggerFcmPushNotification(
            "Upcoming Mahafil Updated",
            "${event.title} details updated by Admin.",
            targetTab = "EVENTS"
        )
    }

    fun deleteEvent(eventId: String) {
        repositoryScope.launch {
            db.eventsDao().deleteEvent(eventId)
        }
        firestoreSync.deleteEventFromCloud(eventId, repositoryScope)
    }

    fun toggleRsvp(eventId: String) {
        val currentEvent = _events.value.find { it.id == eventId } ?: return
        val newRsvp = !currentEvent.isUserRsvp
        val newCount = if (newRsvp) currentEvent.rsvpCount + 1 else (currentEvent.rsvpCount - 1).coerceAtLeast(0)

        repositoryScope.launch {
            db.eventsDao().updateRsvp(eventId, newRsvp, newCount)
        }
        val updatedEvent = currentEvent.copy(rsvpCount = newCount, isUserRsvp = newRsvp)
        firestoreSync.pushEventToCloud(updatedEvent, repositoryScope)
    }

    fun toggleEventReminder(eventId: String, isReminderSet: Boolean, minutesBefore: Int) {
        repositoryScope.launch {
            db.eventsDao().updateReminder(eventId, isReminderSet, minutesBefore)
        }
    }

    // --- Photos & Albums Management ---
    fun addAlbum(album: PhotoAlbum) {
        repositoryScope.launch {
            db.galleryDao().insertAlbum(
                PhotoAlbumEntity(
                    id = album.id,
                    title = album.title,
                    description = album.description,
                    photoCount = album.photoCount,
                    coverUrl = album.coverUrl,
                    date = album.date
                )
            )
        }
        firestoreSync.pushAlbumToCloud(album, repositoryScope)
    }

    fun deleteAlbum(albumId: String) {
        repositoryScope.launch {
            db.galleryDao().deleteAlbum(albumId)
        }
        firestoreSync.deleteAlbumFromCloud(albumId, repositoryScope)
    }

    fun addPhoto(photo: GalleryPhoto) {
        repositoryScope.launch {
            db.galleryDao().insertPhoto(
                GalleryPhotoEntity(
                    id = photo.id,
                    albumId = photo.albumId,
                    title = photo.title,
                    caption = photo.caption,
                    date = photo.date,
                    imageUrl = photo.imageUrl
                )
            )
            db.galleryDao().recalculatePhotoCount(photo.albumId)
        }
        firestoreSync.pushPhotoToCloud(photo, repositoryScope)
    }

    fun deletePhoto(photoId: String) {
        val photoToDelete = _photos.value.find { it.id == photoId }
        repositoryScope.launch {
            db.galleryDao().deletePhoto(photoId)
            if (photoToDelete != null) {
                db.galleryDao().recalculatePhotoCount(photoToDelete.albumId)
            }
        }
        firestoreSync.deletePhotoFromCloud(photoId, repositoryScope)
    }

    // --- Library CRUD (Admin Full CRUD, User View-Only) ---
    fun addBook(book: IslamicBook) {
        val safeFileUrl = sanitizeFileUrlForStorage(book.fileUrl, "book_${book.id}")
        val safeBook = book.copy(fileUrl = safeFileUrl)
        repositoryScope.launch {
            try {
                db.booksDao().insertBook(
                    IslamicBookEntity(
                        id = safeBook.id,
                        title = safeBook.title,
                        author = safeBook.author,
                        category = safeBook.category,
                        pagesCount = safeBook.pagesCount,
                        language = safeBook.language,
                        description = safeBook.description,
                        contentPreview = safeBook.contentPreview,
                        fileType = safeBook.fileType,
                        fileUrl = safeBook.fileUrl,
                        fileSize = safeBook.fileSize,
                        hasAudioRecitation = safeBook.hasAudioRecitation,
                        audioUrl = safeBook.audioUrl,
                        isBookmarked = safeBook.isBookmarked
                    )
                )
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed inserting book into DB: ${e.message}", e)
            }
        }
        firestoreSync.pushBookToCloud(safeBook, repositoryScope)
        triggerFcmPushNotification("New Document Added", "Added to Library: ${safeBook.title}")
    }

    fun updateBook(book: IslamicBook) {
        val safeFileUrl = sanitizeFileUrlForStorage(book.fileUrl, "book_${book.id}")
        val safeBook = book.copy(fileUrl = safeFileUrl)
        repositoryScope.launch {
            try {
                db.booksDao().updateBook(
                    IslamicBookEntity(
                        id = safeBook.id,
                        title = safeBook.title,
                        author = safeBook.author,
                        category = safeBook.category,
                        pagesCount = safeBook.pagesCount,
                        language = safeBook.language,
                        description = safeBook.description,
                        contentPreview = safeBook.contentPreview,
                        fileType = safeBook.fileType,
                        fileUrl = safeBook.fileUrl,
                        fileSize = safeBook.fileSize,
                        hasAudioRecitation = safeBook.hasAudioRecitation,
                        audioUrl = safeBook.audioUrl,
                        isBookmarked = safeBook.isBookmarked
                    )
                )
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed updating book in DB: ${e.message}", e)
            }
        }
        firestoreSync.pushBookToCloud(safeBook, repositoryScope)
        triggerFcmPushNotification("Document Updated", "Updated: ${safeBook.title}")
    }

    fun deleteBook(bookId: String) {
        repositoryScope.launch {
            db.booksDao().deleteBook(bookId)
        }
        firestoreSync.deleteBookFromCloud(bookId, repositoryScope)
    }

    fun toggleBookmarkBook(bookId: String) {
        val currentBook = _books.value.find { it.id == bookId } ?: return
        val newBookmarkState = !currentBook.isBookmarked
        repositoryScope.launch {
            db.booksDao().updateBookmarkStatus(bookId, newBookmarkState)
            if (newBookmarkState) {
                db.bookmarksDao().addBookmark(
                    SavedBookmarkEntity(
                        itemId = bookId,
                        itemType = "BOOK",
                        title = currentBook.title,
                        subtitle = currentBook.author
                    )
                )
            } else {
                db.bookmarksDao().removeBookmark(bookId)
            }
        }
    }

    // --- Prayer Times & Jamat Timings (Dynamic GPS/Location & Admin) ---
    fun syncDeviceLocationAndPrayerTimes(
        force: Boolean = false,
        onCompleted: ((PrayerTimesData) -> Unit)? = null
    ) {
        repositoryScope.launch(Dispatchers.IO) {
            try {
                if (force || PrayerLocationService.shouldSyncLocationDaily(context)) {
                    PrayerLocationService.detectLocationAndCalculatePrayerTimes(context) { newTimes ->
                        _prayerTimes.value = newTimes
                        onCompleted?.invoke(newTimes)
                        Log.d("AlnoorRepository", "Device location and prayer timings synced: ${newTimes.locationName}")
                    }
                } else {
                    val current = PrayerLocationService.loadPrayerTimes(context)
                    _prayerTimes.value = current
                    onCompleted?.invoke(current)
                }
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Error syncing device location and prayer timings: ${e.message}", e)
                val fallback = PrayerLocationService.loadPrayerTimes(context)
                _prayerTimes.value = fallback
                onCompleted?.invoke(fallback)
            }
        }
    }

    fun updatePrayerTimes(newTimes: PrayerTimesData) {
        _prayerTimes.value = newTimes
        PrayerLocationService.savePrayerTimes(context, newTimes, updateTimestamp = true)
        triggerFcmPushNotification("Prayer Schedule Updated", "Prayer timings updated for ${newTimes.locationName}.")
    }

    // --- Mahafil Archive & YouTube Playlists (Admin CRUD) ---
    fun addYouTubePlaylist(url: String, title: String, description: String = "") {
        val playlistId = extractYouTubePlaylistId(url)
        val generatedVideos = listOf(
            MediaArchiveItem(
                id = UUID.randomUUID().toString(),
                title = if (title.isNotBlank()) "$title (Part 1 - Opening)" else "Khatam Sharif & Darood Gathering (Part 1)",
                type = "Khatam Sharif",
                reciter = "Hazrat Allama & Choir",
                duration = "34:20",
                audioUrl = url,
                date = "Aug 2026",
                description = "Opening recitation and Hamd"
            ),
            MediaArchiveItem(
                id = UUID.randomUUID().toString(),
                title = if (title.isNotBlank()) "$title (Part 2 - Naat)" else "Heartfelt Naat-e-Rasool ﷺ & Manqabat (Part 2)",
                type = "Hamd-o-Naat",
                reciter = "Alnoor Naat Council",
                duration = "26:15",
                audioUrl = url,
                date = "Aug 2026",
                description = "Soulful poetry in praise of Sayyiduna Rasoolullah ﷺ"
            ),
            MediaArchiveItem(
                id = UUID.randomUUID().toString(),
                title = if (title.isNotBlank()) "$title (Part 3 - Bayan & Dua)" else "Spiritual Bayan & Closing Collective Dua (Part 3)",
                type = "Zikr & Dua",
                reciter = "Hazrat Allama Peer Syed",
                duration = "48:50",
                audioUrl = url,
                date = "Aug 2026",
                description = "Spiritual counsel and tearful supplication"
            )
        )
        val newPlaylist = YouTubePlaylist(
            id = UUID.randomUUID().toString(),
            title = if (title.isNotBlank()) title else "Mahafil & Khatam Mubarak Playlist",
            playlistUrl = url,
            playlistId = playlistId,
            videoCount = generatedVideos.size,
            description = description.ifBlank { "Official YouTube video playlist from @AlnoorislamiMushahidat" },
            dateAdded = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
            channelTitle = "Alnoor Islami Live",
            channelHandle = "@AlnoorislamiMushahidat",
            videos = generatedVideos,
            items = generatedVideos
        )
        repositoryScope.launch {
            db.playlistsDao().insertPlaylist(
                YouTubePlaylistEntity(
                    id = newPlaylist.id,
                    title = newPlaylist.title,
                    playlistUrl = newPlaylist.playlistUrl,
                    playlistId = newPlaylist.playlistId,
                    videoCount = newPlaylist.videoCount,
                    description = newPlaylist.description,
                    dateAdded = newPlaylist.dateAdded,
                    channelTitle = newPlaylist.channelTitle,
                    channelHandle = newPlaylist.channelHandle
                )
            )
        }
        triggerFcmPushNotification("New Mahafil Playlist Added", newPlaylist.title)
    }

    fun deletePlaylist(playlistId: String) {
        repositoryScope.launch {
            db.playlistsDao().deletePlaylist(playlistId)
        }
    }

    fun deleteVideoFromPlaylist(playlistId: String, videoId: String) {
        _playlists.update { list ->
            list.map { playlist ->
                if (playlist.id == playlistId) {
                    val updatedVideos = playlist.videos.filterNot { it.id == videoId }
                    playlist.copy(videos = updatedVideos, items = updatedVideos, videoCount = updatedVideos.size)
                } else playlist
            }
        }
    }

    private fun extractYouTubePlaylistId(url: String): String {
        return try {
            if (url.contains("list=")) {
                url.substringAfter("list=").substringBefore("&").substringBefore(" ")
            } else if (url.startsWith("PL")) {
                url.trim()
            } else {
                "PL_Alnoor_LiveMahafil_${System.currentTimeMillis() % 10000}"
            }
        } catch (_: Exception) {
            "PL_Alnoor_Mahafil"
        }
    }

    // --- Gallery Assets (Images, PDFs, Documents - Admin Upload & CRUD, User View-Only) ---
    fun addGalleryAsset(asset: GalleryAsset) {
        val safeFileUrl = sanitizeFileUrlForStorage(asset.fileUrl, "asset_${asset.id}")
        val safeAsset = asset.copy(fileUrl = safeFileUrl)
        repositoryScope.launch {
            try {
                db.galleryAssetsDao().insertAsset(
                    GalleryAssetEntity(
                        id = safeAsset.id,
                        title = safeAsset.title,
                        category = safeAsset.category,
                        fileType = safeAsset.fileType,
                        fileExtension = safeAsset.fileExtension,
                        fileSize = safeAsset.fileSize,
                        fileUrl = safeAsset.fileUrl,
                        thumbnailUrl = safeAsset.thumbnailUrl,
                        uploadedDate = safeAsset.uploadedDate,
                        date = safeAsset.date,
                        description = safeAsset.description,
                        type = safeAsset.type,
                        uploadedBy = safeAsset.uploadedBy
                    )
                )
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed inserting gallery asset: ${e.message}", e)
            }
        }
        // Send the asset with original cloud-ready URL/Base64 to Firestore so all devices can view it
        firestoreSync.pushGalleryAssetToCloud(asset, repositoryScope)
        triggerFcmPushNotification("New Gallery Asset", "Uploaded: ${safeAsset.title} (${safeAsset.fileType})")
    }

    fun refreshGalleryAssets(onComplete: (() -> Unit)? = null) {
        repositoryScope.launch(Dispatchers.IO) {
            try {
                firestoreSync.syncAssetsSection(db)
                firestoreSync.syncGallerySection(db)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                }
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed refreshing gallery assets: ${e.message}")
            }
        }
    }

    fun deleteGalleryAsset(assetId: String) {
        repositoryScope.launch {
            db.galleryAssetsDao().deleteAsset(assetId)
        }
        firestoreSync.deleteGalleryAssetFromCloud(assetId, repositoryScope)
    }

    // --- Important Notice Full-Screen Popup (Persisted to Room & Cloud) ---
    fun updateImportantNoticePopup(popup: ImportantNoticePopup) {
        val standardized = popup.copy(id = "important_popup")
        _importantNoticePopup.value = standardized
        repositoryScope.launch {
            try {
                db.importantNoticeDao().insertOrUpdatePopup(
                    ImportantNoticePopupEntity(
                        id = "important_popup",
                        title = standardized.title,
                        message = standardized.message,
                        issuingDepartment = standardized.issuingDepartment,
                        imageUrl = standardized.imageUrl,
                        showOnLogin = standardized.showOnLogin,
                        isActive = standardized.isActive,
                        description = standardized.description,
                        datePublished = standardized.datePublished,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                Log.d("AlnoorRepository", "Successfully persisted login popup message to database: ${standardized.title}")
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed saving login popup message to Room: ${e.message}", e)
            }
        }
        firestoreSync.pushPopupNoticeToCloud(standardized, repositoryScope)
        if (standardized.showOnLogin) {
            triggerFcmPushNotification("🚨 Mandatory Mosque Notice", standardized.title)
        }
    }

    fun setImportantNoticePopup(title: String, description: String, imageUrl: String, isActive: Boolean) {
        val popup = ImportantNoticePopup(
            id = "important_popup",
            title = title,
            message = description,
            description = description,
            imageUrl = imageUrl,
            isActive = isActive,
            showOnLogin = isActive,
            datePublished = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        )
        updateImportantNoticePopup(popup)
        _isImportantNoticeDismissed.value = false
    }

    fun dismissImportantNoticePopup() {
        _isImportantNoticeDismissed.value = true
    }

    // --- Darood Sharif Recitation Submissions & Admin CSV ---
    fun submitDaroodCount(userNameOrNumber: String, count: Long) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        val dateDayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val validCount = count.coerceAtLeast(1)
        val validUser = userNameOrNumber.ifBlank { "Community Member" }
        val now = Date()

        val submission = DaroodSubmission(
            id = UUID.randomUUID().toString(),
            userNameOrNumber = validUser,
            count = validCount,
            timestamp = dateFormat.format(now)
        )

        // Persist permanently in Room Database
        repositoryScope.launch {
            try {
                db.daroodDao().insertSubmission(
                    DaroodSubmissionEntity(
                        id = submission.id,
                        userNameOrNumber = submission.userNameOrNumber,
                        count = submission.count,
                        timestamp = submission.timestamp
                    )
                )
                db.daroodDao().insertRecord(
                    DaroodRecordEntity(
                        date = dateDayFormat.format(now),
                        count = validCount
                    )
                )
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed persisting Darood count to DB: ${e.message}", e)
            }
        }

        // Push to Firestore Cloud so community total syncs across all devices
        firestoreSync.pushDaroodSubmissionToCloud(submission, repositoryScope)

        _daroodState.update { state ->
            state.copy(
                dailyCount = state.dailyCount + validCount,
                personalLifetimeCount = state.personalLifetimeCount + validCount,
                monthlyCommunityTotal = state.monthlyCommunityTotal + validCount
            )
        }

        // Trigger notification to admin
        triggerFcmPushNotification(
            "New Darood Sharif Submission",
            "${submission.userNameOrNumber} submitted ${submission.count} Darood Sharif recitations."
        )
    }

    fun generateDaroodCsv(): String = generateDaroodSubmissionsCsv()

    fun resetDaroodCounts() {
        repositoryScope.launch {
            try {
                db.daroodDao().clearAll()
                db.daroodDao().clearAllSubmissions()
                _daroodSubmissions.value = emptyList()
                _daroodState.update { state ->
                    state.copy(
                        dailyCount = 0L,
                        monthlyCommunityTotal = 0L,
                        personalLifetimeCount = 0L,
                        submissions = emptyList(),
                        grandTotal = 0L
                    )
                }
                Log.d("AlnoorRepository", "Durood recitation counts cleared to 0 by Admin")
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed resetting Durood counts: ${e.message}", e)
            }
        }
        // Broadcast reset to Cloud so all devices zero out simultaneously
        firestoreSync.resetDaroodInCloud(repositoryScope)
        triggerFcmPushNotification(
            "Monthly Durood Recitation Reset",
            "Admin has reset the community Durood recitation count to 0 for the monthly cycle."
        )
    }

    fun generateDaroodSubmissionsCsv(): String {
        val sb = StringBuilder()
        sb.append("User Name/Number,Count,Date & Time\n")
        val allSubs = _daroodSubmissions.value
        for (sub in allSubs) {
            val sanitizedUser = sub.userNameOrNumber.replace("\"", "\"\"").replace(",", " ")
            val sanitizedTime = sub.timestamp.replace("\"", "\"\"").replace(",", " ")
            sb.append("\"").append(sanitizedUser).append("\",")
                .append(sub.count).append(",")
                .append("\"").append(sanitizedTime).append("\"\n")
        }
        sb.append("GRAND TOTAL,").append(_daroodState.value.grandTotal).append(",All Time\n")
        return sb.toString()
    }

    private fun loadCachedDaroodBannerImage(): String {
        return daroodBannerPrefs.getString(
            "darood_banner_image_url",
            "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?q=80&w=1200&auto=format&fit=crop"
        ) ?: "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?q=80&w=1200&auto=format&fit=crop"
    }

    /**
     * Updates the Darood Sharif banner image, saves it to local device storage,
     * and broadcasts it to Firestore so all member devices sync and display it.
     */
    fun updateDaroodBannerImage(newImageUrl: String, title: String = "Blessed Darood Sharif Banner") {
        val now = System.currentTimeMillis()
        daroodBannerPrefs.edit()
            .putString("darood_banner_image_url", newImageUrl)
            .putLong("darood_banner_updated_at", now)
            .putLong("darood_banner_last_synced_week", now)
            .apply()
        _daroodBannerImageUrl.value = newImageUrl
        _daroodBannerLastUpdated.value = now
        firestoreSync.pushDaroodBannerToCloud(newImageUrl, title, repositoryScope)
        triggerFcmPushNotification(
            "Darood Sharif Banner Updated",
            "A new weekly Darood recitation image banner has been published by the Administrator."
        )
    }

    /**
     * Checks if the device should sync the Darood banner from the Cloud.
     * Automatically triggers on the first day of the week (Sunday/Monday) or after 7 days,
     * replacing the local device image with the latest cloud image for the entire week.
     */
    fun syncDaroodBannerFromCloud(force: Boolean = false) {
        repositoryScope.launch(Dispatchers.IO) {
            try {
                val lastSync = daroodBannerPrefs.getLong("darood_banner_last_synced_week", 0L)
                val now = System.currentTimeMillis()
                val calendar = java.util.Calendar.getInstance()
                val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
                val isFirstDayOfWeek = (dayOfWeek == java.util.Calendar.SUNDAY || dayOfWeek == java.util.Calendar.MONDAY)
                val shouldSync = force || (now - lastSync >= sevenDaysMs) ||
                        (isFirstDayOfWeek && (now - lastSync >= 24 * 60 * 60 * 1000L)) ||
                        _daroodBannerImageUrl.value.isBlank()

                if (shouldSync) {
                    val remoteBanner = firestoreSync.fetchDaroodBannerFromCloud()
                    if (remoteBanner != null) {
                        val (cloudUrl, updatedAt) = remoteBanner
                        val currentLocalUpdated = daroodBannerPrefs.getLong("darood_banner_updated_at", 0L)
                        if (cloudUrl.isNotBlank() && (cloudUrl != _daroodBannerImageUrl.value || updatedAt > currentLocalUpdated)) {
                            daroodBannerPrefs.edit()
                                .putString("darood_banner_image_url", cloudUrl)
                                .putLong("darood_banner_updated_at", updatedAt)
                                .putLong("darood_banner_last_synced_week", now)
                                .apply()
                            withContext(Dispatchers.Main) {
                                _daroodBannerImageUrl.value = cloudUrl
                                _daroodBannerLastUpdated.value = updatedAt
                            }
                            Log.d("AlnoorRepository", "Weekly Darood banner updated from cloud: $cloudUrl")
                        } else {
                            daroodBannerPrefs.edit().putLong("darood_banner_last_synced_week", now).apply()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("AlnoorRepository", "Failed weekly Darood banner sync: ${e.message}")
            }
        }
    }

    // --- Media Archive CRUD (Admin) ---
    fun addMediaItem(item: MediaArchiveItem) {
        _mediaArchives.update { listOf(item) + it }
        triggerFcmPushNotification("New Recording Archived", "Added: ${item.title}")
    }

    fun deleteMediaItem(itemId: String) {
        _mediaArchives.update { list -> list.filterNot { it.id == itemId } }
    }

    // Audio Playback
    fun playMedia(item: MediaArchiveItem) {
        _currentlyPlayingMedia.value = item
        _isPlayingAudio.value = true
        _audioProgressSeconds.value = 0
    }

    fun togglePlayPauseAudio() {
        _isPlayingAudio.update { !it }
    }

    fun stopAudio() {
        _isPlayingAudio.value = false
        _currentlyPlayingMedia.value = null
    }

    fun seekAudio(seconds: Int) {
        _audioProgressSeconds.value = seconds
    }

    // --- Important Notices CRUD (Admin with Real-time Cloud Sync) ---
    fun addNotice(notice: NoticeItem) {
        repositoryScope.launch {
            db.noticesDao().insertNotice(
                NoticeItemEntity(
                    id = notice.id,
                    title = notice.title,
                    content = notice.content,
                    priority = notice.priority.name,
                    date = notice.date,
                    isPinned = notice.isPinned,
                    department = notice.department,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        firestoreSync.pushNoticeToCloud(notice, repositoryScope)
        val prefix = if (notice.priority == NoticePriority.URGENT) "🚨 URGENT NOTICE" else "📢 Important Notice"
        triggerFcmPushNotification(prefix, notice.title, targetTab = "NOTICES")
    }

    fun updateNotice(notice: NoticeItem) {
        repositoryScope.launch {
            db.noticesDao().insertNotice(
                NoticeItemEntity(
                    id = notice.id,
                    title = notice.title,
                    content = notice.content,
                    priority = notice.priority.name,
                    date = notice.date,
                    isPinned = notice.isPinned,
                    department = notice.department,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        firestoreSync.pushNoticeToCloud(notice, repositoryScope)
        triggerFcmPushNotification("Notice Updated", notice.title, targetTab = "NOTICES")
    }

    fun deleteNotice(noticeId: String) {
        repositoryScope.launch {
            db.noticesDao().deleteNotice(noticeId)
        }
        firestoreSync.deleteNoticeFromCloud(noticeId, repositoryScope)
    }

    fun togglePinNotice(noticeId: String) {
        val current = _notices.value.find { it.id == noticeId } ?: return
        val updated = current.copy(isPinned = !current.isPinned)
        repositoryScope.launch {
            db.noticesDao().updatePinned(noticeId, updated.isPinned)
        }
        firestoreSync.pushNoticeToCloud(updated, repositoryScope)
    }

    // --- Messages & Inquiries (Persisted to Room & Real-time Cloud Sync) ---
    fun sendChatMessage(
        threadId: String,
        senderName: String,
        senderContact: String,
        text: String,
        isFromAdmin: Boolean,
        category: MessageCategory = MessageCategory.GENERAL,
        subject: String = "Chat Message"
    ) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        val cleanThreadId = threadId.ifBlank {
            val contactKey = senderContact.trim().lowercase().filter { it.isLetterOrDigit() }
            if (contactKey.isNotBlank() && contactKey != "notprovided") "contact_$contactKey"
            else "user_${UUID.randomUUID().toString().take(8)}"
        }

        val newMessage = AdminMessage(
            id = UUID.randomUUID().toString(),
            senderName = senderName.ifBlank { if (isFromAdmin) "Alnoor Admin" else "Community Member" },
            senderContact = senderContact.ifBlank { "Not provided" },
            category = category,
            subject = subject,
            message = text.trim(),
            timestamp = dateFormat.format(Date()),
            status = if (isFromAdmin) MessageStatus.RESOLVED else MessageStatus.PENDING,
            isRead = isFromAdmin,
            adminReply = null,
            internalNotes = null,
            isFromAdmin = isFromAdmin,
            threadId = cleanThreadId,
            createdAt = System.currentTimeMillis()
        )

        repositoryScope.launch {
            try {
                db.inquiriesDao().insertInquiry(
                    UserInquiryEntity(
                        id = newMessage.id,
                        senderName = newMessage.senderName,
                        senderContact = newMessage.senderContact,
                        category = newMessage.category.name,
                        subject = newMessage.subject,
                        message = newMessage.message,
                        timestamp = newMessage.timestamp,
                        status = newMessage.status.name,
                        reply = null,
                        isRead = newMessage.isRead,
                        internalNotes = null,
                        isFromAdmin = newMessage.isFromAdmin,
                        threadId = newMessage.threadId,
                        createdAt = newMessage.createdAt
                    )
                )
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed saving chat message to DB: ${e.message}", e)
            }
        }

        // Push directly to Firestore so the other party's device receives the message immediately
        firestoreSync.pushInquiryToCloud(newMessage, repositoryScope)

        if (isFromAdmin) {
            triggerFcmPushNotification(
                "Admin Response Received",
                "Alnoor Admin: ${text.take(60)}",
                targetTab = "MESSAGES",
                isBroadcast = false
            )
        } else {
            triggerFcmPushNotification(
                "New Helpline Message",
                "${senderName}: ${text.take(60)}",
                targetTab = "MESSAGES",
                isBroadcast = false
            )
        }
    }

    fun submitUserMessage(senderName: String, senderContact: String, category: MessageCategory, subject: String, messageText: String) {
        val contactKey = senderContact.trim().lowercase().filter { it.isLetterOrDigit() }
        val threadId = if (contactKey.isNotBlank() && contactKey != "notprovided") "contact_$contactKey" else "user_${UUID.randomUUID().toString().take(8)}"
        sendChatMessage(
            threadId = threadId,
            senderName = senderName,
            senderContact = senderContact,
            text = messageText,
            isFromAdmin = false,
            category = category,
            subject = subject
        )
    }

    fun markMessageRead(messageId: String, isRead: Boolean) {
        repositoryScope.launch {
            db.inquiriesDao().updateReadStatus(messageId, isRead)
        }
    }

    fun markThreadAsRead(threadId: String, contact: String) {
        repositoryScope.launch {
            db.inquiriesDao().updateThreadReadStatus(threadId, contact, true)
        }
    }

    fun saveMessageInternalNotes(messageId: String, notes: String) {
        repositoryScope.launch {
            db.inquiriesDao().updateInternalNotes(messageId, notes)
        }
    }

    fun replyAndResolveMessage(messageId: String, replyText: String) {
        // Also post as a direct chat message into that inquiry's thread so the 1-to-1 chat is unified!
        val current = _messages.value.find { it.id == messageId }
        if (current != null) {
            val threadId = if (current.threadId.isNotBlank()) current.threadId else "contact_${current.senderContact.trim().lowercase().filter { it.isLetterOrDigit() }}"
            sendChatMessage(
                threadId = threadId,
                senderName = "Alnoor Admin",
                senderContact = "helpline@alnoor.org",
                text = replyText,
                isFromAdmin = true,
                category = current.category,
                subject = "Re: ${current.subject}"
            )
        }

        repositoryScope.launch {
            db.inquiriesDao().updateReply(messageId, MessageStatus.RESOLVED.name, replyText)
        }
        // Push Admin reply to Firestore so user's client receives it instantly
        firestoreSync.updateInquiryReplyInCloud(messageId, MessageStatus.RESOLVED, replyText, repositoryScope)
    }

    fun deleteMessage(messageId: String) {
        repositoryScope.launch {
            db.inquiriesDao().deleteInquiry(messageId)
        }
        firestoreSync.deleteInquiryFromCloud(messageId, repositoryScope)
    }

    fun deleteThread(threadId: String, contact: String, messagesInThread: List<AdminMessage>) {
        repositoryScope.launch {
            db.inquiriesDao().deleteThread(threadId, contact)
            messagesInThread.forEach { msg ->
                firestoreSync.deleteInquiryFromCloud(msg.id, repositoryScope)
            }
        }
    }

    // --- Monthly Darood Counter ---
    suspend fun incrementDaroodCount(byAmount: Long = 1) {
        _daroodState.update { state ->
            state.copy(
                dailyCount = state.dailyCount + byAmount,
                personalLifetimeCount = state.personalLifetimeCount + byAmount,
                monthlyCommunityTotal = state.monthlyCommunityTotal + byAmount
            )
        }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        db.daroodDao().insertRecord(DaroodRecordEntity(date = today, count = byAmount))
    }

    fun resetDailyCount() {
        _daroodState.update { it.copy(dailyCount = 0) }
    }

    fun resetMonthlyCycleAdmin() {
        _daroodState.update { it.copy(monthlyCommunityTotal = 0) }
        triggerFcmPushNotification("Darood Cycle Reset", "New Monthly Darood Sharif cycle started!")
    }

    fun setTargetGoal(goal: Int) {
        _daroodState.update { it.copy(targetGoal = goal) }
    }

    fun toggleVibration() {
        _daroodState.update { it.copy(isVibrationEnabled = !it.isVibrationEnabled) }
    }

    fun toggleSound() {
        _daroodState.update { it.copy(isSoundEnabled = !it.isSoundEnabled) }
    }

    // Aliases for seamless UI integration
    fun toggleLiveStreamStatus(streamId: String) = toggleLiveStatus(streamId)
    fun toggleEventRsvp(eventId: String) = toggleRsvp(eventId)
    fun toggleBookmark(bookId: String) = toggleBookmarkBook(bookId)
    fun playAudio(item: MediaArchiveItem) = playMedia(item)
    fun togglePlayPause() = togglePlayPauseAudio()
    fun addMediaArchive(item: MediaArchiveItem) = addMediaItem(item)
    fun deleteMediaArchive(itemId: String) = deleteMediaItem(itemId)
    fun toggleNoticePin(noticeId: String) = togglePinNotice(noticeId)
    fun submitMessage(msg: AdminMessage) {
        submitUserMessage(
            senderName = msg.senderName,
            senderContact = msg.senderContact,
            category = msg.category,
            subject = msg.subject,
            messageText = msg.message
        )
    }
    fun incrementDarood(byAmount: Long = 1) {
        repositoryScope.launch {
            incrementDaroodCount(byAmount)
        }
    }
    fun resetDailyDarood() = resetDailyCount()
    fun resetMonthlyDaroodAdmin() = resetMonthlyCycleAdmin()
    fun setDaroodTargetGoal(goal: Int) = setTargetGoal(goal)
    fun toggleDaroodVibration() = toggleVibration()
    fun toggleDaroodSound() = toggleSound()

    // --- Mock Starter Seed Data ---
    private fun getInitialLiveStreams(): List<LiveStreamItem> = listOf(
        LiveStreamItem(
            id = "live-official-broadcast",
            title = "Official Live Broadcast: Weekly Spiritual Mehfil & Khatam Sharif",
            speaker = "Hazrat Allama Peer Syed & Renowned Scholars",
            isLiveNow = true,
            scheduledTime = "Currently Broadcasting Live • 24/7 Spiritual Channel",
            youtubeVideoId = "official_live_stream",
            channelUrl = "https://www.youtube.com/@AlnoorislamiMushahidat/streams",
            channelHandle = "@AlnoorislamiMushahidat",
            description = "Welcome to the official live stream channel of Alnoor Islami Center. Featuring live Khatam Sharif, Quran recitation, Hamd-o-Naat, spiritual discourses, and collective supplications for the Muslim Ummah.",
            viewersCount = 1845,
            topicCategory = "Official Live Channel"
        )
    )

    private fun getInitialPlaylists(): List<YouTubePlaylist> = listOf(
        YouTubePlaylist(
            id = "pl-1",
            title = "Annual International Khatam-e-Nabuwwat Conferences",
            playlistUrl = "https://www.youtube.com/playlist?list=PL_Alnoor_Conferences",
            playlistId = "PL_Alnoor_Conferences",
            videoCount = 3,
            description = "Complete video recordings of international scholar addresses, Khatam-e-Nabuwwat discourses, and emotional spiritual duas.",
            dateAdded = "Aug 2026",
            channelTitle = "Alnoor Islami Live",
            channelHandle = "@AlnoorislamiMushahidat",
            videos = listOf(
                MediaArchiveItem(
                    id = "pl-item-1",
                    title = "Conference Inauguration & Quran Recitation",
                    type = "Khatam Sharif",
                    speaker = "Qari Abdul Basit Style Choir",
                    reciter = "Qari Abdul Basit Style Choir",
                    duration = "22:15",
                    audioUrl = "https://www.youtube.com/@AlnoorislamiMushahidat",
                    date = "Aug 2026",
                    description = "Soulful recitation of Surah Al-Ahzab and Surah Al-Fath"
                ),
                MediaArchiveItem(
                    id = "pl-item-2",
                    title = "The Sanctity of Prophethood - Keynote Address",
                    type = "Juma Khutbah",
                    speaker = "Hazrat Allama Peer Syed",
                    reciter = "Hazrat Allama Peer Syed",
                    duration = "54:30",
                    audioUrl = "https://www.youtube.com/@AlnoorislamiMushahidat",
                    date = "Aug 2026",
                    description = "Keynote spiritual address on the finality of Prophethood"
                ),
                MediaArchiveItem(
                    id = "pl-item-3",
                    title = "Grand Manqabat & Collective Closing Dua",
                    type = "Hamd-o-Naat",
                    speaker = "Alnoor Naat Council",
                    reciter = "Alnoor Naat Council",
                    duration = "31:40",
                    audioUrl = "https://www.youtube.com/@AlnoorislamiMushahidat",
                    date = "Aug 2026",
                    description = "Heartfelt prayers and supplications for the Ummah"
                )
            ),
            items = listOf(
                MediaArchiveItem(
                    id = "pl-item-1",
                    title = "Conference Inauguration & Quran Recitation",
                    type = "Khatam Sharif",
                    speaker = "Qari Abdul Basit Style Choir",
                    reciter = "Qari Abdul Basit Style Choir",
                    duration = "22:15",
                    audioUrl = "https://www.youtube.com/@AlnoorislamiMushahidat",
                    date = "Aug 2026",
                    description = "Soulful recitation of Surah Al-Ahzab and Surah Al-Fath"
                )
            )
        ),
        YouTubePlaylist(
            id = "pl-2",
            title = "Weekly Spiritual Khatam Sharif & Darood Gatherings",
            playlistUrl = "https://www.youtube.com/playlist?list=PL_Alnoor_WeeklyKhatam",
            playlistId = "PL_Alnoor_WeeklyKhatam",
            videoCount = 2,
            description = "Weekly Thursday night spiritual gatherings with Salawat recitation and Shajarah Mubarak reading.",
            dateAdded = "Jul 2026",
            channelTitle = "Alnoor Islami Live",
            channelHandle = "@AlnoorislamiMushahidat",
            videos = listOf(
                MediaArchiveItem(
                    id = "pl-item-4",
                    title = "Weekly Khatam Sharif Mubarak - Complete Session",
                    type = "Khatam Sharif",
                    speaker = "Hazrat Allama & Mureedeen",
                    reciter = "Hazrat Allama & Mureedeen",
                    duration = "42:10",
                    audioUrl = "https://www.youtube.com/@AlnoorislamiMushahidat",
                    date = "Jul 2026",
                    description = "Thursday night gathering recitation"
                )
            )
        )
    )

    private fun getInitialGalleryAssets(): List<GalleryAsset> = listOf(
        GalleryAsset(
            id = "ga-1",
            title = "Grand Mosque Central Dome & Calligraphy",
            category = "Mosque Architecture",
            fileType = "IMAGE",
            fileExtension = "jpg",
            fileSize = "3.8 MB",
            fileUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e",
            thumbnailUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e",
            uploadedDate = "Aug 2026",
            date = "Aug 2026",
            description = "High-resolution photograph of the illuminated grand dome and marble calligraphy."
        ),
        GalleryAsset(
            id = "ga-2",
            title = "Alnoor Center Annual Community Report & Financial Audit",
            category = "Annual Reports",
            fileType = "PDF",
            fileExtension = "pdf",
            fileSize = "6.4 MB",
            fileUrl = "https://example.com/docs/annual_report_2026.pdf",
            thumbnailUrl = "",
            uploadedDate = "Jul 2026",
            date = "Jul 2026",
            description = "Certified official PDF document detailing mosque welfare programs, educational stats, and expansion progress."
        ),
        GalleryAsset(
            id = "ga-3",
            title = "Official Khatam Sharif Method & Duas Brochure",
            category = "Spiritual Guides",
            fileType = "PDF",
            fileExtension = "pdf",
            fileSize = "2.1 MB",
            fileUrl = "https://example.com/docs/khatam_sharif_guide.pdf",
            thumbnailUrl = "",
            uploadedDate = "Aug 2026",
            date = "Aug 2026",
            description = "Print-ready PDF booklet containing Arabic text, Urdu translation, and English transliteration of Khatam Sharif."
        ),
        GalleryAsset(
            id = "ga-4",
            title = "Community Volunteer & Muhtamim Guidelines",
            category = "Administrative Documents",
            fileType = "DOCUMENT",
            fileExtension = "docx",
            fileSize = "1.2 MB",
            fileUrl = "https://example.com/docs/volunteer_guidelines.docx",
            thumbnailUrl = "",
            uploadedDate = "Jun 2026",
            date = "Jun 2026",
            description = "Administrative guidelines, code of conduct, and volunteer shift schedules for major Islamic events."
        ),
        GalleryAsset(
            id = "ga-5",
            title = "Spiritual Mehfil-e-Darood Gathering Ceremony",
            category = "Event Photos",
            fileType = "IMAGE",
            fileExtension = "jpg",
            fileSize = "4.5 MB",
            fileUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa",
            thumbnailUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa",
            uploadedDate = "Aug 2026",
            date = "Aug 2026",
            description = "Community gathering reciting Darood Sharif together in the main prayer hall."
        )
    )

    private fun getInitialImportantNoticePopup(): ImportantNoticePopup = ImportantNoticePopup(
        id = "important_popup",
        title = "Important Notice: Juma Mubarak & Courtyard Guidelines",
        message = "Assalamu Alaikum Respected Community Members. Please note that the main courtyard extension is in progress. Kindly use the North Gate for entrance and park in the designated overflow parking areas.",
        issuingDepartment = "Alnoor Central Management",
        imageUrl = "https://images.unsplash.com/photo-1564769625905-50e93615e769",
        showOnLogin = true,
        isActive = true,
        description = "Assalamu Alaikum Respected Community Members. Please note that the main courtyard extension is in progress.",
        datePublished = "August 14, 2026"
    )

    private fun getInitialDaroodSubmissions(): List<DaroodSubmission> = listOf(
        DaroodSubmission("sub-1", "Haji Muhammad Aslam (Lahore)", 12500, "Aug 14, 2026 - 02:15 PM"),
        DaroodSubmission("sub-2", "Brother Tariq Mahmood (+92-300-1234567)", 5000, "Aug 14, 2026 - 11:30 AM"),
        DaroodSubmission("sub-3", "Sister Fatima (+44-7700-900123)", 7860, "Aug 13, 2026 - 09:40 PM"),
        DaroodSubmission("sub-4", "Syed Usman Ali", 25000, "Aug 13, 2026 - 04:20 PM"),
        DaroodSubmission("sub-5", "Qari Abdul Waheed (Karachi)", 10000, "Aug 12, 2026 - 08:10 PM"),
        DaroodSubmission("sub-6", "Alnoor Youth Group", 50000, "Aug 12, 2026 - 03:00 PM")
    )

    private fun getInitialEvents(): List<CommunityEvent> = listOf(
        CommunityEvent(
            id = "evt-1",
            title = "Grand Jumma Congregation & Dars-e-Tasawwuf",
            dateGregorian = "Friday, Sep 18, 2026",
            dateHijri = "06 Rabi' al-Awwal 1448 AH",
            time = "01:00 PM - 02:30 PM",
            venue = "Alnoor Central Mosque & Courtyard",
            address = "Main Mosque Complex, Gate 1",
            description = "Spiritual Friday congregation featuring an enlightening discourse on Tazkiyah (inner purification) and contemplation in preparation for the blessed month of Rabi' al-Awwal.\n\n• Keynote discourse by Hazrat Allama Mufti Muhammad Saeed Sahib\n• Special collective supplications for the Ummah and world peace\n• Dedicated spacious prayer arrangement for sisters and families in Hall 2\n• Tabarruk distribution following the completion of prayer",
            category = "Juma Congregation",
            speaker = "Hazrat Allama Mufti Muhammad Saeed Sahib",
            rsvpCount = 450,
            isUserRsvp = false,
            isReminderSet = false
        ),
        CommunityEvent(
            id = "evt-2",
            title = "Weekly Spiritual Mehfil-e-Darood & Zikr Gatherings",
            dateGregorian = "Thursday, Sep 24, 2026",
            dateHijri = "12 Rabi' al-Awwal 1448 AH",
            time = "08:00 PM - 10:30 PM",
            venue = "Alnoor Central Masjid Hall",
            address = "Main Mosque Complex",
            description = "A blessed weekly gathering dedicated to sending collective Durood-o-Salam upon Sayyiduna Rasoolullah ﷺ on the eve of 12th Rabi' al-Awwal.\n\n• Soulful Naat recitations by renowned international Naat Khawans\n• Inspiring lecture on Seerat-un-Nabi ﷺ and Islamic moral ethics\n• Heartfelt collective Khatam Sharif and spiritual supplications\n• Traditional community dinner and Tabarruk served to all guests",
            category = "Mehfil-e-Darood",
            speaker = "Allama Qari Muhammad Usman & Guests",
            rsvpCount = 310,
            isUserRsvp = false,
            isReminderSet = false
        ),
        CommunityEvent(
            id = "evt-3",
            title = "Annual Grand Khatam-e-Nabuwwat International Conference",
            dateGregorian = "Friday, Oct 02, 2026",
            dateHijri = "20 Rabi' al-Awwal 1448 AH",
            time = "06:30 PM - 11:30 PM",
            venue = "Alnoor Grand Convention Center & Auditorium",
            address = "Auditorium Hall A, Gate 3",
            description = "The premier annual conference gathering prominent scholars, Muftis, and researchers from across the globe to deliver authoritative discourses on the Finality of Prophethood ﷺ.\n\n• Keynote addresses by venerable international Islamic scholars\n• Multilingual simultaneous interpretation facilities provided\n• Presentation of research papers and historical manuscripts exhibition\n• Formal collective dinner and awards ceremony for Quran memorizers",
            category = "International Conference",
            speaker = "Hazrat Allama Peer Syed & Renowned Scholars",
            rsvpCount = 680,
            isUserRsvp = false,
            isReminderSet = false
        ),
        CommunityEvent(
            id = "evt-4",
            title = "Youth Spiritual Development & Quran Recitation Workshop",
            dateGregorian = "Sunday, Oct 18, 2026",
            dateHijri = "07 Rabi' al-Thani 1448 AH",
            time = "10:00 AM - 04:00 PM",
            venue = "Alnoor Youth Educational Wing",
            address = "Education Complex, Floor 2",
            description = "An interactive, engaging day-long workshop designed specifically for youth aged 12 to 25 to build leadership skills and strengthen Islamic knowledge.\n\n• Tajweed and beautiful recitation masterclass with certified Qaris\n• Interactive Q&A on navigating modern ethical and lifestyle challenges\n• Friendly quiz competition with awards and commemorative shields\n• Refreshments, lunch, and networking session with mentors",
            category = "Youth Program",
            speaker = "Youth Mentors & Scholars",
            rsvpCount = 180,
            isUserRsvp = false,
            isReminderSet = false
        )
    )

    private fun getInitialAlbums(): List<PhotoAlbum> = listOf(
        PhotoAlbum(
            id = "alb-1",
            title = "Annual Khatam-e-Nabuwwat Conferences",
            description = "Photographs from past international conferences, scholar receptions, and community gatherings.",
            photoCount = 24,
            coverUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e",
            date = "Aug 2026"
        ),
        PhotoAlbum(
            id = "alb-2",
            title = "Mosque Architecture & Courtyard",
            description = "Views of the grand minaret, mihrab calligraphy, illuminated dome, and marble courtyard.",
            photoCount = 18,
            coverUrl = "https://images.unsplash.com/photo-1584551246679-0daf3d275d0f",
            date = "Jul 2026"
        )
    )

    private fun getInitialPhotos(): List<GalleryPhoto> = listOf(
        GalleryPhoto(
            id = "ph-1",
            albumId = "alb-1",
            title = "Main Conference Hall & Stage",
            caption = "Gathering of over 2,000 attendees in the main prayer hall.",
            date = "Aug 2026",
            imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e"
        ),
        GalleryPhoto(
            id = "ph-2",
            albumId = "alb-1",
            title = "Distinguished Scholars & Speakers",
            caption = "Scholars delivering keynote address on peace and spiritual unity.",
            date = "Aug 2026",
            imageUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa"
        )
    )

    private fun getInitialBooks(): List<IslamicBook> = listOf(
        IslamicBook(
            id = "bk-1",
            title = "Khatam Sharif: Complete Spiritual Guide & Supplications",
            author = "Alnoor Islami Research Bureau",
            category = "Khatam Guide & Duas",
            pagesCount = 64,
            language = "Arabic / Urdu / English",
            description = "The authentic text of Khatam Sharif, Surah recitations, Silsila Shajarah, and specific prayers for blessing the departed and family wellbeing.",
            contentPreview = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n\nقُلْ هُوَ اللَّهُ أَحَدٌ • اللَّهُ الصَّمَدُ • لَمْ يَلِدْ وَلَمْ يُولَدْ • وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ\n\nBenefits of Khatam Sharif: Reciting Surah Al-Ikhlas three times yields the reward of reciting the complete Quran. Follow with sincere intention and supplication.",
            fileType = "PDF",
            fileUrl = "https://example.com/books/khatam_sharif_complete.pdf",
            fileSize = "4.8 MB",
            hasAudioRecitation = true,
            audioUrl = "https://example.com/audio/khatam_sharif.mp3",
            isBookmarked = true
        ),
        IslamicBook(
            id = "bk-2",
            title = "Fadhail-e-Darood Sharif (Virtues of Salawat)",
            author = "Sheikh al-Hadith Muhammad Zakariya",
            category = "Darood & Salawat",
            pagesCount = 180,
            language = "Urdu / English",
            description = "A treasury of Hadiths, spiritual anecdotes, and immense rewards for reciting Darood Sharif upon the Prophet Muhammad ﷺ.",
            contentPreview = "قال رسول الله ﷺ: «مَنْ صَلَّى عَلَيَّ صَلَاةً وَاحِدَةً صَلَّى اللهُ عَلَيْهِ عَشْرَ صَلَوَاتٍ»\n\nThe Prophet ﷺ said: 'Whoever sends blessings upon me once, Allah sends ten blessings upon him, erases ten sins, and raises him ten degrees.'",
            fileType = "PDF",
            fileUrl = "https://example.com/books/fadhail_darood.pdf",
            fileSize = "7.2 MB",
            hasAudioRecitation = true,
            audioUrl = "https://example.com/audio/darood_taj.mp3",
            isBookmarked = false
        ),
        IslamicBook(
            id = "bk-3",
            title = "Hisn al-Muslim (Fortress of the Muslim)",
            author = "Saeed bin Ali bin Wahf al-Qahtani",
            category = "Daily Duas & Adhkar",
            pagesCount = 145,
            language = "Arabic with Transliteration",
            description = "Comprehensive collection of authentic daily supplications for morning, evening, travel, entering the mosque, and times of distress.",
            contentPreview = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَٰهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ\n\nMorning Adhkar brings spiritual shield, tranquility of the heart, and divine protection throughout the day.",
            fileType = "PDF",
            fileUrl = "https://example.com/books/hisn_al_muslim.pdf",
            fileSize = "3.9 MB",
            hasAudioRecitation = false,
            isBookmarked = true
        ),
        IslamicBook(
            id = "bk-4",
            title = "Visual Guide to Prayer Postures and Rules",
            author = "Alnoor Educational Board",
            category = "Fiqh & Visual Guides",
            pagesCount = 32,
            language = "English & Arabic",
            description = "High-resolution illustrated photo guide depicting correct Salah postures, Sunnah methods, and common mistakes.",
            contentPreview = "Step-by-step illustrated guide with color photos for Qiyam, Ruku, Sujood, and Tashahhud.",
            fileType = "IMAGE",
            fileUrl = "https://example.com/books/prayer_guide_images.zip",
            fileSize = "12.5 MB",
            hasAudioRecitation = false,
            isBookmarked = false
        )
    )

    private fun getInitialPrayerTimes(): PrayerTimesData {
        val today = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
        return PrayerTimesData(
            dateGregorian = today,
            dateHijri = "15 Safar 1448 AH",
            locationName = "Alnoor Central Masjid & Community",
            countryName = "United States",
            fajr = "05:05 AM",
            fajrIqamah = "05:30 AM",
            sunrise = "06:22 AM",
            dhuhr = "01:15 PM",
            dhuhrIqamah = "01:45 PM",
            asr = "05:10 PM",
            asrIqamah = "05:30 PM",
            maghrib = "07:55 PM",
            maghribIqamah = "08:00 PM",
            isha = "09:20 PM",
            ishaIqamah = "09:45 PM",
            tahajjud = "03:45 AM",
            qiblaDirectionDeg = 67.5f,
            calculationMethod = "Islamic Society of North America (ISNA)",
            isAutoDetected = true
        )
    }

    private fun getInitialMediaArchives(): List<MediaArchiveItem> = listOf(
        MediaArchiveItem(
            id = "med-1",
            title = "Khatam Sharif Recitation & Silsila Shajarah Complete",
            type = "Khatam Sharif",
            speaker = "Hazrat Allama & Alnoor Choir",
            reciter = "Hazrat Allama & Alnoor Choir",
            duration = "28:45",
            audioUrl = "https://www.youtube.com/@AlnoorislamiMushahidat",
            audioStreamUrl = "https://example.com/audio/khatam_full.mp3",
            date = "Aug 2026",
            description = "Complete audio recording of weekly spiritual Khatam Sharif with soulful Hamd and closing collective Dua.",
            isFavorite = true
        ),
        MediaArchiveItem(
            id = "med-2",
            title = "Darood-e-Taj & Darood-e-Tanjeena Recitation",
            type = "Darood Sharif",
            speaker = "Qari Muhammad Usman",
            reciter = "Qari Muhammad Usman",
            duration = "14:20",
            audioUrl = "https://www.youtube.com/@AlnoorislamiMushahidat",
            audioStreamUrl = "https://example.com/audio/darood_taj.mp3",
            date = "Jul 2026",
            description = "Melodious recitation of Darood-e-Taj and Darood-e-Tanjeena for inner peace and spiritual blessings.",
            isFavorite = true
        )
    )

    private fun getInitialNotices(): List<NoticeItem> = listOf(
        NoticeItem(
            id = "not-1",
            title = "Juma Congregation Parking & Traffic Advisory",
            content = "Due to the ongoing construction of the southern courtyard, kindly park in the West Overflow Lot or utilize carpooling. Volunteers will be on site to assist.",
            priority = NoticePriority.URGENT,
            date = "Aug 14, 2026",
            isPinned = true,
            department = "Administration & Security"
        ),
        NoticeItem(
            id = "not-2",
            title = "Alnoor Monthly Darood Target.",
            content = "Alhamdulillah! The community has collectively completed over 1,400,000 Darood Sharif recitations this month. May Allah accept everyone's sincere devotion.",
            priority = NoticePriority.IMPORTANT,
            date = "Aug 12, 2026",
            isPinned = false,
            department = "Spiritual & Darood Committee"
        )
    )

    private fun getInitialMessages(): List<AdminMessage> = listOf(
        AdminMessage(
            id = "msg-101",
            senderName = "Brother Tariq Mahmood",
            senderContact = "tariq.m@example.com / +1-555-0192",
            category = MessageCategory.MASLA_FATWA,
            subject = "Question regarding Zakat calculation on shared business assets",
            message = "Assalamu Alaikum Mufti Sahib, I have a partnership business where inventory value fluctuates. How should Zakat be calculated at the close of the financial year? JazakAllah.",
            timestamp = "Aug 13, 2026 - 11:20 AM",
            status = MessageStatus.RESOLVED,
            isRead = true,
            adminReply = "Wa Alaikum Assalam. Zakat is due on the current net market value of trade inventory plus liquid cash minus short-term debts. Full detailed ruling sent to your email. - Admin"
        )
    )

    private fun getInitialRegisteredUsers(): List<RegisteredUser> = listOf(
        RegisteredUser(
            userId = "usr_admin_1",
            fullName = "Hazrat Muhtamim Sahib",
            email = "admin@alnoor.org",
            whatsappNumber = "+1 (555) 786-0199",
            gender = UserGender.MALE,
            password = "AdminPass@786",
            role = UserRole.ADMIN,
            registeredAt = "Jul 10, 2026",
            status = "Active"
        ),
        RegisteredUser(
            userId = "usr_101",
            fullName = "Brother Tariq Mahmood",
            email = "tariq.m@example.com",
            whatsappNumber = "+1 (555) 019-2847",
            gender = UserGender.MALE,
            password = "TariqUser#2026",
            role = UserRole.STANDARD_USER,
            registeredAt = "Aug 01, 2026",
            status = "Active"
        ),
        RegisteredUser(
            userId = "usr_102",
            fullName = "Sister Fatima Zahra",
            email = "fatima.z@example.com",
            whatsappNumber = "+1 (555) 443-8921",
            gender = UserGender.FEMALE,
            password = "FatimaZahra!99",
            role = UserRole.STANDARD_USER,
            registeredAt = "Aug 04, 2026",
            status = "Active"
        ),
        RegisteredUser(
            userId = "usr_103",
            fullName = "Brother Bilal Ahmed",
            email = "bilal.ahmed@example.com",
            whatsappNumber = "+1 (555) 321-7654",
            gender = UserGender.MALE,
            password = "BilalSecure*786",
            role = UserRole.STANDARD_USER,
            registeredAt = "Aug 08, 2026",
            status = "Active"
        ),
        RegisteredUser(
            userId = "usr_104",
            fullName = "Sister Maryam Khan",
            email = "maryam.khan@example.com",
            whatsappNumber = "+1 (555) 887-1234",
            gender = UserGender.FEMALE,
            password = "MaryamK!2026",
            role = UserRole.STANDARD_USER,
            registeredAt = "Aug 11, 2026",
            status = "Active"
        ),
        RegisteredUser(
            userId = "usr_105",
            fullName = "Brother Usman Siddiqui",
            email = "usman.s@example.com",
            whatsappNumber = "+1 (555) 901-4321",
            gender = UserGender.MALE,
            password = "UsmanPass123",
            role = UserRole.STANDARD_USER,
            registeredAt = "Aug 13, 2026",
            status = "Active"
        ),
        RegisteredUser(
            userId = "usr_106",
            fullName = "Sister Ayesha Siddiqua",
            email = "ayesha.s@example.com",
            whatsappNumber = "+1 (555) 678-9012",
            gender = UserGender.FEMALE,
            password = "AyeshaNoor786",
            role = UserRole.STANDARD_USER,
            registeredAt = "Aug 14, 2026",
            status = "Active"
        )
    )

    // --- User Management & Registration ---
    fun registerNewUser(
        fullName: String,
        email: String,
        whatsappNumber: String,
        gender: UserGender,
        password: String,
        role: UserRole = UserRole.STANDARD_USER
    ): Result<RegisteredUser> {
        if (fullName.isBlank()) return Result.failure(IllegalArgumentException("Full Name is required"))
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Email Address is required"))
        if (whatsappNumber.isBlank()) return Result.failure(IllegalArgumentException("WhatsApp Number is required"))
        if (password.length < 4) return Result.failure(IllegalArgumentException("Password must be at least 4 characters"))

        val existing = _registeredUsers.value.find { it.email.equals(email.trim(), ignoreCase = true) }
        if (existing != null) {
            return Result.failure(IllegalStateException("An account with this email address already exists"))
        }

        val todayFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        val newUserId = "usr_${System.currentTimeMillis().toString().takeLast(6)}"
        val secureHashedPassword = SecurityCryptoManager.hashPassword(password)
        val newUser = RegisteredUser(
            userId = newUserId,
            fullName = fullName.trim(),
            email = email.trim(),
            whatsappNumber = whatsappNumber.trim(),
            gender = gender,
            password = secureHashedPassword,
            role = role,
            registeredAt = todayFormatted,
            status = "Active"
        )

        // Save to Room DB asynchronously on repositoryScope
        repositoryScope.launch {
            try {
                db.usersDao().insertUser(
                    RegisteredUserEntity(
                        userId = newUser.userId,
                        fullName = newUser.fullName,
                        email = newUser.email,
                        whatsappNumber = newUser.whatsappNumber,
                        gender = newUser.gender.name,
                        password = newUser.password,
                        role = newUser.role.name,
                        registeredAt = newUser.registeredAt,
                        status = newUser.status
                    )
                )
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed saving user to local DB: ${e.message}", e)
            }
        }

        _registeredUsers.update { listOf(newUser) + it }
        // Push user to Firestore Cloud for real-time syncing across devices and Admin console
        firestoreSync.pushUserToCloud(newUser, repositoryScope)
        triggerFcmPushNotification(
            "New Community Registration",
            "${newUser.fullName} (${newUser.gender.label}) registered with WhatsApp: ${newUser.whatsappNumber}",
            isBroadcast = false
        )
        return Result.success(newUser)
    }

    fun adminResetUserPassword(userId: String, newPassword: String): Boolean {
        if (newPassword.isBlank() || newPassword.length < 4) return false

        val user = _registeredUsers.value.find { it.userId == userId } ?: return false
        val secureHashedPassword = SecurityCryptoManager.hashPassword(newPassword)

        repositoryScope.launch {
            try {
                db.usersDao().updatePassword(userId, secureHashedPassword)
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed updating password in DB: ${e.message}", e)
            }
        }

        _registeredUsers.update { list ->
            list.map { if (it.userId == userId) it.copy(password = secureHashedPassword) else it }
        }

        firestoreSync.updateUserPasswordInCloud(userId, secureHashedPassword, repositoryScope)

        triggerFcmPushNotification(
            "Password Reset by Admin",
            "Password successfully updated for ${user.fullName} (${user.email})",
            isBroadcast = false
        )
        return true
    }

    fun findUserForPasswordReset(email: String, whatsappNumber: String): RegisteredUser? {
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = whatsappNumber.filter { it.isDigit() }
        if (cleanEmail.isBlank() || cleanPhone.isBlank()) return null

        return _registeredUsers.value.find { user ->
            val userEmail = user.email.trim().lowercase()
            val userPhone = user.whatsappNumber.filter { it.isDigit() }
            val emailMatch = userEmail == cleanEmail
            val phoneMatch = if (cleanPhone.isNotEmpty() && userPhone.isNotEmpty()) {
                cleanPhone == userPhone ||
                (cleanPhone.length >= 7 && userPhone.length >= 7 &&
                 cleanPhone.takeLast(minOf(cleanPhone.length, userPhone.length, 9)) ==
                 userPhone.takeLast(minOf(cleanPhone.length, userPhone.length, 9)))
            } else false

            emailMatch && phoneMatch
        }
    }

    fun resetUserPasswordSelfService(userId: String, newPassword: String): Result<RegisteredUser> {
        val trimmedPass = newPassword.trim()
        if (trimmedPass.isBlank() || trimmedPass.length < 4) {
            return Result.failure(IllegalArgumentException("Password must be at least 4 characters long."))
        }

        val user = _registeredUsers.value.find { it.userId == userId }
            ?: return Result.failure(IllegalArgumentException("User account record not found."))

        val secureHashedPassword = SecurityCryptoManager.hashPassword(trimmedPass)

        repositoryScope.launch {
            try {
                db.usersDao().updatePassword(userId, secureHashedPassword)
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed updating password in local DB: ${e.message}", e)
            }
        }

        val updatedUser = user.copy(password = secureHashedPassword)
        _registeredUsers.update { list ->
            list.map { if (it.userId == userId) updatedUser else it }
        }

        firestoreSync.updateUserPasswordInCloud(userId, secureHashedPassword, repositoryScope)

        triggerFcmPushNotification(
            "Security: Password Changed",
            "Password has been successfully updated for ${user.fullName}.",
            isBroadcast = false
        )

        return Result.success(updatedUser)
    }

    fun adminDeleteUser(userId: String): Boolean {
        repositoryScope.launch {
            try {
                db.usersDao().deleteUser(userId)
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed deleting user from DB: ${e.message}", e)
            }
        }
        _registeredUsers.update { list -> list.filterNot { it.userId == userId } }
        firestoreSync.deleteUserFromCloud(userId, repositoryScope)
        return true
    }

    fun adminUpdateUserRole(userId: String, newRole: UserRole) {
        repositoryScope.launch {
            try {
                db.usersDao().updateRole(userId, newRole.name)
            } catch (e: Exception) {
                Log.e("AlnoorRepository", "Failed updating user role in DB: ${e.message}", e)
            }
        }
        _registeredUsers.update { list ->
            list.map { if (it.userId == userId) it.copy(role = newRole) else it }
        }
        firestoreSync.updateUserRoleInCloud(userId, newRole, repositoryScope)
    }

    // --- Admin Security PIN Management ---
    fun getAdminPasscode(): String {
        return FirebaseAuthManager.getInstance(context).getAdminPasscode()
    }

    fun updateAdminPasscode(currentPin: String, newPin: String): Result<Unit> {
        val result = FirebaseAuthManager.getInstance(context).updateAdminPasscode(currentPin, newPin)
        if (result.isSuccess) {
            triggerFcmPushNotification(
                "Admin Security PIN Updated",
                "Administrator access PIN / Muhtamim passcode has been securely updated.",
                isBroadcast = false
            )
        }
        return result
    }

    // --- On-Demand Manual Cloud Refresh Methods ---
    fun refreshUsersFromCloud() {
        repositoryScope.launch {
            try {
                firestoreSync.syncUsersSection(db)
            } catch (e: Exception) {
                Log.w("AlnoorRepository", "Manual user sync failed: ${e.message}")
            }
        }
    }

    fun refreshActionCardsFromCloud() {
        repositoryScope.launch {
            try {
                val remoteCards = firestoreSync.fetchActionCardConfigsFromCloud()
                if (remoteCards != null && remoteCards.isNotEmpty()) {
                    _actionCardConfigs.value = remoteCards
                    saveActionCardConfigsToPrefs(remoteCards)
                }
            } catch (e: Exception) {
                Log.w("AlnoorRepository", "Manual action card sync failed: ${e.message}")
            }
        }
    }

    fun refreshEventsFromCloud() {
        repositoryScope.launch {
            try {
                firestoreSync.syncEventsSection(db)
            } catch (e: Exception) {
                Log.w("AlnoorRepository", "Manual events sync failed: ${e.message}")
            }
        }
    }

    fun refreshLibraryFromCloud() {
        repositoryScope.launch {
            try {
                firestoreSync.syncBooksSection(db)
            } catch (e: Exception) {
                Log.w("AlnoorRepository", "Manual books sync failed: ${e.message}")
            }
        }
    }

    // =========================================================================
    // --- ACTION CARDS CONFIGURATION & MEMBER VISIBILITY MANAGEMENT ---
    // =========================================================================

    private fun getInitialActionCardConfigs(): List<ActionCardConfig> = listOf(
        ActionCardConfig(
            cardKey = "ALNOOR_CHANNEL",
            defaultTitle = "Alnoor Islami Channel",
            customTitle = "",
            defaultSubtitle = "Official YouTube channel, recorded Bayanat, and live Islamic broadcasts",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 0
        ),
        ActionCardConfig(
            cardKey = "DAROOD",
            defaultTitle = "Darood Pak Collective Recitation",
            customTitle = "",
            defaultSubtitle = "Contribute daily recitations towards the group milestone",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 2
        ),
        ActionCardConfig(
            cardKey = "HADITH",
            defaultTitle = "Daily Hadith Shareef (حدیث شریف)",
            customTitle = "",
            defaultSubtitle = "Authentic Bukhari & Muslim Hadith in Arabic, Urdu & English with card generator",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 3
        ),
        ActionCardConfig(
            cardKey = "EVENTS",
            defaultTitle = "Islamic Events & Majalis",
            customTitle = "",
            defaultSubtitle = "Upcoming gatherings, Shab-e-Barat, Halaqas & Dars schedules",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 4
        ),
        ActionCardConfig(
            cardKey = "NOTICES",
            defaultTitle = "Official Notices & Bulletins",
            customTitle = "",
            defaultSubtitle = "Important announcements, circulars, and Ramadan timetables",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 5
        ),
        ActionCardConfig(
            cardKey = "QURAN",
            defaultTitle = "Quran-e-Pak (Kanz-ul-Iman)",
            customTitle = "",
            defaultSubtitle = "Tilawat-e-Quran, Kanz-ul-Iman Urdu translation, 30 Paras, 114 Surahs, and daily recitation bookmark",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 4
        ),
        ActionCardConfig(
            cardKey = "PRAYER",
            defaultTitle = "Prayer Timings & Jamat Schedule",
            customTitle = "",
            defaultSubtitle = "Fajr, Dhuhr, Asr, Maghrib, Isha and Friday Jumu'ah timetable",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 5
        ),
        ActionCardConfig(
            cardKey = "LIBRARY",
            defaultTitle = "Islamic Library & Publications",
            customTitle = "",
            defaultSubtitle = "Authentic Islamic books, PDF reader, and saved bookmarks",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 5
        ),
        ActionCardConfig(
            cardKey = "MEDIA",
            defaultTitle = "Mahafil Archive & Bayanaat Audio",
            customTitle = "",
            defaultSubtitle = "Listen to recorded lectures, Naats, and playlist collections",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 6
        ),
        ActionCardConfig(
            cardKey = "GALLERY",
            defaultTitle = "Photo & Community Gallery",
            customTitle = "",
            defaultSubtitle = "Historic gatherings, conferences, and mosque community photos",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 7
        ),
        ActionCardConfig(
            cardKey = "MESSAGES",
            defaultTitle = "Alnoor Islami Admin & Helpline",
            customTitle = "",
            defaultSubtitle = "Submit religious questions, inquiries, and private feedback",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 8
        ),
        ActionCardConfig(
            cardKey = "SEARCH",
            defaultTitle = "Universal Search",
            customTitle = "",
            defaultSubtitle = "Quickly search across books, announcements, events, and audio",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 9
        ),
        ActionCardConfig(
            cardKey = "SETTINGS",
            defaultTitle = "App Settings",
            customTitle = "",
            defaultSubtitle = "Theme colors (Emerald, Navy Blue, White, OLED Black) & preferences",
            customSubtitle = "",
            isVisibleToMembers = true,
            orderIndex = 10
        )
    )

    private fun loadActionCardConfigsFromPrefs(): List<ActionCardConfig> {
        val initialList = getInitialActionCardConfigs()
        return initialList.map { defaultItem ->
            val customTitle = actionCardsPrefs.getString("${defaultItem.cardKey}_title", "") ?: ""
            val customSubtitle = actionCardsPrefs.getString("${defaultItem.cardKey}_subtitle", "") ?: ""
            val isVisible = actionCardsPrefs.getBoolean("${defaultItem.cardKey}_visible", true)
            val orderIndex = actionCardsPrefs.getInt("${defaultItem.cardKey}_order", defaultItem.orderIndex)
            defaultItem.copy(
                customTitle = customTitle,
                customSubtitle = customSubtitle,
                isVisibleToMembers = isVisible,
                orderIndex = orderIndex
            )
        }
    }

    private fun saveActionCardConfigsToPrefs(configs: List<ActionCardConfig>) {
        val editor = actionCardsPrefs.edit()
        for (config in configs) {
            editor.putString("${config.cardKey}_title", config.customTitle)
            editor.putString("${config.cardKey}_subtitle", config.customSubtitle)
            editor.putBoolean("${config.cardKey}_visible", config.isVisibleToMembers)
            editor.putInt("${config.cardKey}_order", config.orderIndex)
        }
        editor.apply()
    }

    /**
     * Update title, subtitle, and member visibility for a single Action Card.
     */
    fun updateActionCardConfig(
        cardKey: String,
        customTitle: String,
        customSubtitle: String,
        isVisibleToMembers: Boolean
    ) {
        _actionCardConfigs.update { list ->
            val updated = list.map { card ->
                if (card.cardKey == cardKey) {
                    card.copy(
                        customTitle = customTitle.trim(),
                        customSubtitle = customSubtitle.trim(),
                        isVisibleToMembers = isVisibleToMembers
                    )
                } else card
            }
            saveActionCardConfigsToPrefs(updated)
            firestoreSync.pushActionCardConfigsToCloud(updated, repositoryScope)
            updated
        }
        triggerFcmPushNotification(
            "Dashboard Layout Updated",
            "Action card settings for '$cardKey' updated by Administrator.",
            targetTab = "HOME"
        )
    }

    /**
     * Update entire list of Action Card configurations at once.
     */
    fun updateAllActionCardConfigs(configs: List<ActionCardConfig>) {
        _actionCardConfigs.value = configs
        saveActionCardConfigsToPrefs(configs)
        firestoreSync.pushActionCardConfigsToCloud(configs, repositoryScope)
        triggerFcmPushNotification(
            "Action Cards Updated",
            "Home dashboard action cards updated and synced to all devices.",
            targetTab = "HOME"
        )
    }

    /**
     * Reset an individual Action Card back to its factory default name & visibility.
     */
    fun resetActionCardToDefault(cardKey: String) {
        val initialList = getInitialActionCardConfigs()
        val defaultItem = initialList.find { it.cardKey == cardKey } ?: return
        _actionCardConfigs.update { list ->
            val updated = list.map { card ->
                if (card.cardKey == cardKey) defaultItem else card
            }
            saveActionCardConfigsToPrefs(updated)
            firestoreSync.pushActionCardConfigsToCloud(updated, repositoryScope)
            updated
        }
    }

    /**
     * Reset all Action Cards back to default names and make all visible to members.
     */
    fun resetAllActionCardsToDefault() {
        val defaultList = getInitialActionCardConfigs()
        _actionCardConfigs.value = defaultList
        saveActionCardConfigsToPrefs(defaultList)
        firestoreSync.pushActionCardConfigsToCloud(defaultList, repositoryScope)
        triggerFcmPushNotification(
            "Dashboard Reset",
            "All action cards restored to factory defaults."
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: AlnoorRepository? = null

        fun getInstance(context: Context): AlnoorRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AlnoorRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
