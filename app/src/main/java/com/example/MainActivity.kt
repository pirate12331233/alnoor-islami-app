package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.auth.FirebaseAuthManager
import com.example.data.model.UserRole
import com.example.data.repository.AlnoorRepository
import com.example.ui.components.AlnoorTopBar
import com.example.ui.components.AppTab
import com.example.ui.components.AppUpdateMandatoryScreen
import com.example.ui.components.FcmNotificationBanner
import com.example.ui.components.FirebaseAuthDialog
import com.example.ui.components.ImportantNoticeFullScreenDialog
import com.example.ui.components.MiniAudioPlayerBar
import com.example.ui.components.StartupVideoFullScreenPlayer
import com.example.ui.components.openOfficialYouTubeChannel
import com.example.ui.screens.ActionCardsManagerScreen
import com.example.ui.screens.AiScholarScreen
import com.example.ui.screens.AppSettingsScreen
import com.example.ui.screens.DaroodCounterScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.FullScreenLoginScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LiveStreamScreen
import com.example.ui.screens.MediaArchiveScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.NoticesScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.QuranReaderScreen
import com.example.ui.screens.UniversalSearchScreen
import com.example.ui.screens.UserManagementScreen
import com.example.ui.theme.Emerald900
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.UrgentRed

