package com.example.data.model

data class QuranSurah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameUrdu: String,
    val meaningEnglish: String,
    val revelationType: String, // "MAKKI" or "MADANI"
    val ayahsCount: Int,
    val startingPara: Int
) {
    val isMakki: Boolean get() = revelationType.equals("MAKKI", ignoreCase = true)
}

data class QuranPara(
    val number: Int,
    val nameArabic: String,
    val nameUrdu: String,
    val startSurahName: String,
    val startAyah: Int,
    val endSurahName: String,
    val endAyah: Int,
    val totalAyahs: Int
)

data class QuranAyah(
    val numberInQuran: Int,
    val numberInSurah: Int,
    val surahNumber: Int,
    val surahNameArabic: String = "",
    val surahNameEnglish: String = "",
    val paraNumber: Int,
    val textArabic: String,
    val translationKanzuliman: String,
    val isBookmarked: Boolean = false,
    val sajda: Boolean = false
)

data class QuranBookmark(
    val id: String, // e.g. "surah_36_ayah_1" or "para_1_ayah_5"
    val surahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val paraNumber: Int,
    val ayahNumber: Int,
    val previewArabic: String,
    val previewTranslation: String,
    val savedAtFormatted: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuranReadingProgress(
    val lastReadPara: Int = 1,
    val lastReadSurahNumber: Int = 1,
    val lastReadSurahName: String = "Al-Fatihah",
    val lastReadSurahNameArabic: String = "الفاتحة",
    val lastReadAyahNumber: Int = 1,
    val timestampFormatted: String = "Today",
    val timestamp: Long = System.currentTimeMillis()
)

data class QuranReaderSettings(
    val arabicFontSize: Float = 24f,
    val urduFontSize: Float = 16f,
    val showUrduTranslation: Boolean = true,
    val isNightMode: Boolean = true,
    val useParchmentMode: Boolean = false
)

enum class QuranBrowseMode {
    PARAS_30,
    SURAHS_114,
    BOOKMARKS
}
