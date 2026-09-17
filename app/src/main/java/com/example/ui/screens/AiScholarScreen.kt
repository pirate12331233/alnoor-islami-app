package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiService
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScholarScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedSection by remember { mutableStateOf(0) } // 0 = Q&A, 1 = Poster / Announcement Maker

    // Q&A States
    var userQuestion by remember { mutableStateOf("") }
    var scholarAnswer by remember { mutableStateOf<String?>(null) }
    var isAsking by remember { mutableStateOf(false) }
    var enableHighThinking by remember { mutableStateOf(true) }

    // Poster Maker States
    var posterTopic by remember { mutableStateOf("Weekly Khatam Sharif & Darood Gathering") }
    var posterDetails by remember { mutableStateOf("Every Thursday after Isha, Hazrat Allama delivering Bayan, Tea & Tabarruk arrangement.") }
    var selectedAspectRatio by remember { mutableStateOf("1:1 (Square)") }
    var generatedAnnouncement by remember { mutableStateOf<String?>(null) }
    var isGeneratingAnnouncement by remember { mutableStateOf(false) }

    val quickQuestions = listOf(
        "What are the virtues of Darood Sharif?",
        "How is Khatam Sharif beneficial for the deceased?",
        "What is the significance of Friday Juma prayer?",
        "Recommended Duas for protection and peace of mind"
    )

    val aspectRatios = listOf("1:1 (Square)", "16:9 (Landscape)", "9:16 (Story)", "4:3 (Banner)")

    fun copyToClipboard(text: String, label: String = "Text") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Gold500),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Scholar", tint = Emerald900, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI Islamic Scholar Assistant",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Powered by Gemini with High-Thinking",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gold300
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Tabs (Q&A vs Poster Creator)
        item {
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = Emerald800,
                contentColor = Gold300,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text("Scholar Q&A", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text("Poster & Announce Maker", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        if (selectedSection == 0) {
            // --- SECTION 0: SCHOLAR Q&A ---
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ask an Islamic Question",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Quick prompt suggestions
                        Text("Suggested Topics:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            quickQuestions.forEach { q ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Emerald800.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            userQuestion = q
                                            coroutineScope.launch {
                                                isAsking = true
                                                val result = GeminiService.askIslamicScholar(q, enableHighThinking)
                                                scholarAnswer = result.getOrNull()
                                                isAsking = false
                                            }
                                        }
                                ) {
                                    Text(
                                        text = "• $q",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Emerald800,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = userQuestion,
                            onValueChange = { userQuestion = it },
                            placeholder = { Text("Type your query on Quran, Hadith, Darood, Khatam...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        // High Thinking Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = Gold600, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("High Thinking Mode (Deep References)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Switch(
                                checked = enableHighThinking,
                                onCheckedChange = { enableHighThinking = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Gold500)
                            )
                        }

                        Button(
                            onClick = {
                                if (userQuestion.isNotBlank()) {
                                    coroutineScope.launch {
                                        isAsking = true
                                        val result = GeminiService.askIslamicScholar(userQuestion, enableHighThinking)
                                        scholarAnswer = result.getOrNull()
                                        isAsking = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald800,
                                contentColor = Color.White
                            ),
                            enabled = !isAsking && userQuestion.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("ask_scholar_button")
                        ) {
                            if (isAsking) {
                                CircularProgressIndicator(color = Gold400, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Consulting Islamic Sources...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ask Scholar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Scholar Answer Output
            scholarAnswer?.let { answer ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Mosque, contentDescription = null, tint = Emerald800)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Scholarly Guidance",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald800
                                    )
                                }

                                IconButton(onClick = { copyToClipboard(answer, "Islamic Guidance") }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Answer", tint = Gold600)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = answer,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        } else {
            // --- SECTION 1: POSTER & ANNOUNCEMENT CREATOR ---
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Islamic Poster & Event Announcement Maker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = posterTopic,
                            onValueChange = { posterTopic = it },
                            label = { Text("Event / Gathering Topic") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = posterDetails,
                            onValueChange = { posterDetails = it },
                            label = { Text("Key Program Details & Schedule") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Text("Aspect Ratio for Social Media & Display:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(aspectRatios) { ratio ->
                                FilterChip(
                                    selected = selectedAspectRatio == ratio,
                                    onClick = { selectedAspectRatio = ratio },
                                    label = { Text(ratio) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Emerald800,
                                        selectedLabelColor = Gold300
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isGeneratingAnnouncement = true
                                    val res = GeminiService.generateIslamicAnnouncementDraft(posterTopic, posterDetails)
                                    generatedAnnouncement = res.getOrNull()
                                    isGeneratingAnnouncement = false
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500, contentColor = Emerald900),
                            enabled = !isGeneratingAnnouncement,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            if (isGeneratingAnnouncement) {
                                CircularProgressIndicator(color = Emerald900, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Composing Announcement Draft...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Announcement & Poster", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Visual Poster Card Preview & Text Draft
            generatedAnnouncement?.let { draft ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Poster Visual Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Emerald900),
                            elevation = CardDefaults.cardElevation(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold300,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = posterTopic.uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold500
                                ) {
                                    Text(
                                        text = "Aspect Ratio: $selectedAspectRatio",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = posterDetails,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Alnoor Islami Association • @AlnoorislamiMushahidat",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gold400
                                )
                            }
                        }

                        // Generated Announcement Text
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Broadcast & Social Message Text", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    IconButton(onClick = { copyToClipboard(draft, "Announcement") }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Text", tint = Gold600)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(draft, fontSize = 13.sp, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
