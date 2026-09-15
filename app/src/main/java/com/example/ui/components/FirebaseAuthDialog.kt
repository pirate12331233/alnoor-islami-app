package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthUserState
import com.example.data.auth.FirebaseAuthManager
import com.example.data.model.UserGender
import com.example.data.model.UserRole
import com.example.data.repository.AlnoorRepository
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Emerald900
import com.example.ui.theme.Gold300
import com.example.ui.theme.Gold400
import com.example.ui.theme.Gold500
import com.example.ui.theme.Gold600
import com.example.ui.theme.UrgentRed
import kotlinx.coroutines.launch

enum class AuthMode {
    COMMUNITY_LOGIN,
    COMMUNITY_SIGNUP,
    ADMIN_SECURE_LOGIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseAuthDialog(
    authManager: FirebaseAuthManager,
    authState: AuthUserState,
    repository: AlnoorRepository? = null,
    onDismiss: () -> Unit,
    onRoleChanged: (UserRole) -> Unit
) {
    val context = LocalContext.current
    val effectiveRepo = remember { repository ?: AlnoorRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val savedCreds = remember { authManager.getSavedCredentials() }
    var currentMode by remember { mutableStateOf(if (authState.isAdmin || savedCreds?.role == UserRole.ADMIN) AuthMode.ADMIN_SECURE_LOGIN else AuthMode.COMMUNITY_LOGIN) }

    var email by remember { mutableStateOf(authState.email ?: savedCreds?.email ?: "") }
    var password by remember { mutableStateOf(savedCreds?.password ?: "") }
    var displayName by remember { mutableStateOf(authState.displayName ?: savedCreds?.displayName ?: "") }
    var whatsappNumber by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<UserGender?>(null) }
    var adminPasscode by remember { mutableStateOf(savedCreds?.adminPasscode ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var customErrorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val isSignupValid = displayName.isNotBlank() &&
            email.isNotBlank() &&
            email.contains("@") &&
            whatsappNumber.isNotBlank() &&
            selectedGender != null &&
            password.isNotBlank() &&
            password.length >= 4

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) Icons.Default.AdminPanelSettings else Icons.Default.Security,
                    contentDescription = null,
                    tint = if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) Gold500 else Emerald800,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentMode) {
                        AuthMode.COMMUNITY_LOGIN -> "Firebase Login"
                        AuthMode.COMMUNITY_SIGNUP -> "Register New Member"
                        AuthMode.ADMIN_SECURE_LOGIN -> "Admin Secure Login"
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // If user is already authenticated
                if (authState.isAuthenticated) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Emerald800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Signed In As:", color = Gold300, fontSize = 11.sp)
                            Text(authState.displayName ?: "Community Member", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(authState.email ?: "", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (authState.isAdmin) Gold500 else Emerald100
                            ) {
                                Text(
                                    text = if (authState.isAdmin) "ADMINISTRATOR (Muhtamim)" else "COMMUNITY MEMBER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (authState.isAdmin) Emerald900 else Emerald800,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val activity = context as? Activity
                            authManager.signOutAndExitApplication(activity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_sign_out_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out & Exit Application", fontWeight = FontWeight.Bold)
                    }

                    Divider()
                }

                // Mode Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        AuthMode.COMMUNITY_LOGIN to "Login",
                        AuthMode.COMMUNITY_SIGNUP to "Sign Up",
                        AuthMode.ADMIN_SECURE_LOGIN to "Admin"
                    ).forEach { (mode, label) ->
                        val isSelected = currentMode == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) {
                                if (mode == AuthMode.ADMIN_SECURE_LOGIN) Gold500 else Emerald800
                            } else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currentMode = mode
                                    customErrorMessage = null
                                    authManager.clearError()
                                }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) {
                                    if (mode == AuthMode.ADMIN_SECURE_LOGIN) Emerald900 else Color.White
                                } else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Error Banner if present
                val displayError = customErrorMessage ?: authState.errorMessage
                displayError?.let { errorMsg ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = UrgentRed.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMsg,
                            color = UrgentRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // SIGNUP MANDATORY FIELDS
                if (currentMode == AuthMode.COMMUNITY_SIGNUP) {
                    // Full Name (Mandatory)
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = {
                            displayName = it
                            customErrorMessage = null
                        },
                        label = { Text("Full Name *") },
                        placeholder = { Text("Enter full name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Emerald700) },
                        singleLine = true,
                        isError = displayName.isBlank() && customErrorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Email Address (Mandatory)
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            customErrorMessage = null
                        },
                        label = { Text("Email Address *") },
                        placeholder = { Text("Enter email address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Emerald700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // WhatsApp Number (Mandatory)
                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = {
                            whatsappNumber = it
                            customErrorMessage = null
                        },
                        label = { Text("WhatsApp Number *") },
                        placeholder = { Text("Enter WhatsApp number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Emerald700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Gender Selection (Radio Buttons, Mandatory)
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Select Gender * (Required)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Emerald800
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Male Radio Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { selectedGender = UserGender.MALE }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedGender == UserGender.MALE,
                                        onClick = { selectedGender = UserGender.MALE },
                                        colors = RadioButtonDefaults.colors(selectedColor = Emerald800)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Male",
                                        fontWeight = if (selectedGender == UserGender.MALE) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Female Radio Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { selectedGender = UserGender.FEMALE }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedGender == UserGender.FEMALE,
                                        onClick = { selectedGender = UserGender.FEMALE },
                                        colors = RadioButtonDefaults.colors(selectedColor = Emerald800)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Female",
                                        fontWeight = if (selectedGender == UserGender.FEMALE) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // LOGIN / ADMIN EMAIL FIELD
                if (currentMode != AuthMode.COMMUNITY_SIGNUP) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) "Admin Email" else "Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Emerald700) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (currentMode == AuthMode.COMMUNITY_SIGNUP) "Create Password *" else "Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald700) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Forgot Password link for Community Login
                if (currentMode == AuthMode.COMMUNITY_LOGIN) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showForgotPasswordDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                tint = Emerald800,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Forgot Password / User ID?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Emerald800
                            )
                        }
                    }
                }

                // Admin Security Passcode Field
                if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) {
                    OutlinedTextField(
                        value = adminPasscode,
                        onValueChange = { adminPasscode = it },
                        label = { Text("Admin Passcode / Muhtamim Key") },
                        placeholder = { Text("Enter passcode") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Gold600) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Admin access requires authorized credential verification and sets Firebase custom claims.",
                        fontSize = 11.sp,
                        color = Gold600
                    )
                }

