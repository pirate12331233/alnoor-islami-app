package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.QuranAyah
import com.example.data.model.QuranBookmark
import com.example.data.model.QuranPara
import com.example.data.model.QuranReaderSettings
import com.example.data.model.QuranReadingProgress
import com.example.data.model.QuranStaticData
import com.example.data.model.QuranSurah
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuranRepository private constructor(private val context: Context) {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alnoor_quran_prefs", Context.MODE_PRIVATE)

    private val cacheDir = File(context.cacheDir, "quran_cache").apply { mkdirs() }

    // Static collections
    val allParas = QuranStaticData.allParas
    val allSurahs = QuranStaticData.allSurahs

    // Reactive StateFlows
    private val _currentAyahs = MutableStateFlow<List<QuranAyah>>(emptyList())
    val currentAyahs = _currentAyahs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _readingProgress = MutableStateFlow(loadReadingProgressFromPrefs())
    val readingProgress = _readingProgress.asStateFlow()

    private val _bookmarks = MutableStateFlow(loadBookmarksFromPrefs())
    val bookmarks = _bookmarks.asStateFlow()

    private val _readerSettings = MutableStateFlow(loadSettingsFromPrefs())
    val readerSettings = _readerSettings.asStateFlow()

    init {
        // Pre-load default Surah 1 (Al-Fatihah) or last read surah on init
        val lastProgress = _readingProgress.value
        loadSurah(lastProgress.lastReadSurahNumber)
    }

    fun loadSurah(surahNumber: Int) {
        val safeSurahNum = surahNumber.coerceIn(1, 114)
        val surahInfo = allSurahs.find { it.number == safeSurahNum } ?: allSurahs.first()

        repositoryScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // 1. Check in-memory preloaded static data
            val preloaded = QuranStaticData.getPreloadedSurah(safeSurahNum)
            if (preloaded != null) {
                val markedAyahs = attachBookmarkStatus(preloaded)
                _currentAyahs.value = markedAyahs
                _isLoading.value = false
            }

            // 2. Check disk cache
            val cacheFile = File(cacheDir, "surah_$safeSurahNum.json")
            if (cacheFile.exists() && cacheFile.length() > 0) {
                try {
                    val cachedAyahs = parseSurahJson(cacheFile.readText(), surahInfo)
                    if (cachedAyahs.isNotEmpty()) {
                        _currentAyahs.value = attachBookmarkStatus(cachedAyahs)
                        _isLoading.value = false
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.w("QuranRepository", "Cache read failed for surah $safeSurahNum: ${e.message}")
                }
            }

            // 3. Fetch from verified Quran API endpoint with Kanz-ul-Iman translation
            try {
                val urlString = "https://api.alquran.cloud/v1/surah/$safeSurahNum/editions/quran-uthmani,ur.kanzuliman"
                val jsonResponse = httpGet(urlString)
                if (!jsonResponse.isNullOrBlank()) {
                    val parsed = parseAlquranCloudSurahResponse(jsonResponse, surahInfo)
                    if (parsed.isNotEmpty()) {
                        // Cache response to disk for 100% offline access
                        cacheFile.writeText(jsonResponse)
                        _currentAyahs.value = attachBookmarkStatus(parsed)
                        _isLoading.value = false
                        return@launch
                    }
                }
            } catch (e: Exception) {
                Log.w("QuranRepository", "Network fetch failed for surah $safeSurahNum: ${e.message}")
                if (_currentAyahs.value.isEmpty()) {
                    _errorMessage.value = "Unable to download Surah $safeSurahNum online. Check internet or read preloaded Surahs (Fatihah, Yasin, Mulk, 30th Para)."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPara(paraNumber: Int) {
        val safeParaNum = paraNumber.coerceIn(1, 30)
        val paraInfo = allParas.find { it.number == safeParaNum } ?: allParas.first()

        repositoryScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // 1. Check disk cache
            val cacheFile = File(cacheDir, "para_$safeParaNum.json")
            if (cacheFile.exists() && cacheFile.length() > 0) {
                try {
                    val cachedAyahs = parseParaJson(cacheFile.readText(), safeParaNum)
                    if (cachedAyahs.isNotEmpty()) {
                        _currentAyahs.value = attachBookmarkStatus(cachedAyahs)
                        _isLoading.value = false
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.w("QuranRepository", "Cache read failed for para $safeParaNum: ${e.message}")
                }
            }

            // 2. Fetch Arabic & Kanz-ul-Iman translation for this Juz
            try {
                val arabicUrl = "https://api.alquran.cloud/v1/juz/$safeParaNum/quran-uthmani"
                val urduUrl = "https://api.alquran.cloud/v1/juz/$safeParaNum/ur.kanzuliman"

                val arabicJson = httpGet(arabicUrl)
                val urduJson = httpGet(urduUrl)

                if (!arabicJson.isNullOrBlank() && !urduJson.isNullOrBlank()) {
                    val parsed = parseJuzPairResponse(arabicJson, urduJson, safeParaNum)
                    if (parsed.isNotEmpty()) {
                        // Store paired array to cache
                        val combinedJson = serializeAyahsToJson(parsed)
                        cacheFile.writeText(combinedJson)

                        _currentAyahs.value = attachBookmarkStatus(parsed)
                        _isLoading.value = false
                        return@launch
                    }
                }
            } catch (e: Exception) {
                Log.w("QuranRepository", "Network fetch failed for para $safeParaNum: ${e.message}")
                _errorMessage.value = "Unable to load Para $safeParaNum online. Please check internet connection."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun attachBookmarkStatus(ayahs: List<QuranAyah>): List<QuranAyah> {
        val currentBookmarkIds = _bookmarks.value.map { it.id }.toSet()
        return ayahs.map { ayah ->
            val id = "surah_${ayah.surahNumber}_ayah_${ayah.numberInSurah}"
            ayah.copy(isBookmarked = currentBookmarkIds.contains(id))
        }
    }

    fun updateReadingProgress(
        para: Int,
        surahNumber: Int,
        surahName: String,
        surahNameArabic: String,
        ayahNumber: Int
    ) {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault())
        val formatted = sdf.format(Date(now))

        val progress = QuranReadingProgress(
            lastReadPara = para,
            lastReadSurahNumber = surahNumber,
            lastReadSurahName = surahName,
            lastReadSurahNameArabic = surahNameArabic,
            lastReadAyahNumber = ayahNumber,
            timestampFormatted = formatted,
            timestamp = now
        )

        _readingProgress.value = progress

        prefs.edit().apply {
            putInt("last_read_para", progress.lastReadPara)
            putInt("last_read_surah_number", progress.lastReadSurahNumber)
            putString("last_read_surah_name", progress.lastReadSurahName)
            putString("last_read_surah_name_ar", progress.lastReadSurahNameArabic)
            putInt("last_read_ayah_number", progress.lastReadAyahNumber)
            putString("last_read_timestamp_fmt", progress.timestampFormatted)
            putLong("last_read_timestamp_ms", progress.timestamp)
            apply()
        }
    }

    fun toggleBookmark(ayah: QuranAyah) {
        val bookmarkId = "surah_${ayah.surahNumber}_ayah_${ayah.numberInSurah}"
        val existing = _bookmarks.value.find { it.id == bookmarkId }

        val updated = if (existing != null) {
            _bookmarks.value.filter { it.id != bookmarkId }
        } else {
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            val bookmark = QuranBookmark(
                id = bookmarkId,
                surahNumber = ayah.surahNumber,
                surahNameArabic = ayah.surahNameArabic,
                surahNameEnglish = ayah.surahNameEnglish,
                paraNumber = ayah.paraNumber,
                ayahNumber = ayah.numberInSurah,
                previewArabic = ayah.textArabic.take(120),
                previewTranslation = ayah.translationKanzuliman.take(120),
                savedAtFormatted = sdf.format(Date(now)),
                timestamp = now
            )
            listOf(bookmark) + _bookmarks.value
        }

        _bookmarks.value = updated
        saveBookmarksToPrefs(updated)

        // Update current ayahs bookmark marker
        _currentAyahs.update { list ->
            list.map { item ->
                if (item.surahNumber == ayah.surahNumber && item.numberInSurah == ayah.numberInSurah) {
                    item.copy(isBookmarked = existing == null)
                } else item
            }
        }
    }

    fun removeBookmark(bookmarkId: String) {
        val updated = _bookmarks.value.filter { it.id != bookmarkId }
        _bookmarks.value = updated
        saveBookmarksToPrefs(updated)

        _currentAyahs.update { list ->
            list.map { item ->
                val id = "surah_${item.surahNumber}_ayah_${item.numberInSurah}"
                if (id == bookmarkId) item.copy(isBookmarked = false) else item
            }
        }
    }

    fun updateSettings(settings: QuranReaderSettings) {
        _readerSettings.value = settings
        prefs.edit().apply {
            putFloat("arabic_font_size", settings.arabicFontSize)
            putFloat("urdu_font_size", settings.urduFontSize)
            putBoolean("show_urdu_translation", settings.showUrduTranslation)
            putBoolean("is_night_mode", settings.isNightMode)
            putBoolean("use_parchment_mode", settings.useParchmentMode)
            apply()
        }
    }

    private fun loadReadingProgressFromPrefs(): QuranReadingProgress {
        return QuranReadingProgress(
            lastReadPara = prefs.getInt("last_read_para", 1),
            lastReadSurahNumber = prefs.getInt("last_read_surah_number", 1),
            lastReadSurahName = prefs.getString("last_read_surah_name", "Al-Fatihah") ?: "Al-Fatihah",
            lastReadSurahNameArabic = prefs.getString("last_read_surah_name_ar", "الفاتحة") ?: "الفاتحة",
            lastReadAyahNumber = prefs.getInt("last_read_ayah_number", 1),
            timestampFormatted = prefs.getString("last_read_timestamp_fmt", "Recently") ?: "Recently",
            timestamp = prefs.getLong("last_read_timestamp_ms", System.currentTimeMillis())
        )
    }

    private fun loadBookmarksFromPrefs(): List<QuranBookmark> {
        val jsonStr = prefs.getString("quran_bookmarks_json", null) ?: return emptyList()
        return try {
            val list = mutableListOf<QuranBookmark>()
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    QuranBookmark(
                        id = obj.getString("id"),
                        surahNumber = obj.getInt("surahNumber"),
                        surahNameArabic = obj.optString("surahNameArabic", ""),
                        surahNameEnglish = obj.optString("surahNameEnglish", ""),
                        paraNumber = obj.getInt("paraNumber"),
                        ayahNumber = obj.getInt("ayahNumber"),
                        previewArabic = obj.optString("previewArabic", ""),
                        previewTranslation = obj.optString("previewTranslation", ""),
                        savedAtFormatted = obj.optString("savedAtFormatted", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveBookmarksToPrefs(bookmarks: List<QuranBookmark>) {
        try {
            val arr = JSONArray()
            for (b in bookmarks) {
                val obj = JSONObject().apply {
                    put("id", b.id)
                    put("surahNumber", b.surahNumber)
                    put("surahNameArabic", b.surahNameArabic)
                    put("surahNameEnglish", b.surahNameEnglish)
                    put("paraNumber", b.paraNumber)
                    put("ayahNumber", b.ayahNumber)
                    put("previewArabic", b.previewArabic)
                    put("previewTranslation", b.previewTranslation)
                    put("savedAtFormatted", b.savedAtFormatted)
                    put("timestamp", b.timestamp)
                }
                arr.put(obj)
            }
            prefs.edit().putString("quran_bookmarks_json", arr.toString()).apply()
        } catch (e: Exception) {
            Log.w("QuranRepository", "Error saving bookmarks: ${e.message}")
        }
    }

    private fun loadSettingsFromPrefs(): QuranReaderSettings {
        return QuranReaderSettings(
            arabicFontSize = prefs.getFloat("arabic_font_size", 24f),
            urduFontSize = prefs.getFloat("urdu_font_size", 16f),
            showUrduTranslation = prefs.getBoolean("show_urdu_translation", true),
            isNightMode = prefs.getBoolean("is_night_mode", true),
            useParchmentMode = prefs.getBoolean("use_parchment_mode", false)
        )
    }

    // HTTP Helper
    private suspend fun httpGet(urlString: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 10000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "AlnoorIslami-App/1.0")
            }
            if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else null
        } catch (e: Exception) {
            Log.w("QuranRepository", "HTTP GET error for $urlString: ${e.message}")
            null
        }
    }

    // JSON Parsing Helpers
    private fun parseAlquranCloudSurahResponse(json: String, surahInfo: QuranSurah): List<QuranAyah> {
        val root = JSONObject(json)
        val dataArray = root.getJSONArray("data")
        if (dataArray.length() < 2) return emptyList()

        val arabicData = dataArray.getJSONObject(0)
        val urduData = dataArray.getJSONObject(1)

        val arabicAyahs = arabicData.getJSONArray("ayahs")
        val urduAyahs = urduData.getJSONArray("ayahs")

        val list = mutableListOf<QuranAyah>()
        val count = minOf(arabicAyahs.length(), urduAyahs.length())

        for (i in 0 until count) {
            val arObj = arabicAyahs.getJSONObject(i)
            val urObj = urduAyahs.getJSONObject(i)

            val numInSurah = arObj.getInt("numberInSurah")
            val numInQuran = arObj.getInt("number")
            val juzNum = arObj.optInt("juz", surahInfo.startingPara)
            val textAr = arObj.getString("text")
            val textUr = urObj.getString("text")
            val sajda = arObj.optBoolean("sajda", false)

            list.add(
                QuranAyah(
                    numberInQuran = numInQuran,
                    numberInSurah = numInSurah,
                    surahNumber = surahInfo.number,
                    surahNameArabic = surahInfo.nameArabic,
                    surahNameEnglish = surahInfo.nameEnglish,
                    paraNumber = juzNum,
                    textArabic = textAr,
                    translationKanzuliman = textUr,
                    sajda = sajda
                )
            )
        }
        return list
    }

    private fun parseJuzPairResponse(arabicJson: String, urduJson: String, paraNumber: Int): List<QuranAyah> {
        val arRoot = JSONObject(arabicJson).getJSONObject("data")
        val urRoot = JSONObject(urduJson).getJSONObject("data")

        val arAyahs = arRoot.getJSONArray("ayahs")
        val urAyahs = urRoot.getJSONArray("ayahs")

        val count = minOf(arAyahs.length(), urAyahs.length())
        val list = mutableListOf<QuranAyah>()

        for (i in 0 until count) {
            val arObj = arAyahs.getJSONObject(i)
            val urObj = urAyahs.getJSONObject(i)

            val surahObj = arObj.getJSONObject("surah")
            val surahNum = surahObj.getInt("number")
            val surahNameAr = surahObj.optString("name", "")
            val surahNameEn = surahObj.optString("englishName", "")

            val numInSurah = arObj.getInt("numberInSurah")
            val numInQuran = arObj.getInt("number")
            val textAr = arObj.getString("text")
            val textUr = urObj.getString("text")
            val sajda = arObj.optBoolean("sajda", false)

            list.add(
                QuranAyah(
                    numberInQuran = numInQuran,
                    numberInSurah = numInSurah,
                    surahNumber = surahNum,
                    surahNameArabic = surahNameAr,
                    surahNameEnglish = surahNameEn,
                    paraNumber = paraNumber,
                    textArabic = textAr,
                    translationKanzuliman = textUr,
                    sajda = sajda
                )
            )
        }
        return list
    }

    private fun serializeAyahsToJson(ayahs: List<QuranAyah>): String {
        val arr = JSONArray()
        for (a in ayahs) {
            val obj = JSONObject().apply {
                put("numberInQuran", a.numberInQuran)
                put("numberInSurah", a.numberInSurah)
                put("surahNumber", a.surahNumber)
                put("surahNameArabic", a.surahNameArabic)
                put("surahNameEnglish", a.surahNameEnglish)
                put("paraNumber", a.paraNumber)
                put("textArabic", a.textArabic)
                put("translationKanzuliman", a.translationKanzuliman)
                put("sajda", a.sajda)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseSurahJson(json: String, surahInfo: QuranSurah): List<QuranAyah> {
        return try {
            if (json.trim().startsWith("{")) {
                parseAlquranCloudSurahResponse(json, surahInfo)
            } else {
                val list = mutableListOf<QuranAyah>()
                val arr = JSONArray(json)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        QuranAyah(
                            numberInQuran = obj.getInt("numberInQuran"),
                            numberInSurah = obj.getInt("numberInSurah"),
                            surahNumber = obj.getInt("surahNumber"),
                            surahNameArabic = obj.optString("surahNameArabic", surahInfo.nameArabic),
                            surahNameEnglish = obj.optString("surahNameEnglish", surahInfo.nameEnglish),
                            paraNumber = obj.optInt("paraNumber", surahInfo.startingPara),
                            textArabic = obj.getString("textArabic"),
                            translationKanzuliman = obj.getString("translationKanzuliman"),
                            sajda = obj.optBoolean("sajda", false)
                        )
                    )
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseParaJson(json: String, paraNumber: Int): List<QuranAyah> {
        return try {
            val list = mutableListOf<QuranAyah>()
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    QuranAyah(
                        numberInQuran = obj.getInt("numberInQuran"),
                        numberInSurah = obj.getInt("numberInSurah"),
                        surahNumber = obj.getInt("surahNumber"),
                        surahNameArabic = obj.optString("surahNameArabic", ""),
                        surahNameEnglish = obj.optString("surahNameEnglish", ""),
                        paraNumber = obj.optInt("paraNumber", paraNumber),
                        textArabic = obj.getString("textArabic"),
                        translationKanzuliman = obj.getString("translationKanzuliman"),
                        sajda = obj.optBoolean("sajda", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: QuranRepository? = null

        fun getInstance(context: Context): QuranRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: QuranRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
