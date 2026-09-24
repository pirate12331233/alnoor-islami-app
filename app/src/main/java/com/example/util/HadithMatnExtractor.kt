package com.example.util

/**
 * Smart Matn Extractor for Authentic Hadiths.
 *
 * Keeps all 15,152 Hadiths in the local database, but automatically filters out
 * the long, repetitive transmission Sanad chains ("حدثنا فلان عن فلان...")
 * to highlight the core statement and guidance of the Prophet ﷺ ("قال رسول الله ﷺ...")
 * and provide a simple, clean, easily understandable "Mafhoom" (مفہومِ حدیث) for daily reading.
 */
object HadithMatnExtractor {

    // Common Arabic Sanad transition markers indicating the beginning of the Matn
    private val ARABIC_MATN_MARKERS = listOf(
        "قَالَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ",
        "قَالَ رَسُولُ اللَّهِ صلى الله عليه وسلم",
        "قَالَ رَسُولُ اللَّهِ صلى الله عليه وسلم",
        "قَالَ رَسُولُ اللَّهِ ﷺ",
        "قَالَ رَسُولُ اللَّهِ ﷺ",
        "أَنَّ رَسُولَ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ",
        "أَنَّ رَسُولَ اللَّهِ صلى الله عليه وسلم قَالَ",
        "أَنَّ رَسُولَ اللَّهِ صلى الله عليه وسلم قَالَ",
        "سَمِعْتُ رَسُولَ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يَقُولُ",
        "سَمِعْتُ رَسُولَ اللَّهِ صلى الله عليه وسلم يَقُولُ",
        "عَنِ النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ",
        "عَنِ النَّبِيِّ صلى الله عليه وسلم قَالَ",
        "عَنِ النَّبِيِّ صلى الله عليه وسلم قَالَ",
        "أَنَّ النَّبِيَّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ",
        "أَنَّ النَّبِيَّ صلى الله عليه وسلم قَالَ",
        "قَالَ النَّبِيُّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ",
        "قَالَ النَّبِيُّ صلى الله عليه وسلم",
        "فَقَالَ رَسُولُ اللَّهِ صلى الله عليه وسلم",
        "فَقَالَ النَّبِيُّ صلى الله عليه وسلم",
        "رَسُولُ اللَّهِ صلى الله عليه وسلم:"
    )

    // Simplified non-tashkeel markers for fallback matching
    private val ARABIC_SIMPLE_MARKERS = listOf(
        "قال رسول الله صلى الله عليه وسلم",
        "قال رسول الله ﷺ",
        "أن رسول الله صلى الله عليه وسلم قال",
        "أن رسول الله ﷺ قال",
        "سمعت رسول الله صلى الله عليه وسلم",
        "عن النبي صلى الله عليه وسلم قال",
        "عن النبي ﷺ قال",
        "أن النبي صلى الله عليه وسلم قال",
        "قال النبي صلى الله عليه وسلم",
        "فقال رسول الله صلى الله عليه وسلم",
        "فقال النبي صلى الله عليه وسلم"
    )

    // Urdu markers where narrator chain ends and the Prophet's ﷺ statement begins
    private val URDU_MAFHOOM_MARKERS = listOf(
        "رسول اللہ صلی اللہ علیہ وسلم نے فرمایا:",
        "رسول اللہ صلی اللہ علیہ وسلم نے فرمایا",
        "نبی کریم صلی اللہ علیہ وسلم نے فرمایا:",
        "نبی کریم صلی اللہ علیہ وسلم نے فرمایا",
        "آپ صلی اللہ علیہ وسلم نے فرمایا:",
        "آپ صلی اللہ علیہ وسلم نے فرمایا",
        "رسول اللہ صلی اللہ علیہ وسلم کا ارشاد ہے:",
        "رسول اللہ صلی اللہ علیہ وسلم کا ارشاد ہے",
        "آنحضرت صلی اللہ علیہ وسلم نے فرمایا:",
        "آنحضرت صلی اللہ علیہ وسلم نے فرمایا",
        "نبی اکرم صلی اللہ علیہ وسلم نے فرمایا:",
        "نبی اکرم صلی اللہ علیہ وسلم نے فرمایا"
    )

    private val URDU_SUB_MARKERS = listOf(
        "کہ رسول اللہ صلی اللہ علیہ وسلم نے فرمایا",
        "کہ نبی کریم صلی اللہ علیہ وسلم نے فرمایا",
        "کہ آپ صلی اللہ علیہ وسلم نے فرمایا",
        "کہ فرمایا:"
    )

    // English markers where the Prophet's ﷺ statement begins
    private val ENGLISH_MATN_MARKERS = listOf(
        "Allah's Messenger (ﷺ) said:",
        "Allah's Messenger (ﷺ) said,",
        "the Messenger of Allah (ﷺ) said:",
        "the Messenger of Allah (ﷺ) said,",
        "the Prophet (ﷺ) said:",
        "the Prophet (ﷺ) said,",
        "Allah's Apostle said:",
        "Allah's Apostle said,",
        "I heard Allah's Messenger (ﷺ) saying,",
        "The Messenger of Allah said:"
    )

