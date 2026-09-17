package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import com.example.BuildConfig
import com.example.data.model.AppVersionInfo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ImportantNoticePopup
import com.example.data.model.NoticeItem
import com.example.data.model.NoticePriority
import com.example.data.model.UserRole
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.UrgentRed
import androidx.compose.foundation.Image
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Link
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.util.FilePickerUtils
import java.util.UUID

@Composable
fun NoticesScreen(
    notices: List<NoticeItem>,
    importantPopup: ImportantNoticePopup? = null,
    appVersionInfo: AppVersionInfo? = null,
    currentRole: UserRole,
    onAddNotice: (NoticeItem) -> Unit,
    onDeleteNotice: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onUpdateNotice: ((NoticeItem) -> Unit)? = null,
    onUpdateImportantPopup: ((ImportantNoticePopup) -> Unit)? = null,
    onUpdateAppVersionInfo: ((AppVersionInfo) -> Unit)? = null,
    onTriggerPreviewPopup: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditPopupDialog by remember { mutableStateOf(false) }
    var showEditVersionDialog by remember { mutableStateOf(false) }
    var noticeToEdit by remember { mutableStateOf<NoticeItem?>(null) }
    var noticeToDelete by remember { mutableStateOf<NoticeItem?>(null) }

    // Native Image Picker for Login Popup Poster
    val popupImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && importantPopup != null && onUpdateImportantPopup != null) {
            val base64DataUrl = FilePickerUtils.encodeImageUriToBase64(context, uri, maxDimension = 1200)
            val finalImageUrl = base64DataUrl ?: uri.toString()
            onUpdateImportantPopup(
                importantPopup.copy(
                    imageUrl = finalImageUrl,
                    showOnLogin = true,
                    isActive = true
                )
            )
            Toast.makeText(context, "Popup Poster Attached & Synced to Cloud!", Toast.LENGTH_SHORT).show()
        }
    }

    // Top-level Image Picker for Edit Notice Dialog (hoisted to prevent lifecycle crashes)
    var dialogPickedImagePayload by remember { mutableStateOf<String?>(null) }
    val editDialogImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64DataUrl = FilePickerUtils.encodeImageUriToBase64(context, uri, maxDimension = 1200)
            dialogPickedImagePayload = base64DataUrl ?: uri.toString()
            Toast.makeText(context, "Poster image selected!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            if (currentRole == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Gold500,
                    contentColor = Emerald900,
                    modifier = Modifier.testTag("publish_notice_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Publish Notice")
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Top Important Notice Banner / Login Popup Config ---
            importantPopup?.let { popup ->
                item {
                    val isPopupActive = popup.showOnLogin && popup.isActive
                    val hasPosterImage = popup.imageUrl.isNotBlank()

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPopupActive) UrgentRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("important_notice_popup_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Top Header with Badges and On/Off Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isPopupActive) UrgentRed else Color.Gray
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Urgent",
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "LOGIN POPUP",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isPopupActive) Emerald800 else Color.DarkGray
                                    ) {
                                        Text(
                                            text = if (isPopupActive) "ACTIVE" else "OFF",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                if (currentRole == UserRole.ADMIN) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Switch(
                                            checked = isPopupActive,
                                            onCheckedChange = { isChecked ->
                                                onUpdateImportantPopup?.invoke(
                                                    popup.copy(
                                                        showOnLogin = isChecked,
                                                        isActive = isChecked
                                                    )
                                                )
                                                Toast.makeText(
                                                    context,
                                                    if (isChecked) "Login Popup Activated" else "Login Popup Disabled",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = UrgentRed)
                                        )
                                        IconButton(onClick = { showEditPopupDialog = true }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Popup", tint = UrgentRed)
                                        }
                                    }
                                }
                            }

                            // --- BLOCK 1: POPUP POSTER IMAGE BLOCK ---
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = "Poster",
                                                tint = UrgentRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "1. Popup Notice Image / Poster",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (hasPosterImage) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Emerald800.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "Poster Attached",
                                                    fontSize = 10.sp,
                                                    color = Emerald800,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (hasPosterImage) {
                                        // Auto-adjusting preview thumbnail
                                        val base64Bitmap = remember(popup.imageUrl) {
                                            FilePickerUtils.decodeBase64Bitmap(popup.imageUrl)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.Black.copy(alpha = 0.05f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 180.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (base64Bitmap != null) {
                                                    Image(
                                                        bitmap = base64Bitmap.asImageBitmap(),
                                                        contentDescription = "Poster Preview",
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(10.dp))
                                                    )
                                                } else {
                                                    AsyncImage(
                                                        model = popup.imageUrl,
                                                        contentDescription = "Poster Preview",
                                                        contentScale = ContentScale.Fit,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(10.dp))
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Users will see this poster in full auto-adjusted size upon login, with a 'Next' button leading to written text.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )

                                        if (currentRole == UserRole.ADMIN) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { popupImagePickerLauncher.launch("image/*") },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f).height(36.dp)
                                                ) {
                                                    Icon(Icons.Default.Upload, contentDescription = "Change", modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Change Poster", fontSize = 11.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        onUpdateImportantPopup?.invoke(popup.copy(imageUrl = ""))
                                                        Toast.makeText(context, "Poster Image Removed", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f).height(36.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Remove", modifier = Modifier.size(14.dp), tint = UrgentRed)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Remove Image", fontSize = 11.sp, color = UrgentRed)
                                                }
                                            }
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "No image currently attached. Users directly view the written text notice.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )

                                                if (currentRole == UserRole.ADMIN) {
                                                    Button(
                                                        onClick = { popupImagePickerLauncher.launch("image/*") },
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                                                        modifier = Modifier.height(36.dp)
                                                    ) {
                                                        Icon(Icons.Default.Upload, contentDescription = "Upload", modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("+ Upload / Attach Poster Image", fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // --- BLOCK 2: WRITTEN NOTICE DETAILS BLOCK ---
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Campaign,
                                                contentDescription = "Message",
                                                tint = UrgentRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "2. Written Notice Content",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (currentRole == UserRole.ADMIN) {
                                            TextButton(
                                                onClick = { showEditPopupDialog = true },
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text("Edit Text", fontSize = 11.sp, color = UrgentRed)
                                            }
                                        }
                                    }

                                    Text(
                                        text = popup.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = UrgentRed
                                    )

                                    Text(
                                        text = popup.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 19.sp,
                                        maxLines = 10,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "Issued by: ${popup.issuingDepartment}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Bottom Preview Action Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { onTriggerPreviewPopup?.invoke() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Icon(Icons.Default.OpenInFull, contentDescription = "Preview", modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Preview Full Popup Flow (Image → Text)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // --- ADMIN CONSOLE: APP VERSION & FORCED CI/CD CLOUD UPDATES ---
            if (currentRole == UserRole.ADMIN && appVersionInfo != null) {
                item {
                    val localVersionCode = BuildConfig.VERSION_CODE
                    val localVersionName = BuildConfig.VERSION_NAME
                    val isForcedActive = appVersionInfo.isForcedUpdate

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isForcedActive) UrgentRed.copy(alpha = 0.6f) else Gold500.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isForcedActive) UrgentRed else Emerald800),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SystemUpdate,
                                            contentDescription = "Version Controller",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "App Version & Update Console",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Automated CI/CD Cloud • Startup Gate",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Gold400,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isForcedActive) UrgentRed else Emerald800
                                ) {
                                    Text(
                                        text = if (isForcedActive) "FORCED: ON" else "OPTIONAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Version comparison details
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Installed Build:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("v$localVersionName (Build $localVersionCode)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Cloud Latest Build:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                "v${appVersionInfo.latestVersionName} (Build ${appVersionInfo.latestVersionCode})",
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald800,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Min Supported Build: ${appVersionInfo.minSupportedVersionCode}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Force Update:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Switch(
                                                checked = isForcedActive,
                                                onCheckedChange = { isChecked ->
                                                    onUpdateAppVersionInfo?.invoke(
                                                        appVersionInfo.copy(isForcedUpdate = isChecked)
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        if (isChecked) "Mandatory update gate activated for all users!" else "Mandatory update gate disabled.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = UrgentRed)
                                            )
                                        }
                                    }
                                }
                            }

                            // Action buttons: Edit Version Settings & Open Cloud Releases
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showEditVersionDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Configure Cloud Release", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        com.example.util.ApkInstallerUtils.openInBrowser(context, appVersionInfo.apkDownloadUrl)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download APK", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald900),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Gold500),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = "Notices", tint = Emerald900, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Notice Board & Announcements",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Official Alnoor circulars, timings and events updates",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gold300
                            )
                        }
                    }
                }
            }

            // Notices List
            items(notices) { notice ->
                val isUrgent = notice.priority == NoticePriority.URGENT

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUrgent) UrgentRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_item_${notice.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (notice.priority) {
                                        NoticePriority.URGENT -> UrgentRed
                                        NoticePriority.IMPORTANT -> Gold500
                                        NoticePriority.GENERAL -> Emerald800
                                    }
                                ) {
                                    Text(
                                        text = notice.priority.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (notice.priority == NoticePriority.IMPORTANT) Emerald900 else Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                if (notice.isPinned) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = Gold600, modifier = Modifier.size(16.dp))
                                }
                            }

                            if (currentRole == UserRole.ADMIN) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onTogglePin(notice.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PushPin,
                                            contentDescription = "Pin/Unpin",
                                            tint = if (notice.isPinned) Gold600 else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { noticeToEdit = notice },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Notice", tint = Emerald700, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { noticeToDelete = notice },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UrgentRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = notice.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isUrgent) UrgentRed else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = notice.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Published: ${notice.date}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "By ${notice.department}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Emerald700
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Admin Edit Important Notice Popup Dialog ---
    if (showEditPopupDialog && importantPopup != null && onUpdateImportantPopup != null) {
        var editTitle by remember(showEditPopupDialog, importantPopup) { mutableStateOf(importantPopup.title) }
        var editMessage by remember(showEditPopupDialog, importantPopup) { mutableStateOf(importantPopup.message) }
        var editDepartment by remember(showEditPopupDialog, importantPopup) { mutableStateOf(importantPopup.issuingDepartment) }
        var editShowOnLogin by remember(showEditPopupDialog, importantPopup) { mutableStateOf(importantPopup.showOnLogin) }

        // Poster image state: safely separate web URL from device payload to prevent BasicTextField crash on huge base64 strings
        val initialIsWebUrl = importantPopup.imageUrl.startsWith("http://") || importantPopup.imageUrl.startsWith("https://")
        var editWebUrl by remember(showEditPopupDialog, importantPopup) {
            mutableStateOf(if (initialIsWebUrl) importantPopup.imageUrl else "")
        }
        var keepExistingDeviceImage by remember(showEditPopupDialog, importantPopup) {
            mutableStateOf(!initialIsWebUrl && importantPopup.imageUrl.isNotBlank())
        }

        val hasAttachedImage = (keepExistingDeviceImage && importantPopup.imageUrl.isNotBlank()) ||
                editWebUrl.isNotBlank() ||
                (dialogPickedImagePayload != null && dialogPickedImagePayload!!.isNotBlank())

        AlertDialog(
            onDismissRequest = {
                showEditPopupDialog = false
                dialogPickedImagePayload = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = UrgentRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin: Edit Notice Content & Settings", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Edit the written announcement text and manage how this notice appears to users on login.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Written Message Content
                    Text("Notice Details", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UrgentRed)

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Notice Title *") },
                        placeholder = { Text("e.g. Important Community Announcement") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_popup_title")
                    )

                    OutlinedTextField(
                        value = editMessage,
                        onValueChange = { editMessage = it },
                        label = { Text("Full Notice Message *") },
                        placeholder = { Text("Write the full notice announcement details here...") },
                        minLines = 4,
                        maxLines = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_popup_message")
                    )

                    OutlinedTextField(
                        value = editDepartment,
                        onValueChange = { editDepartment = it },
                        label = { Text("Issuing Department") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_popup_department")
                    )

                    // Switch for login pop-up trigger
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = UrgentRed.copy(alpha = 0.06f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Trigger Pop-up on User Login", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Displays full-screen alert upon app startup", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = editShowOnLogin,
                                onCheckedChange = { editShowOnLogin = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = UrgentRed)
                            )
                        }
                    }

                    // Optional Poster Image Status
                    Text("Poster Image Attachment (Optional)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Emerald800)

                    if (hasAttachedImage) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Emerald800.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = "Poster",
                                            tint = Emerald800,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when {
                                                dialogPickedImagePayload != null -> "✓ New Device Image Selected"
                                                keepExistingDeviceImage -> "✓ Device Poster Image Attached"
                                                else -> "✓ Web Image Attached"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald800
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { editDialogImagePickerLauncher.launch("image/*") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                    ) {
                                        Text("Change Image", fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            keepExistingDeviceImage = false
                                            dialogPickedImagePayload = ""
                                            editWebUrl = ""
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(34.dp)
                                    ) {
                                        Text("Remove Poster", fontSize = 10.sp, color = UrgentRed)
                                    }
                                }
                            }
                        }
                    } else {
                        // No image attached - provide buttons without risking base64 into TextField
                        Button(
                            onClick = { editDialogImagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = "Pick Image", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Attach Poster Image from Device", fontSize = 11.sp)
                        }

                        OutlinedTextField(
                            value = editWebUrl,
                            onValueChange = { editWebUrl = it },
                            label = { Text("Or Enter Poster Web URL (Optional)") },
                            placeholder = { Text("https://example.com/poster.jpg") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalImageUrl = when {
                            dialogPickedImagePayload != null && dialogPickedImagePayload!!.isNotBlank() -> dialogPickedImagePayload!!
                            dialogPickedImagePayload != null && dialogPickedImagePayload!!.isBlank() -> ""
                            editWebUrl.isNotBlank() && editWebUrl.startsWith("http") -> editWebUrl.trim()
                            keepExistingDeviceImage -> importantPopup.imageUrl
                            else -> ""
                        }

                        onUpdateImportantPopup(
                            importantPopup.copy(
                                title = editTitle.trim().ifBlank { "Important Notice" },
                                message = editMessage.trim(),
                                issuingDepartment = editDepartment.trim().ifBlank { "Markazi Management" },
                                imageUrl = finalImageUrl,
                                showOnLogin = editShowOnLogin,
                                isActive = editShowOnLogin
                            )
                        )
                        showEditPopupDialog = false
                        dialogPickedImagePayload = null
                        Toast.makeText(context, "Notice Text Updated & Saved Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                    modifier = Modifier.testTag("save_popup_notice_btn")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Sync Notice", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditPopupDialog = false
                    dialogPickedImagePayload = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Publish Regular Notice Dialog (Admin)
    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newContent by remember { mutableStateOf("") }
        var newDepartment by remember { mutableStateOf("Alnoor Central Committee") }
        var selectedPriority by remember { mutableStateOf(NoticePriority.IMPORTANT) }
        var isPinned by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Publish Community Notice", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Notice Title") })
                    OutlinedTextField(value = newContent, onValueChange = { newContent = it }, label = { Text("Notice Content / Message") }, minLines = 3)
                    OutlinedTextField(value = newDepartment, onValueChange = { newDepartment = it }, label = { Text("Issuing Department / Board") })

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Priority: ${selectedPriority.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row {
                            NoticePriority.values().forEach { priority ->
                                TextButton(onClick = { selectedPriority = priority }) {
                                    Text(priority.name, fontSize = 11.sp, color = if (selectedPriority == priority) Emerald800 else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pin to Top of Notice Board", fontSize = 12.sp)
                        Switch(
                            checked = isPinned,
                            onCheckedChange = { isPinned = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Gold500)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = NoticeItem(
                            id = UUID.randomUUID().toString(),
                            title = newTitle,
                            content = newContent,
                            priority = selectedPriority,
                            date = "Today",
                            department = newDepartment,
                            isPinned = isPinned
                        )
                        onAddNotice(item)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Publish Notice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Regular Notice Dialog (Admin)
    noticeToEdit?.let { notice ->
        var editTitle by remember(notice.id) { mutableStateOf(notice.title) }
        var editContent by remember(notice.id) { mutableStateOf(notice.content) }
        var editDepartment by remember(notice.id) { mutableStateOf(notice.department) }
        var editPriority by remember(notice.id) { mutableStateOf(notice.priority) }
        var isPinned by remember(notice.id) { mutableStateOf(notice.isPinned) }

        AlertDialog(
            onDismissRequest = { noticeToEdit = null },
            title = { Text("Edit Community Notice", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Notice Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editContent, onValueChange = { editContent = it }, label = { Text("Notice Content / Message") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editDepartment, onValueChange = { editDepartment = it }, label = { Text("Issuing Department") }, modifier = Modifier.fillMaxWidth())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Priority: ${editPriority.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row {
                            NoticePriority.values().forEach { priority ->
                                TextButton(onClick = { editPriority = priority }) {
                                    Text(priority.name, fontSize = 11.sp, color = if (editPriority == priority) Emerald800 else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pin to Top of Notice Board", fontSize = 12.sp)
                        Switch(
                            checked = isPinned,
                            onCheckedChange = { isPinned = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Gold500)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = notice.copy(
                            title = editTitle,
                            content = editContent,
                            priority = editPriority,
                            department = editDepartment,
                            isPinned = isPinned
                        )
                        if (onUpdateNotice != null) {
                            onUpdateNotice(updated)
                        } else {
                            onAddNotice(updated)
                        }
                        noticeToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { noticeToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // Delete confirmation (Admin)
    noticeToDelete?.let { notice ->
        AlertDialog(
            onDismissRequest = { noticeToDelete = null },
            title = { Text("Delete Notice?") },
            text = { Text("Are you sure you want to remove '${notice.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNotice(notice.id)
                        noticeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noticeToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Configure Cloud Version & Forced Update Dialog (Admin)
    if (showEditVersionDialog && appVersionInfo != null) {
        var editVersionName by remember(appVersionInfo) { mutableStateOf(appVersionInfo.latestVersionName) }
        var editVersionCode by remember(appVersionInfo) { mutableStateOf(appVersionInfo.latestVersionCode.toString()) }
        var editMinSupported by remember(appVersionInfo) { mutableStateOf(appVersionInfo.minSupportedVersionCode.toString()) }
        var editDownloadUrl by remember(appVersionInfo) { mutableStateOf(appVersionInfo.apkDownloadUrl) }
        var editNotes by remember(appVersionInfo) { mutableStateOf(appVersionInfo.releaseNotes) }
        var editIsForced by remember(appVersionInfo) { mutableStateOf(appVersionInfo.isForcedUpdate) }

        AlertDialog(
            onDismissRequest = { showEditVersionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Emerald800)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cloud App Release & Update Gate", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Update the latest APK version and configure mandatory update behavior for all user devices.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editVersionName,
                            onValueChange = { editVersionName = it },
                            label = { Text("Version Name (e.g. 1.1.0)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editVersionCode,
                            onValueChange = { editVersionCode = it },
                            label = { Text("Build Code") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = editMinSupported,
                        onValueChange = { editMinSupported = it },
                        label = { Text("Minimum Supported Build Code") },
                        supportingText = { Text("Devices with build < this number MUST update") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editDownloadUrl,
                        onValueChange = { editDownloadUrl = it },
                        label = { Text("APK Download / Direct URL") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Release Notes / What's New") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Force Mandatory Update", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Block all outdated app versions on launch", fontSize = 11.sp, color = UrgentRed)
                        }
                        Switch(
                            checked = editIsForced,
                            onCheckedChange = { editIsForced = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = UrgentRed)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newCode = editVersionCode.toIntOrNull() ?: appVersionInfo.latestVersionCode
                        val newMin = editMinSupported.toIntOrNull() ?: appVersionInfo.minSupportedVersionCode
                        val updated = appVersionInfo.copy(
                            latestVersionName = editVersionName,
                            latestVersionCode = newCode,
                            minSupportedVersionCode = newMin,
                            apkDownloadUrl = editDownloadUrl,
                            releaseNotes = editNotes,
                            isForcedUpdate = editIsForced,
                            releaseDate = "August 2026"
                        )
                        onUpdateAppVersionInfo?.invoke(updated)
                        showEditVersionDialog = false
                        Toast.makeText(context, "Cloud App Version Updated & Synced!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Push & Sync to Cloud")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditVersionDialog = false }) { Text("Cancel") }
            }
        )
    }
}
