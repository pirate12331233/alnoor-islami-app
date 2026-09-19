package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminMessage
import com.example.data.model.ChatThread
import com.example.data.model.MessageCategory
import com.example.data.model.MessageStatus
import com.example.data.model.UserRole
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.LightBg
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.UrgentRed
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Normalized key to identify a user's conversation thread.
 * Combines contact and sender name so 1 user = 1 persistent thread.
 */
fun getMessageThreadId(msg: AdminMessage): String {
    if (msg.threadId.isNotBlank()) return msg.threadId.trim()
    val cleanContact = msg.senderContact.trim().lowercase().filter { it.isLetterOrDigit() }
    if (cleanContact.isNotBlank() && cleanContact != "notprovided" && cleanContact != "unknown") {
        return "contact_$cleanContact"
    }
    val cleanName = msg.senderName.trim().lowercase().filter { it.isLetterOrDigit() }
    if (cleanName.isNotBlank() && cleanName != "communitymember") {
        return "name_$cleanName"
    }
    return "thread_${msg.id.take(8)}"
}

/**
 * Clean data model for a single message bubble in a chat conversation.
 */
data class ChatBubbleItem(
    val id: String,
    val text: String,
    val timestamp: String,
    val isFromAdmin: Boolean,
    val senderName: String,
    val senderContact: String,
    val isRead: Boolean,
    val category: MessageCategory = MessageCategory.GENERAL,
    val originalInquiryId: String = "",
    val isFlash: Boolean = false
)

/**
 * Converts inquiries into chronologically ordered chat bubbles for a thread,
 * cleanly handling both new threaded messages and legacy inquiries with replies.
 */
fun buildChatBubbles(messages: List<AdminMessage>): List<ChatBubbleItem> {
    val bubbles = mutableListOf<ChatBubbleItem>()
    val sorted = messages.sortedBy { it.createdAt }

    for (msg in sorted) {
        val isFlash = msg.threadId == "FLASH_BROADCAST" || msg.subject.contains("FLASH", ignoreCase = true)
        if (msg.isFromAdmin) {
            bubbles.add(
                ChatBubbleItem(
                    id = msg.id,
                    text = msg.message,
                    timestamp = msg.timestamp,
                    isFromAdmin = true,
                    senderName = msg.senderName.ifBlank { if (isFlash) "Alnoor Mosque Administration" else "Alnoor Admin" },
                    senderContact = msg.senderContact,
                    isRead = true,
                    category = msg.category,
                    originalInquiryId = msg.id,
                    isFlash = isFlash
                )
            )
        } else {
            // User message bubble
            bubbles.add(
                ChatBubbleItem(
                    id = msg.id,
                    text = msg.message,
                    timestamp = msg.timestamp,
                    isFromAdmin = false,
                    senderName = msg.senderName,
                    senderContact = msg.senderContact,
                    isRead = msg.isRead,
                    category = msg.category,
                    originalInquiryId = msg.id,
                    isFlash = false
                )
            )
            // If this message has a legacy adminReply attached, render it as the reply bubble right after
            if (!msg.adminReply.isNullOrBlank()) {
                bubbles.add(
                    ChatBubbleItem(
                        id = "${msg.id}_reply",
                        text = msg.adminReply,
                        timestamp = msg.timestamp,
                        isFromAdmin = true,
                        senderName = "Alnoor Admin",
                        senderContact = "helpline@alnoor.org",
                        isRead = true,
                        category = msg.category,
                        originalInquiryId = msg.id,
                        isFlash = false
                    )
                )
            }
        }
    }
    return bubbles
}

/**
 * Aggregates all inquiry messages into WhatsApp-style conversation threads (1 per user).
 */
