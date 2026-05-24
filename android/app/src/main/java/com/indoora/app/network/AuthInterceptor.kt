package com.indoora.app.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

class AuthInterceptor(private val context: Context) : Interceptor {

    private val client = OkHttpClient()

    override fun intercept(chain: Interceptor.Chain): Response {

        // Obtener la petición original
        val originalRequest = chain.request()
        val accessToken = runBlocking { TokenManager.getAccessToken(context) }

        // Se crea la petición con el token
        var request = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        // Se envía la petición controlada
        var response = chain.proceed(request)

        // Refresh de token
        if (response.code == 401) {

            synchronized(this) {

                // Obtener el refresh token
                val refreshToken = runBlocking { TokenManager.getRefreshToken(context) }

                if (refreshToken != null) {
                    try {
                        // Petición de refresh
                        val jsonBody = JSONObject().apply {
                            put("refresh_token", refreshToken)
                        }.toString()

                        val mediaType = "application/json".toMediaType()

                        val refreshRequest = Request.Builder()
                            .url("http://10.0.2.2:8000/auth/refresh")
                            .post(jsonBody.toRequestBody(mediaType))
                            .build()

                        val refreshResponse = client.newCall(refreshRequest).execute()

                        if (refreshResponse.isSuccessful) {
                            val responseBody = refreshResponse.body?.string()
                            if (responseBody != null) {
                                // Actualizar tokens
                                val json = JSONObject(responseBody)
                                val newAccessToken = json.getString("access_token")
                                val newRefreshToken = json.getString("refresh_token")

                                runBlocking {
                                    TokenManager.saveTokens(
                                        context,
                                        access = newAccessToken,
                                        refresh = newRefreshToken
                                    )
                                }
                                RetrofitClient.setToken(newAccessToken)

                                response.close()

                                // Volver a intentar la petición original
                                val newRequest = originalRequest.newBuilder()
                                    .addHeader("Authorization", "Bearer $newAccessToken")
                                    .build()
                                return chain.proceed(newRequest)
                            }
                        } else {
                            Log.e("AuthInterceptor", "Refresh falló: ${refreshResponse.code}")
                        }
                    } catch (e: Exception) {
                        Log.e("AuthInterceptor", "Excepción en refresh: ${e.message}")
                    }
                } else {
                    Log.e("AuthInterceptor", "Refresh token es NULL")
                }

                runBlocking { TokenManager.clearTokens(context) }
                RetrofitClient.setToken(null)
            }
        }
        return response
    }
}