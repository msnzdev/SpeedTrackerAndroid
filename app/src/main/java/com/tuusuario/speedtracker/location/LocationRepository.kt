package com.tuusuario.speedtracker.location

import android.location.Location
import com.tuusuario.speedtracker.data.RouteCoordinate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max

/**
 * Estado compartido en memoria entre el Servicio de ubicación (que corre en segundo plano)
 * y la interfaz (Compose). Al ser un singleton dentro del mismo proceso, no hace falta
 * ningún IPC: la UI simplemente observa estos StateFlow.
 */
object LocationRepository {

    private val _currentSpeedKmh = MutableStateFlow(0.0)
    val currentSpeedKmh: StateFlow<Double> = _currentSpeedKmh

    private val _isGPSActive = MutableStateFlow(false)
    val isGPSActive: StateFlow<Boolean> = _isGPSActive

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds

    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm

    private val _averageSpeedKmh = MutableStateFlow(0.0)
    val averageSpeedKmh: StateFlow<Double> = _averageSpeedKmh

    private val _maxSpeedKmh = MutableStateFlow(0.0)
    val maxSpeedKmh: StateFlow<Double> = _maxSpeedKmh

    // Trazado de la ruta actual (se resetea al empezar, se lee al terminar)
    private val routeCoordinates = mutableListOf<RouteCoordinate>()
    private var lastPathLocation: Location? = null
    private val minMetersBetweenPathPoints = 15.0

    private var startTimeMillis = 0L
    private var lastLocation: Location? = null

    fun startRoute() {
        _distanceKm.value = 0.0
        _maxSpeedKmh.value = 0.0
        _averageSpeedKmh.value = 0.0
        _elapsedSeconds.value = 0L
        lastLocation = null
        routeCoordinates.clear()
        lastPathLocation = null
        startTimeMillis = System.currentTimeMillis()
        _isTracking.value = true
    }

    /** Devuelve las coordenadas grabadas y el tiempo de inicio, para construir el RouteRecord. */
    fun stopRoute(): Pair<List<RouteCoordinate>, Long> {
        _isTracking.value = false
        return Pair(routeCoordinates.toList(), startTimeMillis)
    }

    fun onLocationUpdate(location: Location) {
        // GPS "activo" = precisión horizontal razonable
        _isGPSActive.value = location.hasAccuracy() && location.accuracy in 0f..50f

        if (location.hasSpeed()) {
            val speedKmh = location.speed * 3.6
            _currentSpeedKmh.value = speedKmh
            if (_isTracking.value) {
                _maxSpeedKmh.value = max(_maxSpeedKmh.value, speedKmh)
            }
        }

        if (_isTracking.value) {
            lastLocation?.let { last ->
                val deltaMeters = location.distanceTo(last)
                if (deltaMeters > 0.5f) {
                    _distanceKm.value += deltaMeters / 1000.0
                }
            }
            lastLocation = location

            val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
            _elapsedSeconds.value = elapsed
            if (elapsed > 0) {
                _averageSpeedKmh.value = _distanceKm.value / (elapsed / 3600.0)
            }

            val lastPath = lastPathLocation
            if (lastPath == null || location.distanceTo(lastPath) >= minMetersBetweenPathPoints) {
                routeCoordinates.add(RouteCoordinate(location.latitude, location.longitude))
                lastPathLocation = location
            }
        }
    }
}
