package com.Marcos.nexus

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.widget.Toast
import com.google.firebase.Timestamp

data class Saving(
    val id: String = "",
    val nombre: String = "",
    val monto: Double = 0.0,
    val fecha: Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val context = LocalContext.current

    var saldoDisponible by remember { mutableStateOf<Double?>(null) }
    var saldoGuardado by remember { mutableStateOf(0.0) }
    var userName by remember { mutableStateOf("Usuario") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var saveAmount by remember { mutableStateOf(5000f) }
    var withdrawAmount by remember { mutableStateOf(5000f) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var savings by remember { mutableStateOf<List<Saving>>(emptyList()) }
    var isLoadingTransactions by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            // Obtener datos del usuario
            db.collection("usuarios").document(user.uid)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (document != null && document.exists()) {
                        saldoDisponible = document.getDouble("saldo") ?: 0.0
                        saldoGuardado = document.getDouble("saldoGuardado") ?: 0.0
                        userName = document.getString("nombre") ?: "Usuario"
                    }
                }

            // Obtener historial de ahorros
            db.collection("ahorros")
                .whereEqualTo("usuarioId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoadingTransactions = false
                        return@addSnapshotListener
                    }

                    savings = snapshot?.documents?.mapNotNull { doc ->
                        Saving(
                            id = doc.id,
                            nombre = doc.getString("tipo") ?: "Ahorro",
                            monto = doc.getDouble("monto") ?: 0.0,
                            fecha = doc.getTimestamp("fecha")
                        )
                    }?.sortedByDescending { it.fecha?.toDate() } ?: emptyList()

                    isLoadingTransactions = false
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
                // Título
                item {
                    Text(
                        text = "Mis Ahorros",
                        fontSize = 24.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Card de Saldo Guardado
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black
                        ),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Dinero guardado",
                                        fontSize = 14.sp,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$ ${"%,.2f".format(saldoGuardado)}",
                                        fontSize = 36.sp,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Ahorros",
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Saldo disponible: $ ${"%,.2f".format(saldoDisponible ?: 0.0)}",
                                    fontSize = 12.sp,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Botones de acción
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Guardar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardar",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }

                        Button(
                            onClick = { showWithdrawDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF757575)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = saldoGuardado > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = "Retirar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Retirar",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Título historial
                item {
                    Text(
                        text = "Historial de movimientos",
                        fontSize = 20.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Lista de ahorros
                if (isLoadingTransactions) {
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
                } else if (savings.isEmpty()) {
                    item {
                        Text(
                            text = "Aún no tienes movimientos",
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
                    items(savings) { saving ->
                        SavingItem(saving = saving)
                    }
                }
            }
        }

        // Diálogo de guardar
        if (showSaveDialog) {
            SaveMoneyDialog(
                amount = saveAmount,
                onAmountChange = { saveAmount = it },
                isLoading = isLoading,
                errorMessage = errorMessage,
                maxAmount = saldoDisponible?.toFloat() ?: 0f,
                onDismiss = { showSaveDialog = false },
                onConfirm = {
                    errorMessage = null
                    isLoading = true

                    val user = auth.currentUser
                    if (user != null) {
                        val userRef = db.collection("usuarios").document(user.uid)

                        userRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val saldoActual = document.getDouble("saldo") ?: 0.0
                                    val guardadoActual = document.getDouble("saldoGuardado") ?: 0.0

                                    if (saveAmount > saldoActual) {
                                        errorMessage = "Saldo insuficiente"
                                        isLoading = false
                                        return@addOnSuccessListener
                                    }

                                    val nuevoSaldo = saldoActual - saveAmount
                                    val nuevoGuardado = guardadoActual + saveAmount

                                    userRef.update(
                                        mapOf(
                                            "saldo" to nuevoSaldo,
                                            "saldoGuardado" to nuevoGuardado
                                        )
                                    ).addOnSuccessListener {
                                        val ahorro = hashMapOf(
                                            "usuarioId" to user.uid,
                                            "monto" to saveAmount.toDouble(),
                                            "tipo" to "guardado",
                                            "fecha" to Timestamp.now()
                                        )

                                        db.collection("ahorros").add(ahorro)

                                        isLoading = false
                                        showSaveDialog = false

                                        Toast.makeText(
                                            context,
                                            "Guardado exitoso: $ ${"%,.0f".format(saveAmount)}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.addOnFailureListener { e ->
                                        errorMessage = "Error: ${e.message}"
                                        isLoading = false
                                    }
                                }
                            }
                    }
                }
            )
        }

        // Diálogo de retirar
        if (showWithdrawDialog) {
            WithdrawMoneyDialog(
                amount = withdrawAmount,
                onAmountChange = { withdrawAmount = it },
                isLoading = isLoading,
                errorMessage = errorMessage,
                maxAmount = saldoGuardado.toFloat(),
                onDismiss = { showWithdrawDialog = false },
                onConfirm = {
                    errorMessage = null
                    isLoading = true

                    val user = auth.currentUser
                    if (user != null) {
                        val userRef = db.collection("usuarios").document(user.uid)

                        userRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val saldoActual = document.getDouble("saldo") ?: 0.0
                                    val guardadoActual = document.getDouble("saldoGuardado") ?: 0.0

                                    if (withdrawAmount > guardadoActual) {
                                        errorMessage = "Monto insuficiente"
                                        isLoading = false
                                        return@addOnSuccessListener
                                    }

                                    val nuevoSaldo = saldoActual + withdrawAmount
                                    val nuevoGuardado = guardadoActual - withdrawAmount

                                    userRef.update(
                                        mapOf(
                                            "saldo" to nuevoSaldo,
                                            "saldoGuardado" to nuevoGuardado
                                        )
                                    ).addOnSuccessListener {
                                        val ahorro = hashMapOf(
                                            "usuarioId" to user.uid,
                                            "monto" to withdrawAmount.toDouble(),
                                            "tipo" to "retirado",
                                            "fecha" to Timestamp.now()
                                        )

                                        db.collection("ahorros").add(ahorro)

                                        isLoading = false
                                        showWithdrawDialog = false

                                        Toast.makeText(
                                            context,
                                            "Retiro exitoso: $ ${"%,.0f".format(withdrawAmount)}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }.addOnFailureListener { e ->
                                        errorMessage = "Error: ${e.message}"
                                        isLoading = false
                                    }
                                }
                            }
                    }
                }
            )
        }
    } // <- Cierra Box
} // <- Cierra SavingsScreen

@Composable
fun SaveMoneyDialog(
    amount: Float,
    onAmountChange: (Float) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    maxAmount: Float,
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
                Text(
                    text = "Guardar dinero",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Disponible: $ ${"%,.0f".format(maxAmount)}",
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
                        }
                    },
                    label = {
                        Text(
                            text = "Monto a guardar",
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

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = onConfirm,
                    enabled = !isLoading && amount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
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
                            text = "Guardar",
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

@Composable
fun WithdrawMoneyDialog(
    amount: Float,
    onAmountChange: (Float) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    maxAmount: Float,
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
                Text(
                    text = "Retirar dinero",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Guardado: $ ${"%,.0f".format(maxAmount)}",
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
                        }
                    },
                    label = {
                        Text(
                            text = "Monto a retirar",
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
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
                        focusedLabelColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    textStyle = TextStyle(
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontFamily = Poppins,
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = onConfirm,
                    enabled = !isLoading && amount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
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
                            text = "Retirar",
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

@Composable
fun SavingItem(saving: Saving) {
    val isDeposit = saving.nombre == "guardado"
    val iconColor = if (isDeposit) Color(0xFF4CAF50) else Color(0xFFFF9800)
    val amountColor = if (isDeposit) Color(0xFF4CAF50) else Color(0xFFFF9800)
    val amountPrefix = if (isDeposit) "+" else "-"

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
                    imageVector = if (isDeposit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = if (isDeposit) "Dinero guardado" else "Dinero retirado",
                    fontSize = 14.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = saving.fecha?.toDate()?.toString() ?: "",
                    fontSize = 12.sp,
                    fontFamily = Poppins,
                    color = Color.Gray
                )
            }
        }

        Text(
            text = "$amountPrefix${"%,.0f".format(saving.monto)}",
            fontSize = 16.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}