class MainActivity : FragmentActivity() {
    private val requestedTabFlow = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        enableEdgeToEdge()
        hideSystemControls()
        handleIntentForNotification(intent)
        // Ensure background sync scheduler is active
        com.example.util.AlnoorBackgroundSyncReceiver.schedule(this)
        setContent {
            val context = LocalContext.current
            val repository = remember { AlnoorRepository.getInstance(context) }
            val themeMode by repository.currentThemeMode.collectAsState()

            MyApplicationTheme(themeMode = themeMode) {
                val requestedTab by requestedTabFlow.collectAsState()
                AlnoorAppMainScreen(
                    initialTargetTab = requestedTab,
                    onTargetTabConsumed = { requestedTabFlow.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentForNotification(intent)
    }

    private fun handleIntentForNotification(intent: Intent?) {
        val targetTab = intent?.getStringExtra("target_tab")
        if (!targetTab.isNullOrBlank()) {
            requestedTabFlow.value = targetTab
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemControls()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemControls()
        }
    }

    override fun onStop() {
        super.onStop()
        // Ensure background sync scheduler is active when app is minimized / closed
        com.example.util.AlnoorBackgroundSyncReceiver.schedule(this)
    }

    private fun hideSystemControls() {
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        } catch (e: Exception) {
            // fallback
        }
    }
}

@Composable
fun AlnoorAppMainScreen(
    initialTargetTab: String? = null,
    onTargetTabConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { AlnoorRepository.getInstance(context) }
    val authManager = remember { FirebaseAuthManager.getInstance(context) }
    val authUserState by authManager.authState.collectAsState()

    // State collections
    val currentRole by repository.currentUserRole.collectAsState()
    val latestNotification by repository.latestNotification.collectAsState()
    val liveStreams by repository.liveStreams.collectAsState()
    val events by repository.events.collectAsState()
    val photoAlbums by repository.photoAlbums.collectAsState()
    val galleryAssets by repository.galleryAssets.collectAsState()
    val books by repository.books.collectAsState()
    val prayerTimes by repository.prayerTimes.collectAsState()
    val mediaArchives by repository.mediaArchives.collectAsState()
    val playlists by repository.playlists.collectAsState()
    val notices by repository.notices.collectAsState()
    val importantNoticePopup by repository.importantNoticePopup.collectAsState()
    val messages by repository.messages.collectAsState()
    val unreadMessagesCount = remember(messages, currentRole) {
        if (currentRole == UserRole.ADMIN) {
            messages.count { !it.isFromAdmin && !it.isRead }
        } else {
            messages.count { it.isFromAdmin && !it.isRead }
        }
    }
    val daroodState by repository.daroodState.collectAsState()
    val daroodBannerUrl by repository.daroodBannerImageUrl.collectAsState()
    val currentlyPlayingMedia by repository.currentlyPlayingMedia.collectAsState()
    val isPlayingAudio by repository.isPlayingAudio.collectAsState()
    val registeredUsers by repository.registeredUsers.collectAsState()
    val actionCardConfigs by repository.actionCardConfigs.collectAsState()
    val currentThemeMode by repository.currentThemeMode.collectAsState()
    val isInitialSyncComplete by repository.isInitialSyncComplete.collectAsState()
    val initialSyncMessage by repository.initialSyncMessage.collectAsState()
    val appVersionInfo by repository.appVersionInfo.collectAsState()

    val currentVersionCode = com.example.BuildConfig.VERSION_CODE
    val isAppOutdated = remember(appVersionInfo, currentVersionCode) {
        val minRequired = appVersionInfo.minSupportedVersionCode
        val latestCode = appVersionInfo.latestVersionCode
        val isForced = appVersionInfo.isForcedUpdate
        (currentVersionCode < minRequired) || (isForced && currentVersionCode < latestCode)
    }

    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    var showLoginNoticePopup by remember { mutableStateOf(false) }
    var showStartupVideo by remember { mutableStateOf(true) }
    var adminBypassedUpdate by remember { mutableStateOf(false) }

    // --- REQUIREMENT: 10-Second Startup Animation Video Runs First Every Time App Opens ---
    if (showStartupVideo) {
        StartupVideoFullScreenPlayer(
            onFinished = { showStartupVideo = false }
        )
        return
    }

    // Respond to target tab from push notification tap
    LaunchedEffect(initialTargetTab) {
        if (!initialTargetTab.isNullOrBlank()) {
            val matchedTab = AppTab.values().find {
                it.name.equals(initialTargetTab, ignoreCase = true) ||
                it.title.equals(initialTargetTab, ignoreCase = true)
            }
            if (matchedTab != null) {
                selectedTab = matchedTab
            }
            onTargetTabConsumed()
        }
    }

    // --- REQUIREMENT: Mandatory Update Gate on App Startup ---
    if (isAppOutdated && !adminBypassedUpdate) {
        AppUpdateMandatoryScreen(
            versionInfo = appVersionInfo,
            onRetryCheck = {
                coroutineScope.launch {
                    com.example.data.remote.FirestoreSyncManager.getInstance().syncAppVersionSection()
                }
            },
            isAdminLoggedIn = currentRole == com.example.data.model.UserRole.ADMIN,
            onBypassAsAdmin = {
                adminBypassedUpdate = true
            },
            onUpdateCloudVersionInfo = { updatedInfo ->
                repository.updateAppVersionInfo(updatedInfo)
            }
        )
        return
    }

    val sharedPrefs = remember { context.getSharedPreferences("alnoor_app_prefs", android.content.Context.MODE_PRIVATE) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Automatically sync location and recalculate prayer times once permissions are resolved
        repository.syncDeviceLocationAndPrayerTimes(force = true)
    }

    // Auto-request notifications and sync location & prayer times when authenticated
    LaunchedEffect(authUserState.isAuthenticated) {
        if (authUserState.isAuthenticated) {
            // Request push notification permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasNotificationPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasNotificationPerm) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) {
                repository.syncDeviceLocationAndPrayerTimes(force = false)
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    // Intercept back press when on sub-screens to return smoothly to Home Dashboard
    BackHandler(enabled = selectedTab != AppTab.HOME) {
        selectedTab = AppTab.HOME
    }

    // --- REQUIREMENT: Immediately upon launch, display full-screen login screen until authenticated ---
    if (!authUserState.isAuthenticated) {
        FullScreenLoginScreen(
            authManager = authManager,
            repository = repository,
            onLoginSuccess = { role ->
                repository.setUserRole(role)

                // Automatically set the location of the device, update prayer timings, and save for future use
                repository.syncDeviceLocationAndPrayerTimes(force = true)
                // Request location permissions if not already granted
                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (!hasFine && !hasCoarse) {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }

                // Trigger silent initial full sync in background without blocking the UI
                coroutineScope.launch {
                    try {
                        repository.performInitialFullSync()
                    } catch (e: Exception) {
                        android.util.Log.w("MainActivity", "Silent initial sync error: ${e.message}")
                    }
                }

                // Instantly land on Home Dashboard with action cards immediately (0 wait time)
                selectedTab = AppTab.HOME
                
                // Show Important Notice on login if configured
                showLoginNoticePopup = true
            }
        )
        return
    }

    // --- Authenticated App UI ---
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Emerald900)
                    .statusBarsPadding()
            ) {
                AlnoorTopBar(
                    currentRole = currentRole,
                    selectedTab = selectedTab,
                    onBackToHome = { selectedTab = AppTab.HOME },
                    onRoleClick = { showAuthDialog = true },
                    onOpenLiveChannel = {
                        openOfficialYouTubeChannel(context)
                    },
                    onMessagesClick = {
                        selectedTab = AppTab.MESSAGES
                    },
                    unreadMessagesCount = unreadMessagesCount,
                    onSearchClick = {
                        selectedTab = AppTab.SEARCH
                    },
                    onAuthClick = {
                        showAuthDialog = true
                    },
                    onAdminUsersClick = {
                        selectedTab = AppTab.USERS
                    },
                    onSignOutClick = {
                        showSignOutConfirmDialog = true
                    },
                    isSyncing = !isInitialSyncComplete
                )
                FcmNotificationBanner(
                    message = latestNotification,
                    onDismiss = { repository.clearNotification() }
                )
            }
        },
        bottomBar = {
            if (currentlyPlayingMedia != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Emerald900)
                        .navigationBarsPadding()
                ) {
                    MiniAudioPlayerBar(
                        mediaItem = currentlyPlayingMedia,
                        isPlaying = isPlayingAudio,
                        onPlayPause = { repository.togglePlayPause() },
                        onClose = { repository.stopAudio() }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                AppTab.HOME -> {
                    HomeDashboardScreen(
                        userName = authUserState.displayName ?: authUserState.email?.substringBefore("@") ?: "",
                        currentRole = currentRole,
                        liveStreams = liveStreams,
                        events = events,
                        daroodState = daroodState,
                        notices = notices,
                        prayerTimes = prayerTimes,
                        booksCount = books.size,
                        mediaCount = mediaArchives.size,
                        galleryCount = galleryAssets.size,
                        inquiriesCount = unreadMessagesCount,
                        actionCardConfigs = actionCardConfigs,
                        onNavigateToTab = { tab -> selectedTab = tab }
                    )
                }

                AppTab.LIVE_STREAMS -> {
                    LiveStreamScreen(
                        streams = liveStreams,
                        currentRole = currentRole,
                        onToggleLive = { id -> repository.toggleLiveStreamStatus(id) },
                        onUpdateStream = { stream -> repository.updateLiveStream(stream) }
                    )
                }

                AppTab.EVENTS -> {
                    EventsScreen(
                        events = events,
                        currentRole = currentRole,
                        onToggleRsvp = { id -> repository.toggleEventRsvp(id) },
                        onToggleReminder = { id, isSet, mins -> repository.toggleEventReminder(id, isSet, mins) },
                        onAddEvent = { event -> repository.addEvent(event) },
                        onUpdateEvent = { event -> repository.updateEvent(event) },
                        onDeleteEvent = { id -> repository.deleteEvent(id) }
                    )
                }

                AppTab.DAROOD -> {
                    DaroodCounterScreen(
                        daroodState = daroodState,
                        currentRole = currentRole,
                        bannerImageUrl = daroodBannerUrl,
                        onUpdateBannerImage = { imageUrl, title ->
                            repository.updateDaroodBannerImage(imageUrl, title)
                        },
                        onForceSyncBanner = {
                            repository.syncDaroodBannerFromCloud(force = true)
                        },
                        currentUserName = authUserState.displayName ?: authUserState.email ?: "",
                        onSubmitCount = { name, count -> repository.submitDaroodCount(name, count) },
                        onGenerateCsv = { repository.generateDaroodCsv() },
                        onResetCounts = { repository.resetDaroodCounts() }
                    )
                }

                AppTab.PRAYER_TIMES -> {
                    PrayerTimesScreen(
                        prayerTimes = prayerTimes,
                        currentRole = currentRole,
                        onUpdatePrayerTimes = { updated -> repository.updatePrayerTimes(updated) }
                    )
                }

                AppTab.QURAN -> {
                    QuranReaderScreen(
                        onBackToHome = { selectedTab = AppTab.HOME }
                    )
                }

                AppTab.SEARCH -> {
                    UniversalSearchScreen(
                        books = books,
                        notices = notices,
                        onToggleBookmark = { id -> repository.toggleBookmark(id) },
                        onPlayAudio = { mediaItem -> repository.playAudio(mediaItem) },
                        onBookClick = { _ ->
                            selectedTab = AppTab.LIBRARY
                        },
                        onNoticeClick = { _ ->
                            selectedTab = AppTab.NOTICES
                        }
                    )
                }

                AppTab.LIBRARY -> {
                    LibraryScreen(
                        books = books,
                        currentRole = currentRole,
                        onToggleBookmark = { id -> repository.toggleBookmark(id) },
                        onAddBook = { book -> repository.addBook(book) },
                        onUpdateBook = { book -> repository.updateBook(book) },
                        onDeleteBook = { id -> repository.deleteBook(id) }
                    )
                }

                AppTab.MEDIA -> {
                    MediaArchiveScreen(
                        playlists = playlists,
                        mediaItems = mediaArchives,
                        currentlyPlaying = currentlyPlayingMedia,
                        isPlaying = isPlayingAudio,
                        currentRole = currentRole,
                        onPlayMedia = { mediaItem -> repository.playAudio(mediaItem) },
                        onAddYouTubePlaylist = { url, title -> repository.addYouTubePlaylist(url, title) },
                        onDeletePlaylist = { id -> repository.deletePlaylist(id) },
                        onDeleteVideoFromPlaylist = { pId, vId -> repository.deleteVideoFromPlaylist(pId, vId) }
                    )
                }

                AppTab.GALLERY -> {
                    GalleryScreen(
                        galleryAssets = galleryAssets,
                        currentRole = currentRole,
                        onAddAsset = { asset -> repository.addGalleryAsset(asset) },
                        onDeleteAsset = { id -> repository.deleteGalleryAsset(id) },
                        onRefresh = { repository.refreshGalleryAssets() }
                    )
                }

                AppTab.NOTICES -> {
                    NoticesScreen(
                        notices = notices,
                        importantPopup = importantNoticePopup,
                        appVersionInfo = appVersionInfo,
                        currentRole = currentRole,
                        onAddNotice = { notice -> repository.addNotice(notice) },
                        onUpdateNotice = { notice -> repository.updateNotice(notice) },
                        onDeleteNotice = { id -> repository.deleteNotice(id) },
                        onTogglePin = { id -> repository.toggleNoticePin(id) },
                        onUpdateImportantPopup = { popup -> repository.updateImportantNoticePopup(popup) },
                        onUpdateAppVersionInfo = { info -> repository.updateAppVersionInfo(info) },
                        onTriggerPreviewPopup = { showLoginNoticePopup = true }
                    )
                }

                AppTab.MESSAGES -> {
                    MessagesScreen(
                        inquiries = messages,
                        currentRole = currentRole,
                        onSubmitInquiry = { msg -> repository.submitMessage(msg) },
                        onResolveInquiry = { id, reply -> repository.replyAndResolveMessage(id, reply) },
                        onMarkAsRead = { id, isRead -> repository.markMessageRead(id, isRead) },
                        onSaveInternalNotes = { id, notes -> repository.saveMessageInternalNotes(id, notes) },
                        onDeleteInquiry = { id -> repository.deleteMessage(id) },
                        onSendChatMessage = { threadId, senderName, senderContact, text, isFromAdmin, category ->
                            repository.sendChatMessage(threadId, senderName, senderContact, text, isFromAdmin, category)
                        },
                        onDeleteThread = { threadId, contact, msgs ->
                            repository.deleteThread(threadId, contact, msgs)
                        },
                        onMarkThreadRead = { threadId, contact ->
                            repository.markThreadAsRead(threadId, contact)
                        },
                        onBroadcastFlashMessage = { title, msgText ->
                            repository.broadcastFlashMessage(title, msgText)
                        },
                        onNavigateToAuth = { showAuthDialog = true }
                    )
                }

                AppTab.AI_SCHOLAR -> {
                    AiScholarScreen()
                }

                AppTab.USERS -> {
                    UserManagementScreen(
                        registeredUsers = registeredUsers,
                        currentRole = currentRole,
                        onResetPassword = { userId, newPass -> repository.adminResetUserPassword(userId, newPass) },
                        onDeleteUser = { userId -> repository.adminDeleteUser(userId) },
                        onUpdateRole = { userId, newRole -> repository.adminUpdateUserRole(userId, newRole) },
                        onRegisterUser = { fullName, email, whatsapp, gender, pass, role ->
                            repository.registerNewUser(fullName, email, whatsapp, gender, pass, role)
                        },
                        onUpdateAdminPin = { currentPin, newPin ->
                            repository.updateAdminPasscode(currentPin, newPin)
                        },
                        onRefreshUsers = {
                            repository.refreshUsersFromCloud()
                        },
                        onRequestAdminLogin = {
                            showAuthDialog = true
                        }
                    )
                }

                AppTab.ACTION_CARDS_MANAGER -> {
                    ActionCardsManagerScreen(
                        currentRole = currentRole,
                        actionCardConfigs = actionCardConfigs,
                        onUpdateCard = { cardKey, customTitle, customSubtitle, isVisibleToMembers ->
                            repository.updateActionCardConfig(cardKey, customTitle, customSubtitle, isVisibleToMembers)
                        },
                        onUpdateAllCards = { updatedList ->
                            repository.updateAllActionCardConfigs(updatedList)
                        },
                        onResetCard = { cardKey ->
                            repository.resetActionCardToDefault(cardKey)
                        },
                        onResetAllCards = {
                            repository.resetAllActionCardsToDefault()
                        },
                        onBackToHome = {
                            selectedTab = AppTab.HOME
                        }
                    )
                }

                AppTab.SETTINGS -> {
                    AppSettingsScreen(
                        currentThemeMode = currentThemeMode,
                        onThemeChanged = { newTheme ->
                            repository.setAppThemeMode(newTheme)
                        },
                        onNavigateToQuran = { selectedTab = AppTab.QURAN },
                        onNavigateToPrayer = { selectedTab = AppTab.PRAYER_TIMES },
                        onNavigateToHelpline = { selectedTab = AppTab.MESSAGES },
                        onNavigateToLibrary = { selectedTab = AppTab.LIBRARY }
                    )
                }
            }
        }
    }

