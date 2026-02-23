package com.indoora.app.data.model

data class EmisorDeviceCreate(
    val name: String,
    val macAddress: String,
    val userId: Int
)

data class EmisorDeviceRead(
    val id: Int,
    val name: String,
    val macAddress: String,
    val userId: Int
)

data class ReceptorDeviceCreate(
    val name: String,
    val macAddress: String,
    val roomId: Int
)

data class ReceptorDeviceRead(
    val id: Int,
    val name: String,
    val macAddress: String,
    val roomId: Int
)