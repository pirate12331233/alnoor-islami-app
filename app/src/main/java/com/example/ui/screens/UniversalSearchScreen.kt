package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IslamicBook
import com.example.data.model.MediaArchiveItem
import com.example.data.model.NoticeItem
import com.example.data.model.NoticePriority
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.UrgentRed

sealed class SearchResultItem {
    data class BookResult(val book: IslamicBook) : SearchResultItem()
    data class NoticeResult(val notice: NoticeItem) : SearchResultItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalSearchScreen(
    books: List<IslamicBook>,
    notices: List<NoticeItem>,
    onToggleBookmark: (String) -> Unit = {},
    onPlayAudio: (MediaArchiveItem) -> Unit = {},
    onBookClick: (IslamicBook) -> Unit = {},
    onNoticeClick: (NoticeItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedScope by remember { mutableStateOf("All") } // "All", "Library", "Notices"

    val filteredBooks = remember(searchQuery, books) {
        if (searchQuery.isBlank()) emptyList()
        else books.filter { book ->
            book.title.contains(searchQuery, ignoreCase = true) ||
            book.author.contains(searchQuery, ignoreCase = true) ||
            book.category.contains(searchQuery, ignoreCase = true) ||
            book.description.contains(searchQuery, ignoreCase = true) ||
            book.contentPreview.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredNotices = remember(searchQuery, notices) {
        if (searchQuery.isBlank()) emptyList()
        else notices.filter { notice ->
            notice.title.contains(searchQuery, ignoreCase = true) ||
            notice.content.contains(searchQuery, ignoreCase = true) ||
            notice.department.contains(searchQuery, ignoreCase = true) ||
            notice.priority.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val combinedResults: List<SearchResultItem> = remember(selectedScope, filteredBooks, filteredNotices) {
        when (selectedScope) {
            "Library" -> filteredBooks.map { SearchResultItem.BookResult(it) }
            "Notices" -> filteredNotices.map { SearchResultItem.NoticeResult(it) }
            else -> {
                (filteredBooks.map { SearchResultItem.BookResult(it) } +
                 filteredNotices.map { SearchResultItem.NoticeResult(it) })
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Gold400, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Universal Islamic Search",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Instant indexed search across Islamic Library books, Tafseer, Khatam guides, and Central Announcements.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gold300
                    )
                }
            }
        }

        // Search Input Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search books, authors, Tafseer, rules, notices...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Emerald700) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("universal_search_input")
            )
        }

        // Scope Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to (filteredBooks.size + filteredNotices.size),
                    "Library" to filteredBooks.size,
                    "Notices" to filteredNotices.size
                ).forEach { (scope, count) ->
                    val isSelected = selectedScope == scope
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedScope = scope },
                        label = {
                            Text(if (searchQuery.isNotBlank()) "$scope ($count)" else scope)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald800,
                            selectedLabelColor = Gold300
                        )
                    )
                }
            }
        }

        // Suggestions / Prompt when empty
        if (searchQuery.isBlank()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Popular Search Queries:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        listOf("Khatam Sharif Guide", "Darood & Salawat", "Surah Yaseen Tafseer", "Juma timings", "Milad un Nabi", "Iftar").forEach { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { searchQuery = suggestion }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Gold500, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(suggestion, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else if (combinedResults.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Gold500, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching records found for '$searchQuery'", fontWeight = FontWeight.Bold)
                        Text("Try searching with general Islamic keywords or author names.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Found ${combinedResults.size} relevant results",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Emerald700
                )
            }

            items(combinedResults) { result ->
                when (result) {
                    is SearchResultItem.BookResult -> {
                        val book = result.book
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBookClick(book) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Emerald800
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Book, contentDescription = null, tint = Gold300, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Library Book", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Gold300)
                                        }
                                    }

                                    IconButton(
                                        onClick = { onToggleBookmark(book.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (book.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (book.isBookmarked) Gold600 else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "By ${book.author} • Category: ${book.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Emerald700
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = book.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (book.hasAudioRecitation) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Gold500.copy(alpha = 0.2f),
                                        modifier = Modifier.clickable {
                                            onPlayAudio(
                                                MediaArchiveItem(
                                                    id = book.id,
                                                    title = "Audio Recitation: ${book.title}",
                                                    type = "Library Recitation",
                                                    reciter = book.author,
                                                    duration = "Audio Guide",
                                                    audioStreamUrl = book.audioUrl,
                                                    date = "2026",
                                                    description = book.description
                                                )
                                            )
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Headphones, contentDescription = null, tint = Gold600, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Play Audio Recitation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold600)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is SearchResultItem.NoticeResult -> {
                        val notice = result.notice
                        val isUrgent = notice.priority == NoticePriority.URGENT
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNoticeClick(notice) }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isUrgent) UrgentRed else Gold500
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Campaign, contentDescription = null, tint = if (isUrgent) Color.White else Emerald900, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Notice (${notice.priority.name})",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUrgent) Color.White else Emerald900
                                            )
                                        }
                                    }

                                    if (notice.isPinned) {
                                        Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = Gold600, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = notice.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Dept: ${notice.department} • Date: ${notice.date}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = notice.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
