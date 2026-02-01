package com.indoora.app.feature.auth

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object AuthRepository {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val api = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(AuthApi::class.java)

    suspend fun login(email: String, password: String): LoginResponse {
        return api.login(LoginRequest(email, password))
    }
}
