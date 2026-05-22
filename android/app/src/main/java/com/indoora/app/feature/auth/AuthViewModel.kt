package com.indoora.app.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.LoginResponse
import com.indoora.app.data.model.PositionCreate
import com.indoora.app.data.model.RoomCreate
import com.indoora.app.data.model.RoomRead
import com.indoora.app.data.model.UserCreate
import com.indoora.app.data.model.UserRead
import com.indoora.app.data.repository.AuthRepository
import com.indoora.app.feature.auth.components.PositionData
import com.indoora.app.network.RetrofitClient
import com.indoora.app.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository,
    private val context: Context  
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<LoginResponse>> = _loginState

    private val _registerAndLoginState = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val registerAndLoginState: StateFlow<UiState<LoginResponse>> = _registerAndLoginState

    private val _createSubjectState = MutableStateFlow<UiState<UserRead>>(UiState.Idle)
    val createSubjectState: StateFlow<UiState<UserRead>> = _createSubjectState

    private val _createRoomsState = MutableStateFlow<UiState<List<RoomRead>>>(UiState.Idle)
    val createRoomsState: StateFlow<UiState<List<RoomRead>>> = _createRoomsState

    private val _createPositionsState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val createPositionsState: StateFlow<UiState<Boolean>> = _createPositionsState

    private val _homeIdState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val homeIdState: StateFlow<UiState<Int>> = _homeIdState

    // Mapa para trackear rooms creadas en backend (roomLocalId -> roomBackendId)
    private val createdRoomsMap = mutableMapOf<String, Int>()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = repository.login(username, password)
            _loginState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unknown error") }
            )

            if (result.isSuccess) {
                // Obtener el usuario actual para guardar userId y homeId
                val userResult = repository.getMe()
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()!!
                    val accessToken = result.getOrNull()?.access_token ?: ""
                    val refreshToken = result.getOrNull()?.refresh_token ?: ""
                    val userId = user.id
                    val homeId = user.homeId ?: 0

                    // Guardar sesión completa
                    TokenManager.saveSession(context, accessToken, refreshToken, userId, homeId)
                    RetrofitClient.setToken(accessToken)

                    _homeIdState.value = UiState.Success(homeId)
                } else {
                    _homeIdState.value = UiState.Error("No se pudo obtener el usuario")
                }
            }
        }
    }

    fun registerAndLogin(data: UserCreate) {
        viewModelScope.launch {
            _registerAndLoginState.value = UiState.Loading

            val registerResult = repository.register(data)

            if (registerResult.isSuccess) {
                val loginResult = repository.login(data.username, data.password)
                _registerAndLoginState.value = loginResult.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Login failed after registration") }
                )

                if (loginResult.isSuccess) {
                    // Obtener el usuario actual para guardar userId y homeId
                    val userResult = repository.getMe()
                    if (userResult.isSuccess) {
                        val user = userResult.getOrNull()!!
                        val accessToken = loginResult.getOrNull()?.access_token ?: ""
                        val refreshToken = loginResult.getOrNull()?.refresh_token ?: ""
                        val userId = user.id
                        val homeId = user.homeId ?: 0

                        TokenManager.saveSession(context, accessToken, refreshToken, userId, homeId)
                        RetrofitClient.setToken(accessToken)

                        _homeIdState.value = UiState.Success(homeId)
                    } else {
                        _homeIdState.value = UiState.Error("No se pudo obtener el usuario")
                    }
                }
            } else {
                _registerAndLoginState.value = UiState.Error(
                    registerResult.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    private suspend fun fetchMyHomeId() {
        _homeIdState.value = UiState.Loading
        val result = repository.getMe()
        _homeIdState.value = result.fold(
            onSuccess = { user ->
                user.homeId?.let { UiState.Success(it) }
                    ?: UiState.Error("Usuario sin hogar asignado")
            },
            onFailure = { UiState.Error(it.message ?: "Error obteniendo usuario") }
        )
    }

    fun createSubject(data: UserCreate) {
        viewModelScope.launch {
            _createSubjectState.value = UiState.Loading
            val result = repository.createSubject(data)
            _createSubjectState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unknown error") }
            )
            if (result.isSuccess) {
                fetchMyHomeId()
            }
        }
    }

    fun createRooms(rooms: List<RoomCreate>) {
        viewModelScope.launch {
            _createRoomsState.value = UiState.Loading

            val createdRooms = mutableListOf<RoomRead>()

            try {
                for ((index, room) in rooms.withIndex()) {
                    println("DEBUG - Creando room: $room")

                    val result = repository.createRoom(room)

                    if (result.isSuccess) {
                        val createdRoom = result.getOrThrow()
                        createdRooms.add(createdRoom)

                        println("DEBUG - Room creada con ID backend: ${createdRoom.id}")
                    } else {
                        _createRoomsState.value =
                            UiState.Error(result.exceptionOrNull()?.message ?: "Error creando habitación")
                        return@launch
                    }
                }

                _createRoomsState.value = UiState.Success(createdRooms)

            } catch (e: Exception) {
                _createRoomsState.value = UiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun createPositions(positionsByRoomId: Map<Int, List<PositionData>>) {
        viewModelScope.launch {
            _createPositionsState.value = UiState.Loading

            try {
                positionsByRoomId.forEach { (roomId, positions) ->
                    positions.forEach { position ->
                        if (position.name.isNotBlank()) {
                            val positionCreate = PositionCreate(
                                name = position.name,
                                roomId = roomId
                            )

                            println("DEBUG - Creando position: $positionCreate")

                            val result = repository.createPosition(positionCreate)

                            if (result.isFailure) {
                                _createPositionsState.value = UiState.Error(
                                    result.exceptionOrNull()?.message ?: "Error creando posición"
                                )
                                return@launch
                            }
                        }
                    }
                }

                _createPositionsState.value = UiState.Success(true)

            } catch (e: Exception) {
                _createPositionsState.value = UiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun createPositionsFromList(positions: List<PositionCreate>) {
        viewModelScope.launch {
            _createPositionsState.value = UiState.Loading

            try {
                for (position in positions) {
                    println("DEBUG - Creando position: $position")

                    val result = repository.createPosition(position)

                    if (result.isFailure) {
                        _createPositionsState.value = UiState.Error(
                            result.exceptionOrNull()?.message ?: "Error creando posición"
                        )
                        return@launch
                    }
                }

                _createPositionsState.value = UiState.Success(true)

            } catch (e: Exception) {
                _createPositionsState.value = UiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun markPositionsAsComplete() {
        _createPositionsState.value = UiState.Success(true)
    }

    fun resetLoginState() {
        _loginState.value = UiState.Idle
        _homeIdState.value = UiState.Idle
    }

    fun resetRegisterState() {
        _registerAndLoginState.value = UiState.Idle
        _createSubjectState.value = UiState.Idle
        _createRoomsState.value = UiState.Idle
        _createPositionsState.value = UiState.Idle
        _homeIdState.value = UiState.Idle
    }
}

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}