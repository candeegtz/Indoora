package com.indoora.app.data.repository

import com.indoora.app.data.model.EstadoConfig
import com.indoora.app.data.model.HomeRead
import com.indoora.app.data.model.HomeUpdate
import com.indoora.app.data.model.PositionRead
import com.indoora.app.data.model.RoomRead
import com.indoora.app.network.ApiService
import com.indoora.app.network.RetrofitClient

class HomeRepository {
    private val api = RetrofitClient.api

    suspend fun getHome(homeId: Int): Result<HomeRead> {
        return try {
            val response = api.getHome(homeId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHomeConfigState(homeId: Int, estadoConfig: EstadoConfig): Result<HomeRead> {
        return try {
            val update = HomeUpdate(estadoConfig = estadoConfig)
            val response = api.updateHome(homeId, update)
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

    suspend fun getRooms(homeId: Int): Result<List<RoomRead>> {
        return try {
            val response = api.getRoomsByHomeId(homeId)
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

    suspend fun getPositions(roomId: Int): Result<List<PositionRead>> {
        return try {
            val response = api.getPositionsByRoomId(roomId)
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