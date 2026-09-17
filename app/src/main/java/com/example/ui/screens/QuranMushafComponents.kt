package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuranAyah
import com.example.data.model.QuranReaderSettings
import com.example.data.model.QuranWordToken
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.NastaliqUrduFontFamily

// --- TRADITIONAL MUSHAF COLOR PALETTE ---
private val MushafRed = Color(0xFFC5221F)
private val MushafCrimson = Color(0xFF9E1B1B)
private val MushafDarkRed = Color(0xFF7F1D1D)
private val MushafGreen = Color(0xFF0D7A53)
private val MushafGold = Color(0xFFD4AF37)
private val MushafBorderRed = Color(0xFFA82323)
private val MushafPageParchment = Color(0xFFFFFDF8)
private val MushafPageBorderInner = Color(0xFFB91C1C)

// Arabic Numerals Converter (1 -> ۱, 2 -> ۲, etc.)
fun toArabicDigits(num: Int): String {
    val easternDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return num.toString().map { if (it.isDigit()) easternDigits[it - '0'] else it }.joinToString("")
}

// Tajweed Color Highlighter for Arabic text
fun buildTajweedAnnotatedString(
    text: String,
    enableTajweed: Boolean,
    defaultColor: Color
): AnnotatedString {
    if (!enableTajweed) return AnnotatedString(text)

    return buildAnnotatedString {
        val words = text.split(" ")
        for (i in words.indices) {
            val word = words[i]
            val isAllah = word.contains("اللَّه") || word.contains("اللَّه") || word.contains("لِلَّهِ") || word.contains("لِلَّهِ")
            val hasMadd = word.contains("ٓ") || word.contains("آ") || word.contains("ـٰ")
            val isWaqf = word.contains("ۘ") || word.contains("ۚ") || word.contains("ۗ") || word.contains("ۖ") || word.contains("ۙ") || word.contains("ۜ")

            when {
                isAllah -> {
                    pushStyle(SpanStyle(color = MushafRed, fontWeight = FontWeight.Bold))
                    append(word)
                    pop()
                }
                hasMadd -> {
                    pushStyle(SpanStyle(color = MushafCrimson, fontWeight = FontWeight.SemiBold))
                    append(word)
                    pop()
                }
                isWaqf -> {
                    pushStyle(SpanStyle(color = MushafGold, fontWeight = FontWeight.Bold))
                    append(word)
                    pop()
                }
                else -> {
                    pushStyle(SpanStyle(color = defaultColor))
                    append(word)
                    pop()
                }
            }
            if (i < words.size - 1) append(" ")
        }
    }
}

// Word-by-Word Generator & Aligner for Ayahs
object QuranWordByWordHelper {

