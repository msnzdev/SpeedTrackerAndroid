package com.tuusuario.speedtracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tuusuario.speedtracker.data.RouteRecord
import com.tuusuario.speedtracker.data.RouteStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    routeStore: RouteStore,
    onBack: () -> Unit,
    onShare: (RouteRecord) -> Unit,
    onOpenMap: (RouteRecord) -> Unit
) {
    var routeToDelete by remember { mutableStateOf<RouteRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de rutas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (routeStore.routes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Route, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Sin rutas guardadas")
                    Text("Cuando termines una ruta aparecerá aquí.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(routeStore.routes, key = { it.id }) { route ->
                    RouteRow(
                        route = route,
                        onClick = { onOpenMap(route) },
                        onShare = { onShare(route) },
                        onDelete = { routeToDelete = route }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    routeToDelete?.let { route ->
        AlertDialog(
            onDismissRequest = { routeToDelete = null },
            title = { Text("¿Eliminar esta ruta?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    routeStore.delete(route)
                    routeToDelete = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { routeToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun RouteRow(
    route: RouteRecord,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(route.dateFormatted, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            if (route.coordinates.size > 1) {
                Icon(Icons.Filled.Map, contentDescription = "Tiene mapa", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = "Compartir")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(route.durationFormatted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text(String.format("%.1f km", route.distanceKm), style = MaterialTheme.typography.bodySmall)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                String.format("%.0f km/h media", route.averageSpeedKmh),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Text(String.format("%.0f km/h máx", route.maxSpeedKmh), style = MaterialTheme.typography.bodySmall)
        }
    }
}
