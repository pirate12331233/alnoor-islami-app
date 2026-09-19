package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

/**
 * Action Card with customizable display title & subtitle.
 * Shifts the Hadith block into an expandable sub-section that reveals when the user clicks the card.
 */
@Composable
fun DailyHadithCard(
    title: String = "Daily Hadith Shareef (حدیث شریف)",
    subtitle: String = "Tap to read authentic Bukhari & Muslim Hadith in Arabic, Urdu & English with card generator",
    isHiddenFromMembers: Boolean = false,
    initiallyExpanded: Boolean = false,
    onNavigateToLibrary: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    var hadith by remember { mutableStateOf<HadithData?>(null) }
    var hadithIndex by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var isSavingImage by remember { mutableStateOf(false) }
    var isSharingImage by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Both, 1 = Urdu, 2 = English

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "hadith_arrow_rotation"
    )

    LaunchedEffect(Unit) {
        isLoading = true
        val daily = HadithRepository.getDailyHadith(context)
        hadith = daily
        val foundIdx = HadithRepository.CURATED_SUNNI_AHADITH.indexOfFirst { it.id == daily.id }
        if (foundIdx >= 0) hadithIndex = foundIdx
        isLoading = false
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) Color(0xFF06231A) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isHiddenFromMembers) Color(0xFFF59E0B).copy(alpha = 0.6f)
            else if (isExpanded) Gold500.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 6.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (isExpanded) 6.dp else 2.dp, RoundedCornerShape(20.dp))
            .animateContentSize()
            .testTag("action_card_hadith")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // --- HEADER ROW (CLICKABLE TO EXPAND / COLLAPSE) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 15.dp)
                    .testTag("hadith_card_header_toggle"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large 52dp Icon Surface matching other action cards
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isExpanded) Gold500.copy(alpha = 0.22f) else Emerald900,
                    border = BorderStroke(1.dp, Gold500.copy(alpha = 0.4f)),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = title,
                            tint = Gold400,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title & Subtitle Column (Customizable name from Action Card Config)
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
                            color = if (isExpanded) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 21.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (isHiddenFromMembers) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Hidden",
                                        fontSize = 9.sp,
                                        color = Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isExpanded && hadith != null) {
                            "${hadith!!.book} • ${hadith!!.chapter}"
                        } else {
                            subtitle
                        },
                        fontSize = 12.5.sp,
                        color = if (isExpanded) Gold300.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.5.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Expand / Collapse Action Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isExpanded) Gold500.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = if (isExpanded) BorderStroke(1.dp, Gold400.copy(alpha = 0.45f)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "Hide" else "Read",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) Gold300 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse Hadith" else "Expand Hadith",
                            tint = if (isExpanded) Gold300 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(arrowRotation)
                        )
                    }
                }
            }

            // --- SUB-SECTION (REVEALED WHEN USER CLICKS THE CARD) ---
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("hadith_sub_section_content")
                ) {
                    HorizontalDivider(
                        color = Gold500.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Gold400, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Loading Authentic Hadith...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else if (hadith != null) {
                        val current = hadith!!

                        // Sub-section Header & Tool Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Authentic Sunnah Tag
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold500.copy(alpha = 0.22f),
                                    border = BorderStroke(0.8.dp, Gold400.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Gold300,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = current.grade.ifBlank { "Sahih Hadith" },
                                            color = Gold300,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Emerald900.copy(alpha = 0.85f),
                                    border = BorderStroke(0.5.dp, Gold500.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = current.reference.ifBlank { "Hadith #${current.hadithNumber}" },
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Quick Tool Icon Buttons: Next Hadith, Copy Text, Share, Save Image
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Cycle to Next Hadith
                                IconButton(
                                    onClick = {
                                        val total = HadithRepository.getTotalHadithCount()
                                        hadithIndex = (hadithIndex + 1) % total
                                        hadith = HadithRepository.getHadithByIndex(hadithIndex)
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("btn_next_hadith")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next Hadith",
                                        tint = Gold400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Copy to Clipboard
                                IconButton(
                                    onClick = {
                                        copyHadithToClipboard(context, current)
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("btn_copy_hadith")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Hadith Text",
                                        tint = Gold300,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Share as Image
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            isSharingImage = true
                                            HadithImageGenerator.shareHadithAsImage(context, current)
                                            isSharingImage = false
                                        }
                                    },
                                    enabled = !isSharingImage,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("hadith_share_button")
                                ) {
                                    if (isSharingImage) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Gold400,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share Hadith Image",
                                            tint = Gold300,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Save to Gallery
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            isSavingImage = true
                                            val uri = HadithImageGenerator.saveHadithCardAsImage(context, current)
                                            isSavingImage = false
                                            if (uri != null) {
                                                saveSuccessMessage = "Saved to Gallery!"
                                                Toast.makeText(context, "Hadith card saved to Gallery!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Saved to device storage.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    enabled = !isSavingImage,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("hadith_save_image_button")
                                ) {
                                    if (isSavingImage) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Gold400,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.FileDownload,
                                            contentDescription = "Save as Image",
                                            tint = Gold400,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Narrator & Book Details
                        Text(
                            text = current.narrator,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gold300.copy(alpha = 0.9f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Arabic Calligraphy Text Container
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF041912),
                            border = BorderStroke(0.8.dp, Gold500.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = current.arabicText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 19.sp,
                                        lineHeight = 33.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily.Serif,
                                    color = Color.White,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Translation Tabs Filter: Both / Urdu / English
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            val tabs = listOf("Both", "اردو (Urdu)", "English")
                            tabs.forEachIndexed { index, tabTitle ->
                                val isSelected = activeTab == index
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) Gold500 else Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clickable { activeTab = index }
                                ) {
                                    Text(
                                        text = tabTitle,
                                        color = if (isSelected) Color(0xFF072A1F) else Color.White.copy(alpha = 0.75f),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Urdu Translation Block
                        if (activeTab == 0 || activeTab == 1) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.04f),
                                border = BorderStroke(0.5.dp, Gold500.copy(alpha = 0.22f)),
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

                        // English Translation Block
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

                        // Saved Success Indicator
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

                        // Full Action Buttons: Save Card as Image, Share Card & Next Hadith
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
                                    Text("Save Image", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                    Text("Share Card", fontSize = 12.sp, color = Gold300, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Footer Collapse Button & Next Hadith
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    val total = HadithRepository.getTotalHadithCount()
                                    hadithIndex = (hadithIndex + 1) % total
                                    hadith = HadithRepository.getHadithByIndex(hadithIndex)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Gold400,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Another Hadith",
                                    color = Gold400,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            TextButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.testTag("btn_collapse_hadith")
                            ) {
                                Text(
                                    text = "Close Hadith ▲",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convenient alias for ExpandableHadithActionCard
 */
@Composable
fun ExpandableHadithActionCard(
    title: String = "Daily Hadith Shareef (حدیث شریف)",
    subtitle: String = "Tap to read authentic Bukhari & Muslim Hadith in Arabic, Urdu & English with card generator",
    isHiddenFromMembers: Boolean = false,
    initiallyExpanded: Boolean = false,
    onNavigateToLibrary: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) = DailyHadithCard(
    title = title,
    subtitle = subtitle,
    isHiddenFromMembers = isHiddenFromMembers,
    initiallyExpanded = initiallyExpanded,
    onNavigateToLibrary = onNavigateToLibrary,
    modifier = modifier
)

private fun copyHadithToClipboard(context: Context, h: HadithData) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val formattedText = buildString {
        appendLine("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")
        appendLine(h.arabicText)
        appendLine()
        appendLine("اردو ترجمہ:")
        appendLine(h.urduTranslation)
        appendLine()
        appendLine("English Translation:")
        appendLine(h.englishTranslation)
        appendLine()
        appendLine("— ${h.narrator}")
        appendLine("Book: ${h.book} (${h.reference.ifBlank { "Hadith #${h.hadithNumber}" }})")
        appendLine("Grade: ${h.grade}")
        appendLine("Alnoor International Trust")
        appendLine("WhatsApp: +92-333-2434114 | Email: info@alnoorislami.pk")
        append("Shared via Alnoor Islami App")
    }
    val clip = ClipData.newPlainText("Hadith Shareef", formattedText)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Hadith Shareef copied to clipboard!", Toast.LENGTH_SHORT).show()
}
