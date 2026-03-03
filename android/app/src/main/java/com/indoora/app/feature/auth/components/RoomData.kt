package com.indoora.app.feature.auth.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.indoora.app.data.model.RoomCreate

enum class RoomType(val displayName: String) {
    LIVING_ROOM("Salón"),
    BEDROOM("Habitación"),
    KITCHEN("Cocina"),
    BATHROOM("Baño"),
    DINING_ROOM("Comedor"),
    OTHER("Otro")
}

class RoomData(
    val id: String = java.util.UUID.randomUUID().toString()
) {
    var name by mutableStateOf("")
    var type by mutableStateOf(RoomType.LIVING_ROOM)
    var backendId by mutableStateOf<Int?>(null)  // ← ID del backend cuando se crea

    fun toRoomCreate() = RoomCreate(
        name = name,
        roomType = type.name,
        homeId = 0
    )
}