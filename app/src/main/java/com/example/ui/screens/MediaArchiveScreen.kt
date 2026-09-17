package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.MediaArchiveItem
import com.example.data.model.UserRole
import com.example.data.model.YouTubePlaylist
import com.example.ui.components.openOfficialYouTubeChannel
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.UrgentRed
import com.example.ui.theme.YoutubeRed

@Composable
fun MediaArchiveScreen(
    playlists: List<YouTubePlaylist>,
    mediaItems: List<MediaArchiveItem>,
    currentlyPlaying: MediaArchiveItem?,
    isPlaying: Boolean,
    currentRole: UserRole,
    onPlayMedia: (MediaArchiveItem) -> Unit,
    onAddYouTubePlaylist: (playlistUrl: String, title: String) -> Unit,
    onDeletePlaylist: (playlistId: String) -> Unit,
    onDeleteVideoFromPlaylist: (playlistId: String, videoId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showImportPlaylistDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<YouTubePlaylist?>(null) }
    var videoToDelete by remember { mutableStateOf<Pair<String, MediaArchiveItem>?>(null) }

    fun openVideoUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
            openOfficialYouTubeChannel(context)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (currentRole == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showImportPlaylistDialog = true },
                    containerColor = YoutubeRed,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("import_youtube_playlist_fab")
                ) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = "Import YouTube Playlist")
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
            // --- 1. Top Mahafil Archive Banner ---
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald900),
                    elevation = CardDefaults.cardElevation(5.dp),
                    modifier = Modifier.fillMaxWidth().testTag("mahafil_archive_banner")
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
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(YoutubeRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.VideoLibrary,
                                        contentDescription = "Mahafil Archive",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Mahafil Archive",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "YouTube Playlists & Spiritual Mehfil Recordings",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gold300
                                    )
                                }
                            }

                            if (currentRole == UserRole.ADMIN) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold500
                                ) {
                                    Text(
                                        text = "ADMIN CONTROLS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Emerald900,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Import YouTube Playlist Action for Admin / YouTube Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (currentRole == UserRole.ADMIN) {
                                Button(
                                    onClick = { showImportPlaylistDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = YoutubeRed,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("admin_import_playlist_button")
                                ) {
                                    Icon(Icons.Default.PlaylistAdd, contentDescription = "Add Playlist", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Input Playlist Link", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { openOfficialYouTubeChannel(context) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Emerald800,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Channel", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open YouTube Channel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- 2. YouTube Playlists & Videos ---
            playlists.forEach { playlist ->
                item(key = playlist.id) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(3.dp),
                        modifier = Modifier.fillMaxWidth().testTag("playlist_card_${playlist.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Playlist Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PlaylistPlay,
                                        contentDescription = "Playlist",
                                        tint = YoutubeRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = playlist.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${playlist.videos.size} Videos • ${playlist.channelTitle}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (currentRole == UserRole.ADMIN) {
                                    IconButton(
                                        onClick = { playlistToDelete = playlist },
                                        modifier = Modifier.testTag("delete_playlist_${playlist.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteSweep,
                                            contentDescription = "Delete Playlist",
                                            tint = UrgentRed,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Videos inside this playlist
                            if (playlist.videos.isEmpty()) {
                                Text(
                                    text = "No videos in this playlist.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                playlist.videos.forEach { video ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { openVideoUrl(video.audioUrl) }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Emerald900),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = "Play Video",
                                                    tint = Gold400,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = video.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${video.speaker} • ${video.duration}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (currentRole == UserRole.ADMIN) {
                                            IconButton(
                                                onClick = { videoToDelete = Pair(playlist.id, video) }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete Video",
                                                    tint = UrgentRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                Icons.Default.OpenInNew,
                                                contentDescription = "Watch on YouTube",
                                                tint = YoutubeRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Admin Input YouTube Playlist Dialog ---
    if (showImportPlaylistDialog) {
        var playlistUrlInput by remember { mutableStateOf("") }
        var playlistTitleInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showImportPlaylistDialog = false },
            title = { Text("Admin: Input YouTube Playlist Link", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Paste a YouTube playlist link or URL. The videos will be automatically imported and populated into the Mahafil Archive tab.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = playlistUrlInput,
                        onValueChange = { playlistUrlInput = it },
                        label = { Text("YouTube Playlist Link / URL *") },
                        placeholder = { Text("https://www.youtube.com/playlist?list=PL_...") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = "Link", tint = YoutubeRed) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = playlistTitleInput,
                        onValueChange = { playlistTitleInput = it },
                        label = { Text("Playlist Title (Optional)") },
                        placeholder = { Text("e.g. Weekly Mahafil Sharif 2025") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistUrlInput.isNotBlank()) {
                            onAddYouTubePlaylist(playlistUrlInput.trim(), playlistTitleInput.trim())
                            showImportPlaylistDialog = false
                            Toast.makeText(context, "Playlist imported successfully! Videos populated.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Please enter a valid YouTube playlist link", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YoutubeRed)
                ) {
                    Text("Import & Populate Videos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportPlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Admin Delete Entire Playlist Confirmation ---
    playlistToDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Delete Entire Playlist?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${playlist.title}' and all its ${playlist.videos.size} videos?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlaylist(playlist.id)
                        playlistToDelete = null
                        Toast.makeText(context, "Playlist deleted.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Delete Playlist")
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // --- Admin Delete Individual Video Confirmation ---
    videoToDelete?.let { (playlistId, video) ->
        AlertDialog(
            onDismissRequest = { videoToDelete = null },
            title = { Text("Delete Video from Playlist?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${video.title}' from this playlist?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteVideoFromPlaylist(playlistId, video.id)
                        videoToDelete = null
                        Toast.makeText(context, "Video removed.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Remove Video")
                }
            },
            dismissButton = {
                TextButton(onClick = { videoToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
