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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
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
    var showGoalDialog by remember { mutableStateOf(false) }
    var saveAmount by remember { mutableStateOf(5000f) }
    var withdrawAmount by remember { mutableStateOf(5000f) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var savings by remember { mutableStateOf<List<Saving>>(emptyList()) }
    var isLoadingTransactions by remember { mutableStateOf(true) }
    var currentGoal by remember { mutableStateOf<SavingsGoal?>(null) }

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

            // Obtener meta activa
            db.collection("metasAhorro")
                .whereEqualTo("usuarioId", user.uid)
                .whereEqualTo("activa", true)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    currentGoal = snapshot?.documents?.firstOrNull()?.let { doc ->
                        val montoActual = saldoGuardado
                        val montoObjetivo = doc.getDouble("montoObjetivo") ?: 0.0
                        val completada = montoActual >= montoObjetivo

                        // Si se completó, marcarla como completada
                        if (completada && doc.getBoolean("completada") != true) {
                            db.collection("metasAhorro").document(doc.id)
                                .update(
                                    mapOf(
                                        "completada" to true,
                                        "fechaCompletada" to Timestamp.now()
                                    )
                                )
                        }

                        SavingsGoal(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            montoObjetivo = montoObjetivo,
                            montoActual = montoActual,
                            activa = doc.getBoolean("activa") ?: true,
                            completada = completada,
                            fechaCreacion = doc.getTimestamp("fechaCreacion"),
                            fechaCompletada = doc.getTimestamp("fechaCompletada")
                        )
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

                // Card de Meta
                item {
                    if (currentGoal == null) {
                        // Crear meta
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGoalDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Trazar meta de ahorro",
                                        fontSize = 16.sp,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Define tu objetivo financiero",
                                        fontSize = 12.sp,
                                        fontFamily = Poppins,
                                        color = Color.Gray
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        // Mostrar meta actual
                        GoalProgressCard(
                            goal = currentGoal!!,
                            saldoGuardado = saldoGuardado,
                            onDeleteGoal = {
                                val user = auth.currentUser
                                if (user != null) {
                                    db.collection("metasAhorro").document(currentGoal!!.id)
                                        .update("activa", false)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Meta eliminada", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        )
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
                onDismiss = {
                    showSaveDialog = false
                    errorMessage = null
                },
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
                                        errorMessage = null

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
                onDismiss = {
                    showWithdrawDialog = false
                    errorMessage = null
                },
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
                                        errorMessage = null

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

        // Diálogo de crear meta
        if (showGoalDialog) {
            CreateGoalDialog(
                onDismiss = { showGoalDialog = false },
                onConfirm = { nombre, monto ->
                    val user = auth.currentUser
                    if (user != null) {
                        val meta = hashMapOf(
                            "usuarioId" to user.uid,
                            "nombre" to nombre,
                            "montoObjetivo" to monto,
                            "montoActual" to 0.0,
                            "activa" to true,
                            "completada" to false,
                            "fechaCreacion" to Timestamp.now()
                        )

                        db.collection("metasAhorro").add(meta)
                            .addOnSuccessListener {
                                showGoalDialog = false
                                Toast.makeText(context, "Meta creada exitosamente", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al crear la meta", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            )
        }
    }
}

@Composable
fun GoalProgressCard(
    goal: SavingsGoal,
    saldoGuardado: Double,
    onDeleteGoal: () -> Unit
) {
    val progreso = ((saldoGuardado / goal.montoObjetivo) * 100).coerceIn(0.0, 100.0)
    val porcentajeTexto = "%.0f".format(progreso)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.completada) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (goal.completada) "¡Meta Completada! 🎉" else "Meta de Ahorro",
                        fontSize = if (goal.completada) 18.sp else 14.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = if (goal.completada) Color(0xFF4CAF50) else Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = goal.nombre,
                        fontSize = 16.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                if (!goal.completada) {
                    Text(
                        text = "$porcentajeTexto%",
                        fontSize = 24.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            if (!goal.completada) {
                Spacer(modifier = Modifier.height(16.dp))

                // Barra de progreso
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (progreso / 100).toFloat())
                            .fillMaxHeight()
                            .background(
                                color = Color(0xFF4CAF50),
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Guardado",
                            fontSize = 11.sp,
                            fontFamily = Poppins,
                            color = Color.Gray
                        )
                        Text(
                            text = "$ ${"%,.0f".format(saldoGuardado)}",
                            fontSize = 14.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Objetivo",
                            fontSize = 11.sp,
                            fontFamily = Poppins,
                            color = Color.Gray
                        )
                        Text(
                            text = "$ ${"%,.0f".format(goal.montoObjetivo)}",
                            fontSize = 14.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }

                val faltante = (goal.montoObjetivo - saldoGuardado).coerceAtLeast(0.0)
                if (faltante > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Faltan $ ${"%,.0f".format(faltante)} para tu meta",
                        fontSize = 11.sp,
                        fontFamily = Poppins,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$ ${"%,.0f".format(goal.montoObjetivo)}",
                    fontSize = 28.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onDeleteGoal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Eliminar meta",
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var goalName by remember { mutableStateOf("") }
    var goalAmount by remember { mutableStateOf("") }

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
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Crear Meta de Ahorro",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = goalName,
                    onValueChange = { goalName = it },
                    label = {
                        Text(
                            text = "Nombre de la meta",
                            fontFamily = Poppins,
                            fontSize = 14.sp
                        )
                    },
                    placeholder = {
                        Text(
                            text = "Ej: Vacaciones, Auto nuevo",
                            fontFamily = Poppins,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
                        focusedLabelColor = Color(0xFF4CAF50),
                        cursorColor = Color(0xFF4CAF50)
                    ),
                    textStyle = TextStyle(
                        fontFamily = Poppins,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = goalAmount,
                    onValueChange = { input ->
                        val cleanInput = input.replace(",", "").replace(".", "")
                        if (cleanInput.isEmpty() || cleanInput.all { it.isDigit() }) {
                            goalAmount = cleanInput
                        }
                    },
                    label = {
                        Text(
                            text = "Monto objetivo",
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
                    onClick = {
                        val amount = goalAmount.toDoubleOrNull()
                        if (goalName.isNotBlank() && amount != null && amount > 0) {
                            onConfirm(goalName, amount)
                        }
                    },
                    enabled = goalName.isNotBlank() && goalAmount.toDoubleOrNull() != null && goalAmount.toDouble() > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Crear Meta",
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
                        } else if (cleanInput.isEmpty()) {
                            onAmountChange(0f)
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
                        } else if (cleanInput.isEmpty()) {
                            onAmountChange(0f)
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
                    text = saving.fecha?.toDate()?.let {
                        java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(it)
                    } ?: "",
                    fontSize = 12.sp,
                    fontFamily = Poppins,
                    color = Color.Gray
                )
            }
        }

        Text(
            text = "$amountPrefix$ ${"%,.0f".format(saving.monto)}",
            fontSize = 16.sp,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}