    // Known word mappings for Surah Al-Fatihah & early Al-Baqarah
    private val knownWordsMap = mapOf(
        "بِسْمِ" to Pair("نام سے", "بِسْمِ"),
        "اللَّهِ" to Pair("اللہ کے", "اللّٰہ"),
        "الرَّحْمَٰنِ" to Pair("نہایت مہربان", "الرَّحْمٰن"),
        "الرَّحِيمِ" to Pair("بہت رحم فرمانے والا", "الرَّحِیْم"),
        "الْحَمْدُ" to Pair("سب تعریفیں", "اَلْحَمْدُ"),
        "لِلَّهِ" to Pair("اللہ ہی کے لیے", "لِلّٰہِ"),
        "رَبِّ" to Pair("جو پالنے والا ہے", "رَبِّ"),
        "الْعَالَمِينَ" to Pair("تمام جہانوں کا", "اَلْعٰلَمِیْن"),
        "مَالِكِ" to Pair("مالک ہے", "مٰلِکِ"),
        "يَوْمِ" to Pair("دن کا", "یَوْمِ"),
        "الدِّينِ" to Pair("جزا و سزا کے", "الدِّیْن"),
        "إِيَّاكَ" to Pair("ہم تیری ہی", "اِیَّاکَ"),
        "نَعْبُدُ" to Pair("عبادت کرتے ہیں", "نَعْبُدُ"),
        "وَإِيَّاكَ" to Pair("اور تجھ ہی سے", "وَ اِیَّاکَ"),
        "نَسْتَعِينُ" to Pair("ہم مدد مانگتے ہیں", "نَسْتَعِیْن"),
        "اهْدِنَا" to Pair("ہمیں دکھا", "اِہْدِنَا"),
        "الصِّرَاطَ" to Pair("سیدھا راستہ", "الصِّرَاطَ"),
        "الْمُسْتَقِيمَ" to Pair("بالکل سیدھا", "الْمُسْتَقِیْم"),
        "صِرَاطَ" to Pair("ان لوگوں کا راستہ", "صِرَاطَ"),
        "الَّذِينَ" to Pair("جن پر", "الَّذِیْنَ"),
        "أَنْعَمْتَ" to Pair("تو نے انعام کیا", "اَنْعَمْتَ"),
        "عَلَيْهِمْ" to Pair("ان پر", "عَلَیْہِمْ"),
        "غَيْرِ" to Pair("نہ کہ ان کا جن پر", "غَیْرِ"),
        "الْمَغْضُوبِ" to Pair("غضب ہوا", "الْمَغْضُوْبِ"),
        "وَلَا" to Pair("اور نہ ہی", "وَ لَا"),
        "الضَّالِّينَ" to Pair("گمراہوں کا", "الضَّآلِّیْن"),
        // Surah Al-Baqarah words
        "الم" to Pair("الف لام میم (حروف مقطعات)", "الۤمّۤ"),
        "ذَٰلِكَ" to Pair("یہ", "ذٰلِکَ"),
        "الْكِتَابُ" to Pair("وہ بلند رتبہ کتاب ہے", "الْکِتٰبُ"),
        "لَا" to Pair("نہیں ہے", "لَا"),
        "رَيْبَ" to Pair("کوئی شک", "رَیْبَ"),
        "فِيهِ" to Pair("اس میں", "فِیْہِ"),
        "هُدًى" to Pair("ہدایت ہے", "ہُدًی"),
        "لِّلْمُتَّقِينَ" to Pair("پرہیزگاروں کے لیے", "لِّلْمُتَّقِیْنَ"),
        "يُؤْمِنُونَ" to Pair("جو ایمان لاتے ہیں", "یُؤْمِنُوْنَ"),
        "بِالْغَيْبِ" to Pair("بن دیکھے پر", "بِالْغَیْبِ"),
        "وَيُقِيمُونَ" to Pair("اور وہ قائم رکھتے ہیں", "وَ یُقِیْمُوْنَ"),
        "الصَّلَاةَ" to Pair("نماز کو", "الصَّلٰوۃَ"),
        "وَمِمَّا" to Pair("اور اس میں سے جو", "وَ مِمَّا"),
        "رَزَقْنَاهُمْ" to Pair("ہم نے انہیں رزق دیا", "رَزَقْنٰہُمْ"),
        "يُنفِقُونَ" to Pair("وہ خرچ کرتے ہیں", "یُنْفِقُوْنَ"),
        "أُولَٰئِكَ" to Pair("یہی لوگ", "اُولٰٓئِکَ"),
        "عَلَىٰ" to Pair("اوپر", "عَلٰی"),
        "رَّبِّهِمْ" to Pair("اپنے رب کی طرف سے", "رَّبِّہِمْ"),
        "الْمُفْلِحُونَ" to Pair("فلاح پانے والے ہیں", "الْمُفْلِحُوْنَ"),
        "إِنَّ" to Pair("بے شک", "اِنَّ"),
        "كَفَرُوا" to Pair("جنہوں نے کفر کیا", "کَفَرُوْا"),
        "سَوَاءٌ" to Pair("برابر ہے", "سَوَآءٌ"),
        "أَأَنذَرْتَهُمْ" to Pair("خواہ تم انہیں ڈراؤ", "ءَاَنْذَرْتَہُمْ"),
        "أَمْ" to Pair("یا", "اَمْ"),
        "لَمْ" to Pair("نہ", "لَمْ"),
        "تُنذِرْهُمْ" to Pair("تم انہیں ڈراؤ", "تُنْذِرْہُمْ"),
        "لَا" to Pair("نہیں", "لَا"),
        "خَتَمَ" to Pair("مہر لگا دی ہے", "خَتَمَ"),
        "قُلُوبِهِمْ" to Pair("ان کے دلوں پر", "قُلُوْبِہِمْ"),
        "سَمْعِهِمْ" to Pair("ان کے کانوں پر", "سَمْعِہِمْ"),
        "أَبْصَارِهِمْ" to Pair("ان کی آنکھوں پر", "اَبْصَارِہِمْ"),
        "غِشَاوَةٌ" to Pair("ایک پردہ ہے", "غِشَاوَۃٌ"),
        "عَذَابٌ" to Pair("ایک عذاب ہے", "عَذَابٌ"),
        "عَظِيمٌ" to Pair("بہت بڑا", "عَظِیْمٌ")
    )

