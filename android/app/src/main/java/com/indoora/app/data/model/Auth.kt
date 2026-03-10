package com.indoora.app.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String
)