                if (currentMode == AuthMode.COMMUNITY_SIGNUP && !isSignupValid) {
                    Text(
                        text = "* Full Name, Email, WhatsApp Number, Gender selection, and Password are required.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            val isButtonEnabled = when (currentMode) {
                AuthMode.COMMUNITY_LOGIN -> !authState.isLoading && email.isNotBlank() && password.isNotBlank()
                AuthMode.COMMUNITY_SIGNUP -> !authState.isLoading && isSignupValid
                AuthMode.ADMIN_SECURE_LOGIN -> !authState.isLoading && email.isNotBlank() && password.isNotBlank() && adminPasscode.isNotBlank()
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        when (currentMode) {
                            AuthMode.COMMUNITY_LOGIN -> {
                                val result = authManager.loginWithEmailPassword(email.trim(), password, effectiveRepo.registeredUsers.value)
                                if (result.isSuccess) {
                                    val role = authManager.authState.value.role
                                    val matchedUser = effectiveRepo.registeredUsers.value.find { it.email.equals(email.trim(), ignoreCase = true) }
                                    val resolvedName = matchedUser?.fullName ?: displayName.ifBlank { savedCreds?.displayName ?: email.trim().substringBefore('@') }
                                    authManager.saveCredentials(
                                        email = email.trim(),
                                        password = password,
                                        displayName = resolvedName,
                                        role = role,
                                        rememberMe = true,
                                        biometricEnabled = savedCreds?.biometricEnabled ?: true
                                    )
                                    effectiveRepo.syncDeviceLocationAndPrayerTimes(force = true)
                                    onRoleChanged(role)
                                    onDismiss()
                                } else {
                                    customErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "Invalid email or password"
                                }
                            }
                            AuthMode.COMMUNITY_SIGNUP -> {
                                val gender = selectedGender ?: UserGender.MALE
                                val regResult = effectiveRepo.registerNewUser(
                                    fullName = displayName.trim(),
                                    email = email.trim(),
                                    whatsappNumber = whatsappNumber.trim(),
                                    gender = gender,
                                    password = password,
                                    role = UserRole.STANDARD_USER
                                )
                                if (regResult.isSuccess) {
                                    authManager.signUpWithEmailPassword(email.trim(), password, displayName.trim())
                                    authManager.saveCredentials(
                                        email = email.trim(),
                                        password = password,
                                        displayName = displayName.trim(),
                                        role = UserRole.STANDARD_USER,
                                        rememberMe = true,
                                        biometricEnabled = true
                                    )
                                    effectiveRepo.syncDeviceLocationAndPrayerTimes(force = true)
                                    onRoleChanged(UserRole.STANDARD_USER)
                                    onDismiss()
                                } else {
                                    customErrorMessage = regResult.exceptionOrNull()?.message ?: "Failed to register member"
                                }
                            }
                            AuthMode.ADMIN_SECURE_LOGIN -> {
                                val result = authManager.loginAsAdminSecure(email.trim(), password, adminPasscode.trim(), effectiveRepo.registeredUsers.value)
                                if (result.isSuccess) {
                                    authManager.saveCredentials(
                                        email = email.trim(),
                                        password = password,
                                        displayName = "Administrator",
                                        role = UserRole.ADMIN,
                                        rememberMe = true,
                                        biometricEnabled = savedCreds?.biometricEnabled ?: true,
                                        adminPasscode = adminPasscode.trim()
                                    )
                                    effectiveRepo.syncDeviceLocationAndPrayerTimes(force = true)
                                    onRoleChanged(UserRole.ADMIN)
                                    onDismiss()
                                } else {
                                    customErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "Admin authorization failed"
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) Gold500 else Emerald800,
                    contentColor = if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) Emerald900 else Color.White
                ),
                enabled = isButtonEnabled
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text(
                        text = when (currentMode) {
                            AuthMode.COMMUNITY_LOGIN -> "Sign In"
                            AuthMode.COMMUNITY_SIGNUP -> "Complete Registration"
                            AuthMode.ADMIN_SECURE_LOGIN -> "Authenticate Admin"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            repository = effectiveRepo,
            authManager = authManager,
            initialEmail = email,
            onDismiss = { showForgotPasswordDialog = false },
            onPasswordResetSuccess = { updatedUser ->
                showForgotPasswordDialog = false
                email = updatedUser.email
                password = ""
                onRoleChanged(updatedUser.role)
                onDismiss()
            }
        )
    }
}