    fun getWordTokensForAyah(ayah: QuranAyah): List<QuranWordToken> {
        val arabicWords = ayah.textArabic
            .replace("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "") // Strip leading Basmalah if concatenated
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        if (arabicWords.isEmpty()) {
            return listOf(QuranWordToken(ayah.textArabic, ayah.translationKanzuliman, ""))
        }

        // Clean Urdu translation into words/phrases
        val urduWords = ayah.translationKanzuliman.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }

        return arabicWords.mapIndexed { index, arWord ->
            // Clean punctuation marks from word for lookup
            val cleanAr = arWord.replace("[،؛۔؟۝۩۞]".toRegex(), "")
            val known = knownWordsMap[cleanAr] ?: knownWordsMap[arWord]

            val urduMeaning = if (known != null) {
                known.first
            } else {
                // Heuristic mapping: distribute Urdu words across the Arabic tokens
                if (urduWords.isNotEmpty()) {
                    val ratio = index.toFloat() / arabicWords.size
                    val targetUrduIdx = (ratio * urduWords.size).toInt().coerceIn(0, urduWords.size - 1)
                    urduWords[targetUrduIdx]
                } else {
                    "..."
                }
            }

            val translit = known?.second ?: arWord
            val isTajweedSpecial = arWord.contains("اللَّه") || arWord.contains("اللَّه") || 
                                   arWord.contains("ٓ") || arWord.contains("ۘ")

            QuranWordToken(
                arabic = arWord,
                urdu = urduMeaning,
                transliteration = translit,
                isTajweedSpecial = isTajweedSpecial
            )
        }
    }
}

// =============================================================================
// --- STYLE 2: TWO-COLUMN SPLIT MUSHAF PAGE (Arabic Right | Urdu Left) ---
// (Adopted directly from WhatsApp Image 2026-09-17 at 8.47.54 PM.jpeg)
// =============================================================================

