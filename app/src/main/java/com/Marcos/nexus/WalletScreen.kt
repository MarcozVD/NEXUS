package com.Marcos.nexus

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.widget.Toast
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class WalletTransaction(
    val id: String = "",
    val monto: Double = 0.0,
    val tipo: String = "",
    val descripcion: String = "",
    val fecha: Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val context = LocalContext.current

    var saldoCartera by remember { mutableStateOf(0.0) }
    var saldoDisponible by remember { mutableStateOf(0.0) }
    var walletTransactions by remember { mutableStateOf<List<WalletTransaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferAmount by remember { mutableStateOf(0f) }
    var isTransferring by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            // Obtener saldos
            db.collection("usuarios").document(user.uid)
                .addSnapshotListener { document, error ->
                    if (error != null) return@addSnapshotListener
                    if (document != null && document.exists()) {
                        saldoCartera = document.getDouble("saldoCartera") ?: 0.0
                        saldoDisponible = document.getDouble("saldo") ?: 0.0
                    }
                }

            // Obtener transacciones de cartera
            db.collection("carteraTransacciones")
                .whereEqualTo("usuarioId", user.uid)
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }

                    walletTransactions = snapshot?.documents?.mapNotNull { doc ->
                        WalletTransaction(
                            id = doc.id,
                            monto = doc.getDouble("monto") ?: 0.0,
                            tipo = doc.getString("tipo") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            fecha = doc.getTimestamp("fecha")
                        )
                    } ?: emptyList()

                    isLoading = false
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
                        text = "NEXUS",
                        fontSize = 20.sp,
                        fontFamily = Poppins,
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                item {
                    Text(
                        text = "Mi Cartera",
                        fontSize = 24.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Card de Saldo Cartera
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Saldo en Cartera",
                                        fontSize = 14.sp,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$ ${"%,.2f".format(saldoCartera)}",
                                        fontSize = 36.sp,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Ganancias de inversiones acumuladas",
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Botón Transferir a Disponible
                item {
                    Button(
                        onClick = { showTransferDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = saldoCartera > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Transferir a Disponible",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }

                // Gráfica
                item {
                    Text(
                        text = "Historial de ingresos",
                        fontSize = 20.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Últimos 7 días",
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Gráfica de líneas
                            WalletChart(
                                transactions = walletTransactions,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }

                // Stats rápidas
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Total ingresado",
                            value = "$ ${"%,.0f".format(walletTransactions.filter { it.tipo == "ingreso" }.sumOf { it.monto })}",
                            icon = Icons.Default.TrendingUp,
                            color = Color(0xFF4CAF50)
                        )

                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Transferido",
                            value = "$ ${"%,.0f".format(walletTransactions.filter { it.tipo == "transferencia" }.sumOf { it.monto })}",
                            icon = Icons.Default.SwapHoriz,
                            color = Color(0xFF2196F3)
                        )
                    }
                }

                // Título movimientos
                item {
                    Text(
                        text = "Movimientos recientes",
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
                } else if (walletTransactions.isEmpty()) {
                    item {
                        Text(
                            text = "No hay movimientos en tu cartera",
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
                    items(walletTransactions) { transaction ->
                        WalletTransactionItem(transaction = transaction)
                    }
                }
            }
        }

        // Diálogo de transferencia
        if (showTransferDialog) {
            TransferDialog(
                amount = transferAmount,
                onAmountChange = { transferAmount = it },
                maxAmount = saldoCartera.toFloat(),
                isLoading = isTransferring,
                onDismiss = {
                    showTransferDialog = false
                    transferAmount = 0f
                },
                onConfirm = {
                    isTransferring = true
                    val user = auth.currentUser

                    if (user != null) {
                        val userRef = db.collection("usuarios").document(user.uid)

                        userRef.get().addOnSuccessListener { document ->
                            if (document.exists()) {
                                val carteraActual = document.getDouble("saldoCartera") ?: 0.0
                                val disponibleActual = document.getDouble("saldo") ?: 0.0

                                if (transferAmount > carteraActual) {
                                    Toast.makeText(context, "Monto insuficiente", Toast.LENGTH_SHORT).show()
                                    isTransferring = false
                                    return@addOnSuccessListener
                                }

                                val nuevaCartera = carteraActual - transferAmount
                                val nuevoDisponible = disponibleActual + transferAmount

                                userRef.update(
                                    mapOf(
                                        "saldoCartera" to nuevaCartera,
                                        "saldo" to nuevoDisponible
                                    )
                                ).addOnSuccessListener {
                                    // Registrar transacción
                                    val transaccion = hashMapOf(
                                        "usuarioId" to user.uid,
                                        "monto" to transferAmount.toDouble(),
                                        "tipo" to "transferencia",
                                        "descripcion" to "Transferencia a saldo disponible",
                                        "fecha" to Timestamp.now()
                                    )

                                    db.collection("carteraTransacciones").add(transaccion)

                                    isTransferring = false
                                    showTransferDialog = false
                                    transferAmount = 0f

                                    Toast.makeText(
                                        context,
                                        "Transferencia exitosa: $ ${"%,.0f".format(transferAmount)}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.addOnFailureListener {
                                    isTransferring = false
                                    Toast.makeText(context, "Error en la transferencia", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun WalletChart(
    transactions: List<WalletTransaction>,
    modifier: Modifier = Modifier
) {
    val ingresos = transactions.filter { it.tipo == "ingreso" }

    if (ingresos.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay datos para mostrar",
                fontFamily = Poppins,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        return
    }

    // Agrupar por día (últimos 7 días)
    val calendar = Calendar.getInstance()
    val today = calendar.time
    val dataPoints = mutableListOf<Pair<String, Double>>()

    for (i in 6 downTo 0) {
        calendar.time = today
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        val dayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time

        val dayEnd = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.time

        val dayTotal = ingresos
            .filter {
                val date = it.fecha?.toDate()
                date != null && date.after(dayStart) && date.before(dayEnd)
            }
            .sumOf { it.monto }

        val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(dayStart)
        dataPoints.add(dayLabel to dayTotal)
    }

    val maxValue = dataPoints.maxOfOrNull { it.second } ?: 1.0

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (dataPoints.size - 1)

        // Dibujar líneas
        val path = Path()
        dataPoints.forEachIndexed { index, (_, value) ->
            val x = index * spacing
            val y = height - (value / maxValue * height * 0.8f).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Dibujar puntos
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 6f,
                center = Offset(x, y)
            )
        }

        drawPath(
            path = path,
            color = Color(0xFF4CAF50),
            style = Stroke(width = 4f)
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontFamily = Poppins,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun WalletTransactionItem(transaction: WalletTransaction) {
    val isIncome = transaction.tipo == "ingreso"
    val icon = if (isIncome) Icons.Default.Add else Icons.Default.Remove
    val iconColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFF2196F3)
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
                    text = transaction.descripcion,
                    fontSize = 14.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = formatTransactionDate(transaction.fecha),
                    fontSize = 12.sp,
                    fontFamily = Poppins,
                    color = Color.Gray
                )
            }
        }

        Text(
            text = "$amountPrefix$ ${"%,.0f".format(transaction.monto)}",
            fontSize = 16.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = iconColor
        )
    }
}

@Composable
fun TransferDialog(
    amount: Float,
    onAmountChange: (Float) -> Unit,
    maxAmount: Float,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
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
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Transferir a Disponible",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "En cartera: $ ${"%,.0f".format(maxAmount)}",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = if (amount > 0) "%,.0f".format(amount) else "",
                    onValueChange = { input ->
                        val cleanInput = input.replace(",", "").replace(".", "")
                        val newAmount = cleanInput.toFloatOrNull()
                        if (newAmount != null && newAmount <= maxAmount) {
                            onAmountChange(newAmount)
                        } else if (cleanInput.isEmpty()) {
                            onAmountChange(0f)
                        }
                    },
                    label = {
                        Text(
                            text = "Monto a transferir",
                            fontFamily = Poppins,
                            fontSize = 14.sp
                        )
                    },
                    prefix = {
                        Text(
                            text = "$",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold
                        )
                    },
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
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    enabled = !isLoading && amount > 0 && amount <= maxAmount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Transferir",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

fun formatTransactionDate(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toDate()
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date)
}