    // --- Full-Screen Important Notice Alert Popup on Login / App Launch ---
    if (showLoginNoticePopup && (importantNoticePopup.showOnLogin || currentRole == UserRole.ADMIN)) {
        ImportantNoticeFullScreenDialog(
            popup = importantNoticePopup,
            currentRole = currentRole,
            onDismiss = { showLoginNoticePopup = false },
            onUpdateNotice = { updated -> repository.updateImportantNoticePopup(updated) }
        )
    }

    // --- Firebase Authentication / Switch Role Dialog ---
    if (showAuthDialog) {
        FirebaseAuthDialog(
            authManager = authManager,
            authState = authUserState,
            repository = repository,
            onDismiss = { showAuthDialog = false },
            onRoleChanged = { role ->
                repository.setUserRole(role)
                // Landing screen behavior: upon user login, land on Home Dashboard with vertical action cards
                selectedTab = AppTab.HOME
                // Trigger Important Notice popup on login
                showLoginNoticePopup = true
            }
        )
    }

    // --- Sign Out Confirmation & Complete Process Termination Dialog ---
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = UrgentRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Sign Out & Exit App?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Signing out will completely terminate and close the Alnoor Islami application process so it is not running in the background."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        repository.stopAudio()
                        val activity = context as? Activity
                        authManager.signOutAndExitApplication(activity)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                    modifier = Modifier.testTag("confirm_sign_out_exit_button")
                ) {
                    Text("Sign Out & Exit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


