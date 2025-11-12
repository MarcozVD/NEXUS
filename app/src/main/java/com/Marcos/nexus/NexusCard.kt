package com.Marcos.nexus

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp

data class TransactionHistory(
    val id: String = "",
    val tipo: String = "",
    val monto: Double = 0.0,
    val descripcion: String = "",
    val fecha: Timestamp? = null,
    val nombre: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusCard(
    onBack: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val context = LocalContext.current

    var userName by remember { mutableStateOf("Usuario") }
    var cardNumber by remember { mutableStateOf("2332 1231 1233 4562") }
    var saldoDisponible by remember { mutableStateOf(0.0) }
    var isCardBlocked by remember { mutableStateOf(false) }
    var showMovements by remember { mutableStateOf(false) }
    var showLimitsDialog by remember { mutableStateOf(false) }
    var showBenefitsDialog by remember { mutableStateOf(false) }
    var transactions by remember { mutableStateOf<List<TransactionHistory>>(emptyList()) }
    var savingsGoal by remember { mutableStateOf(0.0) }
    var currentSavings by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            // Cargar datos del usuario
            db.collection("usuarios").document(user.uid)
                .addSnapshotListener { document, error ->
                    if (document != null && document.exists()) {
                        userName = document.getString("nombre") ?: "Usuario"
                        saldoDisponible = document.getDouble("saldo") ?: 0.0
                        isCardBlocked = document.getBoolean("tarjetaBloqueada") ?: false
                        savingsGoal = document.getDouble("metaAhorro") ?: 0.0
                        currentSavings = document.getDouble("ahorroActual") ?: 0.0
                    }
                }

            // Cargar historial de transacciones
            db.collection("transacciones")
                .addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        val allTrans = snapshot.documents.mapNotNull { doc ->
                            val tipo = doc.getString("tipo") ?: ""
                            val remitenteId = doc.getString("remitenteId")
                            val destinatarioId = doc.getString("destinatarioId")
                            val userId = doc.getString("userId")

                            when {
                                // Recargas
                                tipo == "recarga" && userId == user.uid -> {
                                    TransactionHistory(
                                        id = doc.id,
                                        tipo = "recarga",
                                        monto = doc.getDouble("monto") ?: 0.0,
                                        descripcion = "Recarga de saldo",
                                        fecha = doc.getTimestamp("fecha"),
                                        nombre = "NEXUS"
                                    )
                                }
                                // Envíos
                                remitenteId == user.uid && destinatarioId != user.uid -> {
                                    TransactionHistory(
                                        id = doc.id,
                                        tipo = "envio",
                                        monto = doc.getDouble("monto") ?: 0.0,
                                        descripcion = "Transferencia enviada",
                                        fecha = doc.getTimestamp("fecha"),
                                        nombre = doc.getString("destinatarioNombre") ?: "Usuario"
                                    )
                                }
                                // Recibidos
                                destinatarioId == user.uid && remitenteId != user.uid -> {
                                    TransactionHistory(
                                        id = doc.id,
                                        tipo = "recibido",
                                        monto = doc.getDouble("monto") ?: 0.0,
                                        descripcion = "Transferencia recibida",
                                        fecha = doc.getTimestamp("fecha"),
                                        nombre = doc.getString("remitenteNombre") ?: "Usuario"
                                    )
                                }
                                else -> null
                            }
                        }

                        // Cargar inversiones
                        db.collection("inversiones")
                            .whereEqualTo("usuarioId", user.uid)
                            .get()
                            .addOnSuccessListener { invSnapshot ->
                                val investments = invSnapshot.documents.mapNotNull { doc ->
                                    TransactionHistory(
                                        id = doc.id,
                                        tipo = "inversion",
                                        monto = doc.getDouble("monto") ?: 0.0,
                                        descripcion = doc.getString("nombre") ?: "Inversión",
                                        fecha = doc.getTimestamp("fechaInicio"),
                                        nombre = "Inversión"
                                    )
                                }

                                transactions = (allTrans + investments)
                                    .sortedByDescending { it.fecha?.toDate() }
                            }
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = if (isCardBlocked) {
                                            listOf(Color(0xFF757575), Color(0xFF424242))
                                        } else {
                                            listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))
                                        }
                                    )
                                )
                        ) {
                            if (!isCardBlocked) {
                                Image(
                                    painter = painterResource(id = R.drawable.cart),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (isCardBlocked) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Surface(
                                            color = Color.Red.copy(alpha = 0.9f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                ),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "TARJETA BLOQUEADA",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Column {
                                    Text(
                                        text = if (isCardBlocked) "**** **** **** ****" else cardNumber,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 2.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = if (isCardBlocked) "****" else userName.uppercase(),
                                        fontSize = 16.sp,
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
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
                                fontWeight = FontWeight.Medium,
                                color = Color.Black.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "$ ${"%,.0f".format(saldoDisponible)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Meta de ahorro (si existe)
                if (savingsGoal > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Meta de ahorro",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${((currentSavings / savingsGoal) * 100).toInt()}%",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                LinearProgressIndicator(
                                    progress = (currentSavings / savingsGoal).toFloat().coerceIn(0f, 1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = Color(0xFF4CAF50),
                                    trackColor = Color(0xFFC8E6C9)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$ ${"%,.0f".format(currentSavings)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Meta: $ ${"%,.0f".format(savingsGoal)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Detalles de la tarjeta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CardDetailOption(
                            icon = Icons.Default.List,
                            title = "Ver movimientos",
                            subtitle = "Consulta tus transacciones",
                            iconColor = Color.Black,
                            backgroundColor = Color.White,
                            onClick = { showMovements = true }
                        )

                        CardDetailOption(
                            icon = Icons.Default.Savings,
                            title = "Configurar meta",
                            subtitle = "Define tu meta de ahorro",
                            iconColor = Color.Black,
                            backgroundColor = Color.White,
                            onClick = { showLimitsDialog = true }
                        )

                        CardDetailOption(
                            icon = if (isCardBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            title = if (isCardBlocked) "Desbloquear tarjeta" else "Bloquear tarjeta",
                            subtitle = if (isCardBlocked) "Activar tu tarjeta" else "Bloquea temporalmente tu tarjeta",
                            iconColor = if (isCardBlocked) Color.White else Color.Black,
                            backgroundColor = if (isCardBlocked) Color.Red else Color.White,
                            onClick = {
                                val user = auth.currentUser
                                if (user != null) {
                                    db.collection("usuarios").document(user.uid)
                                        .update("tarjetaBloqueada", !isCardBlocked)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                if (isCardBlocked) "Tarjeta desbloqueada" else "Tarjeta bloqueada",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                        )

                        CardDetailOption(
                            icon = Icons.Default.Star,
                            title = "Ver beneficios",
                            subtitle = "Descubre tus ventajas",
                            iconColor = Color.Black,
                            backgroundColor = Color.White,
                            onClick = { showBenefitsDialog = true }
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(24.dp)
                            )

                            Column {
                                Text(
                                    text = "Tarjeta virtual",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Tu tarjeta física llegará en 5-7 días hábiles",
                                    fontSize = 12.sp,
                                    color = Color.Black.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog de Movimientos
        if (showMovements) {
            MovementsDialog(
                transactions = transactions,
                onDismiss = { showMovements = false }
            )
        }

        // Dialog de Meta de Ahorro
        if (showLimitsDialog) {
            SavingsGoalDialog(
                currentGoal = savingsGoal,
                onDismiss = { showLimitsDialog = false },
                onConfirm = { newGoal ->
                    val user = auth.currentUser
                    if (user != null) {
                        db.collection("usuarios").document(user.uid)
                            .update("metaAhorro", newGoal)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Meta de ahorro establecida",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showLimitsDialog = false
                            }
                    }
                }
            )
        }

        // Dialog de Beneficios
        if (showBenefitsDialog) {
            BenefitsDialog(onDismiss = { showBenefitsDialog = false })
        }
    }
}

@Composable
fun CardDetailOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
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
                        color = if (backgroundColor == Color.Red)
                            Color.White.copy(alpha = 0.2f)
                        else
                            Color(0xFFF5F5F5),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (backgroundColor == Color.Red) Color.White else Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (backgroundColor == Color.Red)
                        Color.White.copy(alpha = 0.8f)
                    else
                        Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (backgroundColor == Color.Red) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun MovementsDialog(
    transactions: List<TransactionHistory>,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial de movimientos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                HorizontalDivider()

                // Lista de transacciones
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (transactions.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No hay movimientos",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(transactions) { transaction ->
                            TransactionHistoryItem(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionHistoryItem(transaction: TransactionHistory) {
    val isPositive = transaction.tipo == "recarga" || transaction.tipo == "recibido"
    val icon = when (transaction.tipo) {
        "recarga" -> Icons.Default.Add
        "envio" -> Icons.Default.ArrowUpward
        "recibido" -> Icons.Default.ArrowDownward
        "inversion" -> Icons.Default.TrendingUp
        else -> Icons.Default.SwapHoriz
    }
    val iconColor = when (transaction.tipo) {
        "recarga" -> Color(0xFF4CAF50)
        "envio" -> Color(0xFFF44336)
        "recibido" -> Color(0xFF4CAF50)
        "inversion" -> Color(0xFF2196F3)
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = transaction.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = transaction.descripcion,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Text(
            text = "${if (isPositive) "+" else "-"}$${"%,.0f".format(transaction.monto)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
fun SavingsGoalDialog(
    currentGoal: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(if (currentGoal > 0) currentGoal.toFloat() else 100000f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Configura tu meta de ahorro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Establece un objetivo y haz seguimiento de tu progreso",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = if (amount > 0) "%,.0f".format(amount) else "",
                    onValueChange = { input ->
                        val cleanInput = input.replace(",", "").replace(".", "")
                        val newAmount = cleanInput.toFloatOrNull()
                        if (newAmount != null && newAmount > 0) {
                            amount = newAmount
                        }
                    },
                    label = { Text("Meta de ahorro") },
                    prefix = { Text("$", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFF4CAF50),
                        cursorColor = Color(0xFF4CAF50)
                    ),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (amount > 0) {
                            onConfirm(amount.toDouble())
                        }
                    },
                    enabled = amount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Establecer meta",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun BenefitsDialog(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.75f)
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Beneficios NEXUS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF3E5F5)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFF9C27B0),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "NEXUS Card Premium",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Disfruta de una experiencia bancaria única con beneficios exclusivos diseñados para ti.",
                                    fontSize = 14.sp,
                                    color = Color.Black.copy(alpha = 0.7f),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    item {
                        BenefitItem(
                            icon = Icons.Default.MonetizationOn,
                            title = "0% Comisiones",
                            description = "Sin costos de mantenimiento ni comisiones ocultas. Tu dinero es 100% tuyo.",
                            color = Color(0xFF4CAF50)
                        )
                    }

                    item {
                        BenefitItem(
                            icon = Icons.Default.Security,
                            title = "Seguridad Avanzada",
                            description = "Autenticación biométrica, notificaciones en tiempo real y bloqueo instantáneo de tarjeta.",
                            color = Color(0xFF2196F3)
                        )
                    }

                    item {
                        BenefitItem(
                            icon = Icons.Default.TrendingUp,
                            title = "Inversiones Inteligentes",
                            description = "Accede a opciones de inversión con tasas de hasta 25% anual y sin montos mínimos altos.",
                            color = Color(0xFFFF9800)
                        )
                    }

                    item {
                        BenefitItem(
                            icon = Icons.Default.FlashOn,
                            title = "Transferencias Instantáneas",
                            description = "Envía y recibe dinero en segundos, sin importar el banco. 24/7 sin límites.",
                            color = Color(0xFFFFC107)
                        )
                    }

                    item {
                        BenefitItem(
                            icon = Icons.Default.AccountBalance,
                            title = "Ahorro Programado",
                            description = "Establece metas de ahorro y haz seguimiento automático de tu progreso hacia tus objetivos.",
                            color = Color(0xFF00BCD4)
                        )
                    }

                    item {
                        BenefitItem(
                            icon = Icons.Default.CreditCard,
                            title = "Tarjeta Virtual Inmediata",
                            description = "Usa tu tarjeta desde el primer día mientras esperas tu tarjeta física premium.",
                            color = Color(0xFF9C27B0)
                        )
                    }

                    item {
                        BenefitItem(
                            icon = Icons.Default.Notifications,
                            title = "Notificaciones Inteligentes",
                            description = "Recibe alertas instantáneas de cada movimiento y mantén el control total de tu cuenta.",
                            color = Color(0xFFE91E63)
                        )
                    }

                    item {
                        BenefitItem(
                            icon = Icons.Default.Support,
                            title = "Soporte 24/7",
                            description = "Atención personalizada cuando la necesites, desde la app o por teléfono.",
                            color = Color(0xFF607D8B)
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Celebration,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "¡Y mucho más!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Seguimos agregando beneficios para hacer tu experiencia bancaria excepcional.",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.Black.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }
    }
}