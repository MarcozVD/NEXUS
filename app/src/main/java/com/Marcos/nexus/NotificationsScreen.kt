package com.Marcos.nexus


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

data class Notification(
    val id: String = "",
    val tipo: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val fecha: com.google.firebase.Timestamp? = null,
    val leida: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    val auth = Firebase.auth
    val db = Firebase.firestore
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("notificaciones")
                .whereEqualTo("userId", user.uid)
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener { documents ->
                    notifications = documents.map { doc ->
                        Notification(
                            id = doc.id,
                            tipo = doc.getString("tipo") ?: "info",
                            titulo = doc.getString("titulo") ?: "",
                            mensaje = doc.getString("mensaje") ?: "",
                            fecha = doc.getTimestamp("fecha"),
                            leida = doc.getBoolean("leida") ?: false
                        )
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Notificaciones",
                    fontSize = 24.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Black)
            }
        } else if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No tienes notificaciones",
                    fontFamily = Poppins,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification = notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    val icon = when (notification.tipo) {
        "recarga" -> Icons.Default.Add
        "transferencia" -> Icons.Default.Send
        "alerta" -> Icons.Default.Warning
        else -> Icons.Default.Info
    }

    val iconColor = when (notification.tipo) {
        "recarga" -> Color(0xFF4CAF50)
        "transferencia" -> Color(0xFF2196F3)
        "alerta" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.leida) Color(0xFFF5F5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(if (notification.leida) 1.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.titulo,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.mensaje,
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatDate(notification.fecha),
                    fontFamily = Poppins,
                    fontSize = 11.sp,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""

    val date = timestamp.toDate()
    val now = Date()
    val diff = now.time - date.time

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Hace un momento"
        minutes < 60 -> "Hace ${minutes}m"
        hours < 24 -> "Hace ${hours}h"
        days < 7 -> "Hace ${days}d"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
}