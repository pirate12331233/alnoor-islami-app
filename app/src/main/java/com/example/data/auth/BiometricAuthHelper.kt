package com.example.data.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricAvailability {
    AVAILABLE,          // Biometrics (Fingerprint / Face ID) supported and enrolled
    NOT_ENROLLED,       // Device supports biometrics, but user hasn't enrolled in settings
    NO_HARDWARE,        // Device has no biometric sensor
    HW_UNAVAILABLE,     // Biometric sensor is temporarily unavailable
    UNKNOWN
}

object BiometricAuthHelper {

    /**
     * Checks whether the device hardware supports biometrics (Fingerprint / Face ID)
     * and whether biometrics are currently enrolled.
     */
    fun checkBiometricAvailability(context: Context): BiometricAvailability {
        return try {
            val biometricManager = BiometricManager.from(context)
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            when (biometricManager.canAuthenticate(authenticators)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HW_UNAVAILABLE
                else -> BiometricAvailability.UNKNOWN
            }
        } catch (e: Exception) {
            BiometricAvailability.UNKNOWN
        }
    }

    /**
     * Displays the standard Android BiometricPrompt for Face ID / Fingerprint authentication.
     */
    fun promptBiometricAuthentication(
        activity: FragmentActivity,
        title: String = "Alnoor Biometric Sign In",
        subtitle: String = "Confirm your Face ID or Fingerprint to unlock",
        description: String = "Use your registered biometric to access your account securely.",
        negativeButtonText: String = "Use Password",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user intentionally canceled or pressed "Use Password", do not treat as an error toast
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }

            val biometricPrompt = BiometricPrompt(activity, executor, callback)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Biometric prompt could not be launched.")
        }
    }
}
