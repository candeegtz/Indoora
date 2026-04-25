// feature/activities/ActivitiesViewModel.kt
package com.indoora.app.feature.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.ActivityRead
import com.indoora.app.data.model.PositionRead
import com.indoora.app.data.model.RoomRead
import com.indoora.app.data.repository.ActivityRepository
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.feature.auth.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    private val activityRepository: ActivityRepository,
    private val homeRepository: HomeRepository,
    private val homeId: Int               // ← Recibimos homeId desde NavGraph
) : ViewModel() {

    // Estados para actividades
    private val _activitiesState = MutableStateFlow<UiState<List<ActivityRead>>>(UiState.Idle)
    val activitiesState: StateFlow<UiState<List<ActivityRead>>> = _activitiesState.asStateFlow()

    private val _createState = MutableStateFlow<UiState<ActivityRead?>>(UiState.Idle)
    val createState: StateFlow<UiState<ActivityRead?>> = _createState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<ActivityRead?>>(UiState.Idle)
    val updateState: StateFlow<UiState<ActivityRead?>> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Boolean>> = _deleteState.asStateFlow()

    // Estados para habitaciones y posiciones (diálogos)
    private val _roomsState = MutableStateFlow<UiState<List<RoomRead>>>(UiState.Idle)
    val roomsState: StateFlow<UiState<List<RoomRead>>> = _roomsState.asStateFlow()

    private val _positionsState = MutableStateFlow<Map<Int, UiState<List<PositionRead>>>>(emptyMap())
    val positionsState: StateFlow<Map<Int, UiState<List<PositionRead>>>> = _positionsState.asStateFlow()

    init {
        // Cargar actividades al instante (como en ProfileViewModel)
        loadActivities()
    }

    // ---------------- Operaciones con actividades ----------------

    fun loadActivities() {
        viewModelScope.launch {
            _activitiesState.value = UiState.Loading
            val result = activityRepository.getActivities(homeId)
            _activitiesState.value = result.fold(
                onSuccess = { list ->
                    println("✅ Actividades cargadas: ${list.size}") // Debug
                    UiState.Success(list)
                },
                onFailure = { error ->
                    println("❌ Error al cargar actividades: ${error.message}")
                    UiState.Error(error.message ?: "Error loading activities")
                }
            )
        }
    }

    fun createActivity(name: String, positionIds: List<Int>) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            val result = activityRepository.createActivity(name, homeId, positionIds)
            _createState.value = result.fold(
                onSuccess = { activity ->
                    // Recargar la lista después de crear
                    loadActivities()
                    UiState.Success(activity)
                },
                onFailure = { UiState.Error(it.message ?: "Error creating activity") }
            )
        }
    }

    fun updateActivity(activityId: Int, name: String, positionIds: List<Int>) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val result = activityRepository.updateActivity(activityId, name, positionIds)
            _updateState.value = result.fold(
                onSuccess = { activity ->
                    loadActivities()
                    UiState.Success(activity)
                },
                onFailure = { UiState.Error(it.message ?: "Error updating activity") }
            )
        }
    }

    fun deleteActivity(activityId: Int) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            val result = activityRepository.deleteActivity(activityId)
            _deleteState.value = result.fold(
                onSuccess = {
                    loadActivities()
                    UiState.Success(true)
                },
                onFailure = { UiState.Error(it.message ?: "Error deleting activity") }
            )
        }
    }

    fun resetCreateState() { _createState.value = UiState.Idle }
    fun resetUpdateState() { _updateState.value = UiState.Idle }
    fun resetDeleteState() { _deleteState.value = UiState.Idle }

    // ---------------- Carga de habitaciones y posiciones ----------------

    fun loadRoomsAndPositions() {
        viewModelScope.launch {
            _roomsState.value = UiState.Loading
            val result = homeRepository.getRooms(homeId)
            _roomsState.value = result.fold(
                onSuccess = { rooms ->
                    rooms.forEach { room -> loadPositionsForRoom(room.id) }
                    UiState.Success(rooms)
                },
                onFailure = { UiState.Error(it.message ?: "Error loading rooms") }
            )
        }
    }

    private fun loadPositionsForRoom(roomId: Int) {
        viewModelScope.launch {
            _positionsState.update { current -> current + (roomId to UiState.Loading) }
            val result = homeRepository.getPositions(roomId)
            _positionsState.update { current ->
                current + (roomId to result.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Error loading positions") }
                ))
            }
        }
    }
}