    /**
     * Extracts the clean Matn from the Arabic text, omitting the initial narrator Sanad chain.
     * If the text already starts directly or no long Sanad is detected, returns the original text.
     */
    fun extractMatnArabic(fullArabic: String): String {
        val trimmed = fullArabic.trim()
        if (trimmed.length < 80) return trimmed

        // 1. Direct search with exact marker
        for (marker in ARABIC_MATN_MARKERS) {
            val idx = trimmed.indexOf(marker)
            if (idx > 0) {
                val candidate = trimmed.substring(idx).trim()
                if (candidate.length >= 25) {
                    return candidate
                }
            }
        }

        // 2. Normalized search without harakat
        val stripped = stripArabicDiacritics(trimmed)
        for (marker in ARABIC_SIMPLE_MARKERS) {
            val normMarker = stripArabicDiacritics(marker)
            val idxNorm = stripped.indexOf(normMarker)
            if (idxNorm > 0) {
                // Approximate original index by character ratio
                val approxIdx = mapNormalizedIndexToOriginal(trimmed, stripped, idxNorm)
                if (approxIdx in 0 until trimmed.length) {
                    val candidate = trimmed.substring(approxIdx).trim()
                    if (candidate.length >= 25) {
                        return candidate
                    }
                }
            }
        }

        // 3. Fallback: check if starts with "حَدَّثَنَا" and has a later "قَالَ:"
        if (trimmed.startsWith("حَدَّثَنَا") || trimmed.startsWith("حدثنا") || trimmed.startsWith("أَخْبَرَنَا")) {
            val colonIdx = trimmed.indexOf(":")
            if (colonIdx in 30..400 && colonIdx + 1 < trimmed.length) {
                val afterColon = trimmed.substring(colonIdx + 1).trim()
                if (afterColon.length >= 30) {
                    return afterColon
                }
            }
        }

        return trimmed
    }

    /**
     * Extracts the clean, easy-to-understand Mafhoom from the Urdu translation.
     * Removes the long chain of narrators ("فلان نے ہم سے بیان کیا، ان کو فلان نے خبر دی...")
     * and keeps the core teaching ("رسول اللہ صلی اللہ علیہ وسلم نے فرمایا: ...").
     */
    fun extractMafhoomUrdu(fullUrdu: String): String {
        val trimmed = fullUrdu.trim()
        if (trimmed.length < 70) return trimmed

        for (marker in URDU_MAFHOOM_MARKERS) {
            val idx = trimmed.indexOf(marker)
            if (idx > 0) {
                val candidate = trimmed.substring(idx).trim()
                if (candidate.length >= 25) {
                    return candidate
                }
            }
        }

        for (sub in URDU_SUB_MARKERS) {
            val idx = trimmed.indexOf(sub)
            if (idx > 0) {
                val subCandidate = trimmed.substring(idx).removePrefix("کہ ").trim()
                if (subCandidate.length >= 25) {
                    return subCandidate
                }
            }
        }

        // If it starts with typical narrator chain indicator and contains "کہ"
        if (trimmed.contains("نے ہم سے حدیث بیان کی") || trimmed.contains("نے بیان کیا") || trimmed.contains("نے خبر دی")) {
            val kehIndex = trimmed.indexOf("کہ ")
            if (kehIndex in 25..350 && kehIndex + 3 < trimmed.length) {
                val afterKeh = trimmed.substring(kehIndex + 3).trim()
                if (afterKeh.length >= 30) {
                    return afterKeh
                }
            }
        }

        return trimmed
    }

    /**
     * Extracts the concise English statement, removing the chain of "Narrated [Narrator]: ...".
     */
    fun extractMatnEnglish(fullEnglish: String): String {
        val trimmed = fullEnglish.trim()
        if (trimmed.length < 70) return trimmed

        for (marker in ENGLISH_MATN_MARKERS) {
            val idx = trimmed.indexOf(marker, ignoreCase = true)
            if (idx > 0) {
                val candidate = trimmed.substring(idx).trim()
                if (candidate.length >= 25) {
                    return candidate
                }
            }
        }

        // If starts with "Narrated ...: "
        if (trimmed.startsWith("Narrated", ignoreCase = true)) {
            val firstColon = trimmed.indexOf(':')
            if (firstColon in 10..180 && firstColon + 1 < trimmed.length) {
                val afterColon = trimmed.substring(firstColon + 1).trim()
                if (afterColon.length >= 25) {
                    return afterColon
                }
            }
        }

        return trimmed
    }

    /**
     * Determines whether the Hadith text has a separable Sanad transmission chain.
     */
    fun hasSeparableSanad(arabic: String, urdu: String): Boolean {
        val cleanAr = extractMatnArabic(arabic)
        val cleanUr = extractMafhoomUrdu(urdu)
        return (cleanAr.length < arabic.trim().length - 15) || (cleanUr.length < urdu.trim().length - 15)
    }

    private fun stripArabicDiacritics(input: String): String {
        return input.replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]"), "")
    }

    private fun mapNormalizedIndexToOriginal(original: String, normalized: String, normIndex: Int): Int {
        var normCount = 0
        for (i in original.indices) {
            if (normCount >= normIndex) return i
            val c = original[i]
            // check if character is a diacritic
            val isDiacritic = c in '\u064B'..'\u065F' || c == '\u0670' || c in '\u06D6'..'\u06ED'
            if (!isDiacritic) {
                normCount++
            }
        }
        return original.length
    }
}
