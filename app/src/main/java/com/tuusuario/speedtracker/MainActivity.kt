package com.tuusuario.speedtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tuusuario.speedtracker.data.RouteCoordinate
import com.tuusuario.speedtracker.data.RouteRecord
import com.tuusuario.speedtracker.data.RouteStore
import com.tuusuario.speedtracker.location.LocationRepository
import com.tuusuario.speedtracker.location.LocationTrackingService
import com.tuusuario.speedtracker.ui.ContentScreen
import com.tuusuario.speedtracker.ui.HistoryScreen
import com.tuusuario.speedtracker.ui.RouteMapScreen

private sealed class Screen {
    data object Main : Screen()
    data object History : Screen()
    data class Map(val route: RouteRecord) : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var routeStore: RouteStore

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        startLocationServiceIfPermitted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        routeStore = RouteStore(applicationContext)

        requestNeededPermissions()

        setContent {
            var screen by remember { mutableStateOf<Screen>(Screen.Main) }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (val current = screen) {
                        is Screen.Main -> ContentScreen(
                            onStartRoute = { LocationRepository.startRoute() },
                            onStopRoute = {
                                val (coordinates, startMillis) = LocationRepository.stopRoute()
                                buildRouteRecord(coordinates, startMillis)
                            },
                            onRouteFinished = { record ->
                                routeStore.add(record)
                                shareRoute(record)
                            },
                            onOpenHistory = { screen = Screen.History }
                        )
                        is Screen.History -> HistoryScreen(
                            routeStore = routeStore,
                            onBack = { screen = Screen.Main },
                            onShare = { shareRoute(it) },
                            onOpenMap = { screen = Screen.Map(it) }
                        )
                        is Screen.Map -> RouteMapScreen(
                            route = current.route,
                            onBack = { screen = Screen.History }
                        )
                    }
                }
            }
        }
    }

    private fun buildRouteRecord(coordinates: List<RouteCoordinate>, startMillis: Long): RouteRecord {
        return RouteRecord(
            date = startMillis,
            durationSeconds = LocationRepository.elapsedSeconds.value,
            averageSpeedKmh = LocationRepository.averageSpeedKmh.value,
            maxSpeedKmh = LocationRepository.maxSpeedKmh.value,
            distanceKm = LocationRepository.distanceKm.value,
            coordinates = coordinates
        )
    }

    private fun shareRoute(route: RouteRecord) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, route.shareText)
        }
        startActivity(Intent.createChooser(sendIntent, "Compartir ruta"))
    }

    private fun requestNeededPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    /** El permiso de ubicación en segundo plano se pide aparte, como exige Android 10+. */
    private fun requestBackgroundLocationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    2001
                )
            }
        }
    }

    private fun startLocationServiceIfPermitted() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted) {
            requestBackgroundLocationIfNeeded()
            val serviceIntent = Intent(this, LocationTrackingService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }
}
