package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.MediaArchiveItem
import com.example.data.model.UserRole
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.LiveRed
import com.example.ui.theme.UrgentRed
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import com.example.ui.theme.YoutubeRed

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.FilePickerUtils

enum class AppTab(val title: String, val icon: ImageVector, val adminOnly: Boolean = false) {
    HOME("Home Dashboard", Icons.Default.Home),
    LIVE_STREAMS("Live", Icons.Default.LiveTv),
    EVENTS("Events", Icons.Default.Event),
    DAROOD("Darood Sharif Recitation", Icons.Default.Fingerprint),
    PRAYER_TIMES("Prayer", Icons.Default.Mosque),
    SEARCH("Search", Icons.Default.Search),
    LIBRARY("Library", Icons.Default.LibraryBooks),
    MEDIA("Mahafil Archive", Icons.Default.Radio),
    GALLERY("Gallery", Icons.Outlined.PhotoLibrary),
    NOTICES("Notices", Icons.Default.Campaign),
    MESSAGES("Messages", Icons.Default.Mail),
    ACTION_CARDS_MANAGER("Action Cards Manager", Icons.Default.Tune, adminOnly = true),
    AI_SCHOLAR("AI Scholar", Icons.Default.AutoAwesome, adminOnly = true),
    USERS("User Management", Icons.Default.ManageAccounts, adminOnly = true)
}

@Composable
fun AlnoorTopBar(
    currentRole: UserRole,
    onRoleClick: () -> Unit,
    onOpenLiveChannel: () -> Unit,
    selectedTab: AppTab = AppTab.HOME,
    onBackToHome: (() -> Unit)? = null,
    onSearchClick: () -> Unit = {},
    onAuthClick: () -> Unit = {},
    onAdminUsersClick: (() -> Unit)? = null,
    onSignOutClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Emerald900,
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (selectedTab != AppTab.HOME && onBackToHome != null) {
                    // Back to Dashboard / Subscreen Top Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(
                            onClick = onBackToHome,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("topbar_back_to_dashboard_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Dashboard",
                                tint = Gold300,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            Text(
                                text = selectedTab.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Alnoor Islamic Portal",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gold300
                            )
                        }
                    }
                } else {
                    // Main Dashboard Top Bar (Logo & App Name)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenLiveChannel() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "Alnoor Islami Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Alnoor Islami",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // YouTube live badge button
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = YoutubeRed,
                                    modifier = Modifier
                                        .clickable { onOpenLiveChannel() }
                                        .testTag("youtube_live_badge")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "LIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "@AlnoorislamiMushahidat",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gold300
                            )
                        }
                    }
                }

                // Action Icons (User Management if admin, Search, Role Badge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (currentRole == UserRole.ADMIN && onAdminUsersClick != null) {
                        IconButton(
                            onClick = onAdminUsersClick,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = "Admin User Management",
                                tint = Gold400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Library & Notices",
                            tint = Gold300,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Column containing the Member/Admin Mode button with the Exit button directly BELOW it
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Role Switcher / Badge Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (currentRole == UserRole.ADMIN) Gold500 else Emerald800,
                            modifier = Modifier
                                .clickable { onRoleClick() }
                                .testTag("role_switcher_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentRole == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Security,
                                    contentDescription = "Role Mode",
                                    tint = if (currentRole == UserRole.ADMIN) Emerald900 else Gold300,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentRole == UserRole.ADMIN) "Admin Mode" else "Member",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentRole == UserRole.ADMIN) Emerald900 else Color.White
                                )
                            }
                        }

                        // Exit button positioned below Member button
                        if (onSignOutClick != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = UrgentRed.copy(alpha = 0.22f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, UrgentRed.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .clickable { onSignOutClick() }
                                    .testTag("topbar_exit_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Exit Application",
                                        tint = Gold300,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Exit",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Decorative gold separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Emerald900,
                                Gold400,
                                Gold300,
                                Gold400,
                                Emerald900
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun AlnoorBottomTabs(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    currentRole: UserRole = UserRole.STANDARD_USER,
    modifier: Modifier = Modifier
) {
    val visibleTabs = remember(currentRole) {
        if (currentRole == UserRole.ADMIN) {
            AppTab.values().filterNot { it == AppTab.LIVE_STREAMS }
        } else {
            AppTab.values().filterNot { it.adminOnly || it == AppTab.LIVE_STREAMS }
        }
    }

    val selectedIndex = visibleTabs.indexOf(selectedTab).coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Emerald900,
        contentColor = Gold300,
        edgePadding = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        visibleTabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tab.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isSelected) Gold300 else Color.White.copy(alpha = 0.7f)
                        )
                        if (tab.adminOnly) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Gold500
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Emerald900,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) Gold400 else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
        }
    }
}


