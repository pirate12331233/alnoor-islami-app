package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.example.data.auth.BiometricAuthHelper
import com.example.data.auth.BiometricAvailability
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.auth.FirebaseAuthManager
import com.example.data.model.UserGender
import com.example.data.model.UserRole
import com.example.data.repository.AlnoorRepository
import com.example.data.security.SecurityCryptoManager
import com.example.ui.components.AuthMode
import com.example.ui.components.ForgotPasswordDialog
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

import androidx.compose.foundation.Image

@Composable
fun FullScreenLoginScreen(
    authManager: FirebaseAuthManager,
    repository: AlnoorRepository,
    onLoginSuccess: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authState by authManager.authState.collectAsState()
    val registeredUsers by repository.registeredUsers.collectAsState()

    val savedCreds = remember { authManager.getSavedCredentials() }

    var currentMode by remember {
        mutableStateOf(
            if (savedCreds != null && savedCreds.role == UserRole.ADMIN) AuthMode.ADMIN_SECURE_LOGIN
            else AuthMode.COMMUNITY_LOGIN
        )
    }

    // Form inputs pre-filled from saved device credentials
    var email by remember { mutableStateOf(savedCreds?.email ?: "") }
    var password by remember { mutableStateOf(savedCreds?.password ?: "") }
    var displayName by remember { mutableStateOf(savedCreds?.displayName ?: "") }
    var whatsappNumber by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<UserGender?>(null) }
    var adminPasscode by remember { mutableStateOf(savedCreds?.adminPasscode ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var customErrorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    var rememberCredentials by remember { mutableStateOf(savedCreds?.rememberMe ?: true) }
    var biometricEnabled by remember { mutableStateOf(savedCreds?.biometricEnabled ?: true) }
    val biometricAvailability = remember { BiometricAuthHelper.checkBiometricAvailability(context) }
    var hasAttemptedBiometricAutoPrompt by remember { mutableStateOf(false) }
    var showSecurityAuditDialog by remember { mutableStateOf(false) }

    val hasSavedAccount = savedCreds != null && savedCreds.email.isNotBlank() && savedCreds.password.isNotBlank()

    fun triggerBiometricAuth(
        targetEmail: String = email,
        targetPassword: String = password,
        targetRole: UserRole = if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) UserRole.ADMIN else (savedCreds?.role ?: UserRole.STANDARD_USER)
    ) {
        if (targetEmail.isBlank() || targetPassword.isBlank()) {
            customErrorMessage = "Please enter email and password first."
            return
        }
        val fragmentActivity = context.findFragmentActivity()
        if (fragmentActivity == null) {
            customErrorMessage = "Biometric window could not be attached."
            return
        }

        BiometricAuthHelper.promptBiometricAuthentication(
            activity = fragmentActivity,
            title = "Alnoor Biometric Sign In",
            subtitle = "Confirm Face ID or Fingerprint for $targetEmail",
            description = "Instant biometric verification to enter Alnoor Islami app",
            negativeButtonText = "Use Password",
            onSuccess = {
                coroutineScope.launch {
                    val resultRole: UserRole? = if (targetRole == UserRole.ADMIN && adminPasscode.isNotBlank()) {
                        val result = authManager.loginAsAdminSecure(targetEmail.trim(), targetPassword, adminPasscode.trim(), registeredUsers)
                        if (result.isSuccess) UserRole.ADMIN else null
                    } else {
                        val result = authManager.loginWithEmailPassword(targetEmail.trim(), targetPassword, registeredUsers)
                        if (result.isSuccess) authManager.authState.value.role else null
                    }

                    if (resultRole != null) {
                        val matchedUser = registeredUsers.find { it.email.equals(targetEmail.trim(), ignoreCase = true) }
                        val resolvedName = matchedUser?.fullName ?: displayName.ifBlank { savedCreds?.displayName ?: targetEmail.substringBefore('@') }
                        authManager.saveCredentials(
                            email = targetEmail.trim(),
                            password = targetPassword,
                            displayName = resolvedName,
                            role = resultRole,
                            rememberMe = true,
                            biometricEnabled = true,
                            adminPasscode = if (resultRole == UserRole.ADMIN) adminPasscode.trim() else ""
                        )
                        onLoginSuccess(resultRole)
                    } else {
                        customErrorMessage = "Biometric authentication succeeded, but login failed. Please check credentials."
                    }
                }
            },
            onError = { errMsg ->
                customErrorMessage = errMsg
            },
            onFailed = {
                customErrorMessage = "Biometric not recognized. Please try again or sign in with password."
            }
        )
    }

    // Auto-prompt Face ID / Fingerprint on launch if enabled and saved credentials exist
    LaunchedEffect(savedCreds) {
        if (savedCreds != null &&
            savedCreds.rememberMe &&
            savedCreds.biometricEnabled &&
            biometricAvailability == BiometricAvailability.AVAILABLE &&
            !hasAttemptedBiometricAutoPrompt
        ) {
            hasAttemptedBiometricAutoPrompt = true
            delay(400)
            triggerBiometricAuth(savedCreds.email, savedCreds.password, savedCreds.role)
        }
    }

    val isSignupValid = displayName.isNotBlank() &&
            email.isNotBlank() &&
            email.contains("@") &&
            whatsappNumber.isNotBlank() &&
            selectedGender != null &&
            password.isNotBlank() &&
            password.length >= 4

    val isFormReady = when (currentMode) {
        AuthMode.COMMUNITY_LOGIN -> email.isNotBlank() && password.isNotBlank()
        AuthMode.COMMUNITY_SIGNUP -> isSignupValid
        AuthMode.ADMIN_SECURE_LOGIN -> email.isNotBlank() && password.isNotBlank() && adminPasscode.isNotBlank()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Emerald900,
                        Color(0xFF063323),
                        Color(0xFF031D14)
                    )
                )
            )
            .testTag("full_screen_login_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Header: App Logo & Branding (Dynamic Resolution & Uncropped) ---
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Alnoor Islami Official Logo",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ALNOOR ISLAMI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "ALNOOR INTERNATIONAL TRUST",
                style = MaterialTheme.typography.titleSmall,
                color = Gold400,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "@AlnoorislamiMushahidat",
                style = MaterialTheme.typography.bodyMedium,
                color = Gold300,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Gold500.copy(alpha = 0.18f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold400.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "AUTHENTICATION REQUIRED TO ENTER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Gold300,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Advance Login & Saved Account Quick Access Card ---
            if (hasSavedAccount && currentMode != AuthMode.COMMUNITY_SIGNUP) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF04271B)),
                    border = BorderStroke(1.dp, Gold400.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saved_credentials_welcome_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Gold500.copy(alpha = 0.22f), CircleShape)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (biometricAvailability == BiometricAvailability.AVAILABLE) Icons.Default.Fingerprint else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Gold300,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome Back!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = savedCreds!!.displayName.ifBlank { savedCreds.email },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gold300,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF073A27))
                                        .clickable { showSecurityAuditDialog = true }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Gold300,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Hardware Encrypted (AES-256)",
                                        fontSize = 10.sp,
                                        color = Gold300,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Advance Login: Biometric Face ID / Fingerprint Button
                        if (biometricAvailability == BiometricAvailability.AVAILABLE && biometricEnabled) {
                            Button(
                                onClick = {
                                    triggerBiometricAuth(savedCreds!!.email, savedCreds.password, savedCreds.role)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold500,
                                    contentColor = Emerald900
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("biometric_login_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign In with Face ID / Fingerprint",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        } else if (biometricAvailability == BiometricAvailability.NOT_ENROLLED) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = Gold300,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Biometric sensor (Face ID / Fingerprint) detected. Enroll in Android Settings to activate 1-tap biometric unlock.",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Quick 1-Tap Sign In with Saved Password Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (savedCreds!!.role == UserRole.ADMIN && savedCreds.adminPasscode.isNotBlank()) {
                                        val res = authManager.loginAsAdminSecure(savedCreds.email, savedCreds.password, savedCreds.adminPasscode, registeredUsers)
                                        if (res.isSuccess) {
                                            onLoginSuccess(UserRole.ADMIN)
                                        } else {
                                            customErrorMessage = res.exceptionOrNull()?.localizedMessage ?: "Sign in failed"
                                        }
                                    } else {
                                        val res = authManager.loginWithEmailPassword(savedCreds.email, savedCreds.password, registeredUsers)
                                        if (res.isSuccess) {
                                            onLoginSuccess(authManager.authState.value.role)
                                        } else {
                                            customErrorMessage = res.exceptionOrNull()?.localizedMessage ?: "Sign in failed"
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald800,
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, Gold400.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("one_tap_saved_login_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Gold300,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1-Tap Quick Sign In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Switch Account / Clear Saved
                        TextButton(
                            onClick = {
                                authManager.clearSavedCredentials()
                                email = ""
                                password = ""
                                adminPasscode = ""
                                displayName = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwitchAccount,
                                contentDescription = null,
                                tint = Gold300,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear Saved Credentials / Switch Account",
                                fontSize = 11.sp,
                                color = Gold300
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Authentication Mode Switcher Card ---
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_auth_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Mode Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AuthTabButton(
                            title = "Sign In",
                            icon = Icons.Default.Person,
                            isSelected = currentMode == AuthMode.COMMUNITY_LOGIN,
                            onClick = {
                                currentMode = AuthMode.COMMUNITY_LOGIN
                                customErrorMessage = null
                                authManager.clearError()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        AuthTabButton(
                            title = "Register",
                            icon = Icons.Default.Email,
                            isSelected = currentMode == AuthMode.COMMUNITY_SIGNUP,
                            onClick = {
                                currentMode = AuthMode.COMMUNITY_SIGNUP
                                customErrorMessage = null
                                authManager.clearError()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        AuthTabButton(
                            title = "Admin",
                            icon = Icons.Default.AdminPanelSettings,
                            isSelected = currentMode == AuthMode.ADMIN_SECURE_LOGIN,
                            onClick = {
                                currentMode = AuthMode.ADMIN_SECURE_LOGIN
                                customErrorMessage = null
                                authManager.clearError()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title for current mode
                    Text(
                        text = when (currentMode) {
                            AuthMode.COMMUNITY_LOGIN -> "Community Member Login"
                            AuthMode.COMMUNITY_SIGNUP -> "Register New Member"
                            AuthMode.ADMIN_SECURE_LOGIN -> "Administrator Secure Access"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) Gold600 else Emerald900
                    )

                    Text(
                        text = when (currentMode) {
                            AuthMode.COMMUNITY_LOGIN -> "Sign in with your email address to access live broadcasts & services."
                            AuthMode.COMMUNITY_SIGNUP -> "All fields including WhatsApp number and gender are required."
                            AuthMode.ADMIN_SECURE_LOGIN -> "Enter administrator email, password, and Muhtamim security passcode."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Error Message Banner
                    val errorToShow = customErrorMessage ?: authState.errorMessage
                    if (errorToShow != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = UrgentRed.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, UrgentRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = UrgentRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorToShow,
                                    color = UrgentRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // --- FORM FIELDS ---

                    // 1) Registration Exclusive Fields
                    if (currentMode == AuthMode.COMMUNITY_SIGNUP) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                customErrorMessage = null
                            },
                            label = { Text("Full Name *") },
                            placeholder = { Text("Enter full name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Emerald800) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_fullname_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                customErrorMessage = null
                            },
                            label = { Text("Email Address *") },
                            placeholder = { Text("Enter email address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Emerald800) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = whatsappNumber,
                            onValueChange = {
                                whatsappNumber = it
                                customErrorMessage = null
                            },
                            label = { Text("WhatsApp Number *") },
                            placeholder = { Text("Enter WhatsApp number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Emerald800) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_whatsapp_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Gender Radio Selection
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Gender * (Mandatory)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
                                        Text("Male", fontWeight = if (selectedGender == UserGender.MALE) FontWeight.Bold else FontWeight.Normal)
                                    }

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
                                        Text("Female", fontWeight = if (selectedGender == UserGender.FEMALE) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 2) Email Field for Login and Admin
                    if (currentMode != AuthMode.COMMUNITY_SIGNUP) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                customErrorMessage = null
                            },
                            label = { Text(if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) "Administrator Email *" else "Email Address *") },
                            placeholder = { Text(if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) "Enter administrator email" else "Enter email address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Emerald800) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        if (hasSavedAccount && email == savedCreds?.email) {
                            Text(
                                text = "✓ Pre-saved on this device",
                                fontSize = 11.sp,
                                color = Emerald700,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 3) Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            customErrorMessage = null
                        },
                        label = { Text(if (currentMode == AuthMode.COMMUNITY_SIGNUP) "Create Password *" else "Password *") },
                        placeholder = { Text("Enter password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald800) },
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Forgot Password link for Community Login
                    if (currentMode == AuthMode.COMMUNITY_LOGIN) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showForgotPasswordDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("forgot_password_button")
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

                    // 4) Admin Passcode Field
                    if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = adminPasscode,
                            onValueChange = {
                                adminPasscode = it
                                customErrorMessage = null
                            },
                            label = { Text("Muhtamim Security Key / Passcode *") },
                            placeholder = { Text("Enter admin passcode") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Gold600) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_admin_passcode_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // --- Device Credential Storage & Biometric Options ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberCredentials = !rememberCredentials }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberCredentials,
                            onCheckedChange = { rememberCredentials = it },
                            colors = CheckboxDefaults.colors(checkedColor = Emerald800)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Remember credentials on this device for 1-tap login",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Biometric Option (Face ID / Fingerprint)
                    if (biometricAvailability != BiometricAvailability.NO_HARDWARE) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = Emerald800,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Face ID / Fingerprint Login",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (biometricAvailability == BiometricAvailability.AVAILABLE)
                                            "Advance biometric unlock enabled"
                                        else
                                            "Hardware detected (Enroll in Settings)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = {
                                    biometricEnabled = it
                                    authManager.setBiometricEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Emerald800
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- Primary Action Button ---
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                when (currentMode) {
                                    AuthMode.COMMUNITY_LOGIN -> {
                                        val result = authManager.loginWithEmailPassword(email.trim(), password, registeredUsers)
                                        if (result.isSuccess) {
                                            val role = authManager.authState.value.role
                                            val matchedUser = registeredUsers.find { it.email.equals(email.trim(), ignoreCase = true) }
                                            val resolvedName = matchedUser?.fullName ?: displayName.ifBlank { savedCreds?.displayName ?: email.trim().substringBefore('@') }
                                            if (rememberCredentials) {
                                                authManager.saveCredentials(
                                                    email = email.trim(),
                                                    password = password,
                                                    displayName = resolvedName,
                                                    role = role,
                                                    rememberMe = true,
                                                    biometricEnabled = biometricEnabled
                                                )
                                            } else {
                                                authManager.clearSavedCredentials()
                                            }
                                            onLoginSuccess(role)
                                        } else {
                                            customErrorMessage = result.exceptionOrNull()?.localizedMessage ?: "Invalid email or password"
                                        }
                                    }

                                    AuthMode.COMMUNITY_SIGNUP -> {
                                        val gender = selectedGender ?: UserGender.MALE
                                        val regResult = repository.registerNewUser(
                                            fullName = displayName.trim(),
                                            email = email.trim(),
                                            whatsappNumber = whatsappNumber.trim(),
                                            gender = gender,
                                            password = password,
                                            role = UserRole.STANDARD_USER
                                        )
                                        if (regResult.isSuccess) {
                                            authManager.signUpWithEmailPassword(email.trim(), password, displayName.trim())
                                            if (rememberCredentials) {
                                                authManager.saveCredentials(
                                                    email = email.trim(),
                                                    password = password,
                                                    displayName = displayName.trim(),
                                                    role = UserRole.STANDARD_USER,
                                                    rememberMe = true,
                                                    biometricEnabled = biometricEnabled
                                                )
                                            }
                                            onLoginSuccess(UserRole.STANDARD_USER)
                                        } else {
                                            customErrorMessage = regResult.exceptionOrNull()?.message ?: "Registration failed"
                                        }
                                    }

                                    AuthMode.ADMIN_SECURE_LOGIN -> {
                                        val result = authManager.loginAsAdminSecure(email.trim(), password, adminPasscode.trim(), registeredUsers)
                                        if (result.isSuccess) {
                                            if (rememberCredentials) {
                                                authManager.saveCredentials(
                                                    email = email.trim(),
                                                    password = password,
                                                    displayName = "Administrator",
                                                    role = UserRole.ADMIN,
                                                    rememberMe = true,
                                                    biometricEnabled = biometricEnabled,
                                                    adminPasscode = adminPasscode.trim()
                                                )
                                            } else {
                                                authManager.clearSavedCredentials()
                                            }
                                            onLoginSuccess(UserRole.ADMIN)
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
                        shape = RoundedCornerShape(12.dp),
                        enabled = !authState.isLoading && isFormReady,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (currentMode == AuthMode.ADMIN_SECURE_LOGIN) Icons.Default.Security else Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (currentMode) {
                                        AuthMode.COMMUNITY_LOGIN -> "Sign In to App"
                                        AuthMode.COMMUNITY_SIGNUP -> "Complete Registration & Enter"
                                        AuthMode.ADMIN_SECURE_LOGIN -> "Authenticate as Administrator"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Exit Application Button on login screen
            OutlinedButton(
                onClick = {
                    val activity = context as? Activity
                    activity?.finishAffinity()
                    android.os.Process.killProcess(android.os.Process.myPid())
                    kotlin.system.exitProcess(0)
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold300),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold400.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("login_screen_exit_app_button")
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Exit Application", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security & Privacy Verification Link
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showSecurityAuditDialog = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Gold400, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Security & Privacy: Passwords, Face ID & Biometrics are 100% Encrypted",
                    fontSize = 11.sp,
                    color = Gold300.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showSecurityAuditDialog) {
            SecurityAuditDialog(
                onDismiss = { showSecurityAuditDialog = false }
            )
        }

        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                repository = repository,
                authManager = authManager,
                initialEmail = email,
                onDismiss = { showForgotPasswordDialog = false },
                onPasswordResetSuccess = { updatedUser ->
                    showForgotPasswordDialog = false
                    email = updatedUser.email
                    password = ""
                    coroutineScope.launch {
                        authManager.loginWithEmailPassword(updatedUser.email, updatedUser.password, registeredUsers)
                        onLoginSuccess(updatedUser.role)
                    }
                }
            )
        }
    }
}

@Composable
private fun AuthTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Emerald800 else Color.Transparent,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Gold300 else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun SecurityAuditDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Gold400,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Security & Encryption Audit",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Emerald900
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecurityAuditItem(
                    title = "1. On-Device Credentials Storage",
                    status = "Hardware AES-256-GCM Encrypted",
                    description = "Passwords, usernames, and administrative PINs are encrypted using AES-256-GCM. The master key is safeguarded in the hardware Android KeyStore (TEE / StrongBox) and never accessible to other apps.",
                    isProtected = true
                )

                SecurityAuditItem(
                    title = "2. Biometrics (Face ID & Fingerprint)",
                    status = "Hardware Secure Enclave Isolated",
                    description = "Biometric templates never leave the dedicated hardware security processor (Titan M / Secure World). Under Android OS architecture, apps have zero access to raw biometric data, and biometrics are NEVER transmitted to cloud or remote servers.",
                    isProtected = true
                )

                SecurityAuditItem(
                    title = "3. Cloud & Local Database Storage",
                    status = "Salted SHA-256 Cryptographic Hash",
                    description = "Account passwords saved in the Room DB and synced to Firestore Cloud are protected with Salted SHA-256 hashes with unique random 128-bit salts. Raw plaintext passwords are never stored in databases.",
                    isProtected = true
                )

                SecurityAuditItem(
                    title = "4. Network Transit & Cloud Backup",
                    status = "Strict TLS 1.3 & Backup Excluded",
                    description = "All network traffic is encrypted via HTTPS/TLS 1.3. App credential stores are explicitly excluded from Android OS cloud backup rules to prevent unauthorized extraction.",
                    isProtected = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Got It, Secure", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFFF9FDFB),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun SecurityAuditItem(
    title: String,
    status: String,
    description: String,
    isProtected: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Emerald100.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, Emerald800.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Emerald900,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Emerald800,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Emerald800
            ) {
                Text(
                    text = status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold300,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}
