package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HadithData
import com.example.data.repository.HadithRepository
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.util.HadithImageGenerator
import kotlinx.coroutines.launch

@Composable
fun DailyHadithCard(
    modifier: Modifier = Modifier,
    onNavigateToLibrary: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hadith by remember { mutableStateOf<HadithData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSavingImage by remember { mutableStateOf(false) }
    var isSharingImage by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Both, 1 = Urdu, 2 = English

    LaunchedEffect(Unit) {
        isLoading = true
        hadith = HadithRepository.getDailyHadith(context)
        isLoading = false
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF072A1F) // Premium Deep Islamic Emerald
        ),
        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_hadith_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            // Header: Icon + Title + Refresh / Save
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
                            .background(Gold500.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "Hadith",
                            tint = Gold400,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Daily Hadith",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Gold500.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "Sunnah",
                                    color = Gold300,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = hadith?.book ?: "Authentic Sunni Hadith",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                // Action Buttons: Save as Image & Share
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            hadith?.let { currentHadith ->
                                scope.launch {
                                    isSharingImage = true
                                    HadithImageGenerator.shareHadithAsImage(context, currentHadith)
                                    isSharingImage = false
                                }
                            }
                        },
                        enabled = hadith != null && !isSharingImage,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("hadith_share_button")
                    ) {
                        if (isSharingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Gold400,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Hadith Image",
                                tint = Gold300,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            hadith?.let { currentHadith ->
                                scope.launch {
                                    isSavingImage = true
                                    val uri = HadithImageGenerator.saveHadithCardAsImage(context, currentHadith)
                                    isSavingImage = false
                                    if (uri != null) {
                                        saveSuccessMessage = "Saved to Gallery / Pictures!"
                                        Toast.makeText(context, "Hadith card image saved to Gallery!", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Saved to device storage.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = hadith != null && !isSavingImage,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("hadith_save_image_button")
                    ) {
                        if (isSavingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Gold400,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = "Save as Image",
                                tint = Gold400,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Gold400, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Loading Daily Hadith...",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else if (hadith != null) {
                val current = hadith!!

                // Chapter & Narrator Meta Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = current.chapter,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold300,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Emerald900.copy(alpha = 0.85f),
                        border = BorderStroke(0.5.dp, Gold500.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = current.reference.ifBlank { "Hadith #${current.hadithNumber}" },
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Narrator
                Text(
                    text = current.narrator,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Arabic Text Container
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF041912),
                    border = BorderStroke(0.8.dp, Gold500.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = current.arabicText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 19.sp,
                                lineHeight = 32.sp
                            ),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Serif,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Translation View Filter Tabs: Both / Urdu / English
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val tabs = listOf("Both", "اردو (Urdu)", "English")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = activeTab == index
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Gold500 else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clickable { activeTab = index }
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFF072A1F) else Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Urdu Translation Section
                if (activeTab == 0 || activeTab == 1) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.04f),
                        border = BorderStroke(0.5.dp, Gold500.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Gold500.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "اردو ترجمہ",
                                        color = Gold300,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = current.urduTranslation,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 24.sp
                                ),
                                color = Color(0xFFE8F5E9),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (activeTab == 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // English Translation Section
                if (activeTab == 0 || activeTab == 2) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.04f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "English Translation",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = current.englishTranslation,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    lineHeight = 20.sp
                                ),
                                color = Color.White.copy(alpha = 0.92f),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Saved Success Toast Feedback
                AnimatedVisibility(visible = saveSuccessMessage != null) {
                    saveSuccessMessage?.let { msg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = Gold400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = msg,
                                color = Gold300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar: "Save Card as Image" button + "Share Image" button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                isSavingImage = true
                                val uri = HadithImageGenerator.saveHadithCardAsImage(context, current)
                                isSavingImage = false
                                if (uri != null) {
                                    saveSuccessMessage = "Saved to Gallery / Pictures!"
                                    Toast.makeText(context, "Hadith image saved to device Gallery!", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_hadith_image_full_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSavingImage) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Emerald900, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saving...", fontSize = 12.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save as Image", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isSharingImage = true
                                HadithImageGenerator.shareHadithAsImage(context, current)
                                isSharingImage = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_hadith_image_full_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Gold400.copy(alpha = 0.6f))
                    ) {
                        if (isSharingImage) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Gold400, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sharing...", fontSize = 12.sp, color = Gold300)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Gold400,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Card", fontSize = 12.5.sp, color = Gold300, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
