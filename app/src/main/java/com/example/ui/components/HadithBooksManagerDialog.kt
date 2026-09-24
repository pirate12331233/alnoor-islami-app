package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.BookDownloadState
import com.example.data.repository.HadithBookInfo
import com.example.data.repository.HadithDownloadManager
import com.example.data.repository.HadithRepository
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import kotlinx.coroutines.launch

@Composable
fun HadithBooksManagerDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val downloadState by HadithDownloadManager.downloadState.collectAsState()

    var totalOfflineCount by remember { mutableIntStateOf(15152) }
    val bookCounts = remember { mutableStateMapOf<String, Int>() }
    val bookDownloaded = remember { mutableStateMapOf<String, Boolean>() }

    fun refreshCounts() {
        scope.launch {
            totalOfflineCount = HadithRepository.getOfflineTotalCount(context)
            HadithDownloadManager.SUNNI_HADITH_BOOKS.forEach { book ->
                val count = HadithDownloadManager.getBookCount(context, book.key)
                bookCounts[book.key] = count
                bookDownloaded[book.key] = count > 0 || book.isBundled
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshCounts()
    }

    LaunchedEffect(downloadState) {
        if (downloadState is BookDownloadState.Success) {
            refreshCounts()
            Toast.makeText(context, "Book downloaded successfully for offline reading!", Toast.LENGTH_SHORT).show()
        } else if (downloadState is BookDownloadState.Error) {
            val err = (downloadState as BookDownloadState.Error).errorMessage
            Toast.makeText(context, "Download failed: $err", Toast.LENGTH_LONG).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF041C15)),
            border = BorderStroke(1.dp, Gold500.copy(alpha = 0.5f)),
            modifier = modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Gold500.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Gold400),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LibraryBooks,
                                    contentDescription = null,
                                    tint = Gold300,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Offline Hadith Library",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold300
                            )
                            Text(
                                text = "مجموعہ کتب احادیث مبارکہ",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Emerald900.copy(alpha = 0.9f),
                    border = BorderStroke(0.5.dp, Gold400.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "OFFLINE STORAGE STATUS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold400,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$totalOfflineCount+ Hadiths Stored Offline",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.25f),
                            border = BorderStroke(0.5.dp, Color(0xFF34D399))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "100% Offline",
                                    fontSize = 11.sp,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sahih al-Bukhari & Sahih Muslim are permanently bundled in the APK. You can download the remaining authentic Sunni books below for complete offline access:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // List of 6 Sihah Sittah Books
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(HadithDownloadManager.SUNNI_HADITH_BOOKS) { book ->
                        BookItemCard(
                            book = book,
                            isDownloaded = bookDownloaded[book.key] ?: book.isBundled,
                            hadithCount = bookCounts[book.key] ?: if (book.isBundled) book.approximateCount else 0,
                            downloadState = downloadState,
                            onDownload = {
                                scope.launch {
                                    HadithDownloadManager.downloadBook(context, book.key)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    HadithDownloadManager.deleteBook(context, book.key)
                                    refreshCounts()
                                    Toast.makeText(context, "${book.titleEnglish} removed from offline storage.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Gold500,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Done / پڑھائی جاری رکھیں", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun BookItemCard(
    book: HadithBookInfo,
    isDownloaded: Boolean,
    hadithCount: Int,
    downloadState: BookDownloadState,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val isThisDownloading = downloadState is BookDownloadState.Downloading && downloadState.bookKey == book.key
    val currentProgress = if (isThisDownloading) (downloadState as BookDownloadState.Downloading).progress else 0f
    val currentStatusMsg = if (isThisDownloading) (downloadState as BookDownloadState.Downloading).statusMessage else ""

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (book.isBundled) Color(0xFF073024) else Color(0xFF092A20)
        ),
        border = BorderStroke(
            1.dp,
            if (book.isBundled) Gold500.copy(alpha = 0.5f)
            else if (isDownloaded) Color(0xFF10B981).copy(alpha = 0.4f)
            else Color.White.copy(alpha = 0.12f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = book.titleEnglish,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (book.isBundled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Gold400.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, Gold400)
                            ) {
                                Text(
                                    text = "APK BUNDLED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold300,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = book.titleArabic,
                            fontSize = 12.sp,
                            color = Gold300
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•  ~${book.approximateCount} Hadiths",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Action Button
                if (book.isBundled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, Color(0xFF34D399))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Installed",
                                fontSize = 11.sp,
                                color = Color(0xFF34D399),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (isDownloaded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, Color(0xFF34D399))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Offline ($hadithCount)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete book",
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onDownload,
                        enabled = !isThisDownloading,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Gold400),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold300),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        if (isThisDownloading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Gold400, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saving...", fontSize = 11.sp)
                        } else {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Get (${book.estimatedDownloadMb})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Download Progress Bar
            AnimatedVisibility(visible = isThisDownloading) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Gold400,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentStatusMsg,
                            fontSize = 10.sp,
                            color = Gold300
                        )
                        Text(
                            text = "${(currentProgress * 100).toInt()}%",
                            fontSize = 10.sp,
                            color = Gold400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