fun buildChatThreads(inquiries: List<AdminMessage>): List<ChatThread> {
    val groups = inquiries.groupBy { getMessageThreadId(it) }

    return groups.map { (threadId, msgs) ->
        val sorted = msgs.sortedBy { it.createdAt }
        val latest = sorted.last()

        // Discover most descriptive user name and contact from user messages
        val userMsg = sorted.firstOrNull { !it.isFromAdmin && it.senderName.isNotBlank() && it.senderName != "Community Member" }
            ?: sorted.firstOrNull { !it.isFromAdmin }
            ?: sorted.first()

        val contact = userMsg.senderContact.ifBlank {
            sorted.firstOrNull { it.senderContact.isNotBlank() }?.senderContact ?: "Not provided"
        }

        val unreadCount = msgs.count { !it.isRead && !it.isFromAdmin }
        val isPending = msgs.any { !it.isFromAdmin && it.status == MessageStatus.PENDING }
        val category = sorted.firstOrNull { !it.isFromAdmin }?.category ?: MessageCategory.GENERAL
        val staffNotes = sorted.mapNotNull { it.internalNotes }.lastOrNull { it.isNotBlank() }

        val latestSnippet = if (latest.isFromAdmin) {
            "Admin: ${latest.message}"
        } else if (!latest.adminReply.isNullOrBlank()) {
            "Admin: ${latest.adminReply}"
        } else {
            latest.message
        }

        ChatThread(
            threadId = threadId,
            userName = userMsg.senderName.ifBlank { "Community Member" },
            userContact = contact,
            category = category,
            latestMessage = latestSnippet,
            latestTimestamp = latest.timestamp,
            latestCreatedAt = latest.createdAt,
            unreadCount = unreadCount,
            isPending = isPending,
            messages = sorted,
            internalNotes = staffNotes
        )
    }.sortedByDescending { it.latestCreatedAt }
}

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
    onSendChatMessage: (threadId: String, senderName: String, senderContact: String, text: String, isFromAdmin: Boolean, category: MessageCategory) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteThread: (threadId: String, contact: String, messages: List<AdminMessage>) -> Unit = { _, _, _ -> },
    onMarkThreadRead: (threadId: String, contact: String) -> Unit = { _, _ -> },
    onBroadcastFlashMessage: ((title: String, message: String) -> Unit)? = null,
    onNavigateToAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (currentRole == UserRole.ADMIN) {
        AdminChatMasterView(
            inquiries = inquiries,
            onSendChatMessage = onSendChatMessage,
            onResolveInquiry = onResolveInquiry,
            onMarkAsRead = onMarkAsRead,
            onSaveInternalNotes = onSaveInternalNotes,
            onDeleteInquiry = onDeleteInquiry,
            onDeleteThread = onDeleteThread,
            onMarkThreadRead = onMarkThreadRead,
            onBroadcastFlashMessage = onBroadcastFlashMessage,
            modifier = modifier
        )
    } else {
        UserWhatsAppChatView(
            inquiries = inquiries,
            onSendChatMessage = onSendChatMessage,
            onSubmitInquiry = onSubmitInquiry,
            onAdminLoginPrompt = onNavigateToAuth,
            modifier = modifier
        )
    }
}

/**
 * =====================================================================
 * USER PORTAL: WHATSAPP-STYLE 1-TO-1 THREADED CHAT WITH ADMINISTRATION
 * =====================================================================
 * A single, unified chat thread where all messages sent by this user and all
 * replies from the mosque administration appear in chronological chat bubbles.
 */
