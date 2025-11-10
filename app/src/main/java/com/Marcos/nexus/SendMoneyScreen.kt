package com.Marcos.nexus

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.runtime.LaunchedEffect

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

// Función para formatear el monto con separadores de miles
fun formatCurrency(amount: String): String {
    if (amount.isEmpty()) return ""
    val number = amount.toLongOrNull() ?: return amount
    return number.toString().reversed().chunked(3).joinToString(".").reversed()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreen(
    onBack: () -> Unit
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    val context = LocalContext.current

    var amount by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var saldoDisponible by remember { mutableStateOf(0.0) }
    var saldoCartera by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedSource by remember { mutableStateOf("disponible") }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        saldoDisponible = document.getDouble("saldo") ?: 0.0
                        saldoCartera = document.getDouble("saldoCartera") ?: 0.0
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFFFFEFE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF4F4F4))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NEXUS",
                fontSize = 24.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Digita el monto",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Monto centrado con formato de dinero
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.widthIn(min = 180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = amount,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() }) {
                                    amount = input
                                }
                            },
                            textStyle = TextStyle(
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                color = Color.Transparent,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Mostrar el texto formateado o placeholder
                        Text(
                            text = if (amount.isEmpty()) "--" else formatCurrency(amount),
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            color = if (amount.isEmpty())
                                Color.White.copy(alpha = 0.5f)
                            else
                                Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Campo de mensaje como TextField
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = {
                        Text(
                            text = "Deja un mensaje (opcional)",
                            fontFamily = Poppins,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(
                        fontFamily = Poppins,
                        fontSize = 14.sp
                    ),
                    maxLines = 3
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Enviar desde",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedSource = "disponible" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedSource == "disponible")
                            Color(0xFFF4F4F4) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selectedSource == "disponible") Color.Black else Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Disponible",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$ ${"%,.0f".format(saldoDisponible)}",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedSource = "cartera" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedSource == "cartera")
                            Color(0xFFF4F4F4) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selectedSource == "cartera") Color.Black else Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Cartera",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$ ${"%,.0f".format(saldoCartera)}",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Usuario",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = recipient,
                onValueChange = { recipient = it },
                placeholder = {
                    Text(
                        text = "Nombre o correo del destinatario",
                        fontFamily = Poppins,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.3f),
                    focusedLabelColor = Color.Black,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                fontFamily = Poppins,
                fontSize = 12.sp,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                if (amount.isEmpty() || amount.toDoubleOrNull() == null) {
                    errorMessage = "Ingresa un monto válido"
                    return@Button
                }

                if (recipient.isEmpty()) {
                    errorMessage = "Ingresa el nombre o correo del destinatario"
                    return@Button
                }

                val amountValue = amount.toDouble()
                val currentUser = auth.currentUser

                if (currentUser == null) {
                    errorMessage = "Usuario no autenticado"
                    return@Button
                }

                val availableBalance = if (selectedSource == "disponible") saldoDisponible else saldoCartera
                if (amountValue > availableBalance) {
                    errorMessage = "Saldo insuficiente"
                    return@Button
                }

                errorMessage = null
                isLoading = true

                db.collection("usuarios")
                    .whereEqualTo("email", recipient)
                    .get()
                    .addOnSuccessListener { emailQuery ->
                        if (!emailQuery.isEmpty) {
                            processTransaction(
                                db, currentUser.uid, emailQuery.documents[0].id,
                                amountValue, selectedSource, context, message,
                                onSuccess = {
                                    saldoDisponible = it.first
                                    saldoCartera = it.second
                                    isLoading = false
                                    amount = ""
                                    recipient = ""
                                    message = ""
                                    Toast.makeText(context, "Envío exitoso!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    errorMessage = error
                                    isLoading = false
                                }
                            )
                        } else {
                            db.collection("usuarios")
                                .whereEqualTo("nombre", recipient)
                                .get()
                                .addOnSuccessListener { nameQuery ->
                                    if (!nameQuery.isEmpty) {
                                        val recipientDoc = nameQuery.documents[0]

                                        if (recipientDoc.id == currentUser.uid) {
                                            errorMessage = "No puedes enviarte dinero a ti mismo"
                                            isLoading = false
                                            return@addOnSuccessListener
                                        }

                                        processTransaction(
                                            db, currentUser.uid, recipientDoc.id,
                                            amountValue, selectedSource, context, message,
                                            onSuccess = {
                                                saldoDisponible = it.first
                                                saldoCartera = it.second
                                                isLoading = false
                                                amount = ""
                                                recipient = ""
                                                message = ""
                                                Toast.makeText(context, "Envío exitoso!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { error ->
                                                errorMessage = error
                                                isLoading = false
                                            }
                                        )
                                    } else {
                                        errorMessage = "Usuario no encontrado"
                                        isLoading = false
                                    }
                                }
                                .addOnFailureListener {
                                    errorMessage = "Error al buscar usuario"
                                    isLoading = false
                                }
                        }
                    }
                    .addOnFailureListener {
                        errorMessage = "Error al buscar usuario"
                        isLoading = false
                    }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
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
                    text = "Enviar",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun processTransaction(
    db: FirebaseFirestore,
    senderId: String,
    recipientId: String,
    amount: Double,
    source: String,
    context: Context,
    message: String = "",
    onSuccess: (Pair<Double, Double>) -> Unit,
    onError: (String) -> Unit
) {
    val batch = db.batch()
    val senderRef = db.collection("usuarios").document(senderId)
    val recipientRef = db.collection("usuarios").document(recipientId)

    senderRef.get().addOnSuccessListener { senderDoc ->
        if (!senderDoc.exists()) {
            onError("Error al obtener datos del remitente")
            return@addOnSuccessListener
        }

        val senderSaldoDisponible = senderDoc.getDouble("saldo") ?: 0.0
        val senderSaldoCartera = senderDoc.getDouble("saldoCartera") ?: 0.0

        val newSenderSaldoDisponible = if (source == "disponible") senderSaldoDisponible - amount else senderSaldoDisponible
        val newSenderSaldoCartera = if (source == "cartera") senderSaldoCartera - amount else senderSaldoCartera

        batch.update(senderRef, mapOf(
            "saldo" to newSenderSaldoDisponible,
            "saldoCartera" to newSenderSaldoCartera
        ))

        recipientRef.get().addOnSuccessListener { recipientDoc ->
            if (!recipientDoc.exists()) {
                onError("Destinatario no encontrado")
                return@addOnSuccessListener
            }

            val recipientSaldo = recipientDoc.getDouble("saldo") ?: 0.0
            batch.update(recipientRef, "saldo", recipientSaldo + amount)

            val transaction = hashMapOf(
                "remitenteId" to senderId,
                "destinatarioId" to recipientId,
                "monto" to amount,
                "tipo" to "envio",
                "origen" to source,
                "fecha" to Timestamp.now(),
                "remitenteNombre" to (senderDoc.getString("nombre") ?: "Usuario"),
                "destinatarioNombre" to (recipientDoc.getString("nombre") ?: "Usuario"),
                "mensaje" to message
            )

            val transactionRef = db.collection("transacciones").document()
            batch.set(transactionRef, transaction)

            batch.commit()
                .addOnSuccessListener {
                    Log.d("Transaction", "Transacción exitosa")
                    onSuccess(Pair(newSenderSaldoDisponible, newSenderSaldoCartera))
                }
                .addOnFailureListener { e ->
                    Log.e("Transaction", "Error: ${e.message}")
                    onError("Error al completar la transacción")
                }
        }.addOnFailureListener {
            onError("Error al obtener datos del destinatario")
        }
    }.addOnFailureListener {
        onError("Error al obtener datos del remitente")
    }
}

