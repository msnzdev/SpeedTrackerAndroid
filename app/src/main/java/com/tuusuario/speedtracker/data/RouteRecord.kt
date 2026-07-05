package com.tuusuario.speedtracker.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class RouteCoordinate(
    val latitude: Double,
    val longitude: Double
)

data class RouteRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: Long, // epoch millis
    val durationSeconds: Long,
    val averageSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val distanceKm: Double,
    val coordinates: List<RouteCoordinate> = emptyList()
) {
    val durationFormatted: String
        get() {
            val h = durationSeconds / 3600
            val m = (durationSeconds % 3600) / 60
            val s = durationSeconds % 60
            return if (h > 0) String.format("%dh %02dm %02ds", h, m, s)
            else String.format("%dm %02ds", m, s)
        }

    val dateFormatted: String
        get() = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("es", "ES")).format(Date(date))

    val shareText: String
        get() = buildString {
            append("🏍️ Ruta del $dateFormatted\n")
            append("⏱️ Duración: $durationFormatted\n")
            append("📏 Distancia: ${String.format("%.1f", distanceKm)} km\n")
            append("📊 Velocidad media: ${String.format("%.1f", averageSpeedKmh)} km/h\n")
            append("🚀 Velocidad máxima: ${String.format("%.1f", maxSpeedKmh)} km/h")
        }
}
