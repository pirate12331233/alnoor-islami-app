package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.util.EventDateHelper

@Composable
fun IslamicDatePickerDialog(
    initialHijriDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    // Attempt to parse initial values
    var initialDay = 15
    var initialMonthIndex = 1 // Safar default
    var initialYear = 1448

    try {
        val parts = initialHijriDate.trim().split(" ")
        if (parts.isNotEmpty()) {
            val dayParsed = parts[0].filter { it.isDigit() }.toIntOrNull()
            if (dayParsed != null && dayParsed in 1..30) {
                initialDay = dayParsed
            }
        }
        for (i in EventDateHelper.ISLAMIC_MONTHS.indices) {
            val mName = EventDateHelper.ISLAMIC_MONTHS[i]
            if (initialHijriDate.contains(mName, ignoreCase = true)) {
                initialMonthIndex = i
                break
            }
        }
        val yearParsed = initialHijriDate.filter { it.isDigit() }.takeLast(4).toIntOrNull()
        if (yearParsed != null && yearParsed in 1430..1460) {
            initialYear = yearParsed
        }
    } catch (_: Exception) {}

    var selectedDay by remember { mutableIntStateOf(initialDay) }
    var selectedMonthIndex by remember { mutableIntStateOf(initialMonthIndex) }
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    val formattedHijriDate = remember(selectedDay, selectedMonthIndex, selectedYear) {
        EventDateHelper.formatHijriDate(selectedDay, selectedMonthIndex, selectedYear)
    }

    val hijriYears = listOf(1446, 1447, 1448, 1449, 1450, 1451, 1452)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Gold500.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NightlightRound,
                                contentDescription = null,
                                tint = Gold600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Islamic / Hijri Calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "التقويم الهجري الإسلامي",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gold600,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Selected Date Display Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Emerald900,
                    border = BorderStroke(1.5.dp, Gold500.copy(alpha = 0.5f)),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SELECTED ISLAMIC DATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Gold300,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formattedHijriDate,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = EventDateHelper.ISLAMIC_MONTHS_WITH_ARABIC[selectedMonthIndex],
                            fontSize = 12.sp,
                            color = Gold300
                        )
                    }
                }

                // 1. Month Selector Dropdown
                Column {
                    Text(
                        text = "Islamic Month (الشهر الهجري):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { monthDropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = EventDateHelper.ISLAMIC_MONTHS_WITH_ARABIC[selectedMonthIndex],
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Month",
                                    tint = Emerald800
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false },
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            EventDateHelper.ISLAMIC_MONTHS_WITH_ARABIC.forEachIndexed { index, monthName ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = monthName,
                                                fontWeight = if (selectedMonthIndex == index) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedMonthIndex == index) Emerald800 else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (selectedMonthIndex == index) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Emerald800, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedMonthIndex = index
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Year Selector
                Column {
                    Text(
                        text = "Hijri Year (السنة الهجرية):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(hijriYears) { year ->
                            val isSelected = selectedYear == year
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(1.dp, Gold500) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable { selectedYear = year }
                            ) {
                                Text(
                                    text = "$year AH",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Gold300 else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Day Selector Grid (1 to 30)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hijri Day (اليوم):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Selected: Day $selectedDay",
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald800,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                    ) {
                        items((1..30).toList()) { day ->
                            val isSelected = selectedDay == day
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Gold500 else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(1.5.dp, Emerald900) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedDay = day }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "$day",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        color = if (isSelected) Emerald900 else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(formattedHijriDate)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald800,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Set Islamic Date", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
