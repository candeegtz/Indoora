package com.indoora.app.data.repository

import android.content.Context
import com.indoora.app.data.model.LoginRequest
import com.indoora.app.data.model.LoginResponse
import com.indoora.app.data.model.UserCreate
import com.indoora.app.data.model.UserRead
import com.indoora.app.network.RetrofitClient
import com.indoora.app.network.TokenManager

class AuthRepository(private val context: Context) {
    private val api = RetrofitClient.api

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                response.body()?.let { loginResponse ->
                    TokenManager.saveTokens(
                        context,
                        loginResponse.access_token,
                        loginResponse.refresh_token
                    )
                    RetrofitClient.setToken(loginResponse.access_token)
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
}