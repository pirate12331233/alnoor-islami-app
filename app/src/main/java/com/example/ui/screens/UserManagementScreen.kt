package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegisteredUser
import com.example.data.model.UserGender
import com.example.data.model.UserRole
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.UrgentRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    registeredUsers: List<RegisteredUser>,
    currentRole: UserRole,
    onResetPassword: (userId: String, newPass: String) -> Unit,
    onDeleteUser: (userId: String) -> Unit,
    onUpdateRole: (userId: String, newRole: UserRole) -> Unit,
    onRegisterUser: (fullName: String, email: String, whatsappNumber: String, gender: UserGender, pass: String, role: UserRole) -> Unit,
    onUpdateAdminPin: ((currentPin: String, newPin: String) -> Result<Unit>)? = null,
    onRefreshUsers: (() -> Unit)? = null,
    onRequestAdminLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenderFilter by remember { mutableStateOf<UserGender?>(null) }
    var userToResetPassword by remember { mutableStateOf<RegisteredUser?>(null) }
    var userToDelete by remember { mutableStateOf<RegisteredUser?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Restricted Access Check for Non-Admins
    if (currentRole != UserRole.ADMIN) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(UrgentRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = UrgentRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Administrator Access Required",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )

                    Text(
                        text = "User Management is strictly confidential and reserved for Administrators (Muhtamim). Please authenticate with administrator credentials to view registered member accounts and perform password resets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = onRequestAdminLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Gold300)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log In as Administrator")
                    }
                }
            }
        }
        return
    }

    // Filter Users
    val filteredUsers = registeredUsers.filter { user ->
        val matchesSearch = searchQuery.isBlank() ||
                user.fullName.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.whatsappNumber.contains(searchQuery, ignoreCase = true) ||
                user.role.name.contains(searchQuery, ignoreCase = true)

        val matchesGender = selectedGenderFilter == null || user.gender == selectedGenderFilter

        matchesSearch && matchesGender
    }

    val totalCount = registeredUsers.size
    val maleCount = registeredUsers.count { it.gender == UserGender.MALE }
    val femaleCount = registeredUsers.count { it.gender == UserGender.FEMALE }
    val adminCount = registeredUsers.count { it.role == UserRole.ADMIN }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = Emerald800,
                contentColor = Gold300,
                modifier = Modifier.testTag("admin_add_user_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Gold500),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ManageAccounts,
                                        contentDescription = null,
                                        tint = Emerald900,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "User Management",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Search accounts & reset member passwords",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gold300
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (onRefreshUsers != null) {
                                    IconButton(
                                        onClick = {
                                            onRefreshUsers()
                                            feedbackMessage = "Syncing users with cloud..."
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LockReset,
                                            contentDescription = "Sync Cloud Users",
                                            tint = Gold300,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold500
                                ) {
                                    Text(
                                        text = "ADMIN CONSOLE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Emerald900,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatChip(label = "Total Users", value = "$totalCount", modifier = Modifier.weight(1f))
                            StatChip(label = "Brothers (Male)", value = "$maleCount", modifier = Modifier.weight(1f))
                            StatChip(label = "Sisters (Female)", value = "$femaleCount", modifier = Modifier.weight(1f))
                            StatChip(label = "Admins", value = "$adminCount", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Feedback Message Banner
            if (feedbackMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Emerald100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = feedbackMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Emerald900
                            )
                            IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Dismiss", tint = Emerald900)
                            }
                        }
                    }
                }
            }

            // Admin Security Passcode / Muhtamim PIN Management Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Gold500),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = Emerald900,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Admin Security Passcode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald900
                                )
                                Text(
                                    text = "Manage master authorization PIN for administrator access",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showChangePinDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("admin_change_pin_card_button")
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Gold300, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change PIN", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Bar & Filter Controls
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search users by name, email, or WhatsApp...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Emerald800)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_user_search_input")
                    )

                    // Gender filter chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilterPill(
                            label = "All (${registeredUsers.size})",
                            isSelected = selectedGenderFilter == null,
                            onClick = { selectedGenderFilter = null }
                        )

                        FilterPill(
                            label = "Male ($maleCount)",
                            isSelected = selectedGenderFilter == UserGender.MALE,
                            onClick = { selectedGenderFilter = UserGender.MALE }
                        )

                        FilterPill(
                            label = "Female ($femaleCount)",
                            isSelected = selectedGenderFilter == UserGender.FEMALE,
                            onClick = { selectedGenderFilter = UserGender.FEMALE }
                        )
                    }
                }
            }

            // Users List
            if (filteredUsers.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No users found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "No accounts match \"$searchQuery\"" else "No users registered yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredUsers, key = { it.id }) { user ->
                    UserAccountCard(
                        user = user,
                        onResetPasswordClick = { userToResetPassword = user },
                        onDeleteClick = { userToDelete = user },
                        onToggleRole = {
                            val newRole = if (user.role == UserRole.ADMIN) UserRole.STANDARD_USER else UserRole.ADMIN
                            onUpdateRole(user.id, newRole)
                            feedbackMessage = "Role updated for ${user.fullName}"
                        }
                    )
                }
            }
        }
    }

    // Reset Password Modal Dialog
    userToResetPassword?.let { targetUser ->
        AdminResetPasswordDialog(
            user = targetUser,
            onDismiss = { userToResetPassword = null },
            onConfirmReset = { newPassword ->
                onResetPassword(targetUser.id, newPassword)
                userToResetPassword = null
                feedbackMessage = "Password successfully reset for ${targetUser.fullName} (${targetUser.email})"
            }
        )
    }

    // Delete Confirmation Dialog
    userToDelete?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = UrgentRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Are you sure you want to permanently delete the registered account for ${targetUser.fullName} (${targetUser.email})? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(targetUser.id)
                        userToDelete = null
                        feedbackMessage = "Account deleted for ${targetUser.fullName}"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UrgentRed)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add User Dialog
    if (showAddUserDialog) {
        AdminAddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onRegister = { fullName, email, whatsapp, gender, pass, role ->
                onRegisterUser(fullName, email, whatsapp, gender, pass, role)
                showAddUserDialog = false
                feedbackMessage = "Registered new account: $fullName"
            }
        )
    }

    // Change Admin Security PIN Dialog
    if (showChangePinDialog && onUpdateAdminPin != null) {
        AdminChangePinDialog(
            onDismiss = { showChangePinDialog = false },
            onConfirmChangePin = { currentPin, newPin ->
                val res = onUpdateAdminPin(currentPin, newPin)
                if (res.isSuccess) {
                    showChangePinDialog = false
                    feedbackMessage = "Administrator Security PIN updated successfully."
                }
                res
            }
        )
    }
}

