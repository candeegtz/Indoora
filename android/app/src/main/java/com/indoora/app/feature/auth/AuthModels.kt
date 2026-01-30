package com.indoora.app.feature.auth

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String?,
    val role: String?
)
