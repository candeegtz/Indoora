package com.indoora.app.data.repository

import android.content.Context
import com.indoora.app.data.model.LoginRequest
import com.indoora.app.data.model.LoginResponse
import com.indoora.app.data.model.PositionCreate
import com.indoora.app.data.model.PositionRead
import com.indoora.app.data.model.RoomCreate
import com.indoora.app.data.model.RoomRead
import com.indoora.app.data.model.UserCreate
import com.indoora.app.data.model.UserRead
import com.indoora.app.data.model.UserUpdate
import com.indoora.app.network.RetrofitClient
import com.indoora.app.network.TokenManager
import org.json.JSONArray
import org.json.JSONObject

class AuthRepository(private val context: Context) {
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
                return traducirMensaje(detailStr)
            }

            "Error desconocido"
        } catch (e: Exception) {
            when {
                errorBody?.contains("Username already taken", ignoreCase = true) == true -> "El nombre de usuario ya está en uso"
                errorBody?.contains("Email already registered", ignoreCase = true) == true -> "El correo electrónico ya está registrado"
                errorBody?.contains("Invalid credentials", ignoreCase = true) == true -> "Credenciales incorrectas"
                errorBody?.contains("Subject not found", ignoreCase = true) == true -> "Sujeto no encontrado"
                errorBody?.contains("Home not found", ignoreCase = true) == true -> "Hogar no encontrado"
                errorBody?.contains("Room not found", ignoreCase = true) == true -> "Habitación no encontrada"
                errorBody?.contains("Activity not found", ignoreCase = true) == true -> "Actividad no encontrada"
                errorBody?.contains("Routine not found", ignoreCase = true) == true -> "Rutina no encontrada"
                else -> errorBody?.take(100) ?: "Error de conexión"
            }
        }
    }

    private fun traducirMensaje(mensaje: String): String {
        return when {
            mensaje.contains("Username already taken", ignoreCase = true) -> "El nombre de usuario ya está en uso"
            mensaje.contains("Email already registered", ignoreCase = true) -> "El correo electrónico ya está registrado"
            mensaje.contains("Invalid credentials", ignoreCase = true) -> "Credenciales incorrectas"
            mensaje.contains("User not found", ignoreCase = true) -> "Usuario no encontrado"
            mensaje.contains("Home not found", ignoreCase = true) -> "Hogar no encontrado"
            mensaje.contains("Room not found", ignoreCase = true) -> "Habitación no encontrada"
            mensaje.contains("Position not found", ignoreCase = true) -> "Posición no encontrada"
            mensaje.contains("Activity not found", ignoreCase = true) -> "Actividad no encontrada"
            mensaje.contains("Routine not found", ignoreCase = true) -> "Rutina no encontrada"
            mensaje.contains("home_name is required", ignoreCase = true) -> "El nombre del hogar es obligatorio"
            mensaje.contains("subject_username is required", ignoreCase = true) -> "El nombre de usuario del sujeto es obligatorio"
            mensaje.contains("Password must be at least 6 characters", ignoreCase = true) -> "La contraseña debe tener al menos 6 caracteres"
            mensaje.contains("Routine name cannot be empty", ignoreCase = true) -> "El nombre de la rutina no puede estar vacío"
            mensaje.contains("startTime must be earlier than endTime", ignoreCase = true) -> "La hora de inicio debe ser anterior a la hora de fin"
            mensaje.contains("Routine time overlaps", ignoreCase = true) -> "El horario coincide con otra rutina existente"
            else -> mensaje
        }
    }

    private fun traducirMensajeValidacion(msg: String): String {
        return when {
            msg.contains("An email address must have an @-sign", ignoreCase = true) -> "El correo electrónico debe contener un '@'"
            msg.contains("value is not a valid email address", ignoreCase = true) -> "El correo electrónico no es válido"
            msg.contains("ensure this value has at least", ignoreCase = true) -> "La contraseña debe tener al menos 6 caracteres"
            msg.contains("field required", ignoreCase = true) -> "Campo obligatorio"
            else -> msg
        }
    }


    suspend fun getMe(): Result<UserRead> {
        return try {
            val response = api.getMe()
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

    suspend fun getCurrentUser(): Result<UserRead> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun updateUser(userId: Int, data: UserUpdate): Result<UserRead> {
        return try {
            val response = api.updateUser(userId, data)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    // Guardar tokens tras iniciar sesión
                    val accessToken = loginResponse.access_token
                    val refreshToken = loginResponse.refresh_token
                    TokenManager.saveTokens(context, accessToken, refreshToken)
                    RetrofitClient.setToken(accessToken)

                    // Obtener el usuario para guardar userId y homeId
                    val userResult = getMe()
                    if (userResult.isSuccess) {
                        val user = userResult.getOrNull()!!
                        val userId = user.id
                        val homeId = user.homeId ?: 0
                        TokenManager.saveSession(context, accessToken, refreshToken, userId, homeId)
                    }

                    Result.success(loginResponse)
                } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}"))
        }
    }

    suspend fun register(data: UserCreate): Result<UserRead> {
        return try {
            val response = api.registerSupervisor(data)
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

    suspend fun createSubject(data: UserCreate): Result<UserRead> {
        return try {
            val response = api.createUser(data)
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

    suspend fun createRoom(room: RoomCreate): Result<RoomRead> {
        return try {
            val response = api.createRoom(room)
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

    suspend fun createPosition(position: PositionCreate): Result<PositionRead> {
        return try {
            val response = api.createPosition(position)
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