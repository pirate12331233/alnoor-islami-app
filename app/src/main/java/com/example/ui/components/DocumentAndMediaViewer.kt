package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.util.FilePickerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-fidelity, full-screen interactive In-App Document & PDF Reader.
 * Renders authentic PDF pages using Android's native PdfRenderer, and enables viewing
 * inside any external app (Adobe, Drive) using FileProvider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedPdfDocumentViewerDialog(
    title: String,
    author: String = "Alnoor Islamic Center",
    category: String = "Islamic Publications",
    description: String = "",
    contentPreview: String = "",
    fileUrl: String = "",
    fileSize: String = "2.4 MB",
    pagesCount: Int = 1,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fontSizeDelta by remember { mutableIntStateOf(0) }
    var readingMode by remember { mutableStateOf("PDF Pages") } // "PDF Pages", "Text Reader"
    var readingTheme by remember { mutableStateOf("Emerald Night") } // "Emerald Night", "Sepia", "Day Paper"

    var renderedPdfPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoadingPages by remember { mutableStateOf(true) }

    // Load PDF pages natively in background
    LaunchedEffect(fileUrl) {
        isLoadingPages = true
        withContext(Dispatchers.IO) {
            val pages = FilePickerUtils.renderPdfPages(
                context = context,
                fileUrl = fileUrl,
                title = title,
                author = author,
                category = category,
                description = description,
                contentPreview = contentPreview
            )
            withContext(Dispatchers.Main) {
                renderedPdfPages = pages
                isLoadingPages = false
            }
        }
    }

    val (bgThemeColor, textThemeColor, cardBgColor) = when (readingTheme) {
        "Sepia" -> Triple(Color(0xFFFBF0D9), Color(0xFF3E2723), Color(0xFFF4ECD8))
        "Day Paper" -> Triple(Color(0xFFF9FAFB), Color(0xFF111827), Color(0xFFFFFFFF))
        else -> Triple(Emerald900, Color.White, Emerald800)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(bgThemeColor),
            color = bgThemeColor
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Top App Bar ---
                Surface(
                    color = Emerald900,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
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
                                        contentDescription = null,
                                        tint = Emerald900,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "$author • $category • $fileSize",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gold300,
                                        maxLines = 1
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_pdf_viewer_button")
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Controls Row (Mode selector & Zoom)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = readingMode == "PDF Pages",
                                    onClick = { readingMode = "PDF Pages" },
                                    label = { Text("Original PDF Pages", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Gold500,
                                        selectedLabelColor = Emerald900,
                                        containerColor = Emerald800,
                                        labelColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = readingMode == "Text Reader",
                                    onClick = { readingMode = "Text Reader" },
                                    label = { Text("Text Reader", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Gold500,
                                        selectedLabelColor = Emerald900,
                                        containerColor = Emerald800,
                                        labelColor = Color.White
                                    )
                                )
                            }

                            if (readingMode == "Text Reader") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (fontSizeDelta > -4) fontSizeDelta-- },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomOut, contentDescription = "Smaller", tint = Gold300)
                                    }
                                    Text(
                                        "${14 + fontSizeDelta}sp",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { if (fontSizeDelta < 8) fontSizeDelta++ },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomIn, contentDescription = "Larger", tint = Gold300)
                                    }
                                }
                            }
                        }
                    }
                }

                // --- Content Body ---
                if (readingMode == "PDF Pages") {
                    if (isLoadingPages) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Gold500)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Rendering PDF Pages...", color = Gold300, fontSize = 13.sp)
                            }
                        }
                    } else if (renderedPdfPages.isNotEmpty()) {
                        // Rendered PDF Page List
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(renderedPdfPages) { index, pageBitmap ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            bitmap = pageBitmap.asImageBitmap(),
                                            contentDescription = "Page ${index + 1}",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Surface(
                                            color = Emerald900,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Page ${index + 1} of ${renderedPdfPages.size}",
                                                color = Gold300,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Fallback text view if no pages
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .verticalScroll(scrollState)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textThemeColor
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (contentPreview.isNotBlank()) contentPreview else "Assalamu Alaikum.\nTap 'Open in PDF App' below to view and download this publication.",
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp,
                                        color = textThemeColor
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Text Reader Mode
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (readingTheme == "Emerald Night") Gold300 else Emerald900,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = title,
                                    fontSize = (18 + fontSizeDelta).sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textThemeColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Author / Compiler: $author",
                                    fontSize = (13 + fontSizeDelta).sp,
                                    color = if (readingTheme == "Emerald Night") Gold400 else Emerald800,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = Gold500.copy(alpha = 0.5f), thickness = 1.5.dp)
                                Spacer(modifier = Modifier.height(14.dp))

                                if (description.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (readingTheme == "Emerald Night") Emerald900 else Emerald100,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "PUBLICATION SUMMARY",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (readingTheme == "Emerald Night") Gold300 else Emerald900
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = description,
                                                fontSize = (13 + fontSizeDelta).sp,
                                                lineHeight = (20 + fontSizeDelta).sp,
                                                color = textThemeColor
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                val fullText = if (contentPreview.isNotBlank()) {
                                    contentPreview
                                } else {
                                    "Assalamu Alaikum wa Rahmatullahi wa Barakatuh.\n\n" +
                                            "Welcome to '$title' published by Alnoor Islamic Center.\n\n" +
                                            "This document is distributed for continuous charity (Sadaqah Jariyah), religious learning, and spiritual reflection."
                                }

                                Text(
                                    text = fullText,
                                    fontSize = (14 + fontSizeDelta).sp,
                                    lineHeight = (22 + fontSizeDelta).sp,
                                    color = textThemeColor,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold500.copy(alpha = 0.2f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Alnoor Digital Library  •  Official Publication",
                                        fontSize = 11.sp,
                                        color = if (readingTheme == "Emerald Night") Gold300 else Emerald900,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- Bottom Action Bar (Launch External PDF App, Share) ---
                Surface(
                    color = Emerald900,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    FilePickerUtils.openFileWithIntent(
                                        context = context,
                                        fileUrl = fileUrl,
                                        fileType = "PDF",
                                        title = title,
                                        author = author,
                                        category = category,
                                        description = description,
                                        contentPreview = contentPreview,
                                        scope = coroutineScope
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold500,
                                    contentColor = Emerald900
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_external_pdf_app_button")
                            ) {
                                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open in PDF App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    FilePickerUtils.shareFile(
                                        context = context,
                                        fileUrl = fileUrl,
                                        fileType = "PDF",
                                        title = title,
                                        author = author,
                                        description = description,
                                        contentPreview = contentPreview,
                                        scope = coroutineScope
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Emerald800,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.testTag("share_pdf_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen interactive Zoomable & Pannable Image Viewer with device Gallery intent support.
 */
@Composable
fun EnhancedImageViewerDialog(
    imageUrl: String,
    title: String,
    category: String = "Gallery Photo",
    date: String = "",
    description: String = "",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Decode in-memory Bitmap if Base64
    val decodedBitmap = remember(imageUrl) {
        if (imageUrl.startsWith("data:") || imageUrl.contains("base64,")) {
            FilePickerUtils.decodeBase64Bitmap(imageUrl)
        } else {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            // --- Top App Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Gold500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Emerald900, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        if (category.isNotBlank() || date.isNotBlank()) {
                            Text(
                                text = "$category • $date",
                                color = Gold300,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_image_viewer_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // --- Pinch-to-Zoom & Pannable Image Canvas ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 70.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale == 1f) {
                                offset = Offset.Zero
                            } else {
                                offset += pan
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (decodedBitmap != null) {
                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                } else {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                    )
                }
            }

            // --- Bottom Floating Bar (Open in Gallery, Share, Reset Zoom) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            FilePickerUtils.openFileWithIntent(
                                context = context,
                                fileUrl = imageUrl,
                                fileType = "IMAGE",
                                title = title,
                                description = description,
                                scope = coroutineScope
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gold500,
                            contentColor = Emerald900
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_external_gallery_button")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open in Photos/Gallery", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            FilePickerUtils.shareFile(
                                context = context,
                                fileUrl = imageUrl,
                                fileType = "IMAGE",
                                title = title,
                                description = description,
                                scope = coroutineScope
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald800,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("share_image_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    if (scale > 1.05f) {
                        Button(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Reset", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
