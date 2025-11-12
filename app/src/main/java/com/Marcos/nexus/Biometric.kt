package com.Marcos.nexus

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class BiometricHelper(private val activity: FragmentActivity) {

    companion object {
        private const val PREFS_NAME = "biometric_prefs"
        private const val KEY_EMAIL = "saved_email"
        private const val KEY_PASSWORD = "saved_password"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

        // Guardar credenciales de forma segura
        fun saveCredentials(context: Context, email: String, password: String) {
            try {
                val sharedPreferences = getEncryptedSharedPreferences(context)
                sharedPreferences.edit().apply {
                    putString(KEY_EMAIL, email)
                    putString(KEY_PASSWORD, password)
                    putBoolean(KEY_BIOMETRIC_ENABLED, true)
                    apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Obtener credenciales guardadas
        fun getCredentials(context: Context): Pair<String?, String?> {
            return try {
                val sharedPreferences = getEncryptedSharedPreferences(context)
                val email = sharedPreferences.getString(KEY_EMAIL, null)
                val password = sharedPreferences.getString(KEY_PASSWORD, null)
                Pair(email, password)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(null, null)
            }
        }

        // Limpiar credenciales guardadas
        fun clearCredentials(context: Context) {
            try {
                val sharedPreferences = getEncryptedSharedPreferences(context)
                sharedPreferences.edit().clear().apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Verificar si hay credenciales guardadas
        fun hasStoredCredentials(context: Context): Boolean {
            val (email, password) = getCredentials(context)
            return !email.isNullOrEmpty() && !password.isNullOrEmpty()
        }

        // Verificar si la autenticación biométrica está habilitada
        fun isBiometricEnabled(context: Context): Boolean {
            return try {
                val sharedPreferences = getEncryptedSharedPreferences(context)
                sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        // Obtener SharedPreferences encriptado
        private fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    // Verificar si el dispositivo tiene autenticación biométrica disponible
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    // Autenticar con biometría
    fun authenticateWithBiometric(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            // Usuario canceló, no mostrar error
                            onError("")
                        }
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            onError("Demasiados intentos. Intenta más tarde")
                        }
                        else -> {
                            onError("Error de autenticación: $errString")
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación biométrica")
            .setSubtitle("Inicia sesión con tu huella digital")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}