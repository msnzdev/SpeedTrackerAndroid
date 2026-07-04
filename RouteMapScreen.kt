package com.tuusuario.speedtracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.tuusuario.speedtracker.data.RouteCoordinate
import com.tuusuario.speedtracker.data.RouteRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreen(route: RouteRecord, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(route.dateFormatted) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn("Duración", route.durationFormatted)
                StatColumn("Distancia", String.format("%.1f km", route.distanceKm))
                StatColumn("Media", String.format("%.0f km/h", route.averageSpeedKmh))
                StatColumn("Máxima", String.format("%.0f km/h", route.maxSpeedKmh))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (route.coordinates.size > 1) {
                RoutePathCanvas(route.coordinates)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Esta ruta no tiene trazado guardado")
                }
            }
        }
    }
}

@Composable
private fun StatColumn(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

/**
 * Dibuja el trazado de la ruta como una línea, normalizando las coordenadas GPS
 * al tamaño del lienzo. No requiere API key ni conexión a internet: es un dibujo
 * esquemático del recorrido (no un mapa con calles reales).
 */
@Composable
private fun RoutePathCanvas(coordinates: List<RouteCoordinate>) {
    val minLat = coordinates.minOf { it.latitude }
    val maxLat = coordinates.maxOf { it.latitude }
    val minLon = coordinates.minOf { it.longitude }
    val maxLon = coordinates.maxOf { it.longitude }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10101C))
            .padding(24.dp)
    ) {
        val latRange = (maxLat - minLat).takeIf { it > 0.0001 } ?: 0.0001
        val lonRange = (maxLon - minLon).takeIf { it > 0.0001 } ?: 0.0001

        fun toOffset(coord: RouteCoordinate): Offset {
            val x = ((coord.longitude - minLon) / lonRange) * size.width
            // La latitud crece hacia el norte, pero Y crece hacia abajo en pantalla → invertimos
            val y = size.height - ((coord.latitude - minLat) / latRange) * size.height
            return Offset(x.toFloat(), y.toFloat())
        }

        val points = coordinates.map { toOffset(it) }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color.Cyan,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }

        // Punto de inicio (verde) y fin (rojo)
        drawCircle(color = Color(0xFF4CAF50), radius = 16f, center = points.first())
        drawCircle(color = Color(0xFFE53935), radius = 16f, center = points.last())
    }
}
