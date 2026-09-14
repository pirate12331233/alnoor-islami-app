package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import com.example.data.model.PrayerTimesData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object PrayerLocationService {

    // Kaaba Coordinates in Makkah
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206
    private const val PREFS_NAME = "alnoor_prayer_location_prefs"
    private const val TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000L

    /**
     * Checks if 24 hours have passed since the last device location and prayer timings sync.
     */
    fun shouldSyncLocationDaily(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastSync = prefs.getLong("last_location_prayer_sync_timestamp", 0L)
        val now = System.currentTimeMillis()
        return (now - lastSync) >= TWENTY_FOUR_HOURS_MS || lastSync == 0L
    }

    fun getLastSyncTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong("last_location_prayer_sync_timestamp", 0L)
    }

    fun getSavedCoordinates(context: Context): Pair<Double, Double>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains("saved_latitude") && prefs.contains("saved_longitude")) {
            val lat = prefs.getFloat("saved_latitude", 0f).toDouble()
            val lng = prefs.getFloat("saved_longitude", 0f).toDouble()
            if (lat != 0.0 || lng != 0.0) {
                return Pair(lat, lng)
            }
        }
        return null
    }

    /**
     * Saves the latest PrayerTimesData and coordinates into persistent storage for offline and future use.
     */
    fun savePrayerTimes(
        context: Context,
        data: PrayerTimesData,
        lat: Double? = null,
        lng: Double? = null,
        updateTimestamp: Boolean = true
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .putString("date_gregorian", data.dateGregorian)
            .putString("date_hijri", data.dateHijri)
            .putString("location_name", data.locationName)
            .putString("country_name", data.countryName)
            .putString("fajr", data.fajr)
            .putString("fajr_iqamah", data.fajrIqamah)
            .putString("sunrise", data.sunrise)
            .putString("dhuhr", data.dhuhr)
            .putString("dhuhr_iqamah", data.dhuhrIqamah)
            .putString("asr", data.asr)
            .putString("asr_iqamah", data.asrIqamah)
            .putString("maghrib", data.maghrib)
            .putString("maghrib_iqamah", data.maghribIqamah)
            .putString("isha", data.isha)
            .putString("isha_iqamah", data.ishaIqamah)
            .putString("tahajjud", data.tahajjud)
            .putFloat("qibla_deg", data.qiblaDirectionDeg)
            .putString("calc_method", data.calculationMethod)
            .putBoolean("is_auto_detected", data.isAutoDetected)

        if (lat != null) editor.putFloat("saved_latitude", lat.toFloat())
        if (lng != null) editor.putFloat("saved_longitude", lng.toFloat())
        if (updateTimestamp) editor.putLong("last_location_prayer_sync_timestamp", System.currentTimeMillis())

        editor.apply()
    }

    /**
     * Loads the stored PrayerTimesData from persistent storage for immediate launch / offline use.
     */
    fun loadPrayerTimes(context: Context): PrayerTimesData {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains("fajr")) {
            val defaultTimes = getDefaultPrayerTimes()
            savePrayerTimes(context, defaultTimes, getDefaultLatitudeForLocale(), getDefaultLongitudeForLocale(), updateTimestamp = false)
            return defaultTimes
        }

        val today = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())

        return PrayerTimesData(
            dateGregorian = prefs.getString("date_gregorian", today) ?: today,
            dateHijri = prefs.getString("date_hijri", "18 Safar 1448 AH") ?: "18 Safar 1448 AH",
            locationName = prefs.getString("location_name", "Alnoor Mosque Complex") ?: "Alnoor Mosque Complex",
            countryName = prefs.getString("country_name", getCountryForLocale()) ?: getCountryForLocale(),
            fajr = prefs.getString("fajr", "05:05 AM") ?: "05:05 AM",
            fajrIqamah = prefs.getString("fajr_iqamah", "05:30 AM") ?: "05:30 AM",
            sunrise = prefs.getString("sunrise", "06:22 AM") ?: "06:22 AM",
            dhuhr = prefs.getString("dhuhr", "01:15 PM") ?: "01:15 PM",
            dhuhrIqamah = prefs.getString("dhuhr_iqamah", "01:45 PM") ?: "01:45 PM",
            asr = prefs.getString("asr", "05:10 PM") ?: "05:10 PM",
            asrIqamah = prefs.getString("asr_iqamah", "05:30 PM") ?: "05:30 PM",
            maghrib = prefs.getString("maghrib", "07:55 PM") ?: "07:55 PM",
            maghribIqamah = prefs.getString("maghrib_iqamah", "08:00 PM") ?: "08:00 PM",
            isha = prefs.getString("isha", "09:20 PM") ?: "09:20 PM",
            ishaIqamah = prefs.getString("isha_iqamah", "09:45 PM") ?: "09:45 PM",
            tahajjud = prefs.getString("tahajjud", "03:45 AM") ?: "03:45 AM",
            qiblaDirectionDeg = prefs.getFloat("qibla_deg", 67.5f),
            calculationMethod = prefs.getString("calc_method", "Automatic Device Location") ?: "Automatic Device Location",
            isAutoDetected = prefs.getBoolean("is_auto_detected", true)
        )
    }

    /**
     * Automatically detect device location via GPS / Network / Geocoder, calculate exact prayer times,
     * save them into SharedPreferences for future use, and return the new data.
     */
    @SuppressLint("MissingPermission")
    fun detectLocationAndCalculatePrayerTimes(
        context: Context,
        onResult: (PrayerTimesData) -> Unit
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

        var bestLocation: Location? = null
        try {
            val providers = locationManager?.getProviders(true) ?: emptyList()
            for (provider in providers) {
                val loc = locationManager?.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < (bestLocation?.accuracy ?: Float.MAX_VALUE)) {
                    bestLocation = loc
                }
            }
        } catch (e: Exception) {
            Log.w("PrayerLocationService", "Location access error: ${e.message}")
        }

        val savedCoords = getSavedCoordinates(context)
        val latitude = bestLocation?.latitude ?: savedCoords?.first ?: getDefaultLatitudeForLocale()
        val longitude = bestLocation?.longitude ?: savedCoords?.second ?: getDefaultLongitudeForLocale()

        var city = "Local Area"
        var country = getCountryForLocale()

        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address: Address = addresses[0]
                    city = address.locality ?: address.subAdminArea ?: address.adminArea ?: address.featureName ?: "Local City"
                    country = address.countryName ?: address.countryCode ?: country
                }
            }
        } catch (e: Exception) {
            Log.w("PrayerLocationService", "Geocoder resolution fallback: ${e.message}")
        }

        val timesData = calculatePrayerTimesForCoordinates(
            lat = latitude,
            lng = longitude,
            locationName = "$city, $country",
            countryName = country
        )

        // Save persistently for future use and mark sync timestamp
        savePrayerTimes(context, timesData, latitude, longitude, updateTimestamp = true)

        onResult(timesData)
    }

    private fun getDefaultPrayerTimes(): PrayerTimesData {
        val today = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date())
        return PrayerTimesData(
            dateGregorian = today,
            dateHijri = "18 Safar 1448 AH",
            locationName = "Alnoor Mosque Complex",
            countryName = getCountryForLocale(),
            fajr = "05:05 AM",
            fajrIqamah = "05:30 AM",
            sunrise = "06:22 AM",
            dhuhr = "01:15 PM",
            dhuhrIqamah = "01:45 PM",
            asr = "05:10 PM",
            asrIqamah = "05:30 PM",
            maghrib = "07:55 PM",
            maghribIqamah = "08:00 PM",
            isha = "09:20 PM",
            ishaIqamah = "09:45 PM",
            tahajjud = "03:45 AM",
            qiblaDirectionDeg = 67.5f,
            calculationMethod = "Automatic Device Location",
            isAutoDetected = true
        )
    }

    private fun getDefaultLatitudeForLocale(): Double {
        val country = Locale.getDefault().country.uppercase()
        return when (country) {
            "PK" -> 31.5204 // Lahore / Islamabad
            "GB", "UK" -> 51.5074 // London
            "US" -> 40.7128 // New York
            "SA" -> 21.4225 // Makkah
            "AE" -> 25.2048 // Dubai
            "CA" -> 43.6532 // Toronto
            "IN" -> 28.6139 // Delhi
            "TR" -> 41.0082 // Istanbul
            "EG" -> 30.0444 // Cairo
            else -> 31.5204
        }
    }

    private fun getDefaultLongitudeForLocale(): Double {
        val country = Locale.getDefault().country.uppercase()
        return when (country) {
            "PK" -> 74.3587
            "GB", "UK" -> -0.1278
            "US" -> -74.0060
            "SA" -> 39.8262
            "AE" -> 55.2708
            "CA" -> -79.3832
            "IN" -> 77.2090
            "TR" -> 28.9784
            "EG" -> 31.2357
            else -> 74.3587
        }
    }

    private fun getCountryForLocale(): String {
        val display = Locale.getDefault().displayCountry
        return if (display.isNotBlank()) display else "United States"
    }

    fun calculatePrayerTimesForCoordinates(
        lat: Double,
        lng: Double,
        locationName: String,
        countryName: String
    ): PrayerTimesData {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val timeZoneOffset = TimeZone.getDefault().rawOffset / 3600000.0 + (if (TimeZone.getDefault().inDaylightTime(Date())) 1.0 else 0.0)

        // Solar Declination & Equation of Time calculation
        val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
        val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b) // Equation of time in minutes
        val declination = Math.toRadians(23.45 * sin(Math.toRadians((360.0 / 365.0) * (dayOfYear - 81)))) // Declination angle

        val latRad = Math.toRadians(lat)

        // Solar Noon in local standard time
        val solarNoonMinutes = 720.0 - (4.0 * lng) - eot + (timeZoneOffset * 60.0)
        val dhuhrHour = solarNoonMinutes / 60.0

        // Fajr & Isha Sun Angles
        val fajrAngle = Math.toRadians(-18.0) // Astronomical twilight
        val ishaAngle = Math.toRadians(-17.5)

        // Hour angles calculation
        fun getHourAngle(angleRad: Double): Double {
            val cosH = (sin(angleRad) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))
            val clamped = cosH.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(clamped)) / 15.0
        }

        // Sunrise & Sunset (zenith = -0.833 degrees)
        val sunriseZenith = Math.toRadians(-0.833)
        val sunriseHourAngle = getHourAngle(sunriseZenith)

        val sunriseHour = dhuhrHour - sunriseHourAngle
        val sunsetHour = dhuhrHour + sunriseHourAngle

        // Fajr & Isha
        val fajrHourAngle = getHourAngle(fajrAngle)
        val fajrHour = dhuhrHour - fajrHourAngle

        val ishaHourAngle = getHourAngle(ishaAngle)
        val ishaHour = dhuhrHour + ishaHourAngle

        // Asr (Shafi/Hanafi shadow calculation)
        val shadowFactor = 1.2 // Average between standard and Hanafi
        val asrAngle = Math.atan(1.0 / (shadowFactor + tan(Math.abs(latRad - declination))))
        val asrHourAngle = Math.toDegrees(acos(((sin(asrAngle) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))).coerceIn(-1.0, 1.0))) / 15.0
        val asrHour = dhuhrHour + asrHourAngle

        // Tahajjud: last third of night between Isha and Fajr
        val nightDuration = (24.0 - ishaHour) + fajrHour
        val tahajjudHour = (ishaHour + (nightDuration * 0.67)) % 24.0

        // Qibla Direction
        val deltaLng = Math.toRadians(KAABA_LNG - lng)
        val y = sin(deltaLng)
        val x = cos(latRad) * tan(Math.toRadians(KAABA_LAT)) - sin(latRad) * cos(deltaLng)
        var qiblaDeg = Math.toDegrees(atan2(y, x)).toFloat()
        if (qiblaDeg < 0) qiblaDeg += 360f

        val gregorianFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        val dateGregorian = gregorianFormat.format(Date())

        val fajrStr = formatHourToTime(fajrHour)
        val sunriseStr = formatHourToTime(sunriseHour)
        val dhuhrStr = formatHourToTime(dhuhrHour)
        val asrStr = formatHourToTime(asrHour)
        val maghribStr = formatHourToTime(sunsetHour)
        val ishaStr = formatHourToTime(ishaHour)
        val tahajjudStr = formatHourToTime(tahajjudHour)

        return PrayerTimesData(
            dateGregorian = dateGregorian,
            dateHijri = "18 Safar 1448 AH",
            locationName = locationName,
            countryName = countryName,
            fajr = fajrStr,
            fajrIqamah = addMinutesToTime(fajrStr, 20),
            sunrise = sunriseStr,
            dhuhr = dhuhrStr,
            dhuhrIqamah = addMinutesToTime(dhuhrStr, 15),
            asr = asrStr,
            asrIqamah = addMinutesToTime(asrStr, 15),
            maghrib = maghribStr,
            maghribIqamah = addMinutesToTime(maghribStr, 10),
            isha = ishaStr,
            ishaIqamah = addMinutesToTime(ishaStr, 20),
            tahajjud = tahajjudStr,
            qiblaDirectionDeg = qiblaDeg,
            calculationMethod = "Automatic Device Location (${countryName})",
            isAutoDetected = true
        )
    }

    private fun formatHourToTime(decimalHour: Double): String {
        var hour = floor(decimalHour).toInt() % 24
        if (hour < 0) hour += 24
        val minute = floor((decimalHour - floor(decimalHour)) * 60).toInt().coerceIn(0, 59)

        val isPm = hour >= 12
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (isPm) "PM" else "AM"
        return String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
    }

    private fun addMinutesToTime(timeStr: String, minutesToAdd: Int): String {
        try {
            val format = SimpleDateFormat("hh:mm a", Locale.US)
            val date = format.parse(timeStr) ?: return timeStr
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MINUTE, minutesToAdd)
            return format.format(cal.time)
        } catch (_: Exception) {
            return timeStr
        }
    }
}

