package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DaroodState
import com.example.data.model.DaroodSubmission
import com.example.data.model.UserRole
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.UrgentRed
import com.example.util.FilePickerUtils
import com.example.util.PickedFileInfo
import java.text.NumberFormat
import java.util.Locale

data class DaroodResourceDoc(
    val id: String,
    val title: String,
    val fileType: String, // "PDF", "IMAGE", "DOCUMENT"
    val fileSize: String,
    val uriString: String = "",
    val description: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaroodCounterScreen(
    daroodState: DaroodState,
    currentRole: UserRole,
    bannerImageUrl: String = "",
    onUpdateBannerImage: (imageUrl: String, title: String) -> Unit = { _, _ -> },
    onForceSyncBanner: () -> Unit = {},
    onSubmitCount: (userNameOrNumber: String, count: Long) -> Unit,
    onGenerateCsv: () -> String,
    onResetCounts: () -> Unit = {},
    currentUserName: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var userNameInput by remember { mutableStateOf(currentUserName) }
    var countInput by remember { mutableStateOf("") }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var lastSubmittedInfo by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Admin Banner Upload States
    var customBannerUrlInput by remember { mutableStateOf("") }
    var showBannerUploadDialog by remember { mutableStateOf(false) }
    var pendingBannerDataUri by remember { mutableStateOf<String?>(null) }
    var pendingBannerFileName by remember { mutableStateOf<String?>(null) }

    // --- Native Device File Picker Launcher for Weekly Banner ---
    val bannerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64Data = FilePickerUtils.encodeImageUriToBase64(context, uri, maxDimension = 1280)
            val info = FilePickerUtils.extractFileInfo(context, uri)
            if (base64Data != null) {
                pendingBannerDataUri = base64Data
                pendingBannerFileName = info.name
                showBannerUploadDialog = true
            } else {
                pendingBannerDataUri = uri.toString()
                pendingBannerFileName = info.name
                showBannerUploadDialog = true
            }
        }
    }

    // Admin uploaded Darood resources list (PDFs, images, booklets)
    val uploadedResources = remember {
        mutableStateListOf(
            DaroodResourceDoc(
                id = "doc_darood_taj",
                title = "Darood-e-Taj Arabic Recitation & Benefits Guide",
                fileType = "PDF",
                fileSize = "2.4 MB",
                description = "Complete Arabic text with verse-by-verse translation and spiritual virtues"
            ),
            DaroodResourceDoc(
                id = "doc_darood_ibrahim",
                title = "Darood-e-Ibrahimi Calligraphy High-Res Poster",
                fileType = "IMAGE",
                fileSize = "1.8 MB",
                description = "Official calligraphy art for display in prayer halls and homes"
            )
        )
    }

    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedPickedFile by remember { mutableStateOf<PickedFileInfo?>(null) }
    var pickedFileUri by remember { mutableStateOf<Uri?>(null) }

    // --- Native Device File Picker Launcher for general Darood docs ---
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedFileUri = uri
            val info = FilePickerUtils.extractFileInfo(context, uri)
            selectedPickedFile = info
            showUploadDialog = true
            Toast.makeText(context, "Selected: ${info.name}", Toast.LENGTH_SHORT).show()
        }
    }

    val formattedGrandTotal = remember(daroodState.grandTotal) {
        NumberFormat.getNumberInstance(Locale.US).format(daroodState.grandTotal)
    }

    // Filter personal submissions for standard members
    val effectiveUserIdentifier = remember(userNameInput, currentUserName) {
        when {
            userNameInput.isNotBlank() -> userNameInput.trim()
            currentUserName.isNotBlank() -> currentUserName.trim()
            else -> ""
        }
    }

    val personalSubmissions = remember(daroodState.submissions, effectiveUserIdentifier) {
        if (effectiveUserIdentifier.isBlank()) {
            daroodState.submissions.take(5)
        } else {
            daroodState.submissions.filter { sub ->
                sub.userNameOrNumber.contains(effectiveUserIdentifier, ignoreCase = true)
            }
        }
    }

    val personalTotalRecitations = remember(personalSubmissions) {
        personalSubmissions.sumOf { it.count }
    }

    fun submitCurrentCount() {
        val count = countInput.trim().toLongOrNull()
        if (count == null || count <= 0) {
            Toast.makeText(context, "Please enter a valid recitation count", Toast.LENGTH_SHORT).show()
            return
        }
        val user = if (userNameInput.isBlank()) {
            if (currentUserName.isNotBlank()) currentUserName else "Member Reciter"
        } else {
            userNameInput.trim()
        }
        onSubmitCount(user, count)
        lastSubmittedInfo = "$count Darood recitations contributed by $user"
        showSuccessBanner = true
        countInput = ""
        Toast.makeText(context, "JazakAllah! $count Darood recitations submitted.", Toast.LENGTH_LONG).show()
    }

    fun exportAndShareCsv() {
        val csvData = onGenerateCsv()
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csvData)
                putExtra(Intent.EXTRA_TITLE, "Darood_Sharif_Submissions.csv")
                type = "text/csv"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Download / Export Darood Submissions CSV")
            context.startActivity(shareIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "CSV generated and copied.", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Dynamic Blessed Darood Banner Image Card (Dynamic & Responsive) ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald900),
                elevation = CardDefaults.cardElevation(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Gold500.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("darood_banner_image_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Emerald900),
                    contentAlignment = Alignment.Center
                ) {
                    if (bannerImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = bannerImageUrl,
                            contentDescription = "Darood Sharif Weekly Banner",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(20.dp))
                        )
                    } else {
                        // Fallback artwork box with calligraphy
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Emerald900, Color(0xFF0F1B17))
                                    )
                                )
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Gold500.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Gold400,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold300,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Darood Sharif Recitation Banner",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Admin quick action button on top right of the banner
                    if (currentRole == UserRole.ADMIN) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Gold400),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clickable {
                                    bannerPickerLauncher.launch("image/*")
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Change Banner",
                                    tint = Gold300,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Change Banner",
                                    fontSize = 11.sp,
                                    color = Gold300,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Recitation Submission Form ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth().testTag("darood_submission_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Emerald800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Recitation Input",
                                tint = Gold300,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Enter Darood Recitation Count",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Enter your count to contribute to the community total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = countInput,
                        onValueChange = { countInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Recitations to Submit (e.g. 100, 500, 1000)") },
                        placeholder = { Text("Enter number") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { submitCurrentCount() }),
                        leadingIcon = {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Emerald800)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_darood_count")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = userNameInput,
                        onValueChange = { userNameInput = it },
                        label = { Text("Your Name or Phone (Optional for Personal Tracking)") },
                        placeholder = { Text("e.g. Brother Ahmad / 0300-1234567") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Emerald800)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_darood_user_name")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Count Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(33, 100, 500, 1000).forEach { preset ->
                            FilterChip(
                                selected = countInput == preset.toString(),
                                onClick = { countInput = preset.toString() },
                                label = { Text("+$preset", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { submitCurrentCount() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald800,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_darood_count")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Gold300)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Recitation Count",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    AnimatedVisibility(visible = showSuccessBanner) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Emerald800.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald800),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Emerald800,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = lastSubmittedInfo.ifBlank { "Darood recitation submitted successfully!" },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Emerald800
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Grand Collective Milestone Card (Admin only) ---
        if (currentRole == UserRole.ADMIN) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald900),
                    elevation = CardDefaults.cardElevation(5.dp),
                    modifier = Modifier.fillMaxWidth().testTag("grand_total_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "COMMUNITY GRAND TOTAL",
                            color = Gold400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formattedGrandTotal,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Recitations Sent to the Prophet ﷺ",
                            color = Gold300,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Bar towards next 2 Million Milestone
                        val targetMilestone = 2_000_000L
                        val progressFraction = (daroodState.grandTotal.toFloat() / targetMilestone.toFloat()).coerceIn(0f, 1f)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Next Goal: 2,000,000",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${(progressFraction * 100).toInt()}%",
                                    color = Gold400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Brush.horizontalGradient(listOf(Gold400, Gold600)))
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Personal Submissions History (Adjusted Layout: Count box below heading) ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth().testTag("personal_darood_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header Area
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Emerald800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Gold300,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "My Submitted Recitations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your personal contribution record",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dedicated Count Box placed below the heading for un-cramped display
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Emerald900,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold500.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = Gold400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Personal Total Contributed",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "${NumberFormat.getNumberInstance(Locale.US).format(personalTotalRecitations)} Recited",
                                color = Gold300,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (personalSubmissions.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No recitation records found under this name yet. Submit your first recitation count above!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        personalSubmissions.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = sub.userNameOrNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = sub.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "+${NumberFormat.getNumberInstance(Locale.US).format(sub.count)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // --- 5. Dedicated Admin Action Card: Weekly Darood Recitation Banner Cloud Manager ---
        if (currentRole == UserRole.ADMIN) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold500.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("admin_darood_banner_action_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Gold500),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Emerald900,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Weekly Darood Banner Manager",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Upload & sync the weekly image to all member devices",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Gold500
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Emerald900,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pixel Dimension & Weekly Sync Guidance Information Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Emerald900.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald800.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = null,
                                        tint = Emerald800,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Correct Banner Box Dimensions & Sync Guide:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Emerald800
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Recommended Dimensions: 1200 × 675 px (16:9 ratio) or 1080 × 540 px (2:1 ratio)\n" +
                                           "• Minimum Resolution: 800 × 450 px at 72–150 DPI\n" +
                                           "• Dynamic Display: Image automatically scales to fit user screens gracefully\n" +
                                           "• Weekly Cloud Sync: Local devices cache the image and auto-sync on the 1st day of the week (Sunday/Monday).",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: File Picker & URL input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { bannerPickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Emerald800,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("darood_banner_file_picker_button")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pick Image (File)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { onForceSyncBanner() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("darood_banner_force_sync_button")
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp), tint = Emerald800)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Force Cloud Sync", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald800)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Cloud Image URL Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customBannerUrlInput,
                                onValueChange = { customBannerUrlInput = it },
                                placeholder = { Text("Or paste Cloud Image URL (https://...)") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_darood_banner_url")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (customBannerUrlInput.isNotBlank()) {
                                        onUpdateBannerImage(customBannerUrlInput.trim(), "Weekly Darood Sharif Banner")
                                        customBannerUrlInput = ""
                                        Toast.makeText(context, "Cloud banner image saved & synced!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please enter an image URL", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Gold600),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Emerald900)
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Admin Only: Upload Darood Materials / PDF / Documents ---
        if (currentRole == UserRole.ADMIN) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_darood_upload_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Gold500),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.UploadFile,
                                        contentDescription = null,
                                        tint = Emerald900,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Admin: Upload Darood Documents",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Upload PDF guides, booklets, or calligraphy files",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Gold500
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Emerald900,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald800,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("darood_open_native_picker_button")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Document (Open Device Picker)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Published Darood Materials (${uploadedResources.size}):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        uploadedResources.forEach { doc ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (doc.fileType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Image,
                                            contentDescription = null,
                                            tint = if (doc.fileType == "PDF") UrgentRed else Gold500,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = doc.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${doc.fileType} • ${doc.fileSize}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            FilePickerUtils.openFileWithIntent(
                                                context = context,
                                                fileUrl = doc.uriString.ifBlank { "https://alnoor-islamic.org/darood/${doc.id}.pdf" },
                                                fileType = doc.fileType,
                                                title = doc.title
                                            )
                                        }
                                    ) {
                                        Text("Open", color = Emerald800, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 7. Admin Reset & CSV Export Tools ---
        if (currentRole == UserRole.ADMIN) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier.fillMaxWidth().testTag("admin_darood_tools_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Admin Export & Cycle Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Export submission spreadsheets and manage cycle resets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { exportAndShareCsv() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_export_darood_csv")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Emerald800)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald800)
                            }

                            Button(
                                onClick = { showResetConfirmDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UrgentRed.copy(alpha = 0.15f),
                                    contentColor = UrgentRed
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_reset_darood_counts")
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = UrgentRed)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialog: Confirm New Banner Upload ---
    if (showBannerUploadDialog && pendingBannerDataUri != null) {
        AlertDialog(
            onDismissRequest = {
                showBannerUploadDialog = false
                pendingBannerDataUri = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Emerald800)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Darood Banner to Cloud", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Save this image as the official weekly Darood recitation banner? It will sync across all member devices.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Selected: ${pendingBannerFileName ?: "New Banner Image"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Emerald800
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uriToSave = pendingBannerDataUri
                        if (uriToSave != null) {
                            onUpdateBannerImage(uriToSave, pendingBannerFileName ?: "Weekly Darood Banner")
                            Toast.makeText(context, "Banner published and synced to Cloud!", Toast.LENGTH_LONG).show()
                        }
                        showBannerUploadDialog = false
                        pendingBannerDataUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Save & Publish to Cloud", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBannerUploadDialog = false
                        pendingBannerDataUri = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Dialog: Confirm File Upload ---
    if (showUploadDialog && selectedPickedFile != null) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = Emerald800)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Document", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Publish '${selectedPickedFile?.name}' to the Darood portal?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Type: ${selectedPickedFile?.categoryType} • Size: ${selectedPickedFile?.sizeString}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val info = selectedPickedFile
                        if (info != null) {
                            uploadedResources.add(
                                DaroodResourceDoc(
                                    id = "doc_${System.currentTimeMillis()}",
                                    title = info.name,
                                    fileType = info.categoryType,
                                    fileSize = info.sizeString,
                                    uriString = pickedFileUri?.toString() ?: ""
                                )
                            )
                            Toast.makeText(context, "Published: ${info.name}", Toast.LENGTH_SHORT).show()
                        }
                        showUploadDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Publish", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Dialog: Reset Confirmation ---
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = {
                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = UrgentRed, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Confirm Monthly Count Reset", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to reset all Darood recitation counts to 0? This will zero out the community total for the new cycle.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetCounts()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Community recitation count reset to 0.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Reset Counts to 0", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
