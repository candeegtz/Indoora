package com.indoora.app.data.model

import com.squareup.moshi.Json
data class HomeRead(
    val id: Int,
    val name: String,
    @Json(name = "estadoConfig")
    val estadoConfig: EstadoConfig
)

enum class EstadoConfig {
    @Json(name = "NOT_CONFIG")
    NOT_CONFIG,

    @Json(name = "ONLY_DEVICES_CONFIG")
    ONLY_DEVICES_CONFIG,

    @Json(name = "CONFIG_COMPLETED")
    CONFIG_COMPLETED
}

data class RoomCreate(
    val name: String,
    val roomType: String,
    val homeId: Int
)

data class RoomRead(
    val id: Int,
    val name: String,
    val roomType: String,
    val homeId: Int
)

data class PositionCreate(
    val name: String,
    val roomId: Int
)

data class PositionRead(
    val id: Int,
    val name: String,
    val roomId: Int
)

data class ActivityCreate(
    val name: String,
    val homeId: Int,
    val positionIds: List<Int> = emptyList()
)

data class ActivityRead(
    val id: Int,
    val name: String,
    val homeId: Int
)