@Composable
fun MushafSplitPageView(
    ayahs: List<QuranAyah>,
    surahNumber: Int,
    surahNameArabic: String,
    paraNumber: Int,
    paraNameArabic: String,
    settings: QuranReaderSettings,
    listState: LazyListState,
    onBookmarkClick: (QuranAyah) -> Unit,
    onCopyClick: (QuranAyah) -> Unit,
    onShareClick: (QuranAyah) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isNight = settings.isNightMode && !settings.useParchmentMode
    val pageBg = if (settings.useParchmentMode) Color(0xFFFBF8F0) else if (isNight) Color(0xFF0F1B15) else MushafPageParchment
    val borderColor = if (isNight) Gold600.copy(alpha = 0.5f) else MushafBorderRed
    val innerBorderColor = if (isNight) Gold500.copy(alpha = 0.3f) else MushafPageBorderInner
    val arabicDefaultColor = if (isNight) Color(0xFFF1F5F2) else Color(0xFF111111)
    val urduColor = if (isNight) Color(0xFFCADBD1) else Color(0xFF222222)
    val headerFooterColor = if (isNight) Gold400 else MushafDarkRed

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Traditional Ornate Double Border Box
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(3.dp)
                .border(1.dp, innerBorderColor, RoundedCornerShape(6.dp)),
            color = pageBg,
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- TRADITIONAL MUSHAF PAGE HEADER (Border Top Margins) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isNight) Color(0xFF16281F) else Color(0xFFFFF6F6))
                        .border(BorderStroke(0.75.dp, borderColor.copy(alpha = 0.4f)))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right: Surah Name in Calligraphy (e.g. "البَقَرَة ۲")
                    Text(
                        text = "$surahNameArabic ${toArabicDigits(surahNumber)}",
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerFooterColor
                    )

                    // Center: Page Indicator (e.g. "۵")
                    Surface(
                        shape = CircleShape,
                        color = if (isNight) Emerald900 else Color(0xFFFFECEC),
                        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = toArabicDigits(paraNumber),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = headerFooterColor
                            )
                        }
                    }

                    // Left: Parah Name in Calligraphy (e.g. "الۤمّۤ")
                    Text(
                        text = paraNameArabic,
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerFooterColor
                    )
                }

                // --- SPLIT TWO-COLUMN READING AREA ---
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    itemsIndexed(ayahs, key = { _, item -> "${item.surahNumber}_${item.numberInSurah}" }) { _, ayah ->
                        var showMenu by remember { mutableStateOf(false) }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showMenu = true },
                            color = if (ayah.isBookmarked) {
                                if (isNight) Emerald900.copy(alpha = 0.35f) else Color(0xFFFFF9E6)
                            } else Color.Transparent,
                            border = if (ayah.isBookmarked) BorderStroke(1.dp, Gold400.copy(alpha = 0.6f)) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // LEFT COLUMN: URDU TRANSLATION (48% width)
                                Box(
                                    modifier = Modifier
                                        .weight(0.48f)
                                        .padding(end = 6.dp),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        // Circular Ayah Number Marker (e.g. (۱۲))
                                        Text(
                                            text = "(${toArabicDigits(ayah.numberInSurah)})",
                                            fontFamily = NastaliqUrduFontFamily,
                                            fontSize = (settings.urduFontSize * 0.9f).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MushafRed,
                                            modifier = Modifier.padding(end = 4.dp, top = 2.dp)
                                        )

                                        // Urdu Translation text in authentic Nastaliq calligraphy
                                        Text(
                                            text = ayah.translationKanzuliman,
                                            fontFamily = NastaliqUrduFontFamily,
                                            fontSize = settings.urduFontSize.sp,
                                            lineHeight = (settings.urduFontSize * 1.7f).sp,
                                            color = urduColor,
                                            textAlign = TextAlign.Right,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                textDirection = TextDirection.Rtl
                                            )
                                        )
                                    }
                                }

                                // VERTICAL DIVIDER LINE (Traditional Mushaf spine)
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(borderColor.copy(alpha = 0.25f))
                                )

                                // RIGHT COLUMN: ARABIC TEXT (52% width)
                                Box(
                                    modifier = Modifier
                                        .weight(0.52f)
                                        .padding(start = 6.dp),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        val annotatedArabic = buildTajweedAnnotatedString(
                                            text = ayah.textArabic,
                                            enableTajweed = settings.enableTajweedColors,
                                            defaultColor = arabicDefaultColor
                                        )

                                        Text(
                                            text = annotatedArabic,
                                            fontFamily = AmiriQuranFontFamily,
                                            fontSize = settings.arabicFontSize.sp,
                                            lineHeight = (settings.arabicFontSize * 1.8f).sp,
                                            textAlign = TextAlign.Right,
                                            fontWeight = FontWeight.Normal,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                textDirection = TextDirection.Rtl
                                            )
                                        )

                                        // Ornate Ayah End Badge
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            if (ayah.isBookmarked) {
                                                Icon(
                                                    imageVector = Icons.Default.Bookmark,
                                                    contentDescription = "Bookmarked",
                                                    tint = Gold400,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }

                                            Surface(
                                                shape = CircleShape,
                                                color = Color.Transparent,
                                                border = BorderStroke(1.dp, MushafRed),
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = toArabicDigits(ayah.numberInSurah),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MushafRed
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Ayah Action Menu on tap
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(if (ayah.isBookmarked) "Remove Bookmark" else "Bookmark Ayah")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = null,
                                            tint = Gold400
                                        )
                                    },
                                    onClick = {
                                        onBookmarkClick(ayah)
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Copy Arabic & Translation") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                                    onClick = {
                                        onCopyClick(ayah)
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share Ayah") },
                                    leadingIcon = { Icon(Icons.Default.Share, null) },
                                    onClick = {
                                        onShareClick(ayah)
                                        showMenu = false
                                    }
                                )
                            }
                        }

                        HorizontalDivider(
                            color = borderColor.copy(alpha = 0.15f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                // --- TRADITIONAL MUSHAF PAGE FOOTER ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isNight) Color(0xFF16281F) else Color(0xFFFFF6F6))
                        .border(BorderStroke(0.75.dp, borderColor.copy(alpha = 0.4f)))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "منزل ۱",
                        fontFamily = NastaliqUrduFontFamily,
                        fontSize = 12.sp,
                        color = headerFooterColor
                    )
                    Text(
                        text = "وقف لازم",
                        fontFamily = NastaliqUrduFontFamily,
                        fontSize = 12.sp,
                        color = headerFooterColor
                    )
                }
            }
        }
    }
}

