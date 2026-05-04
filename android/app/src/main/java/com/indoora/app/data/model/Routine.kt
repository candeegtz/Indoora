package com.indoora.app.data.model

data class RoutineRead(
    val id: Int,
    val name: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val days: List<String>,
    val activityId: Int
)

data class RoutineCreate(
    val name: String,
    val description: String?,
    val startTime: String,
    val endTime: String,
    val days: List<String>,
    val activityId: Int
)

data class RoutineUpdate(
    val name: String?,
    val description: String?,
    val startTime: String?,
    val endTime: String?,
    val days: List<String>?,
    val activityId: Int?
)