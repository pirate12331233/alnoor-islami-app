package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.GalleryAsset
import com.example.data.model.UserRole
import com.example.ui.components.EnhancedImageViewerDialog
import com.example.ui.components.EnhancedPdfDocumentViewerDialog
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    galleryAssets: List<GalleryAsset>,
    currentRole: UserRole,
    onAddAsset: (GalleryAsset) -> Unit,
    onDeleteAsset: (String) -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Images", "PDF Documents", "Documents")

    var viewingAsset by remember { mutableStateOf<GalleryAsset?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var assetToDelete by remember { mutableStateOf<GalleryAsset?>(null) }

    var selectedPickedFile by remember { mutableStateOf<PickedFileInfo?>(null) }
    var pickedFileUri by remember { mutableStateOf<Uri?>(null) }

    // --- Native Device File Picker Launcher ---
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

    val filteredAssets = galleryAssets.filter { asset ->
        when (selectedFilter) {
            "Images" -> asset.fileType.equals("IMAGE", ignoreCase = true)
            "PDF Documents" -> asset.fileType.equals("PDF", ignoreCase = true)
            "Documents" -> asset.fileType.equals("DOCUMENT", ignoreCase = true)
            else -> true
        }
    }

    Scaffold(
        floatingActionButton = {
            if (currentRole == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    containerColor = Gold500,
                    contentColor = Emerald900,
                    modifier = Modifier.testTag("upload_gallery_file_fab")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Upload File")
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- 1. Top Header Banner ---
            item(span = { GridItemSpan(2) }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald900),
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Gold500),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Collections,
                                        contentDescription = "Gallery",
                                        tint = Emerald900,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Alnoor Mosque Gallery",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Photos & Media Archive (${galleryAssets.size} files)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gold300,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        isRefreshing = true
                                        onRefresh()
                                        Toast.makeText(context, "Refreshing Gallery from Cloud...", Toast.LENGTH_SHORT).show()
                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                            isRefreshing = false
                                        }, 1200)
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("refresh_gallery_cloud_button")
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Sync Cloud",
                                        tint = Gold300,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (currentRole == UserRole.ADMIN) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Gold500
                                    ) {
                                        Text(
                                            text = "Admin",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Emerald900,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (currentRole == UserRole.ADMIN) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold500,
                                    contentColor = Emerald900
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_upload_gallery_native_button")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload File (Native File Picker)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- 2. Filter Chips ---
            item(span = { GridItemSpan(2) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald800,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // --- 3. Grid of Media Assets ---
            items(filteredAssets) { asset ->
                val isPdf = asset.fileType.equals("PDF", ignoreCase = true)
                val isImage = asset.fileType.equals("IMAGE", ignoreCase = true)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewingAsset = asset }
                        .testTag("gallery_asset_${asset.id}")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Thumbnail Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    when {
                                        isPdf -> UrgentRed.copy(alpha = 0.85f)
                                        isImage -> Emerald800
                                        else -> Gold600
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isImage && asset.fileUrl.isNotBlank()) {
                                val decodedBitmap = remember(asset.fileUrl) {
                                    if (asset.fileUrl.startsWith("data:") || asset.fileUrl.contains("base64,")) {
                                        FilePickerUtils.decodeBase64Bitmap(asset.fileUrl)
                                    } else null
                                }
                                if (decodedBitmap != null) {
                                    Image(
                                        bitmap = decodedBitmap.asImageBitmap(),
                                        contentDescription = asset.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    AsyncImage(
                                        model = asset.fileUrl,
                                        contentDescription = asset.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when {
                                            isPdf -> Icons.Default.PictureAsPdf
                                            isImage -> Icons.Default.Image
                                            else -> Icons.Default.Description
                                        },
                                        contentDescription = asset.fileType,
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ".${asset.fileExtension.uppercase()}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Format Badge
                            Surface(
                                shape = RoundedCornerShape(topStart = 8.dp),
                                color = Emerald900,
                                modifier = Modifier.align(Alignment.BottomEnd)
                            ) {
                                Text(
                                    text = asset.fileSize,
                                    color = Gold300,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Info
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = asset.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = asset.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Admin Delete Option
                            if (currentRole == UserRole.ADMIN) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { assetToDelete = asset },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = UrgentRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Asset Viewer Dialog (Enhanced In-App Viewer & Device Intent Executor) ---
    viewingAsset?.let { asset ->
        val isImage = asset.fileType.equals("IMAGE", ignoreCase = true) || asset.fileExtension.lowercase() in listOf("jpg", "jpeg", "png", "webp")
        if (isImage) {
            EnhancedImageViewerDialog(
                imageUrl = asset.fileUrl,
                title = asset.title,
                category = asset.category,
                date = asset.uploadedDate,
                description = asset.description,
                onDismiss = { viewingAsset = null }
            )
        } else {
            EnhancedPdfDocumentViewerDialog(
                title = asset.title,
                author = "Alnoor Mosque Archive",
                category = asset.category,
                description = asset.description,
                contentPreview = asset.description,
                fileUrl = asset.fileUrl,
                fileSize = asset.fileSize,
                pagesCount = 1,
                onDismiss = { viewingAsset = null }
            )
        }
    }

    // --- Admin Upload File Dialog (Images, PDFs, Documents with Native File Picker) ---
    if (showUploadDialog) {
        var uploadTitle by remember(selectedPickedFile) {
            mutableStateOf(selectedPickedFile?.name?.substringBeforeLast('.') ?: "")
        }
        var uploadCategory by remember { mutableStateOf("Community Events") }
        var uploadDirectUrl by remember { mutableStateOf("") }
        var uploadFileType by remember(selectedPickedFile) {
            mutableStateOf(selectedPickedFile?.categoryType ?: "IMAGE")
        }
        var uploadExt by remember(selectedPickedFile) {
            mutableStateOf(selectedPickedFile?.extension ?: "jpg")
        }
        var uploadSize by remember(selectedPickedFile) {
            mutableStateOf(selectedPickedFile?.sizeString ?: "2.8 MB")
        }
        var uploadDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Admin: Upload File to Gallery", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Chosen file preview & Native File Picker button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Emerald900,
                        modifier = Modifier.fillMaxWidth()
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
                                    imageVector = when (uploadFileType) {
                                        "IMAGE" -> Icons.Default.Image
                                        "PDF" -> Icons.Default.PictureAsPdf
                                        else -> Icons.Default.Description
                                    },
                                    contentDescription = null,
                                    tint = Gold300
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = selectedPickedFile?.name ?: "No file picked yet",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Size: $uploadSize • .$uploadExt",
                                        color = Gold400,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            TextButton(onClick = { filePickerLauncher.launch("*/*") }) {
                                Text("Browse", color = Gold300, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("IMAGE", "PDF", "DOCUMENT").forEach { type ->
                            FilterChip(
                                selected = uploadFileType == type,
                                onClick = {
                                    uploadFileType = type
                                    uploadExt = when (type) {
                                        "IMAGE" -> "jpg"
                                        "PDF" -> "pdf"
                                        else -> "docx"
                                    }
                                },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = { Text("File Title / Caption *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadDirectUrl,
                        onValueChange = { uploadDirectUrl = it },
                        label = { Text("Online Image / Media URL (Optional)") },
                        placeholder = { Text("https://... or Google Drive URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadCategory,
                        onValueChange = { uploadCategory = it },
                        label = { Text("Category / Album") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadDesc,
                        onValueChange = { uploadDesc = it },
                        label = { Text("File Description / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadTitle.isNotBlank()) {
                            val resolvedFileUrl = if (uploadDirectUrl.isNotBlank()) {
                                uploadDirectUrl.trim()
                            } else {
                                selectedPickedFile?.cloudPayloadUrl
                                    ?: selectedPickedFile?.localCachedPath
                                    ?: pickedFileUri?.toString()
                                    ?: "https://images.unsplash.com/photo-1542816417-0983c9c9ad53?w=600&auto=format&fit=crop&q=80"
                            }
                            val resolvedDesc = uploadDesc.ifBlank {
                                selectedPickedFile?.textPreview ?: "Official Islamic asset uploaded by Admin."
                            }

                            try {
                                onAddAsset(
                                    GalleryAsset(
                                        id = UUID.randomUUID().toString(),
                                        title = uploadTitle.trim(),
                                        category = uploadCategory.ifBlank { "Mosque Highlights" },
                                        fileType = uploadFileType,
                                        fileExtension = uploadExt,
                                        fileSize = uploadSize,
                                        uploadedDate = "Just now",
                                        description = resolvedDesc,
                                        fileUrl = resolvedFileUrl,
                                        thumbnailUrl = if (uploadFileType == "IMAGE") resolvedFileUrl else ""
                                    )
                                )
                                showUploadDialog = false
                                selectedPickedFile = null
                                pickedFileUri = null
                                Toast.makeText(context, "Uploaded $uploadTitle to Gallery", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Upload error: ${e.localizedMessage ?: "Unknown"}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Upload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- Admin Delete Confirmation ---
    assetToDelete?.let { asset ->
        AlertDialog(
            onDismissRequest = { assetToDelete = null },
            title = { Text("Delete Asset?") },
            text = { Text("Are you sure you want to permanently delete '${asset.title}' from the Gallery?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAsset(asset.id)
                        assetToDelete = null
                        Toast.makeText(context, "Deleted asset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { assetToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
