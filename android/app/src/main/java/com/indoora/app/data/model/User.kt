package com.indoora.app.data.model

data class UserCreate(
    val username: String,
    val name: String,
    val surnames: String,
    val email: String,
    val password: String,
    val userType: String,
    val homeName: String? = null,
    val subjectUsername: String? = null
)

data class UserRead(
    val id: Int,
    val username: String,
    val name: String,
    val surnames: String,
    val email: String,
    val userType: String,
    val homeId: Int?
)