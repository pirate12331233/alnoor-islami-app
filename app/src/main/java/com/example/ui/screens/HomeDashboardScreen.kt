package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.data.repository.QuranRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.example.ui.components.DailyHadithCard
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ActionCardConfig
import com.example.data.model.CommunityEvent
import com.example.data.model.DaroodState
import com.example.data.model.LiveStreamItem
import com.example.data.model.NoticeItem
import com.example.data.model.PrayerTimesData
import com.example.data.model.UserRole
import com.example.ui.components.AppTab
import com.example.ui.components.openOfficialYouTubeChannel
import com.example.ui.theme.DarkCard
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold100
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.YoutubeRed

@Composable
fun HomeDashboardScreen(
    userName: String,
    currentRole: UserRole,
    liveStreams: List<LiveStreamItem>,
    events: List<CommunityEvent>,
    daroodState: DaroodState,
    notices: List<NoticeItem>,
    prayerTimes: PrayerTimesData,
    booksCount: Int,
    mediaCount: Int,
    galleryCount: Int,
    inquiriesCount: Int,
    actionCardConfigs: List<ActionCardConfig> = emptyList(),
    onNavigateToTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasActiveLiveStream = liveStreams.any { it.isLiveNow }
    val activeLiveStream = liveStreams.firstOrNull { it.isLiveNow } ?: liveStreams.firstOrNull()

    // Helper functions for dynamic card configuration
    fun getCard(key: String): ActionCardConfig? = actionCardConfigs.find { it.cardKey == key }

    fun isCardVisible(key: String): Boolean {
        val config = getCard(key) ?: return true
        return if (currentRole == UserRole.ADMIN) true else config.isVisibleToMembers
    }

    fun isCardHiddenFromMembers(key: String): Boolean {
        val config = getCard(key) ?: return false
        return !config.isVisibleToMembers && currentRole == UserRole.ADMIN
    }

    // Pulse animation for Live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_pulse"
    )

    // Quran progress for Quran-e-Pak action card
    val quranRepo = remember { QuranRepository.getInstance(context) }
    val quranProgress by quranRepo.readingProgress.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_dashboard_scroll"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. Top Bismillah Greeting & Welcome Banner ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Emerald900
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold500.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Emerald900,
                                    Emerald800,
                                    Color(0xFF042B20)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Arabic Bismillah Calligraphy
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            color = Gold300,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Line 1: AL NOOR ISLAMI
                        Text(
                            text = "AL NOOR ISLAMI",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Line 2: AL NOOR INTERNATIONAL TRUST
                        Text(
                            text = "AL NOOR INTERNATIONAL TRUST",
                            color = Gold300,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Line 3: Asalam o Alikum
                        Text(
                            text = "Asalam o Alikum",
                            color = Gold400,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        // Line 4: User name appears below Asalam o Alikum
                        val displayName = if (userName.isNotBlank()) userName else "Community Member"
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayName,
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        // Role Badge
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentRole == UserRole.ADMIN) Gold500.copy(alpha = 0.25f) else Emerald700.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (currentRole == UserRole.ADMIN) Gold400 else Emerald500
                            )
                        ) {
                            Text(
                                text = if (currentRole == UserRole.ADMIN) "⭐ Administrator (Muhtamim)" else "✨ Community Member",
                                color = if (currentRole == UserRole.ADMIN) Gold300 else Emerald100,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 1.5. [ACTION CARD] DAILY HADITH SHAREEF (EXPANDABLE ACTION CARD) ---
        if (isCardVisible("HADITH")) {
            val hadithCard = getCard("HADITH")
            val hadithTitle = hadithCard?.displayTitle ?: "Daily Hadith Shareef (حدیث شریف)"
            val hadithSubtitle = hadithCard?.displaySubtitle
                ?: "Tap to read authentic Bukhari & Muslim Hadith with Arabic, Urdu & English translations"

            item {
                DailyHadithCard(
                    title = hadithTitle,
                    subtitle = hadithSubtitle,
                    isHiddenFromMembers = isCardHiddenFromMembers("HADITH"),
                    onNavigateToLibrary = { onNavigateToTab(AppTab.LIBRARY) }
                )
            }
        }

        // Section Title & Admin Quick Customize Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Gold500)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APPLICATION ACTIONS",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                if (currentRole == UserRole.ADMIN) {
                    TextButton(
                        onClick = { onNavigateToTab(AppTab.ACTION_CARDS_MANAGER) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("btn_quick_manage_cards")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Gold400,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Customize Cards",
                            color = Gold400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- [VERTICAL ACTION CARD] YOUTUBE CHANNEL ---
        if (isCardVisible("YOUTUBE_CHANNEL")) {
            val ytCard = getCard("YOUTUBE_CHANNEL")
            item {
                VerticalActionCard(
                    icon = Icons.Default.PlayArrow,
                    iconTint = Color.White,
                    iconBackground = YoutubeRed,
                    title = ytCard?.displayTitle ?: "Youtube Channel",
                    subtitle = ytCard?.displaySubtitle
                        ?: "Official Alnoor Islami YouTube channel with recorded Bayanat, lectures, and programs",
                    isHiddenFromMembers = isCardHiddenFromMembers("YOUTUBE_CHANNEL"),
                    onClick = {
                        openOfficialYouTubeChannel(context)
                    },
                    testTag = "action_card_youtube_channel"
                )
            }
        }

        // --- [VERTICAL ACTION CARD] LIVE YOUTUBE BROADCAST & STREAM ---
        if (isCardVisible("LIVE_STREAM")) {
            val liveCardConfig = getCard("LIVE_STREAM")
            item {
                VerticalActionCard(
                    icon = Icons.Default.LiveTv,
                    iconTint = if (hasActiveLiveStream) LiveRed else Gold400,
                    iconBackground = if (hasActiveLiveStream) Color(0xFF2C0A0A) else Emerald900,
                    title = liveCardConfig?.displayTitle ?: "Live YouTube Broadcast & Stream",
                    subtitle = liveCardConfig?.displaySubtitle
                        ?: "Shows when a live Jummah Khutbah, Dars-e-Quran, or Majlis is streaming, with direct in-app or YouTube playback.",
                    badgeText = if (hasActiveLiveStream) "LIVE NOW" else null,
                    isHiddenFromMembers = isCardHiddenFromMembers("LIVE_STREAM"),
                    onClick = {
                        openOfficialYouTubeChannel(
                            context = context,
                            targetUrl = if (hasActiveLiveStream && !activeLiveStream?.youtubeVideoId.isNullOrBlank()) {
                                "https://www.youtube.com/watch?v=${activeLiveStream!!.youtubeVideoId}"
                            } else {
                                "https://www.youtube.com/@AlnoorislamiMushahidat/live"
                            }
                        )
                    },
                    testTag = "action_card_live_stream"
                )
            }
        }

        // --- 3. [VERTICAL ACTION CARD 1] DAROOD PAK COLLECTIVE COUNTER ---
        if (isCardVisible("DAROOD")) {
            val daroodCard = getCard("DAROOD")
            item {
                VerticalActionCard(
                    icon = Icons.Default.Fingerprint,
                    iconTint = Gold400,
                    iconBackground = Emerald900,
                    title = daroodCard?.displayTitle ?: "Darood Pak Collective Recitation",
                    subtitle = daroodCard?.displaySubtitle ?: "Contribute daily recitations towards the group milestone",
                    isHiddenFromMembers = isCardHiddenFromMembers("DAROOD"),
                    onClick = { onNavigateToTab(AppTab.DAROOD) },
                    testTag = "action_card_darood"
                )
            }
        }

        // --- 4. [VERTICAL ACTION CARD 2] ISLAMIC EVENTS & PROGRAMS ---
        if (isCardVisible("EVENTS")) {
            val eventsCard = getCard("EVENTS")
            item {
                VerticalActionCard(
                    icon = Icons.Default.Event,
                    iconTint = Color(0xFF60A5FA),
                    iconBackground = Color(0xFF1E293B),
                    title = eventsCard?.displayTitle ?: "Islamic Events & Majalis",
                    subtitle = eventsCard?.displaySubtitle ?: "Upcoming gatherings, Shab-e-Barat, Halaqas & Dars schedules",
                    isHiddenFromMembers = isCardHiddenFromMembers("EVENTS"),
                    onClick = { onNavigateToTab(AppTab.EVENTS) },
                    testTag = "action_card_events"
                )
            }
        }

        // --- 5. [VERTICAL ACTION CARD 3] IMPORTANT NOTICES & CIRCULARS ---
        if (isCardVisible("NOTICES")) {
            val noticesCard = getCard("NOTICES")
            item {
                VerticalActionCard(
                    icon = Icons.Default.Campaign,
                    iconTint = Color(0xFFFBBF24),
                    iconBackground = Color(0xFF332A15),
                    title = noticesCard?.displayTitle ?: "Official Notices & Bulletins",
                    subtitle = noticesCard?.displaySubtitle ?: "Important announcements, circulars, and Ramadan timetables",
                    isHiddenFromMembers = isCardHiddenFromMembers("NOTICES"),
                    onClick = { onNavigateToTab(AppTab.NOTICES) },
                    testTag = "action_card_notices"
                )
            }
        }

        // --- 6. [VERTICAL ACTION CARD 4] QURAN-E-PAK (KANZ-UL-IMAN) ---
        if (isCardVisible("QURAN")) {
            val quranCard = getCard("QURAN")
            item {
                VerticalActionCard(
                    icon = Icons.Default.MenuBook,
                    iconTint = Gold400,
                    iconBackground = Emerald900,
                    title = quranCard?.displayTitle ?: "Quran-e-Pak (Kanz-ul-Iman)",
                    subtitle = if (quranCard?.customSubtitle?.isNotBlank() == true) {
                        quranCard.customSubtitle
                    } else {
                        "Resume: Para ${quranProgress.lastReadPara} • ${quranProgress.lastReadSurahNameArabic} (Ayah ${quranProgress.lastReadAyahNumber}) • Tap to recite"
                    },
                    isHiddenFromMembers = isCardHiddenFromMembers("QURAN"),
                    onClick = { onNavigateToTab(AppTab.QURAN) },
                    testTag = "action_card_quran"
                )
            }
        }

        // --- 7. [VERTICAL ACTION CARD 5] PRAYER TIMETABLE & JAMAT SCHEDULE ---
        if (isCardVisible("PRAYER")) {
            val prayerCard = getCard("PRAYER")
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTab(AppTab.PRAYER_TIMES) }
                        .testTag("action_card_prayer")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Emerald800)
                                    .border(1.dp, Gold500.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = "Prayer Timetable",
                                    tint = Gold400,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prayerCard?.displayTitle ?: "Prayer Timings & Jamat Schedule",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = prayerCard?.displaySubtitle ?: "Fajr, Dhuhr, Asr, Maghrib, Isha, Jumu'ah & Live Qibla Compass",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }

                            if (isCardHiddenFromMembers("PRAYER")) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "Hidden",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick-Access Sub-Buttons: Timetable & Live Qibla Compass
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToTab(AppTab.PRAYER_TIMES) }
                                    .testTag("action_card_prayer_sub_timetable")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mosque,
                                        contentDescription = "Timetable",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Timetable",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToTab(AppTab.PRAYER_TIMES) }
                                    .testTag("action_card_prayer_sub_qibla")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = "Live Qibla",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Live Qibla",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 7. [VERTICAL ACTION CARD 5] ISLAMIC LIBRARY & BOOKS ---
        if (isCardVisible("LIBRARY")) {
            val libraryCard = getCard("LIBRARY")
            item {
                VerticalActionCard(
                    icon = Icons.Default.LibraryBooks,
                    iconTint = Gold300,
                    iconBackground = Color(0xFF282414),
                    title = libraryCard?.displayTitle ?: "Islamic Library & Publications",
                    subtitle = libraryCard?.displaySubtitle ?: "Authentic Islamic books, PDF reader, and saved bookmarks",
                    isHiddenFromMembers = isCardHiddenFromMembers("LIBRARY"),
                    onClick = { onNavigateToTab(AppTab.LIBRARY) },
                    testTag = "action_card_library"
                )
            }
        }

        // --- 8. [VERTICAL ACTION CARD 6] MAHFIL & AUDIO ARCHIVE ---
        if (isCardVisible("MEDIA")) {
            val mediaCard = getCard("MEDIA")
            item {
                VerticalActionCard(
                    icon = Icons.Default.Radio,
                    iconTint = Color(0xFFA78BFA),
                    iconBackground = Color(0xFF251B3B),
                    title = mediaCard?.displayTitle ?: "Mahafil Archive & Bayanaat Audio",
                    subtitle = mediaCard?.displaySubtitle ?: "Listen to recorded lectures, Naats, and playlist collections",
                    isHiddenFromMembers = isCardHiddenFromMembers("MEDIA"),
                    onClick = { onNavigateToTab(AppTab.MEDIA) },
                    testTag = "action_card_media"
                )
            }
        }

        // --- 9. [VERTICAL ACTION CARD 7] PHOTO & VIDEO GALLERY ---
        if (isCardVisible("GALLERY")) {
            val galleryCard = getCard("GALLERY")
            item {
                VerticalActionCard(
                    icon = Icons.Outlined.PhotoLibrary,
                    iconTint = Color(0xFF34D399),
                    iconBackground = Color(0xFF13322B),
                    title = galleryCard?.displayTitle ?: "Photo & Community Gallery",
                    subtitle = galleryCard?.displaySubtitle ?: "Historic gatherings, conferences, and mosque community photos",
                    isHiddenFromMembers = isCardHiddenFromMembers("GALLERY"),
                    onClick = { onNavigateToTab(AppTab.GALLERY) },
                    testTag = "action_card_gallery"
                )
            }
        }

        // --- 10. [VERTICAL ACTION CARD 8] DIRECT ADMIN HELPLINE & MESSAGES ---
        if (isCardVisible("MESSAGES")) {
            val messagesCard = getCard("MESSAGES")
            val hasUnread = inquiriesCount > 0
            item {
                VerticalActionCard(
                    icon = Icons.Default.Mail,
                    iconTint = if (hasUnread) Color(0xFFF87171) else SuccessGreen,
                    iconBackground = if (hasUnread) Color(0xFF3B1818) else Emerald800.copy(alpha = 0.35f),
                    title = messagesCard?.displayTitle ?: "Alnoor Islami Admin & Helpline",
                    subtitle = messagesCard?.displaySubtitle ?: "Direct 1-to-1 WhatsApp-style chat with Alnoor Admin",
                    badgeText = if (hasUnread) "$inquiriesCount NEW" else "ALL READ",
                    isHiddenFromMembers = isCardHiddenFromMembers("MESSAGES"),
                    onClick = { onNavigateToTab(AppTab.MESSAGES) },
                    testTag = "action_card_messages"
                )
            }
        }

        // --- 11. [VERTICAL ACTION CARD 9] UNIVERSAL SEARCH ---
        if (isCardVisible("SEARCH")) {
            val searchCard = getCard("SEARCH")
            item {
                VerticalActionCard(
                    icon = Icons.Default.Search,
                    iconTint = Gold300,
                    iconBackground = Emerald800,
                    title = searchCard?.displayTitle ?: "Universal Search",
                    subtitle = searchCard?.displaySubtitle ?: "Quickly search across books, announcements, events, and audio",
                    isHiddenFromMembers = isCardHiddenFromMembers("SEARCH"),
                    onClick = { onNavigateToTab(AppTab.SEARCH) },
                    testTag = "action_card_search"
                )
            }
        }

        // --- 12. [VERTICAL ACTION CARD 10] APP SETTINGS & THEMES ---
        if (isCardVisible("SETTINGS")) {
            val settingsCard = getCard("SETTINGS")
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTab(AppTab.SETTINGS) }
                        .testTag("action_card_settings")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "App Settings",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = settingsCard?.displayTitle ?: "App Settings",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = settingsCard?.displaySubtitle ?: "Theme colors (Navy, White, Black, Emerald) & preferences",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }

                            if (isCardHiddenFromMembers("SETTINGS")) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "Hidden",
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick-Access Sub-Buttons (User requested: sub button for "themes")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToTab(AppTab.SETTINGS) }
                                    .testTag("action_card_settings_sub_themes")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Themes",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Themes",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToTab(AppTab.SETTINGS) }
                                    .testTag("action_card_settings_sub_more")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "All Settings",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "All Settings",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 12. ADMINISTRATOR TOOLS SECTION ---
        if (currentRole == UserRole.ADMIN) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Gold500)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADMINISTRATOR CONSOLE",
                        color = Gold400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // ACTION CARDS MANAGER CARD IN ADMIN CONSOLE
            item {
                VerticalActionCard(
                    icon = Icons.Default.Tune,
                    iconTint = Color.Black,
                    iconBackground = Gold400,
                    title = "Action Cards Manager & Member Visibility",
                    subtitle = "Rename card titles, update descriptions, and show/hide actions for members",
                    onClick = { onNavigateToTab(AppTab.ACTION_CARDS_MANAGER) },
                    testTag = "action_card_manager_admin"
                )
            }

            item {
                VerticalActionCard(
                    icon = Icons.Default.ManageAccounts,
                    iconTint = Emerald900,
                    iconBackground = Gold400,
                    title = "User Management & Verification",
                    subtitle = "Approve members, change roles, and reset user passwords",
                    onClick = { onNavigateToTab(AppTab.USERS) },
                    testTag = "action_card_users"
                )
            }

            item {
                VerticalActionCard(
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Gold300,
                    iconBackground = Emerald900,
                    title = "AI Islamic Scholar Assistant",
                    subtitle = "AI-powered research assistant for references and rulings",
                    onClick = { onNavigateToTab(AppTab.AI_SCHOLAR) },
                    testTag = "action_card_ai_scholar"
                )
            }
        }
    }
}

/**
 * Modern Spacious Vertical Action Card
 * - Shows full title up to two lines
 * - Shows full subtitle text comfortably
 * - Permanent removal of dynamic count badges / indicator chips for clean, unobstructed readability
 * - Increased box size and luxurious padding
 */
@Composable
fun VerticalActionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    progressFraction: Float? = null,
    isHiddenFromMembers: Boolean = false,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isHiddenFromMembers) Color(0xFFF59E0B).copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box with ample size
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconBackground,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title (up to 2 full lines) & Subtitle (full text)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 21.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (!badgeText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = LiveRed
                        ) {
                            Text(
                                text = badgeText,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.5.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Trailing Navigation Arrow
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open $title",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
