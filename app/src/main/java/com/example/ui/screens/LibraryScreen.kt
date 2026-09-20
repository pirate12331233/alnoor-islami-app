package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IslamicBook
import com.example.data.model.UserGuideProvider
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
fun LibraryScreen(
    books: List<IslamicBook>,
    currentRole: UserRole,
    onToggleBookmark: (String) -> Unit,
    onAddBook: (IslamicBook) -> Unit,
    onUpdateBook: (IslamicBook) -> Unit,
    onDeleteBook: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    val typeFilters = listOf("All", "PDF Documents", "Images", "Documents")

    var viewingBook by remember { mutableStateOf<IslamicBook?>(null) }
    var editingBook by remember { mutableStateOf<IslamicBook?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<IslamicBook?>(null) }

    var selectedPickedFile by remember { mutableStateOf<PickedFileInfo?>(null) }
    var pickedFileUri by remember { mutableStateOf<Uri?>(null) }

    // --- Native Device File Picker Launcher ---
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedFileUri = uri
            val fileInfo = FilePickerUtils.extractFileInfo(context, uri)
            selectedPickedFile = fileInfo
            showUploadDialog = true
            Toast.makeText(context, "Selected: ${fileInfo.name}", Toast.LENGTH_SHORT).show()
        }
    }

    val filteredBooks = books.filter { book ->
        val matchesType = when (selectedTypeFilter) {
            "PDF Documents" -> book.fileType.equals("PDF", ignoreCase = true)
            "Images" -> book.fileType.equals("IMAGE", ignoreCase = true)
            "Documents" -> !book.fileType.equals("PDF", ignoreCase = true) && !book.fileType.equals("IMAGE", ignoreCase = true)
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.author.contains(searchQuery, ignoreCase = true) ||
                book.category.contains(searchQuery, ignoreCase = true)
        matchesType && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            if (currentRole == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = {
                        // Open native file picker directly
                        filePickerLauncher.launch("*/*")
                    },
                    containerColor = Gold500,
                    contentColor = Emerald900,
                    modifier = Modifier.testTag("upload_library_doc_fab")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Upload Document/Image")
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
            // --- 1. Top Header Banner ---
            item {
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
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = "PDFs & Images",
                                        tint = Emerald900,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Islamic Digital Library",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "PDF Books & Images (${books.size} files)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gold300,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (currentRole == UserRole.ADMIN) {
                                Spacer(modifier = Modifier.width(8.dp))
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
                                    .testTag("admin_upload_library_native_button")
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload File (Native File Picker)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search PDF documents, Dua booklets, Tajweed charts...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("library_search_input")
                )
            }

            // Type Filters (PDF Documents vs Images vs Documents)
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(typeFilters) { filter ->
                        FilterChip(
                            selected = selectedTypeFilter == filter,
                            onClick = { selectedTypeFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald800,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Pinned Official Trust Publication: Member User Guide & Manual
            item {
                val officialGuide = books.find { it.id == "bk-official-user-guide" }
                    ?: UserGuideProvider.OFFICIAL_USER_GUIDE_BOOK
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald900),
                    border = BorderStroke(1.5.dp, Gold500),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewingBook = officialGuide }
                        .testTag("pinned_official_user_guide_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Gold500
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Emerald900,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "OFFICIAL TRUST PUBLICATION",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = Emerald900
                                    )
                                }
                            }

                            Text(
                                text = "v3.2 • PDF Manual",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gold300,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Gold500),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = "User Manual",
                                    tint = Emerald900,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Member User Guide & Operation Manual",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Alnoor Trust Management Board • 18 Pages",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gold300
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Complete handbook for Live streams, Prayer timetables, Khatam bookings, Quran reading, PDF publications, and Helpline.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewingBook = officialGuide },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold500,
                                    contentColor = Emerald900
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Read In-App Guide", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    FilePickerUtils.shareFile(
                                        context = context,
                                        fileUrl = officialGuide.fileUrl,
                                        fileType = "PDF",
                                        title = officialGuide.title,
                                        author = officialGuide.author,
                                        description = officialGuide.description,
                                        contentPreview = officialGuide.contentPreview
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Gold400),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Gold300, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share PDF", fontSize = 12.sp, color = Gold300)
                            }
                        }
                    }
                }
            }

            // Document List Cards
            items(filteredBooks) { book ->
                val isPdf = book.fileType.equals("PDF", ignoreCase = true)
                val isImage = book.fileType.equals("IMAGE", ignoreCase = true)
                val isOfficialGuide = book.id == "bk-official-user-guide"
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOfficialGuide) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isOfficialGuide) BorderStroke(1.5.dp, Gold500) else null,
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewingBook = book }
                        .testTag("book_card_${book.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // File Icon
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isPdf -> UrgentRed
                                        isImage -> Emerald800
                                        else -> Gold600
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isPdf -> Icons.Default.PictureAsPdf
                                    isImage -> Icons.Default.Image
                                    else -> Icons.Default.Description
                                },
                                contentDescription = book.fileType,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when {
                                        isPdf -> UrgentRed.copy(alpha = 0.15f)
                                        isImage -> Emerald800.copy(alpha = 0.15f)
                                        else -> Gold600.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = "${book.fileType.uppercase()} DOC",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isPdf -> UrgentRed
                                            isImage -> Emerald800
                                            else -> Gold600
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${book.fileSize} • ${book.pagesCount} pg",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${book.author} • ${book.category}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        // Actions (Admin: Edit/Delete, User: View/Bookmark)
                        if (currentRole == UserRole.ADMIN) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { editingBook = book }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Gold600, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { bookToDelete = book }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UrgentRed, modifier = Modifier.size(20.dp))
                                }
                            }
                        } else {
                            IconButton(onClick = { onToggleBookmark(book.id) }) {
                                Icon(
                                    imageVector = if (book.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (book.isBookmarked) Gold600 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Viewer Dialog (Enhanced In-App Reader & External Viewer) ---
    viewingBook?.let { book ->
        val isImage = book.fileType.equals("IMAGE", ignoreCase = true) || book.fileUrl.endsWith(".jpg", ignoreCase = true) || book.fileUrl.endsWith(".png", ignoreCase = true)
        if (isImage) {
            EnhancedImageViewerDialog(
                imageUrl = book.fileUrl,
                title = book.title,
                category = "${book.author} • ${book.category}",
                description = book.description,
                onDismiss = { viewingBook = null }
            )
        } else {
            EnhancedPdfDocumentViewerDialog(
                title = book.title,
                author = book.author,
                category = book.category,
                description = book.description,
                contentPreview = book.contentPreview,
                fileUrl = book.fileUrl,
                fileSize = book.fileSize,
                pagesCount = book.pagesCount,
                onDismiss = { viewingBook = null }
            )
        }
    }

    // --- Admin Upload Document Dialog (Native File Picker Integrated) ---
    if (showUploadDialog) {
        var uploadTitle by remember(selectedPickedFile) {
            mutableStateOf(selectedPickedFile?.name?.substringBeforeLast('.') ?: "")
        }
        var uploadAuthor by remember { mutableStateOf("") }
        var uploadCategory by remember { mutableStateOf("Khatam Guide & Duas") }
        var uploadFileType by remember(selectedPickedFile) {
            mutableStateOf(selectedPickedFile?.categoryType ?: "PDF")
        }
        var uploadPages by remember { mutableStateOf("15") }
        var uploadSize by remember(selectedPickedFile) {
            mutableStateOf(selectedPickedFile?.sizeString ?: "3.5 MB")
        }
        var uploadExternalUrl by remember { mutableStateOf("") }
        var uploadDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Admin: Upload Library File", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Chosen file card & Native File Picker button
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
                                        text = "Size: $uploadSize • Type: $uploadFileType",
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
                        listOf("PDF", "IMAGE", "DOCUMENT").forEach { type ->
                            FilterChip(
                                selected = uploadFileType == type,
                                onClick = { uploadFileType = type },
                                label = { Text(if (type == "PDF") "PDF Document" else if (type == "IMAGE") "Image" else "General Doc") }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = { Text("Title *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadExternalUrl,
                        onValueChange = { uploadExternalUrl = it },
                        label = { Text("Google Drive / Cloud PDF URL (Optional)") },
                        placeholder = { Text("https://drive.google.com/...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadAuthor,
                        onValueChange = { uploadAuthor = it },
                        label = { Text("Author / Department") },
                        placeholder = { Text("e.g. Alnoor Research Bureau") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadCategory,
                        onValueChange = { uploadCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadPages,
                        onValueChange = { uploadPages = it },
                        label = { Text("Pages / Slides Count") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadDesc,
                        onValueChange = { uploadDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadTitle.isNotBlank()) {
                            val resolvedFileUrl = if (uploadExternalUrl.isNotBlank()) {
                                uploadExternalUrl.trim()
                            } else {
                                selectedPickedFile?.localCachedPath
                                    ?: selectedPickedFile?.cloudPayloadUrl
                                    ?: pickedFileUri?.toString()
                                    ?: "https://example.com/docs/${UUID.randomUUID()}.${uploadFileType.lowercase()}"
                            }
                            val resolvedPreview = selectedPickedFile?.textPreview
                                ?: uploadDesc.ifBlank { "Document preview available. Tap Open Viewer to read complete $uploadFileType." }

                            try {
                                onAddBook(
                                    IslamicBook(
                                        id = UUID.randomUUID().toString(),
                                        title = uploadTitle.trim(),
                                        author = uploadAuthor.ifBlank { "Alnoor Research Bureau" },
                                        category = uploadCategory.ifBlank { "Islamic Publication" },
                                        pagesCount = uploadPages.toIntOrNull() ?: 1,
                                        language = "Arabic / Urdu / English",
                                        description = uploadDesc.ifBlank { "Official Islamic publication uploaded by Admin." },
                                        contentPreview = resolvedPreview,
                                        fileType = uploadFileType,
                                        fileUrl = resolvedFileUrl,
                                        fileSize = uploadSize
                                    )
                                )
                                showUploadDialog = false
                                selectedPickedFile = null
                                pickedFileUri = null
                                uploadExternalUrl = ""
                                Toast.makeText(context, "Uploaded $uploadTitle to Library", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Upload error: ${e.localizedMessage ?: "Unknown"}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Please enter a document title", Toast.LENGTH_SHORT).show()
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

    // --- Admin Edit Document Dialog ---
    editingBook?.let { book ->
        var editTitle by remember { mutableStateOf(book.title) }
        var editAuthor by remember { mutableStateOf(book.author) }
        var editCategory by remember { mutableStateOf(book.category) }
        var editFileType by remember { mutableStateOf(book.fileType) }
        var editFileUrl by remember { mutableStateOf(book.fileUrl) }
        var editPages by remember { mutableStateOf(book.pagesCount.toString()) }
        var editDesc by remember { mutableStateOf(book.description) }

        AlertDialog(
            onDismissRequest = { editingBook = null },
            title = { Text("Admin: Edit Document Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PDF", "IMAGE", "DOCUMENT").forEach { type ->
                            FilterChip(
                                selected = editFileType.equals(type, ignoreCase = true),
                                onClick = { editFileType = type },
                                label = { Text(if (type == "PDF") "PDF Document" else if (type == "IMAGE") "Image" else "Doc") }
                            )
                        }
                    }
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editFileUrl, onValueChange = { editFileUrl = it }, label = { Text("Google Drive / Cloud PDF URL") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editAuthor, onValueChange = { editAuthor = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editCategory, onValueChange = { editCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPages, onValueChange = { editPages = it }, label = { Text("Pages") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editDesc, onValueChange = { editDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateBook(
                            book.copy(
                                title = editTitle,
                                author = editAuthor,
                                category = editCategory,
                                fileType = editFileType,
                                fileUrl = editFileUrl,
                                pagesCount = editPages.toIntOrNull() ?: book.pagesCount,
                                description = editDesc
                            )
                        )
                        editingBook = null
                        Toast.makeText(context, "Updated $editTitle", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBook = null }) { Text("Cancel") }
            }
        )
    }

    // --- Admin Delete Confirmation ---
    bookToDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text("Delete Document?") },
            text = { Text("Are you sure you want to permanently remove '${book.title}' from the Islamic Library?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBook(book.id)
                        bookToDelete = null
                        Toast.makeText(context, "Deleted document", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
