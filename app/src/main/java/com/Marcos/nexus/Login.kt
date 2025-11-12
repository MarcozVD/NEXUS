package com.Marcos.nexus

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.fragment.app.FragmentActivity

val Poppins = FontFamily(
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_regular, FontWeight.Normal)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusLoginScreen(navController: NavController) {
    val auth = Firebase.auth
    val context = LocalContext.current

    // Obtener la Activity correctamente
    val activity = remember {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is FragmentActivity) {
                return@remember ctx
            }
            ctx = ctx.baseContext
        }
        null
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var biometricHelper by remember { mutableStateOf<BiometricHelper?>(null) }
    var showBiometricOption by remember { mutableStateOf(false) }

    // Inicializar BiometricHelper
    LaunchedEffect(Unit) {
        if (activity != null) {
            biometricHelper = BiometricHelper(activity)
            val isBiometricAvailable = biometricHelper?.isBiometricAvailable() == true
            val hasCredentials = BiometricHelper.hasStoredCredentials(context)

            // Debug con Toast
            Toast.makeText(context, "Biometric: $isBiometricAvailable | Credentials: $hasCredentials", Toast.LENGTH_LONG).show()

            showBiometricOption = isBiometricAvailable && hasCredentials

            // Si hay credenciales guardadas, cargar el email
            if (hasCredentials) {
                val (savedEmail, _) = BiometricHelper.getCredentials(context)
                if (savedEmail != null) {
                    email = savedEmail
                }
            }
        } else {
            Toast.makeText(context, "Activity es null - no se puede usar biometric", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Logo y título
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo de la app",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "NEXUS",
                        fontSize = 57.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = "Ingreso",
                    fontSize = 45.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color.Black,
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    placeholder = { Text("Email", fontFamily = Poppins) }
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color.Black,
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    placeholder = { Text("Contraseña", fontFamily = Poppins) }
                )

                // Forgot Password
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { showForgotPasswordDialog = true }
                        .padding(vertical = 4.dp)
                )

                // Error Message
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                color = Color.Red,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Login Button
                Button(
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            errorMessage = "Por favor completa todos los campos"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        auth.signInWithEmailAndPassword(email.trim(), password.trim())
                            .addOnSuccessListener {
                                // Guardar credenciales si la autenticación biométrica está disponible
                                if (biometricHelper?.isBiometricAvailable() == true) {
                                    BiometricHelper.saveCredentials(context, email.trim(), password.trim())
                                    Toast.makeText(context, "Credenciales guardadas para huella", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Biometric no disponible", Toast.LENGTH_SHORT).show()
                                }

                                isLoading = false
                                navController.navigate("home") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            .addOnFailureListener { exception ->
                                isLoading = false
                                errorMessage = when {
                                    exception.message?.contains("password") == true -> "Contraseña incorrecta"
                                    exception.message?.contains("user") == true ||
                                            exception.message?.contains("email") == true -> "Usuario no encontrado"
                                    exception.message?.contains("network") == true -> "Error de conexión. Verifica tu internet"
                                    else -> "Error al iniciar sesión. Intenta nuevamente"
                                }
                            }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Ingresar",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Biometric Login Button
                if (showBiometricOption && activity != null && !isLoading) {
                    OutlinedButton(
                        onClick = {
                            biometricHelper?.authenticateWithBiometric(
                                onSuccess = {
                                    val (savedEmail, savedPassword) = BiometricHelper.getCredentials(context)
                                    if (savedEmail != null && savedPassword != null) {
                                        isLoading = true
                                        errorMessage = null

                                        auth.signInWithEmailAndPassword(savedEmail, savedPassword)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                navController.navigate("home") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                errorMessage = "Error al iniciar sesión con huella"
                                                // Limpiar credenciales si fallan
                                                BiometricHelper.clearCredentials(context)
                                                showBiometricOption = false
                                            }
                                    } else {
                                        errorMessage = "No hay credenciales guardadas"
                                    }
                                },
                                onError = { error ->
                                    if (error.isNotEmpty()) {
                                        errorMessage = error
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Usar huella digital",
                            fontSize = 16.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Register Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿No tienes cuenta? ",
                        fontFamily = Poppins,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    TextButton(onClick = { navController.navigate("register") }) {
                        Text(
                            text = "Regístrate",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                onDismiss = { showForgotPasswordDialog = false },
                onConfirm = { resetEmail ->
                    if (resetEmail.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Por favor ingresa tu correo electrónico",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@ForgotPasswordDialog
                    }

                    auth.sendPasswordResetEmail(resetEmail)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Correo de recuperación enviado. Revisa tu bandeja de entrada.",
                                Toast.LENGTH_LONG
                            ).show()
                            showForgotPasswordDialog = false
                        }
                        .addOnFailureListener { exception ->
                            val errorMsg = when {
                                exception.message?.contains("user") == true ->
                                    "No existe una cuenta con este correo"
                                exception.message?.contains("network") == true ->
                                    "Error de conexión. Verifica tu internet"
                                else -> "Error al enviar el correo"
                            }
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                }
            )
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isValidEmail by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
        },
        title = {
            Text(
                text = "Recuperar contraseña",
                fontWeight = FontWeight.Bold,
                fontFamily = Poppins,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                    fontSize = 14.sp,
                    fontFamily = Poppins,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() || it.isEmpty()
                    },
                    label = { Text("Correo electrónico", fontFamily = Poppins) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null)
                    },
                    isError = !isValidEmail,
                    supportingText = {
                        if (!isValidEmail) {
                            Text(
                                text = "Correo inválido",
                                color = Color.Red,
                                fontFamily = Poppins,
                                fontSize = 12.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
                        focusedLabelColor = Color.Black,
                        cursorColor = Color.Black,
                        errorBorderColor = Color.Red
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotEmpty() && isValidEmail) {
                        onConfirm(email)
                    }
                },
                enabled = email.isNotEmpty() && isValidEmail,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Enviar enlace", fontFamily = Poppins)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Black, fontFamily = Poppins)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}