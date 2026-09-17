package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuranAyah
import com.example.data.model.QuranBookmark
import com.example.data.model.QuranBrowseMode
import com.example.data.model.QuranPara
import com.example.data.model.QuranReaderSettings
import com.example.data.model.QuranSurah
import com.example.data.model.QuranViewMode
import com.example.data.repository.QuranRepository
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.NastaliqUrduFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val quranRepo = remember { QuranRepository.getInstance(context) }

    val currentAyahs by quranRepo.currentAyahs.collectAsState()
    val isLoading by quranRepo.isLoading.collectAsState()
    val errorMessage by quranRepo.errorMessage.collectAsState()
    val readingProgress by quranRepo.readingProgress.collectAsState()
    val bookmarks by quranRepo.bookmarks.collectAsState()
    val settings by quranRepo.readerSettings.collectAsState()

    // Navigation & View Mode State
    var browseMode by remember { mutableStateOf(QuranBrowseMode.PARAS_30) }
    var isReadingViewActive by remember { mutableStateOf(false) }
    var activeSurahNumber by remember { mutableIntStateOf(readingProgress.lastReadSurahNumber) }
    var activeParaNumber by remember { mutableIntStateOf(readingProgress.lastReadPara) }
    var readingByParaMode by remember { mutableStateOf(false) }

    BackHandler(enabled = isReadingViewActive) {
        isReadingViewActive = false
    }

    // Search query
    var searchQuery by remember { mutableStateOf("") }
    var revelationFilter by remember { mutableStateOf("ALL") } // "ALL", "MAKKI", "MADANI"

    // Settings sheet
    var showSettingsSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Dynamic background colors depending on theme
    val bgColor = if (settings.useParchmentMode) Color(0xFFFBF7EE) else if (settings.isNightMode) DarkBg else Color(0xFFF8FAF9)
    val cardColor = if (settings.useParchmentMode) Color(0xFFF3ECE0) else if (settings.isNightMode) DarkCard else Color.White
    val textColor = if (settings.useParchmentMode) Color(0xFF2C2416) else if (settings.isNightMode) Color(0xFFF1F5F2) else Color(0xFF11221A)
    val subTextColor = if (settings.useParchmentMode) Color(0xFF5C4E3A) else if (settings.isNightMode) Color(0xFFCADBD1) else Color(0xFF4A5568)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- HEADER TOP BAR ---
            Surface(
                color = if (settings.isNightMode) Emerald900 else Emerald800,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (isReadingViewActive) {
                                    isReadingViewActive = false
                                } else {
                                    onBackToHome()
                                }
                            },
                            modifier = Modifier.testTag("btn_quran_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Gold400
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            Text(
                                text = if (isReadingViewActive) {
                                    if (readingByParaMode) "پارہ ${activeParaNumber} • ${quranRepo.allParas.find { it.number == activeParaNumber }?.nameArabic ?: ""}"
                                    else "${quranRepo.allSurahs.find { it.number == activeSurahNumber }?.nameArabic ?: ""} (${quranRepo.allSurahs.find { it.number == activeSurahNumber }?.nameEnglish ?: ""})"
                                } else {
                                    "القرآن الکریم"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "کنز الایمان • ترجمہ اعلیٰ حضرت امام احمد رضا خان",
                                fontSize = 11.sp,
                                color = Gold300,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isReadingViewActive) {
                            IconButton(
                                onClick = { showSettingsSheet = true },
                                modifier = Modifier.testTag("btn_quran_font_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatSize,
                                    contentDescription = "Reader Font Settings",
                                    tint = Gold400
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (isReadingViewActive) {
                                    isReadingViewActive = false
                                    browseMode = QuranBrowseMode.BOOKMARKS
                                } else {
                                    browseMode = QuranBrowseMode.BOOKMARKS
                                }
                            },
                            modifier = Modifier.testTag("btn_quran_bookmarks")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Bookmarks",
                                tint = if (browseMode == QuranBrowseMode.BOOKMARKS && !isReadingViewActive) Gold400 else Color.White
                            )
                        }
                    }
                }
            }

            // --- VIEW MODE 1: ACTIVE AYAH-BY-AYAH RECITATION READER ---
            if (isReadingViewActive) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // Quick navigation sub-bar (Prev/Next & Progress indicator)
                        Surface(
                            color = cardColor,
                            border = BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Previous Button
                                OutlinedButton(
                                    onClick = {
                                        if (readingByParaMode) {
                                            if (activeParaNumber > 1) {
                                                activeParaNumber -= 1
                                                quranRepo.loadPara(activeParaNumber)
                                                coroutineScope.launch { listState.scrollToItem(0) }
                                            }
                                        } else {
                                            if (activeSurahNumber > 1) {
                                                activeSurahNumber -= 1
                                                quranRepo.loadSurah(activeSurahNumber)
                                                coroutineScope.launch { listState.scrollToItem(0) }
                                            }
                                        }
                                    },
                                    enabled = if (readingByParaMode) activeParaNumber > 1 else activeSurahNumber > 1,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderDark)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous",
                                        modifier = Modifier.size(16.dp),
                                        tint = Gold400
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Previous", fontSize = 12.sp, color = textColor)
                                }

                                // Mode Summary Badge
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (readingByParaMode) "Para ${activeParaNumber} of 30"
                                        else "Surah ${activeSurahNumber} of 114",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold400
                                    )
                                    Text(
                                        text = "${currentAyahs.size} Ayat",
                                        fontSize = 10.sp,
                                        color = subTextColor
                                    )
                                }

                                // Next Button
                                OutlinedButton(
                                    onClick = {
                                        if (readingByParaMode) {
                                            if (activeParaNumber < 30) {
                                                activeParaNumber += 1
                                                quranRepo.loadPara(activeParaNumber)
                                                coroutineScope.launch { listState.scrollToItem(0) }
                                            }
                                        } else {
                                            if (activeSurahNumber < 114) {
                                                activeSurahNumber += 1
                                                quranRepo.loadSurah(activeSurahNumber)
                                                coroutineScope.launch { listState.scrollToItem(0) }
                                            }
                                        }
                                    },
                                    enabled = if (readingByParaMode) activeParaNumber < 30 else activeSurahNumber < 114,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderDark)
                                ) {
                                    Text("Next", fontSize = 12.sp, color = textColor)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next",
                                        modifier = Modifier.size(16.dp),
                                        tint = Gold400
                                    )
                                }
                            }
                        }

                        // --- MUSHAF VIEW MODE SWITCHER (Split Page vs Word-by-Word vs Verse Cards) ---
                        Surface(
                            color = if (settings.isNightMode) DarkSurface else Color(0xFFF3F4F6),
                            border = BorderStroke(0.5.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 1. Split-Column Mushaf Page (Image 2 style)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (settings.viewMode == QuranViewMode.MUSHAF_SPLIT_PAGE) Emerald700 else Color.Transparent,
                                    border = BorderStroke(
                                        1.dp,
                                        if (settings.viewMode == QuranViewMode.MUSHAF_SPLIT_PAGE) Gold400 else BorderDark
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            quranRepo.updateSettings(settings.copy(viewMode = QuranViewMode.MUSHAF_SPLIT_PAGE))
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "📖 دو کالم مصحف",
                                            fontFamily = NastaliqUrduFontFamily,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (settings.viewMode == QuranViewMode.MUSHAF_SPLIT_PAGE) FontWeight.Bold else FontWeight.Normal,
                                            color = if (settings.viewMode == QuranViewMode.MUSHAF_SPLIT_PAGE) Color.White else textColor
                                        )
                                    }
                                }

                                // 2. Word-by-Word Grid (Image 1 style)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (settings.viewMode == QuranViewMode.WORD_BY_WORD) Emerald700 else Color.Transparent,
                                    border = BorderStroke(
                                        1.dp,
                                        if (settings.viewMode == QuranViewMode.WORD_BY_WORD) Gold400 else BorderDark
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            quranRepo.updateSettings(settings.copy(viewMode = QuranViewMode.WORD_BY_WORD))
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "🔤 لفظی ترجمہ",
                                            fontFamily = NastaliqUrduFontFamily,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (settings.viewMode == QuranViewMode.WORD_BY_WORD) FontWeight.Bold else FontWeight.Normal,
                                            color = if (settings.viewMode == QuranViewMode.WORD_BY_WORD) Color.White else textColor
                                        )
                                    }
                                }

                                // 3. Verse by Verse Cards
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (settings.viewMode == QuranViewMode.VERSE_BY_VERSE) Emerald700 else Color.Transparent,
                                    border = BorderStroke(
                                        1.dp,
                                        if (settings.viewMode == QuranViewMode.VERSE_BY_VERSE) Gold400 else BorderDark
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            quranRepo.updateSettings(settings.copy(viewMode = QuranViewMode.VERSE_BY_VERSE))
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "📋 آیت کارڈز",
                                            fontFamily = NastaliqUrduFontFamily,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (settings.viewMode == QuranViewMode.VERSE_BY_VERSE) FontWeight.Bold else FontWeight.Normal,
                                            color = if (settings.viewMode == QuranViewMode.VERSE_BY_VERSE) Color.White else textColor
                                        )
                                    }
                                }
                            }
                        }

                        if (isLoading && currentAyahs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Gold400, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "تلاوت کلامِ پاک ڈاؤن لوڈ ہو رہی ہے...",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Gold400
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Loading Arabic text & Kanz-ul-Iman translation",
                                        fontSize = 12.sp,
                                        color = subTextColor
                                    )
                                }
                            }
                        } else if (errorMessage != null && currentAyahs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = errorMessage ?: "",
                                        fontSize = 13.sp,
                                        color = Color(0xFFF87171),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            if (readingByParaMode) quranRepo.loadPara(activeParaNumber)
                                            else quranRepo.loadSurah(activeSurahNumber)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                                    ) {
                                        Text("Retry Loading")
                                    }
                                }
                            }
                        } else {
                            val activeSurahObj = quranRepo.allSurahs.find { it.number == activeSurahNumber }
                            val activeParaObj = quranRepo.allParas.find { it.number == activeParaNumber }
                            val curSurahAr = activeSurahObj?.nameArabic ?: currentAyahs.firstOrNull()?.surahNameArabic ?: "القرآن"
                            val curParaAr = activeParaObj?.nameArabic ?: "الم"

                            when (settings.viewMode) {
                                QuranViewMode.MUSHAF_SPLIT_PAGE -> {
                                    MushafSplitPageView(
                                        ayahs = currentAyahs,
                                        surahNumber = activeSurahNumber,
                                        surahNameArabic = curSurahAr,
                                        paraNumber = activeParaNumber,
                                        paraNameArabic = curParaAr,
                                        settings = settings,
                                        listState = listState,
                                        onBookmarkClick = { quranRepo.toggleBookmark(it) },
                                        onCopyClick = { ayah ->
                                            val textToCopy = "${ayah.textArabic}\n\nترجمہ کنز الایمان:\n${ayah.translationKanzuliman}\n\n[سورة $curSurahAr، آية ${ayah.numberInSurah}]"
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Quran Ayah", textToCopy))
                                            Toast.makeText(context, "آیت اور ترجمہ کاپی ہو گیا", Toast.LENGTH_SHORT).show()
                                        },
                                        onShareClick = { ayah ->
                                            val shareText = "${ayah.textArabic}\n\nترجمہ کنز الایمان:\n${ayah.translationKanzuliman}\n\n(سورة $curSurahAr • آیت ${ayah.numberInSurah})\n- النور اسلامی ایپ"
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Holy Ayah"))
                                        }
                                    )
                                }
                                QuranViewMode.WORD_BY_WORD -> {
                                    WordByWordPageView(
                                        ayahs = currentAyahs,
                                        surahNumber = activeSurahNumber,
                                        surahNameArabic = curSurahAr,
                                        paraNumber = activeParaNumber,
                                        paraNameArabic = curParaAr,
                                        settings = settings,
                                        listState = listState,
                                        onBookmarkClick = { quranRepo.toggleBookmark(it) },
                                        onCopyClick = { ayah ->
                                            val textToCopy = "${ayah.textArabic}\n\nترجمہ کنز الایمان:\n${ayah.translationKanzuliman}\n\n[سورة $curSurahAr، آية ${ayah.numberInSurah}]"
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Quran Ayah", textToCopy))
                                            Toast.makeText(context, "آیت اور لفظی ترجمہ کاپی ہو گیا", Toast.LENGTH_SHORT).show()
                                        },
                                        onShareClick = { ayah ->
                                            val shareText = "${ayah.textArabic}\n\nترجمہ کنز الایمان:\n${ayah.translationKanzuliman}\n\n(سورة $curSurahAr • آیت ${ayah.numberInSurah})\n- النور اسلامی ایپ"
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Holy Ayah"))
                                        }
                                    )
                                }
                                QuranViewMode.VERSE_BY_VERSE -> {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(bottom = 32.dp)
                                    ) {
                                        // Bismillah Header (Shown at start of Surahs except Surah At-Tawbah #9)
                                        if (!readingByParaMode && activeSurahNumber != 9) {
                                            item {
                                                BismillahHeaderCard(
                                                    isNightMode = settings.isNightMode,
                                                    useParchmentMode = settings.useParchmentMode
                                                )
                                            }
                                        }

                                        itemsIndexed(currentAyahs, key = { _, item -> "${item.surahNumber}_${item.numberInSurah}" }) { index, ayah ->
                                            AyahReadingCard(
                                                ayah = ayah,
                                                index = index + 1,
                                                totalAyahs = currentAyahs.size,
                                                settings = settings,
                                                textColor = textColor,
                                                subTextColor = subTextColor,
                                                cardColor = cardColor,
                                                onBookmarkClick = { quranRepo.toggleBookmark(ayah) },
                                                onCopyClick = {
                                                    val textToCopy = """
${ayah.textArabic}

ترجمہ کنز الایمان (امام احمد رضا خان):
${ayah.translationKanzuliman}

[القرآن - سورة ${ayah.surahNameArabic ?: ""}، آية ${ayah.numberInSurah} • پاره ${ayah.paraNumber}]
                                                    """.trimIndent()
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("Quran Ayah", textToCopy))
                                                    Toast.makeText(context, "آیت اور ترجمہ کاپی ہو گیا", Toast.LENGTH_SHORT).show()
                                                },
                                                onShareClick = {
                                                    val shareText = """
${ayah.textArabic}

ترجمہ کنز الایمان:
${ayah.translationKanzuliman}

(سورة ${ayah.surahNameArabic.ifEmpty { "القرآن" }} • آیت ${ayah.numberInSurah})
- النور اسلامی ایپ
                                                    """.trimIndent()
                                                    val sendIntent = Intent().apply {
                                                        action = Intent.ACTION_SEND
                                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                                        type = "text/plain"
                                                    }
                                                    val shareIntent = Intent.createChooser(sendIntent, "Share Holy Ayah")
                                                    context.startActivity(shareIntent)
                                                },
                                                onMarkAsReadClick = {
                                                    quranRepo.updateReadingProgress(
                                                        para = ayah.paraNumber,
                                                        surahNumber = ayah.surahNumber,
                                                        surahName = ayah.surahNameEnglish.ifEmpty { "Surah $activeSurahNumber" },
                                                        surahNameArabic = ayah.surahNameArabic.ifEmpty { "القرآن" },
                                                        ayahNumber = ayah.numberInSurah
                                                    )
                                                    Toast.makeText(context, "تلاوت کی جگہ محفوظ ہو گئی (Bookmark Saved)", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // --- VIEW MODE 2: BROWSE DASHBOARD (PARAS / SURAHS / BOOKMARKS) ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {

                    // 1. "RESUME DAILY TILAWAT" QUICK HERO CARD
                    item {
                        ResumeTilawatHeroCard(
                            progress = readingProgress,
                            cardColor = cardColor,
                            textColor = textColor,
                            subTextColor = subTextColor,
                            onResumeClick = {
                                activeSurahNumber = readingProgress.lastReadSurahNumber
                                activeParaNumber = readingProgress.lastReadPara
                                readingByParaMode = false
                                quranRepo.loadSurah(activeSurahNumber)
                                isReadingViewActive = true
                                // Scroll to saved ayah
                                coroutineScope.launch {
                                    val targetIndex = (readingProgress.lastReadAyahNumber - 1).coerceAtLeast(0)
                                    listState.scrollToItem(targetIndex)
                                }
                            }
                        )
                    }

                    // 2. TABS: 30 PARAS / 114 SURAHS / BOOKMARKS
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = cardColor,
                            border = BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                NavigationModeButton(
                                    title = "30 سیپارے (Paras)",
                                    count = "30",
                                    isSelected = browseMode == QuranBrowseMode.PARAS_30,
                                    onClick = { browseMode = QuranBrowseMode.PARAS_30 },
                                    modifier = Modifier.weight(1f)
                                )
                                NavigationModeButton(
                                    title = "114 سورتیں (Surahs)",
                                    count = "114",
                                    isSelected = browseMode == QuranBrowseMode.SURAHS_114,
                                    onClick = { browseMode = QuranBrowseMode.SURAHS_114 },
                                    modifier = Modifier.weight(1f)
                                )
                                NavigationModeButton(
                                    title = "نشانات (Bookmarks)",
                                    count = bookmarks.size.toString(),
                                    isSelected = browseMode == QuranBrowseMode.BOOKMARKS,
                                    onClick = { browseMode = QuranBrowseMode.BOOKMARKS },
                                    modifier = Modifier.weight(1.1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 3. SEARCH & FILTER BAR (When in Surahs or Paras mode)
                    if (browseMode != QuranBrowseMode.BOOKMARKS) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = if (browseMode == QuranBrowseMode.PARAS_30) "Search by Para name or number (e.g. الم, 1, Amma)..."
                                        else "Search Surah (e.g. Yasin, يس, Mulk, 36)...",
                                        fontSize = 13.sp,
                                        color = subTextColor
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Gold400)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Gold400)
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold400,
                                    unfocusedBorderColor = BorderDark,
                                    focusedContainerColor = cardColor,
                                    unfocusedContainerColor = cardColor,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_search_quran")
                            )

                            if (browseMode == QuranBrowseMode.SURAHS_114) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = revelationFilter == "ALL",
                                        onClick = { revelationFilter = "ALL" },
                                        label = { Text("All 114 Surahs", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald700,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    FilterChip(
                                        selected = revelationFilter == "MAKKI",
                                        onClick = { revelationFilter = "MAKKI" },
                                        label = { Text("مکی (Makki)", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald700,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                    FilterChip(
                                        selected = revelationFilter == "MADANI",
                                        onClick = { revelationFilter = "MADANI" },
                                        label = { Text("مدنی (Madani)", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald700,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // --- CONTENT SECTION: 30 PARAS ---
                    if (browseMode == QuranBrowseMode.PARAS_30) {
                        val filteredParas = quranRepo.allParas.filter {
                            searchQuery.isBlank() ||
                            it.number.toString() == searchQuery.trim() ||
                            it.nameArabic.contains(searchQuery, ignoreCase = true) ||
                            it.nameUrdu.contains(searchQuery, ignoreCase = true) ||
                            it.startSurahName.contains(searchQuery, ignoreCase = true)
                        }

                        items(filteredParas, key = { "para_${it.number}" }) { para ->
                            ParaItemCard(
                                para = para,
                                cardColor = cardColor,
                                textColor = textColor,
                                subTextColor = subTextColor,
                                onClick = {
                                    activeParaNumber = para.number
                                    readingByParaMode = true
                                    quranRepo.loadPara(para.number)
                                    isReadingViewActive = true
                                    coroutineScope.launch { listState.scrollToItem(0) }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // --- CONTENT SECTION: 114 SURAHS ---
                    if (browseMode == QuranBrowseMode.SURAHS_114) {
                        val filteredSurahs = quranRepo.allSurahs.filter { surah ->
                            val matchesSearch = searchQuery.isBlank() ||
                                surah.number.toString() == searchQuery.trim() ||
                                surah.nameArabic.contains(searchQuery, ignoreCase = true) ||
                                surah.nameEnglish.contains(searchQuery, ignoreCase = true) ||
                                surah.nameUrdu.contains(searchQuery, ignoreCase = true) ||
                                surah.meaningEnglish.contains(searchQuery, ignoreCase = true)

                            val matchesFilter = when (revelationFilter) {
                                "MAKKI" -> surah.isMakki
                                "MADANI" -> !surah.isMakki
                                else -> true
                            }
                            matchesSearch && matchesFilter
                        }

                        items(filteredSurahs, key = { "surah_${it.number}" }) { surah ->
                            SurahItemCard(
                                surah = surah,
                                cardColor = cardColor,
                                textColor = textColor,
                                subTextColor = subTextColor,
                                onClick = {
                                    activeSurahNumber = surah.number
                                    readingByParaMode = false
                                    quranRepo.loadSurah(surah.number)
                                    isReadingViewActive = true
                                    coroutineScope.launch { listState.scrollToItem(0) }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // --- CONTENT SECTION: BOOKMARKS ---
                    if (browseMode == QuranBrowseMode.BOOKMARKS) {
                        if (bookmarks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.BookmarkBorder,
                                            contentDescription = null,
                                            tint = Gold400,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "کوئی نشان (Bookmark) موجود نہیں",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "تلاوت کرتے وقت کسی بھی آیت پر لگے ربن پر کلک کر کے بک مارک محفوظ کریں۔",
                                            fontSize = 12.sp,
                                            color = subTextColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(bookmarks, key = { it.id }) { bookmark ->
                                BookmarkItemCard(
                                    bookmark = bookmark,
                                    cardColor = cardColor,
                                    textColor = textColor,
                                    subTextColor = subTextColor,
                                    onClick = {
                                        activeSurahNumber = bookmark.surahNumber
                                        readingByParaMode = false
                                        quranRepo.loadSurah(bookmark.surahNumber)
                                        isReadingViewActive = true
                                        coroutineScope.launch {
                                            val targetIndex = (bookmark.ayahNumber - 1).coerceAtLeast(0)
                                            listState.scrollToItem(targetIndex)
                                        }
                                    },
                                    onDelete = { quranRepo.removeBookmark(bookmark.id) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- READER SETTINGS MODAL BOTTOM SHEET ---
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = cardColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "قرآنی فونٹ اور ریڈر سیٹنگز",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold400
                    )
                    Text(
                        text = "Customize reading style, Arabic calligraphy, Tajweed, and contrast",
                        fontSize = 12.sp,
                        color = subTextColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Reading View Mode Selector
                    Text("قرآنی اندازِ تلاوت (Reading View Mode)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (settings.viewMode == QuranViewMode.MUSHAF_SPLIT_PAGE) Emerald700 else Color.Transparent,
                            border = BorderStroke(1.dp, if (settings.viewMode == QuranViewMode.MUSHAF_SPLIT_PAGE) Gold400 else BorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { quranRepo.updateSettings(settings.copy(viewMode = QuranViewMode.MUSHAF_SPLIT_PAGE)) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "📖 دو کالم مصحف",
                                    fontFamily = NastaliqUrduFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (settings.viewMode == QuranViewMode.MUSHAF_SPLIT_PAGE) Color.White else textColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (settings.viewMode == QuranViewMode.WORD_BY_WORD) Emerald700 else Color.Transparent,
                            border = BorderStroke(1.dp, if (settings.viewMode == QuranViewMode.WORD_BY_WORD) Gold400 else BorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { quranRepo.updateSettings(settings.copy(viewMode = QuranViewMode.WORD_BY_WORD)) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "🔤 لفظی ترجمہ",
                                    fontFamily = NastaliqUrduFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (settings.viewMode == QuranViewMode.WORD_BY_WORD) Color.White else textColor
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (settings.viewMode == QuranViewMode.VERSE_BY_VERSE) Emerald700 else Color.Transparent,
                            border = BorderStroke(1.dp, if (settings.viewMode == QuranViewMode.VERSE_BY_VERSE) Gold400 else BorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { quranRepo.updateSettings(settings.copy(viewMode = QuranViewMode.VERSE_BY_VERSE)) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(
                                    text = "📋 آیت کارڈز",
                                    fontFamily = NastaliqUrduFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (settings.viewMode == QuranViewMode.VERSE_BY_VERSE) Color.White else textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Arabic Font Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("عربی فونٹ سائز (Arabic Size)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                        Text("${settings.arabicFontSize.toInt()} sp", fontSize = 13.sp, color = Gold400, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.arabicFontSize,
                        onValueChange = { quranRepo.updateSettings(settings.copy(arabicFontSize = it)) },
                        valueRange = 18f..38f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = Gold400, activeTrackColor = Emerald600)
                    )

                    // Live Preview Arabic
                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            fontFamily = AmiriQuranFontFamily,
                            fontSize = settings.arabicFontSize.sp,
                            color = textColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Urdu Font Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("اردو کنز الایمان سائز (Urdu Size)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                        Text("${settings.urduFontSize.toInt()} sp", fontSize = 13.sp, color = Gold400, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.urduFontSize,
                        onValueChange = { quranRepo.updateSettings(settings.copy(urduFontSize = it)) },
                        valueRange = 12f..24f,
                        steps = 6,
                        colors = SliderDefaults.colors(thumbColor = Gold400, activeTrackColor = Emerald600)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Toggle Tajweed Colors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("تجوید کلر کوڈنگ (Tajweed Colors)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                            Text("لفظِ اللہ سرخ اور احکامِ مد و وقف رنگین دکھائیں", fontSize = 11.sp, color = subTextColor)
                        }
                        Switch(
                            checked = settings.enableTajweedColors,
                            onCheckedChange = { quranRepo.updateSettings(settings.copy(enableTajweedColors = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Gold400, checkedTrackColor = Emerald700)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Toggle Urdu Translation on/off
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("اردو ترجمہ کنز الایمان دکھائیں", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                            Text("Show Kanz-ul-Iman translation under each Ayah", fontSize = 11.sp, color = subTextColor)
                        }
                        Switch(
                            checked = settings.showUrduTranslation,
                            onCheckedChange = { quranRepo.updateSettings(settings.copy(showUrduTranslation = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Gold400, checkedTrackColor = Emerald700)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6. Parchment Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("پرچمنٹ پیپر موڈ (Parchment Cream)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                            Text("Warm antique paper tone for long recitation", fontSize = 11.sp, color = subTextColor)
                        }
                        Switch(
                            checked = settings.useParchmentMode,
                            onCheckedChange = { quranRepo.updateSettings(settings.copy(useParchmentMode = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Gold400, checkedTrackColor = Emerald700)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// =========================================================================
// --- SUB-COMPONENTS ---
// =========================================================================

@Composable
private fun ResumeTilawatHeroCard(
    progress: com.example.data.model.QuranReadingProgress,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onResumeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onResumeClick() }
            .testTag("quran_resume_hero_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Emerald900),
        border = BorderStroke(1.5.dp, Gold500.copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF063524), Color(0xFF0F4E36), Color(0xFF0A2B1E))
                    )
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Gold400.copy(alpha = 0.18f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Gold400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تلاوت جاری رکھیں",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold300
                            )
                            Text(
                                text = "Resume Daily Tilawat",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Button(
                        onClick = onResumeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_resume_tilawat_hero")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Resume",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Gold500.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "پاره ${progress.lastReadPara} • ${progress.lastReadSurahNameArabic} (${progress.lastReadSurahName})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "آیت نمبر ${progress.lastReadAyahNumber} • آخری مرتبہ: ${progress.timestampFormatted}",
                            fontSize = 12.sp,
                            color = Emerald100.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationModeButton(
    title: String,
    count: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Emerald700 else Color.Transparent,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "($count)",
                fontSize = 10.sp,
                color = if (isSelected) Gold300 else Color.Gray
            )
        }
    }
}

@Composable
private fun ParaItemCard(
    para: QuranPara,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Para number gold badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald900,
                    border = BorderStroke(1.dp, Gold400),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${para.number}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold300
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "پارہ ${para.number} • ${para.nameUrdu}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Starts at ${para.startSurahName} (Ayah ${para.startAyah})",
                        fontSize = 11.sp,
                        color = subTextColor
                    )
                }
            }

            // Arabic Calligraphy title
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = para.nameArabic,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400,
                    textAlign = TextAlign.End
                )
                Text(
                    text = "${para.totalAyahs} آیات",
                    fontSize = 11.sp,
                    color = subTextColor
                )
            }
        }
    }
}

@Composable
private fun SurahItemCard(
    surah: QuranSurah,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Surah number circular badge
                Surface(
                    shape = CircleShape,
                    color = Emerald900,
                    border = BorderStroke(1.dp, Gold400),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${surah.number}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold300
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = surah.nameEnglish,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (surah.isMakki) Emerald800 else Color(0xFF1E3A8A)
                        ) {
                            Text(
                                text = if (surah.isMakki) "مکی" else "مدنی",
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${surah.meaningEnglish} • ${surah.ayahsCount} آیات • پارہ ${surah.startingPara}",
                        fontSize = 11.sp,
                        color = subTextColor
                    )
                }
            }

            // Arabic Surah Calligraphy
            Text(
                text = surah.nameArabic,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Gold400,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun BookmarkItemCard(
    bookmark: QuranBookmark,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, BorderDark)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = Gold400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سورة ${bookmark.surahNameArabic} (${bookmark.surahNameEnglish}) • آیت ${bookmark.ayahNumber}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Bookmark",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (bookmark.previewArabic.isNotBlank()) {
                Text(
                    text = bookmark.previewArabic,
                    fontSize = 15.sp,
                    color = Gold300,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (bookmark.previewTranslation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bookmark.previewTranslation,
                    fontSize = 12.sp,
                    color = subTextColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "محفوظ شدہ: ${bookmark.savedAtFormatted} • پارہ ${bookmark.paraNumber}",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun BismillahHeaderCard(
    isNightMode: Boolean,
    useParchmentMode: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (useParchmentMode) Color(0xFFECE4D0) else if (isNightMode) Emerald950 else Emerald900
        ),
        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    fontFamily = AmiriQuranFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold400,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اللہ کے نام سے شروع جو بہت مہربان رحمت والا",
                    fontFamily = NastaliqUrduFontFamily,
                    fontSize = 13.sp,
                    color = Emerald100,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AyahReadingCard(
    ayah: QuranAyah,
    index: Int,
    totalAyahs: Int,
    settings: com.example.data.model.QuranReaderSettings,
    textColor: Color,
    subTextColor: Color,
    cardColor: Color,
    onBookmarkClick: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onMarkAsReadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("quran_ayah_card_${ayah.surahNumber}_${ayah.numberInSurah}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            if (ayah.isBookmarked) 1.5.dp else 1.dp,
            if (ayah.isBookmarked) Gold400 else BorderDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Ayah Action Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Verse Number Badge (۝)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Emerald900,
                        border = BorderStroke(1.dp, Gold400),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${ayah.numberInSurah}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold300
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "آیت ${ayah.numberInSurah} • پاره ${ayah.paraNumber}",
                        fontSize = 11.sp,
                        color = subTextColor
                    )

                    if (ayah.sajda) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDC2626)
                        ) {
                            Text(
                                text = "سجدہ",
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Action icons: Read mark, Copy, Share, Bookmark
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMarkAsReadClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Set as Reading Mark",
                            tint = Gold400,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopyClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Ayah",
                            tint = subTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Ayah",
                            tint = subTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (ayah.isBookmarked) Gold400 else subTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ARABIC AYAH TEXT WITH AMIRI QURANIC SCRIPT & TAJWEED HIGHLIGHTING
            val annotatedArabic = buildTajweedAnnotatedString(
                text = ayah.textArabic,
                enableTajweed = settings.enableTajweedColors,
                defaultColor = textColor
            )
            Text(
                text = annotatedArabic,
                fontFamily = AmiriQuranFontFamily,
                fontSize = settings.arabicFontSize.sp,
                lineHeight = (settings.arabicFontSize * 1.75f).sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            // KANZ-UL-IMAN URDU TRANSLATION WITH NASTALIQ SCRIPT
            if (settings.showUrduTranslation && ayah.translationKanzuliman.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Gold400.copy(alpha = 0.25f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Emerald800.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "کنز الایمان",
                            fontFamily = NastaliqUrduFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold300,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ayah.translationKanzuliman,
                        fontFamily = NastaliqUrduFontFamily,
                        fontSize = settings.urduFontSize.sp,
                        lineHeight = (settings.urduFontSize * 1.65f).sp,
                        color = textColor.copy(alpha = 0.9f),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private val Emerald950 = Color(0xFF031E14)
