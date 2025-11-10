package com.Marcos.nexus

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusHomeScreen(
    onNavigateToSendMoney: () -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSavings: () -> Unit = {}
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val context = LocalContext.current

    var saldo by remember { mutableStateOf<Double?>(null) }
    var saldoGuardado by remember { mutableStateOf(0.0) }
    var userName by remember { mutableStateOf("Usuario") }
    var showRechargeDialog by remember { mutableStateOf(false) }
    var rechargeAmount by remember { mutableStateOf(5000f) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Calcular saldo total y disponible
    val saldoTotal = (saldo ?: 0.0) + saldoGuardado
    val saldoDisponible = saldo ?: 0.0

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (document != null && document.exists()) {
                        saldo = document.getDouble("saldo") ?: 0.0
                        saldoGuardado = document.getDouble("saldoGuardado") ?: 0.0
                        userName = document.getString("nombre") ?: "Usuario"
                    } else {
                        saldo = 0.0
                        saldoGuardado = 0.0
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
                    Column {
                        Text(
                            text = "Hola, $userName",
                            fontSize = 16.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "NEXUS",
                            fontSize = 24.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = {onNavigateToNotifications() }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.Black
                            )
                        }
                        IconButton(onClick = {onNavigateToSettings() }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Configuración",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Card de Saldo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
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
                            verticalAlignment = Alignment.Top
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
                                    text = saldo?.let { "$ ${"%,.2f".format(it)}" } ?: "Cargando...",
                                    fontSize = 36.sp,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Total en pequeño
                                Text(
                                    text = "Total: $ ${"%,.2f".format(saldoTotal)}",
                                    fontSize = 12.sp,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }

                            Image(
                                painter = painterResource(id = R.drawable.targeta12),
                                contentDescription = "Logo",
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "NEXUS CARD",
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "**** 4562",
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Título Acciones Rápidas
                Text(
                    text = "Acciones rápidas",
                    fontSize = 18.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Acciones en Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.recarga,
                        title = "Recargar",
                        onClick = { showRechargeDialog = true }
                    )

                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.enviar,
                        title = "Enviar",
                        onClick = onNavigateToSendMoney
                    )

                    ActionCard(
                        modifier = Modifier.weight(1f),
                        icon = R.drawable.guardar,
                        title = "Guardar",
                        onClick = onNavigateToSavings
                    )
                }

                // Card de Productos
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProducts() },
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
                                text = "Mis Productos",
                                fontSize = 18.sp,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tarjetas, inversiones y más",
                                fontSize = 12.sp,
                                fontFamily = Poppins,
                                color = Color.Gray
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.enviar),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Info adicional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "NEXUS • Banca digital",
                        fontSize = 12.sp,
                        fontFamily = Poppins,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Diálogo de recarga
        if (showRechargeDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showRechargeDialog = false }
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
                            text = "Recargar saldo",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Recarga desde $5.000 hasta $100.000",
                            fontFamily = Poppins,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = if (rechargeAmount > 0) "%,.0f".format(rechargeAmount) else "",
                            onValueChange = { input ->
                                val cleanInput = input.replace(",", "").replace(".", "")
                                val amount = cleanInput.toFloatOrNull()
                                if (amount != null && amount in 5000f..100000f) {
                                    rechargeAmount = amount
                                }
                            },
                            label = {
                                Text(
                                    text = "Monto a recargar",
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Slider(
                            value = rechargeAmount,
                            onValueChange = { rechargeAmount = it },
                            valueRange = 5000f..100000f,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Black,
                                activeTrackColor = Color.Black,
                                inactiveTrackColor = Color.Black.copy(alpha = 0.3f)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$5.000",
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "$100.000",
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Button(
                            onClick = {
                                errorMessage = null
                                isLoading = true

                                val user = auth.currentUser
                                if (user != null) {
                                    val userRef = db.collection("usuarios").document(user.uid)

                                    userRef.get()
                                        .addOnSuccessListener { document ->
                                            if (document.exists()) {
                                                val saldoActual = document.getDouble("saldo") ?: 0.0
                                                val nuevoSaldo = saldoActual + rechargeAmount

                                                userRef.update("saldo", nuevoSaldo)
                                                    .addOnSuccessListener {
                                                        val transaccionRecarga = hashMapOf(
                                                            "remitenteId" to user.uid,
                                                            "destinatarioId" to user.uid,
                                                            "monto" to rechargeAmount.toDouble(),
                                                            "tipo" to "recarga",
                                                            "origen" to "cajero",
                                                            "fecha" to com.google.firebase.Timestamp.now(),
                                                            "remitenteNombre" to (document.getString("nombre") ?: "Usuario"),
                                                            "destinatarioNombre" to (document.getString("nombre") ?: "Usuario"),
                                                            "mensaje" to "Recarga de saldo"
                                                        )

                                                        db.collection("transacciones").add(transaccionRecarga)

                                                        saldo = nuevoSaldo
                                                        isLoading = false
                                                        showRechargeDialog = false

                                                        Toast.makeText(
                                                            context,
                                                            "Recarga exitosa: ${"%,.0f".format(rechargeAmount)}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        errorMessage = "Error: ${e.message}"
                                                        isLoading = false
                                                    }
                                            }
                                        }
                                }
                            },
                            enabled = !isLoading,
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
                                    text = "Recargar",
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
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.05f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