@Composable
private fun UserAccountCard(
    user: RegisteredUser,
    onResetPasswordClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleRole: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // User Avatar and Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (user.gender == UserGender.MALE) Emerald800 else Gold600),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Role Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (user.role == UserRole.ADMIN) Gold500 else Emerald100
                            ) {
                                Text(
                                    text = if (user.role == UserRole.ADMIN) "ADMIN" else "MEMBER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (user.role == UserRole.ADMIN) Emerald900 else Emerald900,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }

                            // Gender Badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (user.gender == UserGender.MALE) "Brother (Male)" else "Sister (Female)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Delete button
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete User",
                        tint = UrgentRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Info Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Email
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Emerald800, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // WhatsApp Number
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Emerald800, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WhatsApp: ${user.whatsappNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Password & Security Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = Gold600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stored Hash / Password: •••••••• (${user.password.length} chars)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Registration timestamp
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Registered: ${user.registeredAtFormatted}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reset Password Button (Primary Requirement)
                Button(
                    onClick = onResetPasswordClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_password_button_${user.id}")
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = Gold300, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Password", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Toggle Role Button
                OutlinedButton(
                    onClick = onToggleRole,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Emerald800, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.role == UserRole.ADMIN) "Make Member" else "Make Admin",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminResetPasswordDialog(
    user: RegisteredUser,
    onDismiss: () -> Unit,
    onConfirmReset: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = Emerald800,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Admin Password Reset", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("For ${user.fullName}", fontSize = 12.sp, color = Gold600)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "As an Administrator, you are directly updating the authentication credentials for ${user.email}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = UrgentRed.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = UrgentRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("New Password *") },
                    placeholder = { Text("Enter new password") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Emerald800) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_new_password_input")
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm New Password *") },
                    placeholder = { Text("Re-enter new password") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Emerald800) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_confirm_password_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword.isBlank()) {
                        errorMessage = "Password cannot be empty"
                    } else if (newPassword.length < 4) {
                        errorMessage = "Password must be at least 4 characters"
                    } else if (newPassword != confirmPassword) {
                        errorMessage = "Passwords do not match"
                    } else {
                        onConfirmReset(newPassword)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                modifier = Modifier.testTag("admin_submit_reset_password_button")
            ) {
                Text("Set New Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AdminAddUserDialog(
    onDismiss: () -> Unit,
    onRegister: (fullName: String, email: String, whatsapp: String, gender: UserGender, pass: String, role: UserRole) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(UserGender.MALE) }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STANDARD_USER) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Emerald800)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register New Member", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (errorMsg != null) {
                    Text(errorMsg!!, color = UrgentRed, fontSize = 11.sp)
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Gender radio buttons
                Text("Gender *", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedGender = UserGender.MALE }
                    ) {
                        RadioButton(
                            selected = selectedGender == UserGender.MALE,
                            onClick = { selectedGender = UserGender.MALE },
                            colors = RadioButtonDefaults.colors(selectedColor = Emerald800)
                        )
                        Text("Male")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedGender = UserGender.FEMALE }
                    ) {
                        RadioButton(
                            selected = selectedGender == UserGender.FEMALE,
                            onClick = { selectedGender = UserGender.FEMALE },
                            colors = RadioButtonDefaults.colors(selectedColor = Emerald800)
                        )
                        Text("Female")
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Initial Password *") },
                    placeholder = { Text("Enter password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank() || email.isBlank() || whatsapp.isBlank() || password.isBlank()) {
                        errorMsg = "All fields are mandatory"
                    } else {
                        onRegister(fullName.trim(), email.trim(), whatsapp.trim(), selectedGender, password, selectedRole)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
            ) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Emerald800,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Gold300
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Emerald800 else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AdminChangePinDialog(
    onDismiss: () -> Unit,
    onConfirmChangePin: (currentPin: String, newPin: String) -> Result<Unit>
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var currentPinVisible by remember { mutableStateOf(false) }
    var newPinVisible by remember { mutableStateOf(false) }
    var confirmPinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = Gold600,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Change Admin Security PIN", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Update Muhtamim Master Passcode", fontSize = 12.sp, color = Gold600)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "This PIN authorizes administrator sign-in, member management, and real-time content changes across devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = UrgentRed.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = UrgentRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Current PIN
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = {
                        currentPin = it
                        errorMessage = null
                    },
                    label = { Text("Current Admin PIN *") },
                    placeholder = { Text("Enter current PIN") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Emerald800) },
                    trailingIcon = {
                        IconButton(onClick = { currentPinVisible = !currentPinVisible }) {
                            Icon(
                                imageVector = if (currentPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility"
                            )
                        }
                    },
                    visualTransformation = if (currentPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_current_pin_input")
                )

                // New PIN
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        newPin = it
                        errorMessage = null
                    },
                    label = { Text("New Admin PIN (Min 4 digits) *") },
                    placeholder = { Text("Enter new PIN") },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = Gold600) },
                    trailingIcon = {
                        IconButton(onClick = { newPinVisible = !newPinVisible }) {
                            Icon(
                                imageVector = if (newPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility"
                            )
                        }
                    },
                    visualTransformation = if (newPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_new_pin_input")
                )

                // Confirm New PIN
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it
                        errorMessage = null
                    },
                    label = { Text("Confirm New Admin PIN *") },
                    placeholder = { Text("Re-enter new PIN") },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = Gold600) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPinVisible = !confirmPinVisible }) {
                            Icon(
                                imageVector = if (confirmPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility"
                            )
                        }
                    },
                    visualTransformation = if (confirmPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_confirm_pin_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentPin.isBlank()) {
                        errorMessage = "Please enter the current Admin PIN"
                    } else if (newPin.isBlank() || newPin.length < 4) {
                        errorMessage = "New PIN must be at least 4 characters"
                    } else if (newPin != confirmPin) {
                        errorMessage = "New PIN and Confirmation PIN do not match"
                    } else {
                        val res = onConfirmChangePin(currentPin, newPin)
                        if (res.isFailure) {
                            errorMessage = res.exceptionOrNull()?.message ?: "Failed to update PIN."
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                modifier = Modifier.testTag("admin_submit_change_pin_button")
            ) {
                Text("Save New PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
