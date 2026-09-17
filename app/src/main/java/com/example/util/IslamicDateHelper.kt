package com.example.util

import android.content.Context
import android.os.Build
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object IslamicDateHelper {

    private const val PREFS_NAME = "alnoor_prayer_location_prefs"
    private const val KEY_HIJRI_OFFSET = "hijri_day_offset"
    private const val KEY_CACHED_HIJRI = "cached_live_hijri_date"
    private const val KEY_CACHED_HIJRI_GREG = "cached_live_hijri_gregorian_date"

    val ISLAMIC_MONTHS = listOf(
        "Muharram",
        "Safar",
        "Rabi' al-Awwal",
        "Rabi' al-Thani",
        "Jumada al-Awwal",
        "Jumada al-Thani",
        "Rajab",
        "Sha'ban",
        "Ramadan",
        "Shawwal",
        "Dhu al-Qi'dah",
        "Dhu al-Hijjah"
    )

    val ISLAMIC_MONTHS_ARABIC = listOf(
        "محرّم",
        "صفر",
        "ربيع الأوّل",
        "ربيع الثاني",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوّال",
        "ذو القعدة",
        "ذو الحجة"
    )

    /**
     * Gets user/admin configured moon sighting offset in days (-2, -1, 0, +1, +2).
     */
    fun getOffsetDays(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_HIJRI_OFFSET, 0)
    }

    /**
     * Saves user/admin configured moon sighting offset in days.
     */
    fun saveOffsetDays(context: Context, offset: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_HIJRI_OFFSET, offset).apply()
    }

    /**
     * Computes the real-time dynamic Hijri date for today, considering user offset and cached online values.
     */
    fun getTodayHijriDate(context: Context, customOffset: Int? = null): String {
        val offset = customOffset ?: getOffsetDays(context)
        val todayGregorianKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedGreg = prefs.getString(KEY_CACHED_HIJRI_GREG, "")
        val cachedHijri = prefs.getString(KEY_CACHED_HIJRI, "")

        // If we have a cached online Hijri date for today and offset is 0, use it
        if (cachedGreg == todayGregorianKey && !cachedHijri.isNullOrBlank() && offset == 0) {
            return cachedHijri
        }

        val cal = Calendar.getInstance()
        if (offset != 0) {
            cal.add(Calendar.DAY_OF_YEAR, offset)
        }
        return getHijriDateForCalendar(cal)
    }

    /**
     * Converts a given Calendar instance into a formatted Hijri Date string: "04 Rabi' al-Thani 1448 AH".
     */
    fun getHijriDateForCalendar(cal: Calendar): String {
        // 1. Try Android ICU IslamicCalendar (Umm al-Qura standard) if available (API 24+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val icuCal = android.icu.util.IslamicCalendar()
                icuCal.calculationType = android.icu.util.IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
                icuCal.time = cal.time
                val hYear = icuCal.get(android.icu.util.Calendar.YEAR)
                val hMonthIndex = icuCal.get(android.icu.util.Calendar.MONTH) // 0-based
                val hDay = icuCal.get(android.icu.util.Calendar.DAY_OF_MONTH)

                if (hDay in 1..30 && hMonthIndex in 0..11 && hYear in 1300..1600) {
                    val monthName = ISLAMIC_MONTHS.getOrElse(hMonthIndex) { "Rabi' al-Thani" }
                    val dayFormatted = if (hDay < 10) "0$hDay" else "$hDay"
                    return "$dayFormatted $monthName $hYear AH"
                }
            } catch (t: Throwable) {
                Log.w("IslamicDateHelper", "ICU IslamicCalendar fallback: ${t.message}")
            }
        }

        // 2. High-precision Astronomical Julian Day Umm al-Qura / Kuwaiti calculation
        val year = cal.get(Calendar.YEAR)
        val month1Based = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val (hDay, hMonth, hYear) = gregorianToHijriAstronomical(year, month1Based, day)
        val monthName = ISLAMIC_MONTHS.getOrElse(hMonth - 1) { "Rabi' al-Thani" }
        val dayFormatted = if (hDay < 10) "0$hDay" else "$hDay"
        return "$dayFormatted $monthName $hYear AH"
    }

    /**
     * Converts Gregorian date components into Hijri (Day, Month 1..12, Year)
     * using the astronomical tabular and lunar cycle algorithm.
     */
    fun gregorianToHijriAstronomical(year: Int, month1Based: Int, day: Int): Triple<Int, Int, Int> {
        var y = year
        var m = month1Based
        if (m < 3) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        val jd = (365.25 * (y + 4716)).toLong() + (30.6001 * (m + 1)).toInt() + day + b - 1524
        var l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        l -= 10631 * n - 354
        val j = (((10985 - l) / 5316) * ((50 * l) / 17719) + (l / 5670) * ((43 * l) / 15238)).toInt()
        l = l - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hijriMonth = ((24 * l) / 709).toInt().coerceIn(1, 12)
        val hijriDay = (l - (709 * hijriMonth) / 24).toInt().coerceIn(1, 30)
        val hijriYear = (30 * n + j - 30)
        return Triple(hijriDay, hijriMonth, hijriYear)
    }

    /**
     * Queries Aladhan's API in the background to fetch official country-specific Hijri calendar data.
     */
    fun syncLiveHijriDate(
        context: Context,
        lat: Double,
        lng: Double,
        onDateFetched: ((String) -> Unit)? = null
    ) {
        Thread {
            try {
                val url = URL("https://api.aladhan.com/v1/timings?latitude=$lat&longitude=$lng")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.setRequestProperty("User-Agent", "AlnoorIslamicApp")

                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(response)
                    val dataObj = root.optJSONObject("data")
                    val dateObj = dataObj?.optJSONObject("date")
                    val hijriObj = dateObj?.optJSONObject("hijri")

                    if (hijriObj != null) {
                        val rawDay = hijriObj.optString("day", "")
                        val monthObj = hijriObj.optJSONObject("month")
                        val monthNumber = monthObj?.optInt("number", 0) ?: 0
                        val year = hijriObj.optString("year", "")
                        val designation = hijriObj.optJSONObject("designation")?.optString("abbreviated", "AH") ?: "AH"

                        val monthName = if (monthNumber in 1..12) {
                            ISLAMIC_MONTHS[monthNumber - 1]
                        } else {
                            monthObj?.optString("en", "") ?: ""
                        }

                        val dayInt = rawDay.toIntOrNull()
                        val dayFormatted = if (dayInt != null && dayInt < 10) "0$dayInt" else rawDay

                        if (dayFormatted.isNotBlank() && monthName.isNotBlank() && year.isNotBlank()) {
                            val fullHijri = "$dayFormatted $monthName $year $designation"

                            // Cache for today
                            val todayGregorianKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString(KEY_CACHED_HIJRI, fullHijri)
                                .putString(KEY_CACHED_HIJRI_GREG, todayGregorianKey)
                                .putString("date_hijri", fullHijri)
                                .apply()

                            onDateFetched?.invoke(fullHijri)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("IslamicDateHelper", "Error fetching live Hijri date from Aladhan: ${e.message}")
            }
        }.start()
    }
}
