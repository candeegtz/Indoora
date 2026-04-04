package com.indoora.app.data.repository

import com.indoora.app.data.model.HomeRead
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
}