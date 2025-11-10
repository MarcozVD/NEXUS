package com.Marcos.nexus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {}

) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val user = auth.currentUser

    var userName by remember { mutableStateOf("Cargando...") }
    var userEmail by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (user != null) {
            userEmail = user.email ?: ""

            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("nombre") ?: "Usuario"
                        userPhone = document.getString("telefono") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Mi Perfil",
                    fontSize = 24.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Icono de perfil grande
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.05f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName,
                    fontSize = 24.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Usuario NEXUS",
                    fontSize = 14.sp,
                    fontFamily = Poppins,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Card de información
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Información personal",
                            fontSize = 16.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        // Nombre
                        InfoField(
                            icon = Icons.Default.Person,
                            label = "Nombre completo",
                            value = userName
                        )

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // Email
                        InfoField(
                            icon = Icons.Default.Email,
                            label = "Correo electrónico",
                            value = userEmail
                        )

                        Divider(color = Color.Gray.copy(alpha = 0.2f))

                        // Teléfono
                        if (userPhone.isNotEmpty()) {
                            InfoField(
                                icon = Icons.Default.Phone,
                                label = "Teléfono",
                                value = userPhone
                            )
                            Divider(color = Color.Gray.copy(alpha = 0.2f))
                        }

                        // Contraseña
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Column {
                                        Text(
                                            text = "Contraseña",
                                            fontSize = 12.sp,
                                            fontFamily = Poppins,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (showPassword) "••••••••" else "••••••••",
                                            fontSize = 16.sp,
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showPassword = !showPassword }
                                ) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                                        tint = Color.Black.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Información de cuenta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Información de cuenta",
                            fontSize = 16.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ID de Usuario",
                                fontSize = 14.sp,
                                fontFamily = Poppins,
                                color = Color.Gray
                            )
                            Text(
                                text = user?.uid?.take(12) + "...",
                                fontSize = 14.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Estado",
                                fontSize = 14.sp,
                                fontFamily = Poppins,
                                color = Color.Gray
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = Color(0xFF4CAF50),
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = "Activa",
                                    fontSize = 14.sp,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de cerrar sesión
                Button(
                    onClick = {

                        auth.signOut()
                        onLogout()


                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar sesión",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun InfoField(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = Poppins,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}