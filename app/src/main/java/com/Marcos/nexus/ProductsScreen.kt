package com.Marcos.nexus

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp

data class Transaction(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val monto: Double = 0.0,
    val tipo: String = "",
    val fecha: Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onNavigateToCartera: () -> Unit = {},
    onNavigateToInversiones: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateToNexusCard: () -> Unit = {}
) {
    val db = Firebase.firestore
    val auth = Firebase.auth

    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            // Obtener transacciones donde el usuario es remitente
            db.collection("transacciones")
                .whereEqualTo("remitenteId", user.uid)
                .addSnapshotListener { snapshot1, error1 ->
                    if (error1 != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    val sentTransactions = snapshot1?.documents?.mapNotNull { doc ->
                        val tipo = doc.getString("tipo") ?: "envio"
                        Transaction(
                            id = doc.id,
                            nombre = doc.getString("destinatarioNombre") ?: "Usuario",
                            descripcion = when(tipo) {
                                "recarga" -> "Recarga Cajero"
                                "envio" -> "Envío Cajero"
                                "retiro" -> "Retiro Cajero"
                                else -> tipo.replaceFirstChar { it.uppercase() }
                            },
                            monto = doc.getDouble("monto") ?: 0.0,
                            tipo = tipo,
                            fecha = doc.getTimestamp("fecha")
                        )
                    } ?: emptyList()

                    // Obtener transacciones donde el usuario es destinatario (dinero recibido)
                    db.collection("transacciones")
                        .whereEqualTo("destinatarioId", user.uid)
                        .addSnapshotListener { snapshot2, error2 ->
                            if (error2 != null) {
                                isLoading = false
                                return@addSnapshotListener
                            }

                            // 🔥 CORRECCIÓN: Filtrar transacciones donde remitente = destinatario
                            val receivedTransactions = snapshot2?.documents?.mapNotNull { doc ->
                                val remitenteId = doc.getString("remitenteId")
                                val destinatarioId = doc.getString("destinatarioId")

                                // Si es la misma persona (recarga), ignorar aquí para evitar duplicados
                                if (remitenteId == destinatarioId) {
                                    return@mapNotNull null
                                }

                                Transaction(
                                    id = doc.id,
                                    nombre = doc.getString("remitenteNombre") ?: "Usuario",
                                    descripcion = "Recibido",
                                    monto = doc.getDouble("monto") ?: 0.0,
                                    tipo = "recibido",
                                    fecha = doc.getTimestamp("fecha")
                                )
                            } ?: emptyList()

                            // Combinar todas las transacciones y ordenarlas por fecha
                            transactions = (sentTransactions + receivedTransactions)
                                .sortedByDescending { it.fecha?.toDate() }
                                .take(10)

                            isLoading = false
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
                    text = "NEXUS",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título: Mis Productos
            item {
                Text(
                    text = "Mis Productos",
                    fontSize = 24.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Tarjeta NEXUS CARD
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable {onNavigateToNexusCard() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
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
                        Image(
                            painter = painterResource(id = R.drawable.cart),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {


                            // Logo tipo Mastercard
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NEXUS CARD",
                                    fontSize = 16.sp,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }



                        }
                    }
                }
            }

            // Cartera e Inversiones
            // Cartera e Inversiones
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cartera Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable { onNavigateToCartera() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Cartera",
                                        fontSize = 16.sp,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )

                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                // Icono de billetera
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Inversiones Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clickable { onNavigateToInversiones() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Inversiones",
                                        fontSize = 16.sp,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )

                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                // Icono de gráfica
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Título: Transacciones
            item {
                Text(
                    text = "Transacciones",
                    fontSize = 20.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Lista de transacciones
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }
            } else if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No hay transacciones recientes",
                        fontSize = 14.sp,
                        fontFamily = Poppins,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                }
            } else {
                items(transactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isIncome = transaction.tipo == "recarga" || transaction.tipo == "ingreso" || transaction.tipo == "recibido"
    val icon = if (isIncome) "↑" else "↓"
    val iconColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    val amountPrefix = if (isIncome) "+" else "-"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de flecha
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp,
                    color = iconColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Información
            Column {
                Text(
                    text = transaction.nombre,
                    fontSize = 14.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = transaction.descripcion,
                    fontSize = 12.sp,
                    fontFamily = Poppins,
                    color = Color.Gray
                )
            }
        }

        // Monto
        Text(
            text = "$amountPrefix$${"%,.0f".format(transaction.monto)}",
            fontSize = 16.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}

