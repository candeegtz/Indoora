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
import kotlinx.coroutines.launch

class ActivitiesViewModel(
    private val activityRepository: ActivityRepository,
    private val homeRepository: HomeRepository
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

    // ========== Operaciones con actividades ==========

    fun loadActivities(homeId: Int) {
        viewModelScope.launch {
            _activitiesState.value = UiState.Loading
            val result = activityRepository.getActivities(homeId)
            _activitiesState.value = result.fold(
                onSuccess = { list ->
                    println("✅ Activities loaded: ${list.size} items") // Debug
                    UiState.Success(list)
                },
                onFailure = { error ->
                    println("❌ Error loading activities: ${error.message}") // Debug
                    UiState.Error(error.message ?: "Error loading activities")
                }
            )
        }
    }

    fun createActivity(name: String, homeId: Int, positionIds: List<Int>) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            val result = activityRepository.createActivity(name, homeId, positionIds)
            _createState.value = result.fold(
                onSuccess = { activity ->
                    UiState.Success(activity)
                },
                onFailure = { UiState.Error(it.message ?: "Error creating activity") }
            )
            // ✅ Recargar DESPUÉS de que createState se actualice
            if (_createState.value is UiState.Success) {
                loadActivities(homeId)
            }
        }
    }

    fun updateActivity(activityId: Int, name: String, positionIds: List<Int>, homeId: Int) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val result = activityRepository.updateActivity(activityId, name, positionIds)
            _updateState.value = result.fold(
                onSuccess = { activity ->
                    UiState.Success(activity)
                },
                onFailure = { UiState.Error(it.message ?: "Error updating activity") }
            )
            // ✅ Recargar DESPUÉS de que updateState se actualice
            if (_updateState.value is UiState.Success) {
                loadActivities(homeId)
            }
        }
    }

    fun deleteActivity(activityId: Int, homeId: Int) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            val result = activityRepository.deleteActivity(activityId)
            _deleteState.value = result.fold(
                onSuccess = {
                    UiState.Success(true)
                },
                onFailure = { UiState.Error(it.message ?: "Error deleting activity") }
            )
            // ✅ Recargar DESPUÉS de que deleteState se actualice
            if (_deleteState.value is UiState.Success) {
                loadActivities(homeId)
            }
        }
    }

    fun resetCreateState() { _createState.value = UiState.Idle }
    fun resetUpdateState() { _updateState.value = UiState.Idle }
    fun resetDeleteState() { _deleteState.value = UiState.Idle }

    // ========== Carga de habitaciones y posiciones ==========

    fun loadRoomsAndPositions(homeId: Int) {
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
            val currentMap = _positionsState.value.toMutableMap()
            currentMap[roomId] = UiState.Loading
            _positionsState.value = currentMap

            val result = homeRepository.getPositions(roomId)
            val newMap = _positionsState.value.toMutableMap()
            newMap[roomId] = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error loading positions") }
            )
            _positionsState.value = newMap
        }
    }
}