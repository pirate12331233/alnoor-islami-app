package com.example.util

import com.example.data.model.CommunityEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object EventDateHelper {

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

    val ISLAMIC_MONTHS_WITH_ARABIC = listOf(
        "1. Muharram (محرّم)",
        "2. Safar (صفر)",
        "3. Rabi' al-Awwal (ربيع الأوّل)",
        "4. Rabi' al-Thani (ربيع الثاني)",
        "5. Jumada al-Awwal (جمادى الأولى)",
        "6. Jumada al-Thani (جمادى الآخرة)",
        "7. Rajab (رجب)",
        "8. Sha'ban (شعبان)",
        "9. Ramadan (رمضان)",
        "10. Shawwal (شوّال)",
        "11. Dhu al-Qi'dah (ذو القعدة)",
        "12. Dhu al-Hijjah (ذو الحجة)"
    )

    /**
     * Parses diverse Gregorian date string formats into milliseconds timestamp.
     */
    fun parseEventDateMillis(dateStr: String): Long {
        if (dateStr.isBlank()) return Long.MAX_VALUE
        val clean = dateStr.trim()
        val formats = listOf(
            "EEEE, MMM dd, yyyy",
            "EEEE, MMMM dd, yyyy",
            "EEEE, d MMM yyyy",
            "EEEE, dd MMMM yyyy",
            "MMM dd, yyyy",
            "MMMM dd, yyyy",
            "dd MMM yyyy",
            "d MMM yyyy",
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "MM/dd/yyyy",
            "dd/MM/yyyy"
        )
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                sdf.isLenient = true
                val date = sdf.parse(clean)
                if (date != null) {
                    return date.time
                }
            } catch (_: Exception) {}
        }
        return Long.MAX_VALUE
    }

    /**
     * Sorts events so that nearby coming events (today or future) appear first,
     * ordered from closest to furthest in the future, followed by past events in date-wise order.
     */
    fun sortEventsUpcomingFirst(events: List<CommunityEvent>): List<CommunityEvent> {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val parsedEvents = events.map { event ->
            event to parseEventDateMillis(event.dateGregorian)
        }

        // 1. Upcoming events (today or future): sorted nearest first (ascending time)
        val upcoming = parsedEvents
            .filter { it.second >= todayStart && it.second != Long.MAX_VALUE }
            .sortedBy { it.second }
            .map { it.first }

        // 2. Remaining / past events: sorted date-wise (descending or ascending)
        val past = parsedEvents
            .filter { it.second < todayStart && it.second != Long.MAX_VALUE }
            .sortedByDescending { it.second }
            .map { it.first }

        // 3. Any unparsed events
        val unparsed = parsedEvents
            .filter { it.second == Long.MAX_VALUE }
            .map { it.first }

        return upcoming + past + unparsed
    }

    /**
     * Formats calendar into full display date e.g. "Friday, Sep 18, 2026"
     */
    fun formatGregorianDate(cal: Calendar): String {
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.ENGLISH)
        return sdf.format(cal.time)
    }

    /**
     * Computes the day name e.g. "Friday", "Saturday"
     */
    fun getAutoDayName(cal: Calendar): String {
        val sdf = SimpleDateFormat("EEEE", Locale.ENGLISH)
        return sdf.format(cal.time)
    }

    /**
     * Converts a Gregorian date into Islamic (Hijri) date: Triple(day, month [1..12], year)
     */
    fun gregorianToHijri(year: Int, monthZeroIndexed: Int, day: Int): Triple<Int, Int, Int> {
        var m = monthZeroIndexed + 1
        var y = year
        if (m <= 2) {
            m += 12
            y -= 1
        }
        val a = (y / 100)
        val b = 2 - a + (a / 4)
        val jd = (365.25 * (y + 4716)).toLong() + (30.6001 * (m + 1)).toInt() + day + b - 1524

        val z = jd - 1948440
        val hijriYear = ((30 * z + 10646) / 10631).toInt()
        val remainderYear = z - ((10631 * hijriYear - 10617) / 30)
        val hijriMonth = minOf(12, maxOf(1, ((remainderYear / 29.5) + 1).toInt()))
        val hijriDay = minOf(30, maxOf(1, (remainderYear - (29.5 * (hijriMonth - 1))).toInt()))
        return Triple(hijriDay, hijriMonth, hijriYear)
    }

    /**
     * Formats Hijri date into standard display string e.g. "15 Safar 1448 AH"
     */
    fun formatHijriDate(day: Int, monthIndexZeroBased: Int, year: Int): String {
        val safeMonth = ISLAMIC_MONTHS.getOrElse(monthIndexZeroBased) { "Safar" }
        val formattedDay = if (day < 10) "0$day" else "$day"
        return "$formattedDay $safeMonth $year AH"
    }

    /**
     * Estimates Hijri date string directly from a Calendar instance
     */
    fun estimateHijriDateFromCalendar(cal: Calendar): String {
        val (hDay, hMonth, hYear) = gregorianToHijri(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return formatHijriDate(hDay, hMonth - 1, hYear)
    }
}