@Composable
fun FcmNotificationBanner(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Gold500,
                    contentColor = Emerald900
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Push Notification",
                            tint = Emerald900,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Emerald900,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Emerald900,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniAudioPlayerBar(
    mediaItem: MediaArchiveItem?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (mediaItem != null) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Emerald800,
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("mini_audio_player")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Gold500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Audio playing",
                            tint = Emerald900,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = mediaItem.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${mediaItem.type} • ${mediaItem.reciter}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gold300,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Gold400,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Player",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// Official Live Broadcast link
const val OFFICIAL_YOUTUBE_LIVE_URL = "https://www.youtube.com/@AlnoorislamiMushahidat/live"

// Deep Linking utility function for YouTube Channel & Live Streams
fun openOfficialYouTubeChannel(context: Context, targetUrl: String = OFFICIAL_YOUTUBE_LIVE_URL) {
    try {
        // Try opening in YouTube native app via intent
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            setPackage("com.google.android.youtube")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(appIntent)
    } catch (e: Exception) {
        // Fallback to web browser
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Opening YouTube: $targetUrl", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun RoleSelectionDialog(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentRole) }
    var adminPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Switch User Role",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select your access level. Admin mode enables publishing notices, updating prayer schedules, managing live streams, events, and moderating messages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedRole == UserRole.STANDARD_USER) Emerald800 else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedRole = UserRole.STANDARD_USER
                            pinError = false
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (selectedRole == UserRole.STANDARD_USER) Gold300 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Community Member",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedRole == UserRole.STANDARD_USER) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "View live streams, prayer times, recite Darood & submit messages",
                                fontSize = 11.sp,
                                color = if (selectedRole == UserRole.STANDARD_USER) Gold300 else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedRole == UserRole.ADMIN) Gold500 else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedRole = UserRole.ADMIN
                            pinError = false
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = if (selectedRole == UserRole.ADMIN) Emerald900 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Administrator (Muhtamim)",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedRole == UserRole.ADMIN) Emerald900 else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Full CRUD publishing & moderation controls",
                                fontSize = 11.sp,
                                color = if (selectedRole == UserRole.ADMIN) Emerald900 else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (selectedRole == UserRole.ADMIN && currentRole != UserRole.ADMIN) {
                    androidx.compose.material3.OutlinedTextField(
                        value = adminPin,
                        onValueChange = {
                            adminPin = it
                            pinError = false
                        },
                        label = { Text("Enter Admin PIN (Default: 7860)") },
                        isError = pinError,
                        supportingText = {
                            if (pinError) {
                                Text("Incorrect PIN. Use '7860' for demo.", color = UrgentRed)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRole == UserRole.ADMIN && currentRole != UserRole.ADMIN) {
                        if (adminPin == "7860" || adminPin == "1234" || adminPin == "alnoor") {
                            onRoleSelected(UserRole.ADMIN)
                            onDismiss()
                        } else {
                            pinError = true
                        }
                    } else {
                        onRoleSelected(selectedRole)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Apply Role")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class NoticePopupStage {
    IMAGE_POSTER,
    WRITTEN_TEXT
}

@Composable
fun ImportantNoticeFullScreenDialog(
    popup: com.example.data.model.ImportantNoticePopup,
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onUpdateNotice: (com.example.data.model.ImportantNoticePopup) -> Unit
) {
    val hasImage = popup.imageUrl.isNotBlank()
    var currentStage by remember(popup.imageUrl) {
        mutableStateOf(if (hasImage) NoticePopupStage.IMAGE_POSTER else NoticePopupStage.WRITTEN_TEXT)
    }
    var isEditing by remember { mutableStateOf(false) }
    var editTitle by remember { mutableStateOf(popup.title) }
    var editMessage by remember { mutableStateOf(popup.message) }
    var editDepartment by remember { mutableStateOf(popup.issuingDepartment) }
    var editImageUrl by remember { mutableStateOf(popup.imageUrl) }
    var editShowOnLogin by remember { mutableStateOf(popup.showOnLogin) }

    // Full screen responsive dialog container
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // --- Top Header with Stage Indicator and Title ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(UrgentRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentStage == NoticePopupStage.IMAGE_POSTER) Icons.Default.Image else Icons.Default.Campaign,
                                contentDescription = "Alert",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = UrgentRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (hasImage) {
                                        if (currentStage == NoticePopupStage.IMAGE_POSTER) "1/2 • OFFICIAL POSTER" else "2/2 • WRITTEN NOTICE"
                                    } else {
                                        "MANDATORY LOGIN NOTICE"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = UrgentRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = popup.title.ifBlank { "Important Announcement" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = UrgentRed,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (currentRole == UserRole.ADMIN && !isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Edit Notice",
                                tint = Gold600
                            )
                        }
                    }
                }

                // --- Admin Edit Mode Form ---
                if (isEditing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Admin Mode: Edit Notice & Poster",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gold600,
                            fontWeight = FontWeight.Bold
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Notice Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Only allow web URLs in text field to prevent BasicTextField crash on huge base64 strings
                        val isBase64Img = editImageUrl.startsWith("data:") || (!editImageUrl.startsWith("http") && editImageUrl.length > 200)
                        if (isBase64Img) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Emerald800.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Poster: Device Image Attached", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                                    TextButton(onClick = { editImageUrl = "" }) {
                                        Text("Remove Poster", fontSize = 11.sp, color = UrgentRed)
                                    }
                                }
                            }
                        } else {
                            androidx.compose.material3.OutlinedTextField(
                                value = editImageUrl,
                                onValueChange = { editImageUrl = it },
                                label = { Text("Poster Image Web URL (Optional)") },
                                placeholder = { Text("https://... or web URL") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        androidx.compose.material3.OutlinedTextField(
                            value = editMessage,
                            onValueChange = { editMessage = it },
                            label = { Text("Notice Message Body") },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = editDepartment,
                            onValueChange = { editDepartment = it },
                            label = { Text("Issuing Department") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show on User Login (ON/OFF)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = editShowOnLogin,
                                onCheckedChange = { editShowOnLogin = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = UrgentRed)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isEditing = false }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onUpdateNotice(
                                        popup.copy(
                                            title = editTitle.trim(),
                                            message = editMessage.trim(),
                                            issuingDepartment = editDepartment.trim(),
                                            imageUrl = editImageUrl.trim(),
                                            showOnLogin = editShowOnLogin,
                                            isActive = editShowOnLogin
                                        )
                                    )
                                    isEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                            ) {
                                Text("Save Updates")
                            }
                        }
                    }
                } else {
                    // --- STAGE 1: AUTO-ADJUSTING FULL-SIZE IMAGE POSTER ---
                    if (currentStage == NoticePopupStage.IMAGE_POSTER && hasImage) {
                        val base64Bitmap = remember(popup.imageUrl) {
                            FilePickerUtils.decodeBase64Bitmap(popup.imageUrl)
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Black.copy(alpha = 0.05f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 380.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                if (base64Bitmap != null) {
                                    Image(
                                        bitmap = base64Bitmap.asImageBitmap(),
                                        contentDescription = "Official Notice Poster",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    AsyncImage(
                                        model = popup.imageUrl,
                                        contentDescription = "Official Notice Poster",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                        }

                        // Next Button to Written Notice
                        Button(
                            onClick = {
                                if (popup.message.isNotBlank()) {
                                    currentStage = NoticePopupStage.WRITTEN_TEXT
                                } else {
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("popup_next_to_text_button")
                        ) {
                            Text(
                                text = if (popup.message.isNotBlank()) "Next: Read Written Notice" else "I Have Read This Notice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (popup.message.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // --- STAGE 2: WRITTEN NOTICE TEXT ---
                    if (currentStage == NoticePopupStage.WRITTEN_TEXT || !hasImage) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = UrgentRed.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = popup.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = UrgentRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Issued by: ${popup.issuingDepartment}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = popup.message.ifBlank { "Please follow all guidelines and announcements published by the management." },
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasImage) {
                                OutlinedButton(
                                    onClick = { currentStage = NoticePopupStage.IMAGE_POSTER },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Back to Image", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("View Poster", fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(if (hasImage) 1.4f else 1f)
                                    .height(46.dp)
                                    .testTag("acknowledge_notice_button")
                            ) {
                                Text("I Have Read This Notice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
