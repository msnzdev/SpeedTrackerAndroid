package com.tuusuario.speedtracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuusuario.speedtracker.data.RouteRecord
import com.tuusuario.speedtracker.location.LocationRepository
import java.util.Locale

@Composable
fun ContentScreen(
    onStartRoute: () -> Unit,
    onStopRoute: () -> RouteRecord,
    onRouteFinished: (RouteRecord) -> Unit,
    onOpenHistory: () -> Unit
) {
    val currentSpeed by LocationRepository.currentSpeedKmh.collectAsState()
    val isGPSActive by LocationRepository.isGPSActive.collectAsState()
    val isTracking by LocationRepository.isTracking.collectAsState()
    val elapsedSeconds by LocationRepository.elapsedSeconds.collectAsState()
    val averageSpeed by LocationRepository.averageSpeedKmh.collectAsState()
    val maxSpeed by LocationRepository.maxSpeedKmh.collectAsState()
    val distanceKm by LocationRepository.distanceKm.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10101C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Indicador de GPS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isGPSActive) Color(0xFF4CAF50) else Color(0xFFFF9800))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isGPSActive) "GPS Activo" else "GPS Inactivo",
                    color = if (isGPSActive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // Velocidad actual
            Text("VELOCIDAD ACTUAL", color = Color.Gray, fontSize = 13.sp)
            Text(
                text = String.format("%.0f", currentSpeed),
                color = Color.White,
                fontSize = 90.sp,
                fontWeight = FontWeight.Bold
            )
            Text("km/h", color = Color.Gray, fontSize = 18.sp)

            Spacer(Modifier.height(24.dp))

            if (isTracking) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatBox("TIEMPO", formatTime(elapsedSeconds), Modifier.weight(1f))
                    StatBox("MEDIA", String.format("%.0f km/h", averageSpeed), Modifier.weight(1f))
                    StatBox("MÁXIMA", String.format("%.0f km/h", maxSpeed), Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    String.format(Locale.getDefault(), "%.2f km recorridos", distanceKm),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (isTracking) {
                        val record = onStopRoute()
                        onRouteFinished(record)
                    } else {
                        onStartRoute()
                    }
                },
                enabled = isTracking || isGPSActive,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isTracking -> Color(0xFFE53935)
                        isGPSActive -> Color(0xFF43A047)
                        else -> Color.Gray
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    if (isTracking) "TERMINAR RUTA" else "EMPEZAR RUTA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (!isTracking && !isGPSActive) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Esperando señal GPS antes de poder empezar…",
                    color = Color(0xFFFF9800),
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onOpenHistory) {
                Icon(Icons.Filled.History, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.width(8.dp))
                Text("Ver historial de rutas", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun StatBox(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp)
    ) {
        Text(title, color = Color.Gray, fontSize = 11.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

private fun formatTime(totalSeconds: Long): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}