// =============================================================================
// --- STYLE 1: WORD-BY-WORD MUSHAF GRID (Lafzi Tarjuma) ---
// (Adopted directly from WhatsApp Image 2026-09-17 at 8.51.47 PM.jpeg)
// =============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordByWordPageView(
    ayahs: List<QuranAyah>,
    surahNumber: Int,
    surahNameArabic: String,
    paraNumber: Int,
    paraNameArabic: String,
    settings: QuranReaderSettings,
    listState: LazyListState,
    onBookmarkClick: (QuranAyah) -> Unit,
    onCopyClick: (QuranAyah) -> Unit,
    onShareClick: (QuranAyah) -> Unit,
    modifier: Modifier = Modifier
) {
    val isNight = settings.isNightMode && !settings.useParchmentMode
    val pageBg = if (settings.useParchmentMode) Color(0xFFFBF8F0) else if (isNight) Color(0xFF0F1B15) else MushafPageParchment
    val borderColor = if (isNight) Gold600.copy(alpha = 0.5f) else MushafBorderRed
    val innerBorderColor = if (isNight) Gold500.copy(alpha = 0.3f) else MushafPageBorderInner
    val headerFooterColor = if (isNight) Gold400 else MushafDarkRed
    val cellBorderColor = if (isNight) Color(0xFF233B2E) else Color(0xFFFFDEDE)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Traditional Mushaf Page Frame
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(3.dp)
                .border(1.dp, innerBorderColor, RoundedCornerShape(6.dp)),
            color = pageBg,
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Top Ornate Mushaf Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isNight) Color(0xFF16281F) else Color(0xFFFFF6F6))
                        .border(BorderStroke(0.75.dp, borderColor.copy(alpha = 0.4f)))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$surahNameArabic ${toArabicDigits(surahNumber)}",
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerFooterColor
                    )
                    Text(
                        text = "لفظی ترجمہ • کلمہ بہ کلمہ",
                        fontFamily = NastaliqUrduFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MushafRed
                    )
                    Text(
                        text = paraNameArabic,
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerFooterColor
                    )
                }

                // Scrollable list of Word-by-Word Ayahs
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    itemsIndexed(ayahs, key = { _, item -> "${item.surahNumber}_${item.numberInSurah}" }) { _, ayah ->
                        val wordTokens = remember(ayah.textArabic, ayah.translationKanzuliman) {
                            QuranWordByWordHelper.getWordTokensForAyah(ayah)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("word_by_word_card_${ayah.numberInSurah}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (ayah.isBookmarked) {
                                    if (isNight) Emerald900.copy(alpha = 0.4f) else Color(0xFFFFF9E6)
                                } else if (isNight) DarkCard else Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (ayah.isBookmarked) Gold400 else cellBorderColor
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                // Header of Ayah: Ayah number & bookmark quick action
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isNight) Color(0xFF1E382B) else Color(0xFFFFEEEE),
                                        border = BorderStroke(0.5.dp, MushafRed)
                                    ) {
                                        Text(
                                            text = "آیت نمبر ${toArabicDigits(ayah.numberInSurah)}",
                                            fontFamily = NastaliqUrduFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MushafRed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onBookmarkClick(ayah) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Bookmark",
                                                tint = Gold400,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onCopyClick(ayah) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = if (isNight) Color.LightGray else Color.DarkGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { onShareClick(ayah) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = if (isNight) Color.LightGray else Color.DarkGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 3-TIER WORD-BY-WORD FLOW GRID (Directly from Image 1)
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // End Ayah Badge (appears first in RTL flow)
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Transparent,
                                        border = BorderStroke(1.dp, MushafRed),
                                        modifier = Modifier
                                            .size(28.dp)
                                            .align(Alignment.CenterVertically)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = toArabicDigits(ayah.numberInSurah),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MushafRed
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Display tokens in reverse order for correct Arabic reading sequence
                                    wordTokens.reversed().forEach { token ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isNight) DarkSurface else Color(0xFFFFFDF8),
                                            border = BorderStroke(0.75.dp, cellBorderColor),
                                            modifier = Modifier.padding(horizontal = 3.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                // TIER 1: ARABIC WORD (Amiri Quran Font with Tajweed Red/Black styling)
                                                Text(
                                                    text = token.arabic,
                                                    fontFamily = AmiriQuranFontFamily,
                                                    fontSize = (settings.arabicFontSize * 0.95f).sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (token.isTajweedSpecial) MushafRed else if (isNight) Color(0xFFF1F5F2) else Color(0xFF111111),
                                                    textAlign = TextAlign.Center
                                                )

                                                // TIER 2: TRANSLITERATION / ACCENT
                                                if (token.transliteration.isNotBlank() && token.transliteration != token.arabic) {
                                                    Text(
                                                        text = token.transliteration,
                                                        fontFamily = NastaliqUrduFontFamily,
                                                        fontSize = 11.sp,
                                                        color = MushafCrimson,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(vertical = 1.dp)
                                                    )
                                                }

                                                // DIVIDER LINE
                                                HorizontalDivider(
                                                    color = cellBorderColor,
                                                    thickness = 0.5.dp,
                                                    modifier = Modifier
                                                        .width(42.dp)
                                                        .padding(vertical = 2.dp)
                                                )

                                                // TIER 3: LITERAL URDU MEANING (لفظی ترجمہ) IN NASTALIQ
                                                Text(
                                                    text = token.urdu,
                                                    fontFamily = NastaliqUrduFontFamily,
                                                    fontSize = (settings.urduFontSize * 0.9f).sp,
                                                    color = if (isNight) Gold300 else MushafDarkRed,
                                                    textAlign = TextAlign.Center,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        textDirection = TextDirection.Rtl
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // FLOWING CONTEXTUAL SENTENCE TRANSLATION (بامحاورہ ترجمہ - کنز الایمان)
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isNight) Color(0xFF16251D) else Color(0xFFFFF6F6),
                                    border = BorderStroke(0.5.dp, MushafBorderRed.copy(alpha = 0.35f))
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text(
                                            text = "بامحاورہ ترجمہ (کنز الایمان):",
                                            fontFamily = NastaliqUrduFontFamily,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MushafRed
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = ayah.translationKanzuliman,
                                            fontFamily = NastaliqUrduFontFamily,
                                            fontSize = settings.urduFontSize.sp,
                                            lineHeight = (settings.urduFontSize * 1.65f).sp,
                                            color = if (isNight) Color(0xFFCADBD1) else Color(0xFF222222),
                                            textAlign = TextAlign.Right,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                textDirection = TextDirection.Rtl
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Page Margins
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isNight) Color(0xFF16281F) else Color(0xFFFFF6F6))
                        .border(BorderStroke(0.75.dp, borderColor.copy(alpha = 0.4f)))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "منزل ۱",
                        fontFamily = NastaliqUrduFontFamily,
                        fontSize = 12.sp,
                        color = headerFooterColor
                    )
                    Text(
                        text = "وقف لازم",
                        fontFamily = NastaliqUrduFontFamily,
                        fontSize = 12.sp,
                        color = headerFooterColor
                    )
                }
            }
        }
    }
}
