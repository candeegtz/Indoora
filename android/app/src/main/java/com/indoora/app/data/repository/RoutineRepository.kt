package com.indoora.app.data.repository

import com.indoora.app.data.model.RoutineCreate
import com.indoora.app.data.model.RoutineRead
import com.indoora.app.data.model.RoutineUpdate
import com.indoora.app.network.RetrofitClient
import org.json.JSONArray
import org.json.JSONObject

class RoutineRepository {
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
                return traducirMensajeRutina(detailStr)
            }

            "Error desconocido"
        } catch (e: Exception) {
            "Error de conexión"
        }
    }

    private fun traducirMensajeRutina(mensaje: String): String {
        return when {
            mensaje.contains("Routine name cannot be empty", ignoreCase = true) -> "El nombre de la rutina no puede estar vacío"
            mensaje.contains("startTime must be earlier than endTime", ignoreCase = true) -> "La hora de inicio debe ser anterior a la hora de fin"
            mensaje.contains("Routine time overlaps with existing routine on the same day", ignoreCase = true) -> "El horario coincide con otra rutina existente en el mismo día"
            mensaje.contains("Activity not found", ignoreCase = true) -> "Actividad no encontrada"
            mensaje.contains("Activity must be associated with a Home", ignoreCase = true) -> "La actividad debe pertenecer a un hogar"
            mensaje.contains("Forbidden: You don't have access to this home's routines", ignoreCase = true) -> "No tienes permisos para acceder a las rutinas de este hogar"
            mensaje.contains("Original activity not found", ignoreCase = true) -> "La actividad original no fue encontrada"
            mensaje.contains("Home not found", ignoreCase = true) -> "Hogar no encontrado"
            else -> mensaje
        }
    }

    private fun traducirMensajeValidacion(msg: String): String {
        return when {
            msg.contains("field required", ignoreCase = true) -> "Campo obligatorio"
            msg.contains("value is not a valid time", ignoreCase = true) -> "Formato de hora no válido"
            else -> msg
        }
    }

    suspend fun getRoutines(homeId: Int): Result<List<RoutineRead>> {
        return try {
            val response = api.getRoutinesByHomeId(homeId)
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

    suspend fun createRoutine(routine: RoutineCreate): Result<RoutineRead> {
        return try {
            val response = api.createRoutine(routine)
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

    suspend fun updateRoutine(routineId: Int, routine: RoutineUpdate): Result<RoutineRead> {
        return try {
            val response = api.updateRoutine(routineId, routine)
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

    suspend fun deleteRoutine(routineId: Int): Result<Boolean> {
        return try {
            val response = api.deleteRoutine(routineId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }
}