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

class AuthRepository(private val context: Context) {
    private val api = RetrofitClient.api

    suspend fun getMe(): Result<UserRead> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<UserRead> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(userId: Int, data: UserUpdate): Result<UserRead> {
        return try {
            val response = api.updateUser(userId, data)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(data: UserCreate): Result<UserRead> {
        return try {
            val response = api.registerSupervisor(data)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSubject(data: UserCreate): Result<UserRead> {
        return try {
            val response = api.createUser(data)
            if(response.isSuccessful){
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRoom(room: RoomCreate): Result<RoomRead> {
        return try {
            println("BODY ENVIADO: $room")
            val response = api.createRoom(room)

            println("RESPONSE CODE: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val errorBody = response.errorBody()?.string()
                println("ERROR BODY: $errorBody")
                Result.failure(Exception("Error ${response.code()} $errorBody"))
            }
        } catch (e: Exception) {
            println("EXCEPTION: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createPosition(position: PositionCreate): Result<PositionRead> {
        return try {
            val response = api.createPosition(position)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}