@Composable
fun UserWhatsAppChatView(
    inquiries: List<AdminMessage>,
    onSendChatMessage: (threadId: String, senderName: String, senderContact: String, text: String, isFromAdmin: Boolean, category: MessageCategory) -> Unit,
    onSubmitInquiry: (AdminMessage) -> Unit,
    onAdminLoginPrompt: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val prefs = remember { context.getSharedPreferences("alnoor_user_inquiries_prefs", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("alnoor_auth_security_prefs", Context.MODE_PRIVATE) }

    // Persistent User Identity automatically pre-pulled from database/session (or saved prefs fallback)
    var savedName by remember {
        val sessionName = authPrefs.getString("saved_user_name", "")?.takeIf { it.isNotBlank() }
            ?: authPrefs.getString("saved_cred_name", "")?.takeIf { it.isNotBlank() }
            ?: prefs.getString("last_sender_name", "") ?: ""
        mutableStateOf(sessionName)
    }
    var savedContact by remember {
        val sessionContact = authPrefs.getString("saved_user_email", "")?.takeIf { it.isNotBlank() }
            ?: authPrefs.getString("saved_cred_email", "")?.takeIf { it.isNotBlank() }
            ?: prefs.getString("last_sender_contact", "") ?: ""
        mutableStateOf(sessionContact)
    }
    var deviceThreadId by remember {
        val existing = prefs.getString("user_device_thread_id", "") ?: ""
        if (existing.isNotBlank()) {
            mutableStateOf(existing)
        } else {
            val generated = "device_${UUID.randomUUID().toString().take(8)}"
            prefs.edit().putString("user_device_thread_id", generated).apply()
            mutableStateOf(generated)
        }
    }
    var sentIds by remember {
        mutableStateOf(prefs.getStringSet("sent_inquiry_ids", emptySet())?.toSet() ?: emptySet())
    }

    var showInfoDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(MessageCategory.GENERAL) }
    var messageInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Determine this user's current conversation thread
    val normalizedContactKey = savedContact.trim().lowercase().filter { it.isLetterOrDigit() }
    val normalizedNameKey = savedName.trim().lowercase().filter { it.isLetterOrDigit() }

    // Find all messages belonging to this user
    val userMessages = inquiries.filter { msg ->
        val msgContact = msg.senderContact.trim().lowercase().filter { it.isLetterOrDigit() }
        val msgName = msg.senderName.trim().lowercase().filter { it.isLetterOrDigit() }

        msg.threadId == "FLASH_BROADCAST" ||
                (msg.isFromAdmin && msg.subject.contains("FLASH", ignoreCase = true)) ||
                sentIds.contains(msg.id) ||
                (msg.threadId.isNotBlank() && (msg.threadId == deviceThreadId || (normalizedContactKey.isNotBlank() && msg.threadId == "contact_$normalizedContactKey"))) ||
                (normalizedContactKey.isNotBlank() && msgContact == normalizedContactKey) ||
                (normalizedNameKey.isNotBlank() && msgName == normalizedNameKey && normalizedNameKey != "communitymember")
    }.sortedBy { it.createdAt }

    val chatBubbles = buildChatBubbles(userMessages)

    // Auto-scroll to bottom on new messages
    LaunchedEffect(chatBubbles.size) {
        if (chatBubbles.isNotEmpty()) {
            listState.animateScrollToItem(chatBubbles.size - 1)
        }
    }

    // Effective threadId for this user
    val effectiveThreadId = if (normalizedContactKey.isNotBlank()) {
        "contact_$normalizedContactKey"
    } else {
        deviceThreadId
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- WHATSAPP-STYLE HEADER BAR ---
        Surface(
            color = Emerald900,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Gold500)
                        ) {
                            Icon(
                                Icons.Default.Mosque,
                                contentDescription = null,
                                tint = Emerald900,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Alnoor Islami Admin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SuccessGreen)
                                )
                            }
                            Text(
                                text = "Official 1-to-1 Support & Dua Requests",
                                fontSize = 11.sp,
                                color = Gold300
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onAdminLoginPrompt) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = "Staff Login",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Identity Ribbon (Pre-pulled User Name and Contact from Database/Session, Read-Only)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald800,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (savedName.isNotBlank()) "Chatting as: $savedName ${if (savedContact.isNotBlank()) "($savedContact)" else ""}" else if (savedContact.isNotBlank()) "Chatting as: $savedContact" else "Chatting as: Community Member",
                            fontSize = 11.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // --- CHAT MESSAGE STREAM ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(LightBg)
        ) {
            if (chatBubbles.isEmpty()) {
                // Empty state greeting card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Emerald800.copy(alpha = 0.1f))
                            ) {
                                Icon(
                                    Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = Emerald800,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Assalamu Alaikum wa Rahmatullah",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Emerald900,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Welcome to the direct Alnoor Islamic Helpline thread. You can write your questions, Dua requests, or feedback below.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Gold300.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "🔒 Direct 1-to-1 conversation with Alnoor Admin. All replies appear right here.",
                                    fontSize = 11.sp,
                                    color = Gold600,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Date / Security Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 1.dp
                            ) {
                                Text(
                                    text = "🔒 Direct conversation with Alnoor Admin",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    items(chatBubbles, key = { it.id }) { bubble ->
                        UserChatBubble(
                            bubble = bubble,
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(bubble.text))
                                Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // --- BOTTOM WHATSAPP INPUT BAR ---
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = {
                        Text(
                            text = "Type a message to Alnoor Admin...",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        cursorColor = Emerald800,
                        focusedBorderColor = Emerald800,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedPlaceholderColor = Color(0xFF64748B),
                        unfocusedPlaceholderColor = Color(0xFF64748B),
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                Button(
                    onClick = {
                        if (messageInput.trim().isNotBlank()) {
                            val textToSend = messageInput.trim()
                            val userName = savedName.ifBlank { "Community Member" }
                            val userContact = savedContact.ifBlank { "Not provided" }

                            // Send via threaded system
                            onSendChatMessage(
                                effectiveThreadId,
                                userName,
                                userContact,
                                textToSend,
                                false, // isFromAdmin = false
                                selectedCategory
                            )

                            // Save thread mapping to local prefs
                            val newSent = sentIds.toMutableSet()
                            newSent.add(effectiveThreadId)
                            prefs.edit().putStringSet("sent_inquiry_ids", newSent).apply()
                            sentIds = newSent

                            messageInput = ""
                            Toast.makeText(context, "Message delivered to Mosque Admin", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = messageInput.trim().isNotBlank(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald800,
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Helpline Information Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mosque, contentDescription = null, tint = Emerald800)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Alnoor Islami Admin", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Operating Hours: Daily 9:00 AM – 9:00 PM", fontSize = 13.sp)
                    Text("• Dua Requests: Mentioned during Friday Jummah & Weekly Khatam Sharif.", fontSize = 13.sp)
                    Text("• Masla & Fatawa: Forwarded to qualified Islamic Scholars.", fontSize = 13.sp)
                    Text("• Emergency Inquiries: Please visit the administration office or contact the Imam directly.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Got It")
                }
            }
        )
    }
}

/**
 * Individual WhatsApp Chat Bubble for the User Screen
 */
@Composable
fun UserChatBubble(
    bubble: ChatBubbleItem,
    onCopy: () -> Unit
) {
    val isUser = !bubble.isFromAdmin

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (bubble.isFlash) Color(0xFFFFF1F2)
                    else if (isUser) Emerald800
                    else Color.White
                )
                .clickable { onCopy() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                if (bubble.isFlash) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFDC2626),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "⚡ FLASH BROADCAST",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                } else if (!isUser) {
                    // Admin Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Gold500.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Alnoor Admin",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold600,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else if (bubble.category != MessageCategory.GENERAL) {
                    // Category Badge for user message
                    Text(
                        text = bubble.category.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gold300,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Message Text
                Text(
                    text = bubble.text,
                    fontSize = 14.sp,
                    color = if (bubble.isFlash) Color(0xFF881337) else if (isUser) Color.White else Color(0xFF1E293B),
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp & Checkmark
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bubble.timestamp,
                        fontSize = 10.sp,
                        color = if (isUser) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = if (bubble.isRead) Gold400 else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * =====================================================================
 * ADMIN VIEW: 1-TO-1 WHATSAPP CONVERSATION THREADS MASTER-DETAIL VIEW
 * =====================================================================
 * Admin sees an organized inbox of User Threads. Clicking a User Thread
 * opens the full 1-to-1 conversation where Admin replies directly in the chat!
 */
@Composable
fun AdminChatMasterView(
    inquiries: List<AdminMessage>,
    onSendChatMessage: (threadId: String, senderName: String, senderContact: String, text: String, isFromAdmin: Boolean, category: MessageCategory) -> Unit,
    onResolveInquiry: (String, String) -> Unit,
    onMarkAsRead: (String, Boolean) -> Unit,
    onSaveInternalNotes: (String, String) -> Unit,
    onDeleteInquiry: (String) -> Unit,
    onDeleteThread: (threadId: String, contact: String, messages: List<AdminMessage>) -> Unit,
    onMarkThreadRead: (threadId: String, contact: String) -> Unit,
    onBroadcastFlashMessage: ((title: String, message: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Selected conversation thread currently being viewed by Admin (null = Inbox list)
    var selectedThreadId by remember { mutableStateOf<String?>(null) }

    val allThreads = remember(inquiries) {
        buildChatThreads(inquiries)
    }

    val currentThread = allThreads.find { it.threadId == selectedThreadId }

    if (currentThread != null) {
        // Render 1-to-1 Chat Conversation between Admin and this specific User
        AdminOneToOneChatView(
            thread = currentThread,
            onBack = { selectedThreadId = null },
            onSendReply = { text ->
                onSendChatMessage(
                    currentThread.threadId,
                    "Alnoor Admin",
                    "helpline@alnoor.org",
                    text,
                    true, // isFromAdmin = true
                    currentThread.category
                )
            },
            onSaveStaffNotes = { notes ->
                currentThread.messages.firstOrNull()?.let { firstMsg ->
                    onSaveInternalNotes(firstMsg.id, notes)
                }
            },
            onDeleteThread = {
                onDeleteThread(currentThread.threadId, currentThread.userContact, currentThread.messages)
                selectedThreadId = null
            },
            onMarkAsRead = {
                onMarkThreadRead(currentThread.threadId, currentThread.userContact)
            },
            modifier = modifier
        )
    } else {
        // Render Inbox of all User Threads (WhatsApp-style Chat List)
        AdminChatInboxView(
            threads = allThreads,
            onSelectThread = { thread ->
                onMarkThreadRead(thread.threadId, thread.userContact)
                selectedThreadId = thread.threadId
            },
            onDeleteThread = { thread ->
                onDeleteThread(thread.threadId, thread.userContact, thread.messages)
            },
            onBroadcastFlashMessage = onBroadcastFlashMessage,
            modifier = modifier
        )
    }
}

/**
 * Admin Inbox: Lists all User Conversations (1 User = 1 Item)
 */
@Composable
fun AdminChatInboxView(
    threads: List<ChatThread>,
    onSelectThread: (ChatThread) -> Unit,
    onDeleteThread: (ChatThread) -> Unit,
    onBroadcastFlashMessage: ((title: String, message: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var threadToDelete by remember { mutableStateOf<ChatThread?>(null) }
    var showFlashBroadcastDialog by remember { mutableStateOf(false) }

    val filteredThreads = remember(threads, searchQuery, selectedFilter) {
        threads.filter { thread ->
            val matchesSearch = searchQuery.isBlank() ||
                    thread.userName.contains(searchQuery, ignoreCase = true) ||
                    thread.userContact.contains(searchQuery, ignoreCase = true) ||
                    thread.latestMessage.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "PENDING" -> thread.isPending
                "RESOLVED" -> !thread.isPending
                "UNREAD" -> thread.unreadCount > 0
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    val totalUnread = threads.sumOf { it.unreadCount }
    val pendingCount = threads.count { it.isPending }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- HEADER BANNER ---
        Surface(
            color = Emerald900,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Helpline & Chat Threads",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${threads.size} active user conversations",
                            fontSize = 12.sp,
                            color = Gold300
                        )
                    }

                    if (totalUnread > 0) {
                        Surface(
                            shape = CircleShape,
                            color = UrgentRed
                        ) {
                            Text(
                                text = "$totalUnread NEW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by user name, phone, or message...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gold400) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Gold400,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        focusedContainerColor = Emerald800.copy(alpha = 0.4f),
                        unfocusedContainerColor = Emerald800.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (onBroadcastFlashMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showFlashBroadcastDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🚨 Broadcast Flash Alert To All Users",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }

        // --- FILTER CHIPS ---
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All (${threads.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Emerald800, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = selectedFilter == "UNREAD",
                    onClick = { selectedFilter = "UNREAD" },
                    label = { Text("Unread ($totalUnread)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UrgentRed, selectedLabelColor = Color.White)
                )
            }
        }

        // --- THREADS LIST ---
        if (filteredThreads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Forum,
                        contentDescription = null,
                        tint = Gold500,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No conversation threads found", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Incoming inquiries from users will create a dedicated thread.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredThreads, key = { it.threadId }) { thread ->
                    AdminThreadCard(
                        thread = thread,
                        onClick = { onSelectThread(thread) },
                        onDelete = { threadToDelete = thread }
                    )
                }
            }
        }
    }

    // Delete Thread Confirmation Dialog
    threadToDelete?.let { thread ->
        AlertDialog(
            onDismissRequest = { threadToDelete = null },
            title = { Text("Delete Conversation?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the entire conversation history with ${thread.userName}? All ${thread.messages.size} messages will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteThread(thread)
                        threadToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Yes, Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { threadToDelete = null }) {
                    Text("No, Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    if (showFlashBroadcastDialog && onBroadcastFlashMessage != null) {
        AdminFlashBroadcastDialog(
            onDismiss = { showFlashBroadcastDialog = false },
            onBroadcast = { title, message ->
                onBroadcastFlashMessage(title, message)
            }
        )
    }
}

/**
 * Dialog for Admin to compose and trigger Option 1 Full-Screen Flash Alert.
 * Supports 1,000+ characters, non-skippable alert over lock screen, and helpline chat recording.
 */
@Composable
fun AdminFlashBroadcastDialog(
    onDismiss: () -> Unit,
    onBroadcast: (title: String, message: String) -> Unit
) {
    var title by remember { mutableStateOf("URGENT MOSQUE ANNOUNCEMENT") }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Broadcast Flash Alert", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Text(
                        text = "⚡ Full-screen Alarm/Call style alert. It will pop up immediately on all users' devices (locked or active, app open or closed). User cannot skip without clicking 'OK'. It will also be saved in their 1-to-1 helpline chat history.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF991B1B),
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Alert Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Announcement Message (Supports 1,000+ chars)") },
                    placeholder = { Text("Write full mosque announcement here...") },
                    minLines = 7,
                    maxLines = 14,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Length: ${message.length} characters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (message.length >= 1000) Color(0xFF059669) else Color(0xFF64748B)
                    )
                    if (message.length >= 1000) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFECFDF5)
                        ) {
                            Text(
                                text = "✓ 1,000+ chars target met",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (message.trim().isNotBlank()) {
                        onBroadcast(title.trim(), message.trim())
                        Toast.makeText(context, "Flash alert broadcasted to all users!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    }
                },
                enabled = message.trim().isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("🚨 Broadcast Now", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Single User Thread Card in the Admin Inbox (WhatsApp-style list item)
 * Styled with Alnoor Islami deep emerald background, crisp white bold text, and gold accents.
 */
@Composable
fun AdminThreadCard(
    thread: ChatThread,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isUnread = thread.unreadCount > 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) Color(0xFF065F46) else Color(0xFF064E3B)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isUnread) 1.5.dp else 1.dp,
            color = if (isUnread) Gold400 else Gold500.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(if (isUnread) 4.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar Circle (Gold with Dark Emerald initial)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Gold500)
            ) {
                Text(
                    text = thread.userName.take(1).uppercase().ifBlank { "U" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF064E3B)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Thread Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = thread.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = thread.latestTimestamp,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gold300
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // User Identity Box (User Name / Contact / ID in prominent box)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Emerald800,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Gold400.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Gold300,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val userIdentifier = if (thread.userContact.isNotBlank() && thread.userContact != "Not provided") {
                            "${thread.userName} • ${thread.userContact}"
                        } else {
                            "${thread.userName} • ID #${thread.threadId.takeLast(6)}"
                        }
                        Text(
                            text = userIdentifier,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold300,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Latest Message Snippet (Clean, readable white/light-emerald text)
                Text(
                    text = thread.latestMessage,
                    fontSize = 13.5.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Badges & Action
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isUnread) {
                    Surface(
                        shape = CircleShape,
                        color = UrgentRed
                    ) {
                        Text(
                            text = "${thread.unreadCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = "Read",
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Isolated Delete Button to prevent accidental taps
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Conversation",
                        tint = UrgentRed.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * =====================================================================
 * ADMIN 1-TO-1 CHAT VIEW (Admin conversing directly with User)
 * =====================================================================
 */
@Composable
fun AdminOneToOneChatView(
    thread: ChatThread,
    onBack: () -> Unit,
    onSendReply: (String) -> Unit,
    onSaveStaffNotes: (String) -> Unit,
    onDeleteThread: () -> Unit,
    onMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    var replyInput by remember { mutableStateOf("") }
    var showNotesDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val chatBubbles = remember(thread.messages) {
        buildChatBubbles(thread.messages)
    }

    LaunchedEffect(chatBubbles.size) {
        if (chatBubbles.isNotEmpty()) {
            listState.animateScrollToItem(chatBubbles.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- TOP CONVERSATION BAR ---
        Surface(
            color = Emerald900,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Inbox",
                        tint = Color.White
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Gold500)
                ) {
                    Text(
                        text = thread.userName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Emerald900,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = thread.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val contactOrId = if (thread.userContact.isNotBlank() && thread.userContact != "Not provided") {
                        "${thread.userContact} • ID #${thread.threadId.takeLast(6)}"
                    } else {
                        "User ID #${thread.threadId.takeLast(6)}"
                    }
                    Text(
                        text = contactOrId,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gold300,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Phone Call Action (If user provided contact)
                if (thread.userContact.isNotBlank() && thread.userContact.any { it.isDigit() }) {
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${thread.userContact}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not launch phone dialer", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call User", tint = Color.White)
                    }
                }

                // Staff Notes Button
                IconButton(onClick = { showNotesDialog = true }) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = "Internal Staff Notes",
                        tint = if (!thread.internalNotes.isNullOrBlank()) Gold400 else Color.White.copy(alpha = 0.8f)
                    )
                }

                // Delete Thread Button
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Conversation", tint = UrgentRed)
                }
            }
        }

        // Internal Staff Note Banner (if present)
        if (!thread.internalNotes.isNullOrBlank()) {
            Surface(
                color = Gold300.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Gold600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Staff Note: ${thread.internalNotes}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gold600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { showNotesDialog = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Edit", fontSize = 11.sp, color = Gold600, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- CHAT BUBBLES STREAM ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(LightBg)
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(chatBubbles, key = { it.id }) { bubble ->
                    AdminChatBubble(
                        bubble = bubble,
                        userName = thread.userName,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(bubble.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // --- QUICK REPLY TEMPLATE CHIPS ---
        Surface(
            color = Emerald900.copy(alpha = 0.07f),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Text(
                        text = "Quick:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900,
                        modifier = Modifier.padding(top = 5.dp, start = 2.dp)
                    )
                }
                item {
                    TemplateChip("Assalamu Alaikum wa Rahmatullah") { replyInput = it }
                }
                item {
                    TemplateChip("Ameen, Dua has been recorded in Khatam Sharif.") { replyInput = it }
                }
                item {
                    TemplateChip("JazakAllah Khair for reaching out.") { replyInput = it }
                }
                item {
                    TemplateChip("Forwarded to Mufti Sahab for review.") { replyInput = it }
                }
            }
        }

        // --- BOTTOM REPLY INPUT BAR ---
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = replyInput,
                    onValueChange = { replyInput = it },
                    placeholder = { 
                        Text(
                            text = "Type reply to ${thread.userName}...", 
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        ) 
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        cursorColor = Emerald800,
                        focusedBorderColor = Emerald800,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedPlaceholderColor = Color(0xFF64748B),
                        unfocusedPlaceholderColor = Color(0xFF64748B),
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )

                Button(
                    onClick = {
                        if (replyInput.trim().isNotBlank()) {
                            onSendReply(replyInput.trim())
                            replyInput = ""
                            Toast.makeText(context, "Reply sent to ${thread.userName}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = replyInput.trim().isNotBlank(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald800,
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Reply",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Staff Notes Dialog
    if (showNotesDialog) {
        var tempNotes by remember { mutableStateOf(thread.internalNotes ?: "") }

        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Staff Private Notes", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("These notes are internal and visible only to Alnoor Mosque administrators:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = tempNotes,
                        onValueChange = { tempNotes = it },
                        placeholder = { Text("e.g. Discussed with Mufti Sahab; Dua will be made after Asr prayer.") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveStaffNotes(tempNotes)
                        showNotesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entire Conversation?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete all messages with ${thread.userName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteThread()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Bubble rendered inside the Admin's view:
 * Admin's responses are on the right (Emerald), User messages are on the left (White).
 */
@Composable
fun AdminChatBubble(
    bubble: ChatBubbleItem,
    userName: String,
    onCopy: () -> Unit
) {
    val isAdmin = bubble.isFromAdmin

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAdmin) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isAdmin) 16.dp else 4.dp,
                        bottomEnd = if (isAdmin) 4.dp else 16.dp
                    )
                )
                .background(if (isAdmin) Emerald800 else Color.White)
                .clickable { onCopy() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                if (!isAdmin) {
                    // User Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 3.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = userName,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                    }
                } else {
                    // Admin badge
                    Text(
                        text = "You (Alnoor Admin)",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold300,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = bubble.text,
                    fontSize = 14.sp,
                    color = if (isAdmin) Color.White else Color(0xFF0F172A),
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bubble.timestamp,
                        fontSize = 10.sp,
                        color = if (isAdmin) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B)
                    )
                    if (isAdmin) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = Gold400,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Quick Template Chip for Admin
 */
@Composable
fun TemplateChip(
    text: String,
    onClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald800.copy(alpha = 0.2f)),
        shadowElevation = 1.dp,
        modifier = Modifier.clickable { onClick(text) }
    ) {
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = Emerald900,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
        )
    }
}
