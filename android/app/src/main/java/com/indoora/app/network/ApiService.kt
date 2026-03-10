package com.indoora.app.network

import com.indoora.app.data.model.ActivityCreate
import com.indoora.app.data.model.ActivityRead
import com.indoora.app.data.model.EmisorDeviceCreate
import com.indoora.app.data.model.EmisorDeviceRead
import com.indoora.app.data.model.HomeRead
import com.indoora.app.data.model.LoginRequest
import com.indoora.app.data.model.LoginResponse
import com.indoora.app.data.model.PositionCreate
import com.indoora.app.data.model.PositionRead
import com.indoora.app.data.model.ReceptorDeviceCreate
import com.indoora.app.data.model.ReceptorDeviceRead
import com.indoora.app.data.model.RoomCreate
import com.indoora.app.data.model.RoomRead
import com.indoora.app.data.model.RoutineRead
import com.indoora.app.data.model.UserCreate
import com.indoora.app.data.model.UserRead
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register-supervisor")
    suspend fun registerSupervisor(@Body request: UserCreate): Response<UserRead>

    @GET("auth/me")
    suspend fun getMe(): Response<UserRead>

    // Users
    @POST("users/")
    suspend fun createUser(@Body request: UserCreate): Response<UserRead>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<UserRead>

    // Homes
    @GET("homes/{id}")
    suspend fun getHome(@Path("id") id: Int): Response<HomeRead>

    // Rooms
    @POST("homes/rooms")
    suspend fun createRoom(@Body request: RoomCreate): Response<RoomRead>

    @GET("homes/{homeId}/rooms")
    suspend fun getRoomsByHome(@Path("homeId") homeId: Int): Response<List<RoomRead>>

    @DELETE("homes/rooms/{id}")
    suspend fun deleteRoom(@Path("id") id: Int): Response<Map<String, String>>

    // Positions
    @POST("homes/positions")
    suspend fun createPosition(@Body request: PositionCreate): Response<PositionRead>

    @GET("homes/rooms/{roomId}/positions")
    suspend fun getPositionsByRoom(@Path("roomId") roomId: Int): Response<List<PositionRead>>

    @DELETE("homes/positions/{id}")
    suspend fun deletePosition(@Path("id") id: Int): Response<Map<String, String>>

    // Activities
    @POST("homes/activities")
    suspend fun createActivity(@Body request: ActivityCreate): Response<ActivityRead>

    @GET("homes/{homeId}/activities")
    suspend fun getActivitiesByHome(@Path("homeId") homeId: Int): Response<List<ActivityRead>>

    // Devices
    @POST("devices/emisor")
    suspend fun createEmisorDevice(@Body request: EmisorDeviceCreate): Response<EmisorDeviceRead>

    @GET("devices/emisor")
    suspend fun getAllEmisorDevices(): Response<List<EmisorDeviceRead>>

    @POST("devices/receptor")
    suspend fun createReceptorDevice(@Body request: ReceptorDeviceCreate): Response<ReceptorDeviceRead>

    @GET("devices/receptor")
    suspend fun getAllReceptorDevices(): Response<List<ReceptorDeviceRead>>

    // Routines
    @GET("routines/")
    suspend fun getAllRoutines(): Response<List<RoutineRead>>
}