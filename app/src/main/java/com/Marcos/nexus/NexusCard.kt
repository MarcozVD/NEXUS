package com.Marcos.nexus

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusCard(
    onBack: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},

) {
    val auth = Firebase.auth
    val db = Firebase.firestore

    var userName by remember { mutableStateOf("Usuario") }
    var cardNumber by remember { mutableStateOf("**** **** **** 4562") }
    var saldoDisponible by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("nombre") ?: "Usuario"
                        saldoDisponible = document.getDouble("saldo") ?: 0.0
                        // Si tienes número de tarjeta guardado, cárgalo aquí
                        // cardNumber = document.getString("numeroTarjeta") ?: "**** **** **** 4321"
                    }
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.Black
                    )
                }

                Text(
                    text = "NEXUS CARD",
                    fontSize = 20.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Configuración",
                            tint = Color.Black
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Tarjeta NEXUS CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2C2C2C),
                                        Color(0xFF1A1A1A)
                                    )
                                )
                            )
                    ) {
                        // Imagen de fondo de la tarjeta
                        Image(
                            painter = painterResource(id = R.drawable.cart),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {

                            Spacer(modifier = Modifier.weight(1f))
                            // Número de tarjeta y nombre
                            Column {
                                Text(
                                    text = cardNumber,
                                    fontSize = 22.sp,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 2.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = userName.uppercase(),
                                    fontSize = 16.sp,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // Saldo disponible
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Saldo disponible",
                            fontSize = 14.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "$ ${"%,.0f".format(saldoDisponible)}",
                            fontSize = 32.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            // Título: Detalles de la tarjeta
            item {
                Text(
                    text = "Detalles de la tarjeta",
                    fontSize = 20.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Opciones de la tarjeta
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CardDetailOption(
                        icon = R.drawable.noti,
                        title = "Ver movimientos",
                        subtitle = "Consulta tus transacciones",
                        onClick = { }
                    )

                    CardDetailOption(
                        icon = R.drawable.nut,
                        title = "Configurar límites",
                        subtitle = "Define límites de gasto",
                        onClick = { }
                    )

                    CardDetailOption(
                        icon = R.drawable.guardar,
                        title = "Bloquear tarjeta",
                        subtitle = "Bloquea temporalmente tu tarjeta",
                        onClick = { }
                    )

                    CardDetailOption(
                        icon = R.drawable.recarga,
                        title = "Ver beneficios",
                        subtitle = "Descubre tus ventajas",
                        onClick = { }
                    )
                }
            }

            // Información adicional
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.noti),
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(24.dp)
                        )

                        Column {
                            Text(
                                text = "Tarjeta virtual",
                                fontSize = 14.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Tu tarjeta física llegará en 5-7 días hábiles",
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardDetailOption(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontFamily = Poppins,
                    color = Color.Gray
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.enviar),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

