package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityEvent
import com.example.data.model.UserRole
import com.example.ui.components.IslamicDatePickerDialog
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.UrgentRed
import com.example.util.EventDateHelper
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    events: List<CommunityEvent>,
    currentRole: UserRole,
    onToggleRsvp: (String) -> Unit = {},
    onToggleReminder: (String, Boolean, Int) -> Unit = { _, _, _ -> },
    onAddEvent: (CommunityEvent) -> Unit,
    onUpdateEvent: (CommunityEvent) -> Unit,
    onDeleteEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedIslamicFilter by remember { mutableStateOf<String?>(null) }
    var showUserIslamicPicker by remember { mutableStateOf(false) }

    val categories = listOf("All", "International Conference", "Mehfil-e-Darood", "Youth Program", "Juma Congregation")

    var showAddDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CommunityEvent?>(null) }
    var eventToDelete by remember { mutableStateOf<CommunityEvent?>(null) }

    // Sort events: nearby coming events first, remaining in chronological order
    val sortedEvents = remember(events) {
        EventDateHelper.sortEventsUpcomingFirst(events)
    }

    val filteredEvents = remember(sortedEvents, selectedCategory, selectedIslamicFilter) {
        var list = sortedEvents
        if (selectedCategory != "All") {
            list = list.filter { it.category.contains(selectedCategory, ignoreCase = true) }
        }
        if (!selectedIslamicFilter.isNullOrBlank()) {
            list = list.filter { it.dateHijri.contains(selectedIslamicFilter!!, ignoreCase = true) }
        }
        list
    }

    Scaffold(
        floatingActionButton = {
            if (currentRole == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Gold500,
                    contentColor = Emerald900,
                    modifier = Modifier.testTag("add_event_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald900),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alnoor Islami Mahafils",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Emerald800
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Gold300,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${sortedEvents.size} Total Events",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold300
                                    )
                                }
                            }

                            if (!selectedIslamicFilter.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold500.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, Gold500),
                                    modifier = Modifier.clickable { selectedIslamicFilter = null }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "Filter: $selectedIslamicFilter",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Gold300
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear Filter",
                                            tint = Gold300,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald800,
                                selectedLabelColor = Gold300,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            // Events List - Sorted near by coming event as first
            if (filteredEvents.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Text(
                                text = "No events found for this filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredEvents) { event ->
                    EventCard(
                        event = event,
                        isAdmin = currentRole == UserRole.ADMIN,
                        onEditClick = { editingEvent = event },
                        onDeleteClick = { eventToDelete = event }
                    )
                }
            }
        }
    }

    // Islamic Calendar Date Picker for user filtering
    if (showUserIslamicPicker) {
        IslamicDatePickerDialog(
            initialHijriDate = "15 Safar 1448 AH",
            onDismiss = { showUserIslamicPicker = false },
            onDateSelected = { pickedDate ->
                // Filter by month name from the picked Islamic date
                val month = EventDateHelper.ISLAMIC_MONTHS.firstOrNull { pickedDate.contains(it, ignoreCase = true) }
                selectedIslamicFilter = month ?: pickedDate
            }
        )
    }

    // Add Event Dialog (Admin)
    if (showAddDialog) {
        EventFormDialog(
            title = "Add Community Event",
            initialEvent = null,
            onDismiss = { showAddDialog = false },
            onSave = { newEvent ->
                onAddEvent(newEvent)
                showAddDialog = false
            }
        )
    }

    // Edit Event Dialog (Admin)
    editingEvent?.let { event ->
        EventFormDialog(
            title = "Edit Event Details",
            initialEvent = event,
            onDismiss = { editingEvent = null },
            onSave = { updatedEvent ->
                onUpdateEvent(updatedEvent)
                editingEvent = null
            }
        )
    }

    // Delete Confirmation Dialog (Admin)
    eventToDelete?.let { event ->
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Delete Event?") },
            text = { Text("Are you sure you want to remove '${event.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEvent(event.id)
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EventCard(
    event: CommunityEvent,
    isAdmin: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRsvpClick: () -> Unit = {},
    onReminderClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Badge and Admin Actions only
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald800
                ) {
                    Text(
                        text = event.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold300,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                if (isAdmin) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Gold600, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UrgentRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Event Title: Box Style with White Background and Bold Red Text
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFFDC2626).copy(alpha = 0.45f)),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_title_box_${event.id}")
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFDC2626), // Prominent vibrant Red
                    fontSize = 17.sp,
                    lineHeight = 23.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dates: Gregorian with Day name and Islamic / Hijri Calendar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Date",
                    tint = Emerald800,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.dateGregorian,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (event.dateHijri.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Gold500.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, Gold500.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "🌙 ${event.dateHijri}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold600,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Time Schedule
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Venue Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Venue",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.venue,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Scholar / Speaker
            if (event.speaker.isNotBlank()) {
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Speaker",
                        tint = Gold600,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.speaker,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gold600
                    )
                }
            }

            // Event Description Display Box: Formatted display box accommodating up to 8 lines
            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Event Details & Highlights:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 21.sp,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventFormDialog(
    title: String,
    initialEvent: CommunityEvent?,
    onDismiss: () -> Unit,
    onSave: (CommunityEvent) -> Unit
) {
    val context = LocalContext.current

    var titleText by remember { mutableStateOf(initialEvent?.title ?: "") }
    var categoryText by remember { mutableStateOf(initialEvent?.category ?: "Mehfil-e-Darood") }
    var speakerText by remember { mutableStateOf(initialEvent?.speaker ?: "Hazrat Allama & Guest Scholars") }
    var dateGreg by remember { mutableStateOf(initialEvent?.dateGregorian ?: "Friday, Sep 18, 2026") }
    var dateHijri by remember { mutableStateOf(initialEvent?.dateHijri ?: "06 Rabi' al-Awwal 1448 AH") }
    var timeText by remember { mutableStateOf(initialEvent?.time ?: "08:00 PM - 10:30 PM") }
    var venueText by remember { mutableStateOf(initialEvent?.venue ?: "Alnoor Central Complex") }
    var descriptionText by remember { mutableStateOf(initialEvent?.description ?: "") }

    // Auto day name extraction
    var autoDayName by remember {
        val initialDay = if (dateGreg.contains(",")) dateGreg.substringBefore(",").trim() else "Friday"
        mutableStateOf(initialDay)
    }

    var showIslamicDatePicker by remember { mutableStateOf(false) }

    // Gregorian Date Picker Dialog launcher
    val openGregorianDatePicker = {
        val cal = Calendar.getInstance()
        val parsedMillis = EventDateHelper.parseEventDateMillis(dateGreg)
        if (parsedMillis != Long.MAX_VALUE) {
            cal.timeInMillis = parsedMillis
        }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(context, { _, y, m, d ->
            val newCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
            }
            dateGreg = EventDateHelper.formatGregorianDate(newCal)
            autoDayName = EventDateHelper.getAutoDayName(newCal)
            // Auto update approximate Hijri date if empty or unchanged
            if (dateHijri.isBlank() || dateHijri.contains("Select")) {
                dateHijri = EventDateHelper.estimateHijriDateFromCalendar(newCal)
            }
        }, year, month, day).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                item {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Event Title") },
                        placeholder = { Text("e.g. Grand Khatam Sharif Congregation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Category
                item {
                    OutlinedTextField(
                        value = categoryText,
                        onValueChange = { categoryText = it },
                        label = { Text("Category (e.g. Mehfil-e-Darood, Conference)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Scholar / Speaker
                item {
                    OutlinedTextField(
                        value = speakerText,
                        onValueChange = { speakerText = it },
                        label = { Text("Scholar / Speaker") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Gregorian Date Picker with Auto Day Name Display
                item {
                    Column {
                        Text(
                            text = "Gregorian Date (Date Picker):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, Emerald800.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openGregorianDatePicker() }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dateGreg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Emerald800
                                    ) {
                                        Text(
                                            text = "Day: $autoDayName (Auto-Detected)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Gold300,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { openGregorianDatePicker() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Pick Date",
                                        tint = Emerald800
                                    )
                                }
                            }
                        }
                    }
                }

                // Separate Date Picker of Islamic Calendar for display
                item {
                    Column {
                        Text(
                            text = "Islamic / Hijri Calendar Date (Separate Picker):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, Gold500.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showIslamicDatePicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dateHijri,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold600
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Gold500.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "🌙 Islamic Hijri Calendar",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Gold600,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showIslamicDatePicker = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Gold500, contentColor = Emerald900),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.NightlightRound, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick Hijri", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Time Schedule
                item {
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Time Schedule") },
                        placeholder = { Text("e.g. 08:00 PM - 10:30 PM") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Venue Location
                item {
                    OutlinedTextField(
                        value = venueText,
                        onValueChange = { venueText = it },
                        label = { Text("Venue Location") },
                        placeholder = { Text("e.g. Alnoor Central Complex, Hall 1") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Event Description - 8 lines comfortable input
                item {
                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        label = { Text("Event Description (up to 8 lines displayed)") },
                        placeholder = { Text("Enter detailed program schedule, topics, guest speakers, dinner arrangements...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 10
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val saved = initialEvent?.copy(
                        title = titleText,
                        category = categoryText,
                        speaker = speakerText,
                        dateGregorian = dateGreg,
                        dateHijri = dateHijri,
                        time = timeText,
                        venue = venueText,
                        description = descriptionText
                    ) ?: CommunityEvent(
                        id = UUID.randomUUID().toString(),
                        title = titleText,
                        category = categoryText,
                        speaker = speakerText,
                        dateGregorian = dateGreg,
                        dateHijri = dateHijri,
                        time = timeText,
                        venue = venueText,
                        address = venueText,
                        description = descriptionText
                    )
                    onSave(saved)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Event", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    // Dedicated Islamic Calendar Date Picker Dialog
    if (showIslamicDatePicker) {
        IslamicDatePickerDialog(
            initialHijriDate = dateHijri,
            onDismiss = { showIslamicDatePicker = false },
            onDateSelected = { selectedHijri ->
                dateHijri = selectedHijri
            }
        )
    }
}
