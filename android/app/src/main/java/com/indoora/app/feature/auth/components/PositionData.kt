package com.indoora.app.feature.auth.components

data class PositionData(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String = ""
)