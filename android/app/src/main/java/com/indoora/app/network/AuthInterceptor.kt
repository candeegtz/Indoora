package com.indoora.app.network

import android.content.Context
import android.util.Log
import com.indoora.app.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AuthInterceptor(private val context: Context) : Interceptor {

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Obtener el token actual (bloqueante con runBlocking)
        val accessToken = runBlocking { TokenManager.getAccessToken(context) }

        Log.d("AuthInterceptor", "Token actual: ${accessToken?.take(20)}...")

        // Añadir token a la petición
        var request = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        var response = chain.proceed(request)

        Log.d("AuthInterceptor", "Código respuesta: ${response.code}")

        // Si es 401 (no autorizado), intentar refresh
        if (response.code == 401) {
            Log.d("AuthInterceptor", "Token expirado, intentando refresh...")
            synchronized(this) {
                val refreshToken = runBlocking { TokenManager.getRefreshToken(context) }
                if (refreshToken != null) {
                    // Llamar a refresh (suspend dentro de runBlocking)
                    val newTokens = runBlocking {
                        val refreshResponse = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                        if (refreshResponse.isSuccessful) {
                            refreshResponse.body()
                        } else {
                            null
                        }
                    }

                    if (newTokens != null) {
                        // Guardar nuevos tokens
                        runBlocking {
                            TokenManager.saveTokens(
                                context,
                                access = newTokens.access_token,
                                refresh = newTokens.refresh_token
                            )
                        }
                        // Actualizar token en RetrofitClient
                        RetrofitClient.setToken(newTokens.access_token)

                        // Cerrar respuesta anterior y reintentar con nuevo token
                        response.close()
                        val newRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer ${newTokens.access_token}")
                            .build()
                        return chain.proceed(newRequest)
                    }
                }
                // Falló refresh: limpiar sesión
                runBlocking { TokenManager.clearTokens(context) }
                RetrofitClient.setToken(null)
            }
        }
        return response
    }
}