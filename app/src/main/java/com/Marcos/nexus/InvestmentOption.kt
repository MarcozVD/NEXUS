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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

data class Investment(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val monto: Double = 0.0,
    val tasaInteres: Double = 0.0,
    val plazo: Int = 0,
    val fechaInicio: Timestamp? = null,
    val fechaFin: Timestamp? = null,
    val estado: String = "activa"
)

data class InvestmentOption(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val tasaInteres: Double,
    val plazoMeses: Int,
    val montoMinimo: Double,
    val riesgo: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val context = LocalContext.current

    var saldoDisponible by remember { mutableStateOf<Double?>(null) }
    var myInvestments by remember { mutableStateOf<List<Investment>>(emptyList()) }
    var isLoadingInvestments by remember { mutableStateOf(true) }
    var showInvestDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<InvestmentOption?>(null) }

    val investmentOptions = listOf(
        InvestmentOption(
            id = "ahorro_programado",
            nombre = "Ahorro Programado",
            descripcion = "Inversión segura con retorno fijo mensual",
            tasaInteres = 5.0,
            plazoMeses = 6,
            montoMinimo = 50000.0,
            riesgo = "Bajo",
            icon = Icons.Default.Savings,
            color = Color(0xFF4CAF50)
        ),
        InvestmentOption(
            id = "cdt",
            nombre = "CDT Digital",
            descripcion = "Certificado de Depósito a Término con alta rentabilidad",
            tasaInteres = 8.5,
            plazoMeses = 12,
            montoMinimo = 100000.0,
            riesgo = "Bajo",
            icon = Icons.Default.Security,
            color = Color(0xFF2196F3)
        ),
        InvestmentOption(
            id = "fondo_moderado",
            nombre = "Fondo Moderado",
            descripcion = "Diversificación en acciones y bonos con riesgo controlado",
            tasaInteres = 12.0,
            plazoMeses = 24,
            montoMinimo = 200000.0,
            riesgo = "Medio",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFFFF9800)
        ),
        InvestmentOption(
            id = "acciones",
            nombre = "Portafolio de Acciones",
            descripcion = "Inversión en bolsa con alto potencial de crecimiento",
            tasaInteres = 18.0,
            plazoMeses = 36,
            montoMinimo = 500000.0,
            riesgo = "Alto",
            icon = Icons.Default.ShowChart,
            color = Color(0xFFF44336)
        ),
        InvestmentOption(
            id = "cripto",
            nombre = "Criptomonedas",
            descripcion = "Portafolio diversificado en activos digitales",
            tasaInteres = 25.0,
            plazoMeses = 12,
            montoMinimo = 100000.0,
            riesgo = "Muy Alto",
            icon = Icons.Default.AttachMoney,
            color = Color(0xFF9C27B0)
        )
    )

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            // 🔥 VERIFICAR INVERSIONES COMPLETADAS
            verificarInversionesCompletadas(user.uid, db, context)

            db.collection("usuarios").document(user.uid)
                .addSnapshotListener { document, error ->
                    if (error != null) return@addSnapshotListener
                    if (document != null && document.exists()) {
                        saldoDisponible = document.getDouble("saldo") ?: 0.0
                    }
                }

            db.collection("inversiones")
                .whereEqualTo("usuarioId", user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoadingInvestments = false
                        return@addSnapshotListener
                    }

                    myInvestments = snapshot?.documents?.mapNotNull { doc ->
                        Investment(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            tipo = doc.getString("tipo") ?: "",
                            monto = doc.getDouble("monto") ?: 0.0,
                            tasaInteres = doc.getDouble("tasaInteres") ?: 0.0,
                            plazo = doc.getLong("plazo")?.toInt() ?: 0,
                            fechaInicio = doc.getTimestamp("fechaInicio"),
                            fechaFin = doc.getTimestamp("fechaFin"),
                            estado = doc.getString("estado") ?: "activa"
                        )
                    }?.sortedByDescending { it.fechaInicio?.toDate() } ?: emptyList()

                    isLoadingInvestments = false
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
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
                item {
                    Text(
                        text = "Inversiones",
                        fontSize = 24.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

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
                                        text = "Saldo disponible",
                                        fontSize = 14.sp,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$ ${"%,.2f".format(saldoDisponible ?: 0.0)}",
                                        fontSize = 28.sp,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            if (myInvestments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(12.dp))

                                val totalInvertido = myInvestments
                                    .filter { it.estado == "activa" }
                                    .sumOf { it.monto }

                                Text(
                                    text = "Total invertido: $ ${"%,.0f".format(totalInvertido)}",
                                    fontSize = 12.sp,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Opciones de inversión",
                        fontSize = 20.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(investmentOptions) { option ->
                    InvestmentOptionCard(
                        option = option,
                        onClick = {
                            selectedOption = option
                            showInvestDialog = true
                        }
                    )
                }

                if (myInvestments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Mis inversiones",
                            fontSize = 20.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(myInvestments) { investment ->
                        MyInvestmentCard(investment = investment)
                    }
                }
            }
        }

        if (showInvestDialog && selectedOption != null) {
            InvestDialog(
                option = selectedOption!!,
                saldoDisponible = saldoDisponible ?: 0.0,
                onDismiss = { showInvestDialog = false },
                onConfirm = { amount ->
                    val user = auth.currentUser
                    if (user != null) {
                        val userRef = db.collection("usuarios").document(user.uid)

                        userRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val saldoActual = document.getDouble("saldo") ?: 0.0

                                    if (amount > saldoActual) {
                                        Toast.makeText(context, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
                                        return@addOnSuccessListener
                                    }

                                    val nuevoSaldo = saldoActual - amount

                                    userRef.update("saldo", nuevoSaldo)
                                        .addOnSuccessListener {
                                            val calendar = java.util.Calendar.getInstance()
                                            calendar.add(java.util.Calendar.MONTH, selectedOption!!.plazoMeses)

                                            val inversion = hashMapOf(
                                                "usuarioId" to user.uid,
                                                "nombre" to selectedOption!!.nombre,
                                                "tipo" to selectedOption!!.id,
                                                "monto" to amount,
                                                "tasaInteres" to selectedOption!!.tasaInteres,
                                                "plazo" to selectedOption!!.plazoMeses,
                                                "fechaInicio" to Timestamp.now(),
                                                "fechaFin" to Timestamp(calendar.time),
                                                "estado" to "activa"
                                            )

                                            db.collection("inversiones").add(inversion)
                                                .addOnSuccessListener {
                                                    // 🔥 CREAR NOTIFICACIÓN DE INVERSIÓN REALIZADA
                                                    val notificacion = hashMapOf(
                                                        "userId" to user.uid,
                                                        "tipo" to "inversion",
                                                        "titulo" to "Inversión realizada",
                                                        "mensaje" to "Has invertido $${"%,.0f".format(amount)} en ${selectedOption!!.nombre} con una tasa del ${selectedOption!!.tasaInteres}% anual por ${selectedOption!!.plazoMeses} meses.",
                                                        "fecha" to Timestamp.now(),
                                                        "leida" to false
                                                    )

                                                    db.collection("notificaciones").add(notificacion)

                                                    showInvestDialog = false
                                                    Toast.makeText(
                                                        context,
                                                        "Inversión realizada exitosamente",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Error al procesar inversión", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Error al procesar inversión", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                    }
                }
            )
        }
    }
}

// 🔥 FUNCIÓN PARA VERIFICAR INVERSIONES COMPLETADAS
fun verificarInversionesCompletadas(
    userId: String,
    db: com.google.firebase.firestore.FirebaseFirestore,
    context: android.content.Context
) {
    val ahora = Timestamp.now()

    db.collection("inversiones")
        .whereEqualTo("usuarioId", userId)
        .whereEqualTo("estado", "activa")
        .get()
        .addOnSuccessListener { snapshot ->
            snapshot.documents.forEach { doc ->
                val fechaFin = doc.getTimestamp("fechaFin")

                if (fechaFin != null && fechaFin.toDate() <= ahora.toDate()) {
                    // La inversión ha terminado
                    val monto = doc.getDouble("monto") ?: 0.0
                    val tasaInteres = doc.getDouble("tasaInteres") ?: 0.0
                    val plazo = doc.getLong("plazo")?.toInt() ?: 0
                    val nombre = doc.getString("nombre") ?: "Inversión"

                    // Calcular ganancia
                    val gananciaAnual = monto * (tasaInteres / 100)
                    val gananciaMensual = gananciaAnual / 12
                    val gananciaTotal = gananciaMensual * plazo
                    val montoFinal = monto + gananciaTotal

                    // Actualizar saldo del usuario
                    val userRef = db.collection("usuarios").document(userId)
                    userRef.get().addOnSuccessListener { userDoc ->
                        if (userDoc.exists()) {
                            val saldoActual = userDoc.getDouble("saldo") ?: 0.0
                            val nuevoSaldo = saldoActual + montoFinal

                            userRef.update(
                                mapOf(
                                    "saldo" to saldoActual,  // Saldo disponible se mantiene
                                    "saldoCartera" to (userDoc.getDouble("saldoCartera") ?: 0.0) + montoFinal  // Agregar a cartera
                                )
                            ).addOnSuccessListener {
                                    // Marcar inversión como completada
                                    doc.reference.update("estado", "completada")


                                    // 🔥 CREAR NOTIFICACIÓN DE INVERSIÓN COMPLETADA
                                    val notificacion = hashMapOf(
                                        "userId" to userId,
                                        "tipo" to "ganancia",
                                        "titulo" to "¡Inversión completada!",
                                        "mensaje" to "Tu inversión en $nombre ha finalizado. Ganaste $${"%,.0f".format(gananciaTotal)}. Total recibido: $${"%,.0f".format(montoFinal)}",
                                        "fecha" to Timestamp.now(),
                                        "leida" to false
                                    )

                                    db.collection("notificaciones").add(notificacion)


                                    // Registrar transacción
                                    val transaccion = hashMapOf(
                                        "remitenteId" to userId,
                                        "destinatarioId" to userId,
                                        "monto" to montoFinal,
                                        "tipo" to "inversion_completada",
                                        "origen" to "inversiones",
                                        "fecha" to Timestamp.now(),
                                        "remitenteNombre" to (userDoc.getString("nombre") ?: "Usuario"),
                                        "destinatarioNombre" to (userDoc.getString("nombre") ?: "Usuario"),
                                        "mensaje" to "Inversión completada: $nombre"
                                    )

                                    db.collection("transacciones").add(transaccion)
                                // 🔥 AGREGAR TRANSACCIÓN A CARTERA
                                    val carteraTransaccion = hashMapOf(
                                        "usuarioId" to userId,
                                        "monto" to montoFinal,
                                        "tipo" to "ingreso",
                                        "descripcion" to "Ganancia de inversión: $nombre",
                                        "fecha" to Timestamp.now()
                                    )

                                    db.collection("carteraTransacciones").add(carteraTransaccion)

                                    Toast.makeText(
                                        context,
                                        "Inversión completada: +$${"%,.0f".format(gananciaTotal)}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }
            }
        }
}

@Composable
fun InvestmentOptionCard(
    option: InvestmentOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = option.color.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = option.color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.nombre,
                        fontSize = 16.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.descripcion,
                        fontSize = 12.sp,
                        fontFamily = Poppins,
                        color = Color.Gray,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = option.color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${option.tasaInteres}% anual",
                                fontSize = 11.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                color = option.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = Color.Black.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${option.plazoMeses} meses",
                                fontSize = 11.sp,
                                fontFamily = Poppins,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun MyInvestmentCard(investment: Investment) {
    val isCompleted = investment.estado == "completada"

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = investment.nombre,
                    fontSize = 16.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Surface(
                    color = if (isCompleted) Color.Gray.copy(alpha = 0.1f) else Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isCompleted) "Completada" else "Activa",
                        fontSize = 11.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color.Gray else Color(0xFF4CAF50),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Monto invertido",
                        fontSize = 12.sp,
                        fontFamily = Poppins,
                        color = Color.Gray
                    )
                    Text(
                        text = "$ ${"%,.0f".format(investment.monto)}",
                        fontSize = 18.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tasa de interés",
                        fontSize = 12.sp,
                        fontFamily = Poppins,
                        color = Color.Gray
                    )
                    Text(
                        text = "${investment.tasaInteres}%",
                        fontSize = 18.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Plazo: ${investment.plazo} meses",
                fontSize = 12.sp,
                fontFamily = Poppins,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun InvestDialog(
    option: InvestmentOption,
    saldoDisponible: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(option.montoMinimo.toFloat()) }
    var isLoading by remember { mutableStateOf(false) }

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
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = option.color,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = option.nombre,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = option.descripcion,
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoChip(label = "Tasa", value = "${option.tasaInteres}%", color = option.color)
                    InfoChip(label = "Plazo", value = "${option.plazoMeses}m", color = Color.Black)
                    InfoChip(label = "Riesgo", value = option.riesgo, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Disponible: $ ${"%,.0f".format(saldoDisponible)}",
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = if (amount > 0) "%,.0f".format(amount) else "",
                    onValueChange = { input ->
                        val cleanInput = input.replace(",", "").replace(".", "")
                        val newAmount = cleanInput.toFloatOrNull()
                        if (newAmount != null && newAmount >= option.montoMinimo && newAmount <= saldoDisponible) {
                            amount = newAmount
                        }
                    },
                    label = {
                        Text(
                            text = "Monto a invertir",
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
                        focusedBorderColor = option.color,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
                        focusedLabelColor = option.color,
                        cursorColor = option.color
                    ),
                    textStyle = TextStyle(
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mínimo: $ ${"%,.0f".format(option.montoMinimo)}",
                    fontFamily = Poppins,
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (amount >= option.montoMinimo && amount <= saldoDisponible) {
                            onConfirm(amount.toDouble())
                        }
                    },
                    enabled = !isLoading && amount >= option.montoMinimo && amount <= saldoDisponible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Invertir ahora",
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
fun InfoChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = Poppins,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}