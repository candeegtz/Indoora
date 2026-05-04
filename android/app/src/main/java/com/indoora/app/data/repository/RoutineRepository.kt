package com.indoora.app.data.repository

import com.indoora.app.data.model.RoutineCreate
import com.indoora.app.data.model.RoutineRead
import com.indoora.app.data.model.RoutineUpdate
import com.indoora.app.network.RetrofitClient

class RoutineRepository {
    private val api = RetrofitClient.api

    suspend fun getRoutines(homeId: Int): Result<List<RoutineRead>> {
        return try {
            val response = api.getRoutinesByHomeId(homeId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRoutine(routine: RoutineCreate): Result<RoutineRead> {
        return try {
            val response = api.createRoutine(routine)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRoutine(routineId: Int, routine: RoutineUpdate): Result<RoutineRead> {
        return try {
            val response = api.updateRoutine(routineId, routine)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRoutine(routineId: Int): Result<Boolean> {
        return try {
            val response = api.deleteRoutine(routineId)
            if (response.isSuccessful) Result.success(true)
            else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}