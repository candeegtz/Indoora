package com.indoora.app.data.model

data class HomeRead(
    val id: Int,
    val name: String
)

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