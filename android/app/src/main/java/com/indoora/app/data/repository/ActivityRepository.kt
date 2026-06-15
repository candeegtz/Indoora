package com.indoora.app.data.repository

import com.indoora.app.data.model.ActivityCreate
import com.indoora.app.data.model.ActivityRead
import com.indoora.app.data.model.ActivityWithPositionsResponse
import com.indoora.app.network.RetrofitClient
import org.json.JSONArray
import org.json.JSONObject

class ActivityRepository {
    private val api = RetrofitClient.api

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val trimmed = errorBody?.trim() ?: return "Error desconocido"

            if (trimmed.startsWith("[")) {
                val jsonArray = JSONArray(trimmed)
                if (jsonArray.length() > 0) {
                    val firstError = jsonArray.getJSONObject(0)
                    val msg = firstError.optString("msg", "")
                    if (msg.isNotEmpty()) {
                        return traducirMensajeValidacion(msg)
                    }
                }
                return "Error de validación"
            }

            val json = JSONObject(trimmed)

            val detailArray = json.optJSONArray("detail")
            if (detailArray != null && detailArray.length() > 0) {
                val firstError = detailArray.getJSONObject(0)
                val msg = firstError.optString("msg", "")
                if (msg.isNotEmpty()) {
                    return traducirMensajeValidacion(msg)
                }
            }

            val detailStr = json.optString("detail", "")
            if (detailStr.isNotEmpty() && detailStr != "null") {
                return traducirMensajeActividad(detailStr)
            }

            "Error desconocido"
        } catch (e: Exception) {
            when {
                errorBody?.contains("Activity name cannot be empty", ignoreCase = true) == true -> "El nombre de la actividad no puede estar vacío"
                errorBody?.contains("Activity not found", ignoreCase = true) == true -> "Actividad no encontrada"
                errorBody?.contains("Home not found", ignoreCase = true) == true -> "Hogar no encontrado"
                errorBody?.contains("Position not found", ignoreCase = true) == true -> "Posición no encontrada"
                errorBody?.contains("Room not found", ignoreCase = true) == true -> "Habitación no encontrada"
                errorBody?.contains("Only admins and supervisors", ignoreCase = true) == true -> "No tienes permisos para realizar esta acción"
                else -> "Error de conexión"
            }
        }
    }

    private fun traducirMensajeActividad(mensaje: String): String {
        return when {
            mensaje.contains("Activity name cannot be empty", ignoreCase = true) -> "El nombre de la actividad no puede estar vacío"
            mensaje.contains("Activity not found", ignoreCase = true) -> "Actividad no encontrada"
            mensaje.contains("Home not found", ignoreCase = true) -> "Hogar no encontrado"
            mensaje.contains("Position not found", ignoreCase = true) -> "Posición no encontrada"
            mensaje.contains("Room not found", ignoreCase = true) -> "Habitación no encontrada"
            mensaje.contains("Only admins and supervisors", ignoreCase = true) -> "No tienes permisos para realizar esta acción"
            else -> mensaje
        }
    }

    private fun traducirMensajeValidacion(msg: String): String {
        return when {
            msg.contains("field required", ignoreCase = true) -> "Campo obligatorio"
            msg.contains("ensure this value has at least", ignoreCase = true) -> "El valor introducido es demasiado corto"
            else -> msg
        }
    }

    suspend fun getActivities(homeId: Int): Result<List<ActivityRead>> {
        return try {
            val response = api.getActivitiesByHomeId(homeId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun createActivity(
        name: String,
        homeId: Int,
        positionIds: List<Int> = emptyList()
    ): Result<ActivityRead> {
        return try {
            val request = ActivityCreate(name, homeId, positionIds)
            val response = api.createActivity(request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun updateActivity(
        activityId: Int,
        name: String,
        positionIds: List<Int> = emptyList()
    ): Result<ActivityRead> {
        return try {
            val request = ActivityCreate(name, 0, positionIds) // homeId no se usa en update
            val response = api.updateActivity(activityId, request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun deleteActivity(activityId: Int): Result<Boolean> {
        return try {
            val response = api.deleteActivity(activityId)
            if (response.isSuccessful) Result.success(true)
            else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun getActivityById(activityId: Int): Result<ActivityWithPositionsResponse> {
        return try {
            val response = api.getActivityById(activityId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }
}