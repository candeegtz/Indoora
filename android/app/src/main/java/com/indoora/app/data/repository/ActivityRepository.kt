package com.indoora.app.data.repository

import com.indoora.app.data.model.ActivityCreate
import com.indoora.app.data.model.ActivityRead
import com.indoora.app.data.model.ActivityWithPositionsResponse
import com.indoora.app.network.RetrofitClient

class ActivityRepository {
    private val api = RetrofitClient.api

    suspend fun getActivities(homeId: Int): Result<List<ActivityRead>> {
        return try {
            val response = api.getActivitiesByHomeId(homeId)
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

    suspend fun createActivity(
        name: String,
        homeId: Int,
        positionIds: List<Int> = emptyList()
    ): Result<ActivityRead> {
        return try {
            val request = ActivityCreate(name, homeId, positionIds)
            val response = api.createActivity(request)
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

    suspend fun updateActivity(
        activityId: Int,
        name: String,
        positionIds: List<Int> = emptyList()
    ): Result<ActivityRead> {
        return try {
            // Nota: el backend probablemente no necesita homeId para update, usamos 0 como placeholder
            val request = ActivityCreate(name, 0, positionIds)
            val response = api.updateActivity(activityId, request)
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

    suspend fun deleteActivity(activityId: Int): Result<Boolean> {
        return try {
            val response = api.deleteActivity(activityId)
            if (response.isSuccessful) Result.success(true)
            else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActivityById(activityId: Int): Result<ActivityWithPositionsResponse> {
        return try {
            val response = api.getActivityById(activityId)
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
}