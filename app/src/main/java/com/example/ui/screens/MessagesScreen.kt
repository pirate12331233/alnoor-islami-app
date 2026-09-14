package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.data.model.AdminMessage
import com.example.data.model.MessageCategory
import com.example.data.model.MessageStatus
import com.example.data.model.UserRole
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.UrgentRed
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    inquiries: List<AdminMessage>,
    currentRole: UserRole,
    onSubmitInquiry: (AdminMessage) -> Unit,
    onResolveInquiry: (String, String) -> Unit,
    onMarkAsRead: (String, Boolean) -> Unit,
    onSaveInternalNotes: (String, String) -> Unit,
    onDeleteInquiry: (String) -> Unit,
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (currentRole == UserRole.ADMIN) {
        ModerationInboxView(
            inquiries = inquiries,
            onResolveInquiry = onResolveInquiry,
            onMarkAsRead = onMarkAsRead,
            onSaveInternalNotes = onSaveInternalNotes,
            onDeleteInquiry = onDeleteInquiry,
            modifier = modifier
        )
    } else {
        UserContactFormView(
            onSubmitInquiry = onSubmitInquiry,
            onAdminLoginPrompt = onNavigateToAuth,
            modifier = modifier
        )
    }
}

@Composable
fun UserContactFormView(
    onSubmitInquiry: (AdminMessage) -> Unit,
    onAdminLoginPrompt: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var senderName by remember { mutableStateOf("") }
    var senderContact by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MessageCategory.DUA_REQUEST) }
    var subject by remember { mutableStateOf("") }
    var messageContent by remember { mutableStateOf("") }
    var submittedSuccessfully by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                            text = "Contact Alnoor Administration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = onAdminLoginPrompt,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Login",
                                tint = Gold400,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Submit Dua requests, Fatwa/Masla queries, or event volunteering directly to Muhtamim & Admin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gold300
                    )
                }
            }
        }

        if (submittedSuccessfully) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("JazakAllah Khair!", fontWeight = FontWeight.Bold, color = SuccessGreen)
                            Text("Your message has been sent to the Alnoor Moderation Inbox.", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = { Text("Your Full Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = senderContact,
                        onValueChange = { senderContact = it },
                        label = { Text("Contact Info (Email or Phone) *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Inquiry Category:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MessageCategory.values().forEach { category ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedCategory == category) Emerald800 else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCategory = category }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedCategory == category) Gold300 else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = messageContent,
                        onValueChange = { messageContent = it },
                        label = { Text("Your Message / Request Details *") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (senderName.isNotBlank() && subject.isNotBlank() && messageContent.isNotBlank()) {
                                val inquiry = AdminMessage(
                                    id = UUID.randomUUID().toString(),
                                    senderName = senderName,
                                    senderContact = senderContact,
                                    category = selectedCategory,
                                    subject = subject,
                                    message = messageContent,
                                    timestamp = "Today",
                                    isRead = false,
                                    status = MessageStatus.PENDING
                                )
                                onSubmitInquiry(inquiry)
                                submittedSuccessfully = true
                                senderName = ""
                                senderContact = ""
                                subject = ""
                                messageContent = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gold500,
                            contentColor = Emerald900
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_inquiry_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Message to Admin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Moderation Inbox Screen for Administrators
 * Includes viewing details, marking Read/Unread, resolving, adding internal notes, and filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationInboxView(
    inquiries: List<AdminMessage>,
    onResolveInquiry: (String, String) -> Unit,
    onMarkAsRead: (String, Boolean) -> Unit,
    onSaveInternalNotes: (String, String) -> Unit,
    onDeleteInquiry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedCategoryFilter by remember { mutableStateOf<MessageCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var inspectingInquiry by remember { mutableStateOf<AdminMessage?>(null) }
    var replyingInquiry by remember { mutableStateOf<AdminMessage?>(null) }
    var editingNotesInquiry by remember { mutableStateOf<AdminMessage?>(null) }
    var inquiryToDelete by remember { mutableStateOf<AdminMessage?>(null) }

    val unreadCount = inquiries.count { !it.isRead }
    val pendingCount = inquiries.count { it.status == MessageStatus.PENDING }

    val filteredList = inquiries.filter { inquiry ->
        val matchesStatus = when (selectedFilter) {
            "Unread" -> !inquiry.isRead
            "Pending" -> inquiry.status == MessageStatus.PENDING
            "Resolved" -> inquiry.status == MessageStatus.RESOLVED
            else -> true
        }
        val matchesCategory = selectedCategoryFilter == null || inquiry.category == selectedCategoryFilter
        val matchesSearch = searchQuery.isBlank() ||
                inquiry.subject.contains(searchQuery, ignoreCase = true) ||
                inquiry.senderName.contains(searchQuery, ignoreCase = true) ||
                inquiry.message.contains(searchQuery, ignoreCase = true) ||
                (inquiry.internalNotes?.contains(searchQuery, ignoreCase = true) == true)

        matchesStatus && matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Moderation Inbox Header
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
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Gold400, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Moderation Inbox",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (unreadCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = UrgentRed
                                ) {
                                    Text(
                                        text = "$unreadCount Unread",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Gold500
                            ) {
                                Text(
                                    text = "$pendingCount Pending",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald900,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Review incoming messages, manage internal staff notes, mark read/unread, and reply to community inquiries.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gold300
                    )
                }
            }
        }

        // Search in Inbox
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by sender, subject, keywords or notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Emerald700) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Unread", "Pending", "Resolved").forEach { status ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = { selectedFilter = status },
                        label = { Text(status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald800,
                            selectedLabelColor = Gold300
                        )
                    )
                }
            }
        }

        // Inquiries List
        if (filteredList.isEmpty()) {
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
                        Icon(Icons.Default.Mail, contentDescription = null, tint = Gold500, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No messages match this filter", fontWeight = FontWeight.Bold)
                        Text("All community queries are up to date.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredList) { inquiry ->
                val isPending = inquiry.status == MessageStatus.PENDING

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!inquiry.isRead) Emerald100.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(if (!inquiry.isRead) 3.dp else 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // View details & auto-mark read
                            onMarkAsRead(inquiry.id, true)
                            inspectingInquiry = inquiry
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isPending) Gold500.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isPending) Icons.Default.HourglassEmpty else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isPending) Gold600 else SuccessGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = inquiry.status.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPending) Gold600 else SuccessGreen
                                        )
                                    }
                                }

                                if (!inquiry.isRead) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = UrgentRed
                                    ) {
                                        Text(
                                            text = "UNREAD",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onMarkAsRead(inquiry.id, !inquiry.isRead) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (inquiry.isRead) Icons.Default.MarkEmailUnread else Icons.Default.MarkEmailRead,
                                        contentDescription = if (inquiry.isRead) "Mark Unread" else "Mark Read",
                                        tint = Emerald800,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { inquiryToDelete = inquiry },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = UrgentRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = inquiry.subject,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (!inquiry.isRead) FontWeight.ExtraBold else FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "From: ${inquiry.senderName} (${inquiry.senderContact})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Category: ${inquiry.category.title} • Date: ${inquiry.timestamp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gold600,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = inquiry.message,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        // Internal Admin Notes display
                        if (!inquiry.internalNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Gold300.copy(alpha = 0.2f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Gold600, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Staff Note:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Gold600)
                                        Text(inquiry.internalNotes, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Admin Reply display
                        if (inquiry.adminReply != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Emerald800.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Official Admin Reply:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Emerald800)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(inquiry.adminReply, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { editingNotesInquiry = inquiry },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Default.EditNote, contentDescription = "Notes", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Note", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { replyingInquiry = inquiry },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPending) Gold500 else Emerald800,
                                    contentColor = if (isPending) Emerald900 else Color.White
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(34.dp)
                            ) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = "Reply", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPending) "Reply & Resolve" else "Update Reply", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Message Details Modal Dialog
    inspectingInquiry?.let { inquiry ->
        AlertDialog(
            onDismissRequest = { inspectingInquiry = null },
            title = {
                Text(
                    text = inquiry.subject,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("From: ${inquiry.senderName} (${inquiry.senderContact})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Category: ${inquiry.category.title}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Emerald700)
                    Text("Date: ${inquiry.timestamp}", fontSize = 12.sp)

                    Divider()

                    Text("Message Content:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(inquiry.message, fontSize = 13.sp, modifier = Modifier.padding(10.dp))
                    }

                    if (!inquiry.internalNotes.isNullOrBlank()) {
                        Text("Internal Staff Notes:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Gold600)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Gold300.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(inquiry.internalNotes, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                        }
                    }

                    if (inquiry.adminReply != null) {
                        Text("Admin Reply:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald800)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Emerald800.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(inquiry.adminReply, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        replyingInquiry = inquiry
                        inspectingInquiry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Reply to Message")
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectingInquiry = null }) { Text("Close") }
            }
        )
    }

    // Internal Notes Dialog
    editingNotesInquiry?.let { inquiry ->
        var notesText by remember { mutableStateOf(inquiry.internalNotes ?: "") }

        AlertDialog(
            onDismissRequest = { editingNotesInquiry = null },
            title = { Text("Internal Admin Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add private notes visible only to Alnoor administrators:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = { Text("e.g. Forwarded to Mufti Sahab; scheduled for Sunday Khutbah.") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveInternalNotes(inquiry.id, notesText)
                        editingNotesInquiry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNotesInquiry = null }) { Text("Cancel") }
            }
        )
    }

    // Reply Dialog (Admin)
    replyingInquiry?.let { inquiry ->
        var replyText by remember {
            mutableStateOf(
                inquiry.adminReply
                    ?: "Assalamu Alaikum wa Rahmatullah. JazakAllah Khair for reaching out. We have registered your request and prayed for your family."
            )
        }

        AlertDialog(
            onDismissRequest = { replyingInquiry = null },
            title = { Text("Reply to ${inquiry.senderName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Subject: ${inquiry.subject}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Admin Response") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResolveInquiry(inquiry.id, replyText)
                        replyingInquiry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Send & Mark Resolved")
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingInquiry = null }) { Text("Cancel") }
            }
        )
    }

    // Delete Dialog (Admin)
    inquiryToDelete?.let { inquiry ->
        AlertDialog(
            onDismissRequest = { inquiryToDelete = null },
            title = { Text("Delete Inquiry?") },
            text = { Text("Are you sure you want to delete message from ${inquiry.senderName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteInquiry(inquiry.id)
                        inquiryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { inquiryToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
