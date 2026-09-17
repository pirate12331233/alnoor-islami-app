package com.example

import com.example.data.model.PrayerTimesData
import com.example.util.PrayerLocationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class PrayerCalculationTest {

    private val samplePrayerData = PrayerTimesData(
        dateGregorian = "Wednesday, September 16, 2026",
        dateHijri = "05 Rabi' al-Thani 1448 AH",
        locationName = "Lahore, Pakistan",
        countryName = "Pakistan",
        fajr = "04:29 AM",
        fajrIqamah = "04:49 AM",
        sunrise = "05:52 AM",
        dhuhr = "12:01 PM",
        dhuhrIqamah = "12:16 PM",
        asr = "03:46 PM",
        asrIqamah = "04:01 PM",
        maghrib = "06:10 PM",
        maghribIqamah = "06:20 PM",
        isha = "07:31 PM",
        ishaIqamah = "07:51 PM",
        tahajjud = "03:45 AM",
        qiblaDirectionDeg = 261.0f,
        calculationMethod = "Automatic",
        isAutoDetected = true
    )

    private fun getCalendarFor(hour24: Int, minute: Int): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour24)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    @Test
    fun testParseTimeToMinutes() {
        assertEquals(269, PrayerLocationService.parseTimeToMinutes("04:29 AM"))
        assertEquals(721, PrayerLocationService.parseTimeToMinutes("12:01 PM"))
        assertEquals(946, PrayerLocationService.parseTimeToMinutes("03:46 PM"))
        assertEquals(1090, PrayerLocationService.parseTimeToMinutes("06:10 PM"))
        assertEquals(1171, PrayerLocationService.parseTimeToMinutes("07:31 PM"))
        assertEquals(225, PrayerLocationService.parseTimeToMinutes("03:45 AM"))
        assertEquals(1267, PrayerLocationService.parseTimeToMinutes("09:07 PM"))
    }

    @Test
    fun testUserScenario_At907PM_NextPrayerIsTahajjudQiyam() {
        // User screenshot scenario: 9:07 PM (21:07), after Maghrib (6:10 PM) and Isha (7:31 PM)
        val cal = getCalendarFor(21, 7)
        val nextPrayer = PrayerLocationService.getNextPrayer(samplePrayerData, cal)

        assertNotNull(nextPrayer)
        assertEquals("Tahajjud", nextPrayer.highlightKey)
        assertEquals("Tahajjud (Qiyam)", nextPrayer.displayName)
        assertEquals("03:45 AM", nextPrayer.adhanTime)
    }

    @Test
    fun testEarlyMorning_BeforeTahajjud() {
        // 01:30 AM (01:30) -> Before Tahajjud (03:45 AM)
        val cal = getCalendarFor(1, 30)
        val nextPrayer = PrayerLocationService.getNextPrayer(samplePrayerData, cal)

        assertEquals("Tahajjud", nextPrayer.highlightKey)
        assertEquals("03:45 AM", nextPrayer.adhanTime)
    }

    @Test
    fun testEarlyMorning_AfterTahajjudBeforeFajr() {
        // 04:00 AM (04:00) -> After Tahajjud (03:45 AM), before Fajr (04:29 AM)
        val cal = getCalendarFor(4, 0)
        val nextPrayer = PrayerLocationService.getNextPrayer(samplePrayerData, cal)

        assertEquals("Fajr", nextPrayer.highlightKey)
        assertEquals("04:29 AM", nextPrayer.adhanTime)
    }

    @Test
    fun testMorning_AfterFajrBeforeDhuhr() {
        // 09:30 AM (09:30) -> Before Dhuhr (12:01 PM)
        val cal = getCalendarFor(9, 30)
        val nextPrayer = PrayerLocationService.getNextPrayer(samplePrayerData, cal)

        assertEquals("Dhuhr", nextPrayer.highlightKey)
        assertEquals("12:01 PM", nextPrayer.adhanTime)
    }

    @Test
    fun testAfternoon_BetweenDhuhrAndAsr() {
        // 01:30 PM (13:30) -> Before Asr (03:46 PM)
        val cal = getCalendarFor(13, 30)
        val nextPrayer = PrayerLocationService.getNextPrayer(samplePrayerData, cal)

        assertEquals("Asr", nextPrayer.highlightKey)
        assertEquals("03:46 PM", nextPrayer.adhanTime)
    }

    @Test
    fun testLateAfternoon_BetweenAsrAndMaghrib() {
        // 05:00 PM (17:00) -> Before Maghrib (06:10 PM)
        val cal = getCalendarFor(17, 0)
        val nextPrayer = PrayerLocationService.getNextPrayer(samplePrayerData, cal)

        assertEquals("Maghrib", nextPrayer.highlightKey)
        assertEquals("06:10 PM", nextPrayer.adhanTime)
    }

    @Test
    fun testEvening_BetweenMaghribAndIsha() {
        // 06:45 PM (18:45) -> Before Isha (07:31 PM)
        val cal = getCalendarFor(18, 45)
        val nextPrayer = PrayerLocationService.getNextPrayer(samplePrayerData, cal)

        assertEquals("Isha", nextPrayer.highlightKey)
        assertEquals("07:31 PM", nextPrayer.adhanTime)
    }
}
