package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.FirebaseAuthManager
import com.example.data.model.RegisteredUser
import com.example.data.repository.AlnoorRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class ResetStep {
    VERIFY_CREDENTIALS,
    SET_NEW_PASSWORD
}

@Composable
fun ForgotPasswordDialog(
    repository: AlnoorRepository,
    authManager: FirebaseAuthManager,
    initialEmail: String = "",
    onDismiss: () -> Unit,
    onPasswordResetSuccess: (RegisteredUser) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(ResetStep.VERIFY_CREDENTIALS) }
    var inputEmail by remember { mutableStateOf(initialEmail) }
    var inputWhatsapp by remember { mutableStateOf("") }
    var verifiedUser by remember { mutableStateOf<RegisteredUser?>(null) }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showContactMohsinDialog by remember { mutableStateOf(false) }

    fun openWhatsAppForHelp(helpMessage: String) {
        try {
            val encodedMsg = Uri.encode(helpMessage)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=$encodedMsg")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, helpMessage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Contact Administration"))
            } catch (_: Exception) {
                Toast.makeText(context, "No messaging application found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Secondary Dialog for when user forgets both Email & WhatsApp Number
    if (showContactMohsinDialog) {
        AlertDialog(
            onDismissRequest = { showContactMohsinDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Gold500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = Gold700,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Alnoor Administration Support",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Dear Respected Member! If you forget login email id & registered number then contact Mohsin Sb for further help to reset your credentials.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Text(
                        text = "Mohsin Sb can verify your identity and instantly issue a new password or find your registered email.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val msg = "Assalam-o-Alaikum Mohsin Sb, I have forgotten both my login email ID and registered number for the Alnoor app. Please help me recover my login credentials."
                        openWhatsAppForHelp(msg)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Contact Mohsin Sb", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactMohsinDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (currentStep == ResetStep.SET_NEW_PASSWORD) Emerald800 else Gold500.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (currentStep == ResetStep.SET_NEW_PASSWORD) Icons.Default.CheckCircle else Icons.Default.LockReset,
                        contentDescription = null,
                        tint = if (currentStep == ResetStep.SET_NEW_PASSWORD) Color.White else Gold700,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = if (currentStep == ResetStep.VERIFY_CREDENTIALS) "Reset Password & Find ID" else "Set New Password",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (currentStep == ResetStep.VERIFY_CREDENTIALS) "Step 1 of 2: Account Verification" else "Step 2 of 2: Create New Password",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Error message display
                errorMessage?.let { errorMsg ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = UrgentRed.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = UrgentRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = errorMsg, color = UrgentRed, fontSize = 12.sp)
                        }
                    }
                }

                // STEP 1: Verify Credentials
                if (currentStep == ResetStep.VERIFY_CREDENTIALS) {
                    Text(
                        text = "Enter your registered Email Address and WhatsApp Number to verify your identity.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = {
                            inputEmail = it
                            errorMessage = null
                        },
                        label = { Text("Registered Email Address *") },
                        placeholder = { Text("e.g. name@example.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Emerald800) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_email_input")
                    )

                    OutlinedTextField(
                        value = inputWhatsapp,
                        onValueChange = {
                            inputWhatsapp = it
                            errorMessage = null
                        },
                        label = { Text("Registered WhatsApp Number *") },
                        placeholder = { Text("e.g. 03001234567 or (555) 786-0199") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Emerald800) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_whatsapp_input")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Explicit Help Option for Users Who Forgot Both
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Gold50,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold300),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showContactMohsinDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Gold700,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Forgot both Email & WhatsApp?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Gold900
                                )
                                Text(
                                    text = "Tap here to contact Mohsin Sb for direct recovery assistance.",
                                    fontSize = 10.sp,
                                    color = Gold700
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Gold700,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // STEP 2: Account Confirmed & Enter New Password
                if (currentStep == ResetStep.SET_NEW_PASSWORD && verifiedUser != null) {
                    val user = verifiedUser!!

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Emerald800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Gold400, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verified Account Details", color = Gold300, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(user.fullName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Login ID: ${user.email}", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                            Text("WhatsApp: ${user.whatsappNumber}", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = "Please choose a strong new password for your account (minimum 4 characters):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = { Text("New Password *") },
                        placeholder = { Text("Enter new password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald800) },
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility"
                                )
                            }
                        },
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_password_input")
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm New Password *") },
                        placeholder = { Text("Re-enter new password") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = Emerald800) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility"
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_new_password_input")
                    )
                }
            }
        },
        confirmButton = {
            if (currentStep == ResetStep.VERIFY_CREDENTIALS) {
                Button(
                    onClick = {
                        if (inputEmail.isBlank() || inputWhatsapp.isBlank()) {
                            errorMessage = "Please enter both Email and WhatsApp number."
                            return@Button
                        }

                        val matchedUser = repository.findUserForPasswordReset(inputEmail.trim(), inputWhatsapp.trim())
                        if (matchedUser != null) {
                            verifiedUser = matchedUser
                            currentStep = ResetStep.SET_NEW_PASSWORD
                            errorMessage = null
                        } else {
                            errorMessage = "No matching account found with this Email and WhatsApp number."
                            // Automatically present the Mohsin Sb contact dialog if user credentials do not match
                            showContactMohsinDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading && inputEmail.isNotBlank() && inputWhatsapp.isNotBlank(),
                    modifier = Modifier.testTag("verify_account_button")
                ) {
                    Text("Verify & Continue", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (newPassword.isBlank() || newPassword.length < 4) {
                            errorMessage = "Password must be at least 4 characters long."
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            errorMessage = "Passwords do not match. Please verify."
                            return@Button
                        }

                        val user = verifiedUser ?: return@Button
                        isLoading = true

                        coroutineScope.launch {
                            val resetResult = repository.resetUserPasswordSelfService(user.userId, newPassword)
                            isLoading = false

                            if (resetResult.isSuccess) {
                                val updated = resetResult.getOrThrow()
                                // Auto-save credentials so user can log in immediately
                                authManager.saveCredentials(
                                    email = updated.email,
                                    password = newPassword.trim(),
                                    displayName = updated.fullName,
                                    role = updated.role,
                                    rememberMe = true,
                                    biometricEnabled = true
                                )

                                Toast.makeText(
                                    context,
                                    "Password successfully updated! Logging in as ${updated.fullName}...",
                                    Toast.LENGTH_LONG
                                ).show()

                                onPasswordResetSuccess(updated)
                            } else {
                                errorMessage = resetResult.exceptionOrNull()?.message ?: "Failed to update password. Please try again."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading && newPassword.length >= 4 && confirmPassword.isNotBlank(),
                    modifier = Modifier.testTag("save_new_password_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Set Password & Sign In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (currentStep == ResetStep.SET_NEW_PASSWORD) {
                        currentStep = ResetStep.VERIFY_CREDENTIALS
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (currentStep == ResetStep.SET_NEW_PASSWORD) "Back" else "Cancel")
            }
        }
    )
}
