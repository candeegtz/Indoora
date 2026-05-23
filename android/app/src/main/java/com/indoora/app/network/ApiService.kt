package com.indoora.app.network

import com.indoora.app.data.model.ActivityCreate
import com.indoora.app.data.model.ActivityRead
import com.indoora.app.data.model.ActivityWithPositionsResponse
import com.indoora.app.data.model.AlertResponse
import com.indoora.app.data.model.HomeRead
import com.indoora.app.data.model.HomeUpdate
import com.indoora.app.data.model.LoginRequest
import com.indoora.app.data.model.LoginResponse
import com.indoora.app.data.model.PositionCreate
import com.indoora.app.data.model.PositionRead
import com.indoora.app.data.model.RefreshTokenRequest
import com.indoora.app.data.model.RoomCreate
import com.indoora.app.data.model.RoomRead
import com.indoora.app.data.model.RoutineCreate
import com.indoora.app.data.model.RoutineRead
import com.indoora.app.data.model.RoutineUpdate
import com.indoora.app.data.model.StablePositionRequest
import com.indoora.app.data.model.UserCreate
import com.indoora.app.data.model.UserRead
import com.indoora.app.data.model.UserUpdate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register-supervisor")
    suspend fun registerSupervisor(@Body request: UserCreate): Response<UserRead>

    @GET("auth/me")
    suspend fun getMe(): Response<UserRead>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<LoginResponse>
    // Users
    @POST("users/")
    suspend fun createUser(@Body request: UserCreate): Response<UserRead>

    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: Int,
        @Body data: UserUpdate
    ): Response<UserRead>

    // Homes
    @GET("homes/{id}")
    suspend fun getHome(@Path("id") id: Int): Response<HomeRead>

    // Rooms
    @POST("homes/rooms")
    suspend fun createRoom(@Body request: RoomCreate): Response<RoomRead>

    // Positions
    @POST("homes/positions")
    suspend fun createPosition(@Body request: PositionCreate): Response<PositionRead>

    // Activities
    @POST("activities")
    suspend fun createActivity(@Body request: ActivityCreate): Response<ActivityRead>

    @PUT("homes/{homeId}")
    suspend fun updateHome(@Path("homeId") homeId: Int,  @Body homeUpdate: HomeUpdate): Response<HomeRead>

    @GET("routines/home/{home_id}")
    suspend fun getRoutinesByHomeId(@Path("home_id") homeId: Int): Response<List<RoutineRead>>

    @POST("routines")
    suspend fun createRoutine(@Body routine: RoutineCreate): Response<RoutineRead>

    @PUT("routines/{routine_id}")
    suspend fun updateRoutine(@Path("routine_id") routineId: Int, @Body routine: RoutineUpdate): Response<RoutineRead>

    @DELETE("routines/{routine_id}")
    suspend fun deleteRoutine(@Path("routine_id") routineId: Int): Response<Unit>

    @GET("homes/{home_id}/rooms")
    suspend fun getRoomsByHomeId(@Path("home_id") homeId: Int): Response<List<RoomRead>>

    @GET("homes/rooms/{room_id}/positions")
    suspend fun getPositionsByRoomId(@Path("room_id") roomId: Int): Response<List<PositionRead>>

    // Listar actividades de una casa
    @GET("activities/home/{home_id}")
    suspend fun getActivitiesByHomeId(@Path("home_id") homeId: Int): Response<List<ActivityRead>>

    // Actualizar actividad (ya existe)
    @PUT("activities/{activity_id}")
    suspend fun updateActivity(@Path("activity_id") activityId: Int, @Body request: ActivityCreate): Response<ActivityRead>

    // Eliminar actividad (si quieres)
    @DELETE("activities/{activity_id}")
    suspend fun deleteActivity(@Path("activity_id") activityId: Int): Response<Unit>

    @GET("activities/{activity_id}")
    suspend fun getActivityById(@Path("activity_id") activityId: Int): Response<ActivityWithPositionsResponse>

    @GET("positioning/rooms_positions/{homeId}")
    suspend fun getRoomsPositions(@Path("homeId") homeId: Int): Response<Map<String, List<String>>>

    @POST("positioning/stable")
    suspend fun sendStablePosition(@Body request: StablePositionRequest): Response<Unit>

    @GET("positioning/alerts/unread/{homeId}")
    suspend fun getUnreadAlerts(@Path("homeId") homeId: Int): Response<List<AlertResponse>>

    @PATCH("positioning/alerts/{alertId}/read")
    suspend fun markAlertAsRead(@Path("alertId") alertId: Int): Response<Unit>
}