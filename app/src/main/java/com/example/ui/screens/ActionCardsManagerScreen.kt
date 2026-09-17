package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionCardConfig
import com.example.data.model.UserRole
import com.example.ui.theme.BorderDark
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.LiveRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCardsManagerScreen(
    currentRole: UserRole,
    actionCardConfigs: List<ActionCardConfig>,
    onUpdateCard: (cardKey: String, customTitle: String, customSubtitle: String, isVisibleToMembers: Boolean) -> Unit,
    onUpdateAllCards: (List<ActionCardConfig>) -> Unit,
    onResetCard: (String) -> Unit,
    onResetAllCards: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Local mutable copy of configs so admin can make multiple edits smoothly
    var editableConfigs by remember(actionCardConfigs) { mutableStateOf(actionCardConfigs) }
    var searchQuery by remember { mutableStateOf("") }
    var showResetAllConfirmDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    val filteredConfigs = remember(editableConfigs, searchQuery) {
        if (searchQuery.isBlank()) editableConfigs
        else editableConfigs.filter {
            it.displayTitle.contains(searchQuery, ignoreCase = true) ||
            it.defaultTitle.contains(searchQuery, ignoreCase = true) ||
            it.cardKey.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalCount = editableConfigs.size
    val visibleCount = editableConfigs.count { it.isVisibleToMembers }
    val hiddenCount = editableConfigs.count { !it.isVisibleToMembers }
    val customizedCount = editableConfigs.count { it.customTitle.isNotBlank() || it.customSubtitle.isNotBlank() || !it.isVisibleToMembers }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER BANNER ---
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ActionCardsHeaderCard(
                    totalCount = totalCount,
                    visibleCount = visibleCount,
                    hiddenCount = hiddenCount,
                    customizedCount = customizedCount,
                    hasUnsavedChanges = hasUnsavedChanges,
                    onSaveAll = {
                        onUpdateAllCards(editableConfigs)
                        hasUnsavedChanges = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("All action cards updated and synced with cloud!")
                        }
                    },
                    onResetAll = { showResetAllConfirmDialog = true }
                )
            }

            // --- SEARCH BAR ---
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search cards by name or key...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Gold400
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold400,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("action_cards_search_input")
                )
            }

            // --- ACTION CARD EDITORS ---
            items(
                items = filteredConfigs,
                key = { it.cardKey }
            ) { config ->
                ActionCardEditorItem(
                    config = config,
                    onConfigChanged = { updated ->
                        editableConfigs = editableConfigs.map { if (it.cardKey == updated.cardKey) updated else it }
                        hasUnsavedChanges = true
                        onUpdateCard(updated.cardKey, updated.customTitle, updated.customSubtitle, updated.isVisibleToMembers)
                    },
                    onResetCard = {
                        onResetCard(config.cardKey)
                        val resetItem = actionCardConfigs.find { it.cardKey == config.cardKey }?.copy(
                            customTitle = "",
                            customSubtitle = "",
                            isVisibleToMembers = true
                        ) ?: config.copy(customTitle = "", customSubtitle = "", isVisibleToMembers = true)
                        editableConfigs = editableConfigs.map { if (it.cardKey == config.cardKey) resetItem else it }
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Card '${config.defaultTitle}' restored to default.")
                        }
                    }
                )
            }

            // --- BOTTOM PADDING & FINISH BUTTON ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onBackToHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_back_to_dashboard")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Gold300)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Return to Home Dashboard",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    // --- RESET ALL CONFIRMATION DIALOG ---
    if (showResetAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = Gold400)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Action Cards?", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            text = {
                Text(
                    "This will restore all action card names to their original defaults and make all cards visible to regular community members. Are you sure?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllCards()
                        hasUnsavedChanges = false
                        showResetAllConfirmDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("All action cards restored to factory defaults.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                ) {
                    Text("Yes, Reset All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllConfirmDialog = false }) {
                    Text("Cancel", color = Gold400)
                }
            },
            containerColor = DarkCard,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ActionCardsHeaderCard(
    totalCount: Int,
    visibleCount: Int,
    hiddenCount: Int,
    customizedCount: Int,
    hasUnsavedChanges: Boolean,
    onSaveAll: () -> Unit,
    onResetAll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Gold500.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Emerald700, Emerald900)
                                )
                            )
                            .border(1.dp, Gold400.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Action Cards Manager",
                            tint = Gold400,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Action Cards Manager",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Admin Console • Live Cloud Sync",
                            fontSize = 12.sp,
                            color = Gold400,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald900,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = Gold300,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-Synced",
                            fontSize = 11.sp,
                            color = Gold300,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Control what community members see on the home dashboard. Rename card titles, update descriptions, and toggle card visibility on or off with instant real-time sync across all devices.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(
                    label = "Total Cards",
                    value = totalCount.toString(),
                    color = Emerald500,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = "Visible",
                    value = visibleCount.toString(),
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = "Hidden",
                    value = hiddenCount.toString(),
                    color = if (hiddenCount > 0) Color(0xFFF59E0B) else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    label = "Customized",
                    value = customizedCount.toString(),
                    color = Gold400,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onResetAll,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset All", fontSize = 13.sp)
                }

                Button(
                    onClick = onSaveAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasUnsavedChanges) Gold500 else Emerald700
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_save_all_action_cards")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = if (hasUnsavedChanges) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasUnsavedChanges) "Save & Sync Cloud" else "Saved & Active",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasUnsavedChanges) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActionCardEditorItem(
    config: ActionCardConfig,
    onConfigChanged: (ActionCardConfig) -> Unit,
    onResetCard: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val cardIcon = getCardIcon(config.cardKey)
    val hasCustomizations = config.customTitle.isNotBlank() || config.customSubtitle.isNotBlank() || !config.isVisibleToMembers

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (!config.isVisibleToMembers) Color(0xFFF59E0B).copy(alpha = 0.4f)
                else if (hasCustomizations) Gold500.copy(alpha = 0.4f)
                else BorderDark,
                RoundedCornerShape(18.dp)
            )
            .testTag("editor_card_${config.cardKey}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // --- TOP ROW: ICON, TITLE, MEMBER VISIBILITY TOGGLE ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Card Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (config.isVisibleToMembers) Emerald900 else Color(0xFF374151))
                        .border(
                            1.dp,
                            if (config.isVisibleToMembers) Gold500.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = cardIcon,
                        contentDescription = null,
                        tint = if (config.isVisibleToMembers) Gold400 else Color.LightGray,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Titles
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = config.displayTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (config.customTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Gold500.copy(alpha = 0.2f),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "Custom",
                                    fontSize = 9.sp,
                                    color = Gold400,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (config.isVisibleToMembers) "Visible to Members" else "Hidden from Members",
                        fontSize = 11.sp,
                        color = if (config.isVisibleToMembers) Color(0xFF10B981) else Color(0xFFF59E0B),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Visibility Switch
                Switch(
                    checked = config.isVisibleToMembers,
                    onCheckedChange = { isChecked ->
                        onConfigChanged(config.copy(isVisibleToMembers = isChecked))
                    },
                    thumbContent = {
                        Icon(
                            imageVector = if (config.isVisibleToMembers) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                            tint = if (config.isVisibleToMembers) Emerald900 else Color.White
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Gold400,
                        checkedTrackColor = Emerald700,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color(0xFF374151)
                    ),
                    modifier = Modifier.testTag("switch_visibility_${config.cardKey}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle snippet / preview
            Text(
                text = config.displaySubtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- EXPAND / COLLAPSE EDIT FIELDS BUTTON ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceVariant
                ) {
                    Text(
                        text = "Key: ${config.cardKey}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hasCustomizations) {
                        TextButton(
                            onClick = onResetCard,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("btn_toggle_edit_${config.cardKey}")
                    ) {
                        Text(
                            text = if (isExpanded) "Hide Edit Fields ▲" else "Edit Name & Details ▼",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold400
                        )
                    }
                }
            }

            // --- EXPANDED EDIT FIELDS ---
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = BorderDark, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Title Text Field
                    Text(
                        text = "Card Title / Display Name:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = config.customTitle,
                        onValueChange = { newTitle ->
                            onConfigChanged(config.copy(customTitle = newTitle))
                        },
                        placeholder = {
                            Text(
                                "Default: ${config.defaultTitle}",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold400,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_title_${config.cardKey}")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subtitle Text Field
                    Text(
                        text = "Card Subtitle / Description:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = config.customSubtitle,
                        onValueChange = { newSubtitle ->
                            onConfigChanged(config.copy(customSubtitle = newSubtitle))
                        },
                        placeholder = {
                            Text(
                                "Default: ${config.defaultSubtitle}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold400,
                            unfocusedBorderColor = BorderDark,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_subtitle_${config.cardKey}")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Member Visibility Detailed Toggle Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (config.isVisibleToMembers) Emerald900.copy(alpha = 0.4f) else Color(0xFF7F1D1D).copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (config.isVisibleToMembers) Emerald600 else Color(0xFFEF4444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (config.isVisibleToMembers) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = if (config.isVisibleToMembers) Emerald100 else Color(0xFFFCA5A5),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (config.isVisibleToMembers) "Card is Active for Members" else "Card is Hidden from Members",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (config.isVisibleToMembers) Color.White else Color(0xFFFCA5A5)
                                    )
                                    Text(
                                        text = if (config.isVisibleToMembers) "Community members see this card on their dashboard" else "Only Administrators can see and access this card",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = config.isVisibleToMembers,
                                onCheckedChange = { isChecked ->
                                    onConfigChanged(config.copy(isVisibleToMembers = isChecked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Gold400,
                                    checkedTrackColor = Emerald700,
                                    uncheckedThumbColor = Color.LightGray,
                                    uncheckedTrackColor = Color(0xFF374151)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCardIcon(cardKey: String): ImageVector {
    return when (cardKey) {
        "YOUTUBE_CHANNEL" -> Icons.Default.PlayArrow
        "LIVE_STREAM" -> Icons.Default.LiveTv
        "DAROOD" -> Icons.Default.Fingerprint
        "HADITH" -> Icons.Default.FormatQuote
        "EVENTS" -> Icons.Default.Event
        "NOTICES" -> Icons.Default.Campaign
        "PRAYER" -> Icons.Default.Mosque
        "QURAN" -> Icons.Default.MenuBook
        "LIBRARY" -> Icons.Default.LibraryBooks
        "MEDIA" -> Icons.Default.Radio
        "GALLERY" -> Icons.Outlined.PhotoLibrary
        "MESSAGES" -> Icons.Default.Mail
        "SEARCH" -> Icons.Default.Search
        "SETTINGS" -> Icons.Default.Settings
        else -> Icons.Default.Tune
    }
}
