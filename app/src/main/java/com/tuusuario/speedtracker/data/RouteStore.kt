package com.tuusuario.speedtracker.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RouteStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("speed_tracker_routes", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "saved_routes_v1"

    // Lista observable por Compose: cualquier pantalla que la use se recompone sola al cambiar.
    val routes = mutableStateListOf<RouteRecord>()

    init {
        load()
    }

    fun add(route: RouteRecord) {
        routes.add(0, route)
        save()
    }

    fun delete(route: RouteRecord) {
        routes.removeAll { it.id == route.id }
        save()
    }

    private fun save() {
        val json = gson.toJson(routes.toList())
        prefs.edit().putString(key, json).apply()
    }

    private fun load() {
        val json = prefs.getString(key, null) ?: return
        val type = object : TypeToken<List<RouteRecord>>() {}.type
        val decoded: List<RouteRecord> = try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        routes.clear()
        routes.addAll(decoded)
    }
}
