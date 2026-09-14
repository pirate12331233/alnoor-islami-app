package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.widget.Toast
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerTimesData
import com.example.data.model.UserRole
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.util.PrayerLocationService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    prayerTimes: PrayerTimesData,
    currentRole: UserRole,
    onUpdatePrayerTimes: (PrayerTimesData) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var isDetectingLocation by remember { mutableStateOf(false) }
    var isAzanPlaying by remember { mutableStateOf(false) }
    var showCountrySelector by remember { mutableStateOf(false) }

    // Pre-defined Country presets with Coordinates
    val countryPresets = remember {
        listOf(
            Triple("Pakistan", "Lahore / Islamabad, Pakistan", Pair(31.5204, 74.3587)),
            Triple("Saudi Arabia", "Makkah al-Mukarramah, Saudi Arabia", Pair(21.4225, 39.8262)),
            Triple("United Arab Emirates", "Dubai, UAE", Pair(25.2048, 55.2708)),
            Triple("United Kingdom", "London, United Kingdom", Pair(51.5074, -0.1278)),
            Triple("United States", "New York, United States", Pair(40.7128, -74.0060)),
            Triple("Canada", "Toronto, Canada", Pair(43.6532, -79.3832)),
            Triple("India", "New Delhi, India", Pair(28.6139, 77.2090)),
            Triple("Turkey", "Istanbul, Turkey", Pair(41.0082, 28.9784)),
            Triple("Egypt", "Cairo, Egypt", Pair(30.0444, 31.2357))
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isDetectingLocation = true
        PrayerLocationService.detectLocationAndCalculatePrayerTimes(context) { newTimes ->
            onUpdatePrayerTimes(newTimes)
            isDetectingLocation = false
            Toast.makeText(context, "Location detected: ${newTimes.locationName}", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-detect on initial load if not already detected
    LaunchedEffect(Unit) {
        if (!prayerTimes.isAutoDetected) {
            PrayerLocationService.detectLocationAndCalculatePrayerTimes(context) { newTimes ->
                onUpdatePrayerTimes(newTimes)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Device Location & Country Detection Banner ---
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald900),
                elevation = CardDefaults.cardElevation(5.dp),
                modifier = Modifier.fillMaxWidth().testTag("location_detection_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Gold500),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = "GPS Location",
                                    tint = Emerald900,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Current Country & Location",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = prayerTimes.locationName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gold300
                                )
                            }
                        }

                        if (isDetectingLocation) {
                            CircularProgressIndicator(
                                color = Gold400,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = "Detect Location",
                                    tint = Gold400,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Gold500,
                                contentColor = Emerald900
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("detect_location_button")
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Auto Detect", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-Detect GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showCountrySelector = !showCountrySelector },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(Icons.Default.Public, contentDescription = "Change Country", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Country", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Country Presets Selector
                    if (showCountrySelector) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Emerald800,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Select Country for Dynamic Timings:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gold300,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                countryPresets.forEach { (country, locName, coords) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                val newTimes = PrayerLocationService.calculatePrayerTimesForCoordinates(
                                                    lat = coords.first,
                                                    lng = coords.second,
                                                    locationName = locName,
                                                    countryName = country
                                                )
                                                onUpdatePrayerTimes(newTimes)
                                                showCountrySelector = false
                                                Toast.makeText(context, "Calculated timings for $locName", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 6.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(locName, color = Color.White, fontSize = 12.sp)
                                        if (prayerTimes.countryName == country) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Gold400, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Emerald800.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = Gold300,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-syncs on login & updates daily every 24 hours.",
                                color = Gold300,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Top Next Prayer Hero Card ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald800),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Prayer: Maghrib",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (currentRole == UserRole.ADMIN) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Gold500
                            ) {
                                IconButton(
                                    onClick = { showEditDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Timings", tint = Emerald900, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${prayerTimes.dateGregorian} • ${prayerTimes.dateHijri}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Azan Alert audio button
                    Button(
                        onClick = { isAzanPlaying = !isAzanPlaying },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAzanPlaying) Gold500 else Emerald900,
                            contentColor = if (isAzanPlaying) Emerald900 else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Azan Alert", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isAzanPlaying) "Azan Audio (Playing)" else "Play Azan Notification Sound", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 3. 5 Daily Prayers & Jamat Schedule Table ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth().testTag("prayer_schedule_table")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Prayer & Jamat Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Emerald800
                        ) {
                            Text(
                                text = prayerTimes.countryName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold300,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Emerald800.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Prayer", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                        Text("Adhan (Begins)", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("Jamat (Iqamah)", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Emerald800)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    PrayerRow(name = "Fajr", arabic = "الفجر", adhan = prayerTimes.fajr, jamat = prayerTimes.fajrIqamah)
                    PrayerRow(name = "Sunrise (Shurooq)", arabic = "الشروق", adhan = prayerTimes.sunrise, jamat = "--", isNonJamat = true)
                    PrayerRow(name = "Dhuhr", arabic = "الظهر", adhan = prayerTimes.dhuhr, jamat = prayerTimes.dhuhrIqamah)
                    PrayerRow(name = "Asr", arabic = "العصر", adhan = prayerTimes.asr, jamat = prayerTimes.asrIqamah)
                    PrayerRow(name = "Maghrib", arabic = "المغرب", adhan = prayerTimes.maghrib, jamat = prayerTimes.maghribIqamah, isHighlight = true)
                    PrayerRow(name = "Isha", arabic = "العشاء", adhan = prayerTimes.isha, jamat = prayerTimes.ishaIqamah)
                    PrayerRow(name = "Tahajjud", arabic = "التهجد", adhan = prayerTimes.tahajjud, jamat = "Qiyam", isNonJamat = true)
                }
            }
        }

        // --- 4. Qibla Compass Heading & Calculation Info ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Emerald800),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Qibla Direction",
                            tint = Gold400,
                            modifier = Modifier
                                .size(30.dp)
                                .rotate(prayerTimes.qiblaDirectionDeg)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Qibla Direction: ${String.format(Locale.US, "%.1f", prayerTimes.qiblaDirectionDeg)}°",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Method: ${prayerTimes.calculationMethod}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Edit Prayer Timings Dialog (Admin)
    if (showEditDialog) {
        var fajrIq by remember { mutableStateOf(prayerTimes.fajrIqamah) }
        var dhuhrIq by remember { mutableStateOf(prayerTimes.dhuhrIqamah) }
        var asrIq by remember { mutableStateOf(prayerTimes.asrIqamah) }
        var maghribIq by remember { mutableStateOf(prayerTimes.maghribIqamah) }
        var ishaIq by remember { mutableStateOf(prayerTimes.ishaIqamah) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Adjust Jamat (Iqamah) Timings", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fajrIq, onValueChange = { fajrIq = it }, label = { Text("Fajr Jamat") })
                    OutlinedTextField(value = dhuhrIq, onValueChange = { dhuhrIq = it }, label = { Text("Dhuhr Jamat") })
                    OutlinedTextField(value = asrIq, onValueChange = { asrIq = it }, label = { Text("Asr Jamat") })
                    OutlinedTextField(value = maghribIq, onValueChange = { maghribIq = it }, label = { Text("Maghrib Jamat") })
                    OutlinedTextField(value = ishaIq, onValueChange = { ishaIq = it }, label = { Text("Isha Jamat") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdatePrayerTimes(
                            prayerTimes.copy(
                                fajrIqamah = fajrIq,
                                dhuhrIqamah = dhuhrIq,
                                asrIqamah = asrIq,
                                maghribIqamah = maghribIq,
                                ishaIqamah = ishaIq
                            )
                        )
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Save Timings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PrayerRow(
    name: String,
    arabic: String,
    adhan: String,
    jamat: String,
    isHighlight: Boolean = false,
    isNonJamat: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHighlight) Gold500.copy(alpha = 0.15f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1.2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
                color = if (isHighlight) Emerald800 else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = arabic,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = adhan,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Text(
            text = jamat,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isNonJamat) MaterialTheme.colorScheme.onSurfaceVariant else Emerald800,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
}
