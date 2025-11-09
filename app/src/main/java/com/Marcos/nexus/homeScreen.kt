package com.Marcos.nexus

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.layout.ContentScale

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusHomeScreen(onNavigateToSendMoney: () -> Unit = {},
                    onNavigateToProducts: () -> Unit = {}) {
    val auth = Firebase.auth
    val db = Firebase.firestore

    var saldo by remember { mutableStateOf<Double?>(null) }
    var showRechargeDialog by remember { mutableStateOf(false) }
    var rechargeAmount by remember { mutableStateOf(5000f) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        saldo = document.getDouble("saldo") ?: 0.0
                    } else {
                        saldo = 0.0
                    }
                }
                .addOnFailureListener {
                    saldo = 0.0
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFFFFEFE))
        ) {
            Column(
                modifier = Modifier
                    .background(color = Color(0xFFF4F4F4))
                    .fillMaxWidth()
                    .fillMaxHeight(0.27f)
            ) {
                // Encabezado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "NEXUS",
                        fontSize = 30.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(R.drawable.noti),
                        contentDescription = "Notificaciones",
                        tint = Color.Black,
                        modifier = Modifier.size(35.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(R.drawable.nut),
                        contentDescription = "Configuración",
                        tint = Color.Black,
                        modifier = Modifier.size(35.dp)
                    )
                }

                // Fila con saldo y tarjeta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Columna izquierda: saldo
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Saldo disponible",
                            fontSize = 14.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = saldo?.let { "$ ${"%,.2f".format(it)}" } ?: "Cargando...",
                            fontSize = 30.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    // Columna derecha: tarjeta
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.targeta12),
                            contentDescription = "Tarjeta de la app",
                            modifier = Modifier.size(110.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "NEXO CARD™",
                            fontSize = 14.sp,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.25f)
                        )
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.line),
                    contentDescription = "Línea",
                    modifier = Modifier
                        .size(350.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            // Card "Productos" con imagen de fondo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .clickable { onNavigateToProducts() },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(color = Color.Black)) {
                    // Imagen de fondo
                    Image(
                        painter = painterResource(id = R.drawable.productos),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Contenido de la Card
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Productos",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            fontSize = 22.sp,
                            modifier = Modifier
                                .align(Alignment.Top)
                                .padding(top = 20.dp)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.manos),
                            contentDescription = "Tarjeta de la app",
                            modifier = Modifier
                                .size(250.dp)
                                .align(Alignment.Bottom)
                                .padding(top = 22.dp)
                        )
                    }
                }
            }

            // Card de "Acciones"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Acciones",
                        fontSize = 18.sp,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Acción 1: Enviar
                        // Acción 1: Enviar
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onNavigateToSendMoney() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.enviar),
                                contentDescription = "Enviar",
                                tint = Color.Black,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enviar",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

// Acción 2: Recargar
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showRechargeDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.recarga),
                                contentDescription = "Recargar",
                                tint = Color.Black,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Recargar",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

                        // Acción 3: Guardar
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.guardar),
                                contentDescription = "Guardar",
                                tint = Color.Black,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Guardar",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }


                    }
                }
            }
        }

        // Diálogo de recarga con fondo oscurecido
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
                        ) { /* Evita cerrar al hacer clic en el card */ },
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
                            text = "Selecciona el monto a recargar",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Recarga tu cuenta desde \$5.000",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = Color.Black.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Slider
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de texto para ingresar monto exacto
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
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.5f),
                                focusedLabelColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            textStyle = TextStyle(
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mostrar rango válido
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Mín: $5.000",
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = Color.Black.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Máx: $100.000",
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = Color.Black.copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Mostrar mensaje de error si existe
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                fontFamily = Poppins,
                                fontSize = 12.sp,
                                color = Color.Red,
                                modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Botón Recargar
                        Button(
                            onClick = {
                                Log.d("Recarga", "Botón presionado")
                                errorMessage = null
                                isLoading = true

                                val user = auth.currentUser
                                Log.d("Recarga", "Usuario: ${user?.uid}")

                                if (user != null) {
                                    val userRef = db.collection("usuarios").document(user.uid)
                                    Log.d("Recarga", "Iniciando recarga de ${rechargeAmount}")

                                    // Obtener el saldo actual y sumarle la recarga
                                    userRef.get()
                                        .addOnSuccessListener { document ->
                                            Log.d("Recarga", "Documento obtenido: ${document.exists()}")
                                            if (document.exists()) {
                                                val saldoActual = document.getDouble("saldo") ?: 0.0
                                                val nuevoSaldo = saldoActual + rechargeAmount

                                                Log.d("Recarga", "Saldo actual: $saldoActual, Nuevo saldo: $nuevoSaldo")

                                                // Actualizar el saldo en Firestore
                                                // Actualizar el saldo en Firestore
                                                userRef.update("saldo", nuevoSaldo)
                                                    .addOnSuccessListener {
                                                        Log.d("Recarga", "Saldo actualizado exitosamente")

                                                        // Registrar la transacción de recarga
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
                                                            .addOnSuccessListener {
                                                                Log.d("Recarga", "Transacción registrada")
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Log.e("Recarga", "Error al registrar transacción: ${e.message}")
                                                            }

                                                        // Actualizar el saldo local
                                                        saldo = nuevoSaldo
                                                        isLoading = false
                                                        showRechargeDialog = false

                                                        // Mostrar Toast de éxito
                                                        Toast.makeText(
                                                            context,
                                                            "Recarga exitosa: ${"%,.0f".format(rechargeAmount)}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("Recarga", "Error al actualizar saldo: ${e.message}")
                                                        errorMessage = "Error al actualizar: ${e.message}"
                                                        isLoading = false
                                                    }
                                            } else {
                                                // Si el documento no existe, crearlo
                                                Log.d("Recarga", "Documento no existe, creando nuevo")
                                                userRef.set(mapOf("saldo" to rechargeAmount.toDouble()))
                                                    .addOnSuccessListener {
                                                        Log.d("Recarga", "Documento creado exitosamente")
                                                        saldo = rechargeAmount.toDouble()
                                                        isLoading = false
                                                        showRechargeDialog = false

                                                        Toast.makeText(
                                                            context,
                                                            "Recarga exitosa: ${"%,.0f".format(rechargeAmount)}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("Recarga", "Error al crear documento: ${e.message}")
                                                        errorMessage = "Error al crear cuenta: ${e.message}"
                                                        isLoading = false
                                                    }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Recarga", "Error al obtener documento: ${e.message}")
                                            errorMessage = "Error al conectar: ${e.message}"
                                            isLoading = false
                                        }
                                } else {
                                    Log.e("Recarga", "Usuario no autenticado")
                                    errorMessage = "Usuario no autenticado"
                                    isLoading = false

                                    Toast.makeText(
                                        context,
                                        "Debes iniciar sesión para recargar",
                                        Toast.LENGTH_SHORT
                                    ).show()
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

@Preview(showBackground = true)
@Composable
fun NexusHomeScreenPreview() {
    MaterialTheme {
        NexusHomeScreen(onNavigateToSendMoney = {})
    }
}