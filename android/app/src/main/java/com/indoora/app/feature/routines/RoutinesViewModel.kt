package com.indoora.app.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.RoutineCreate
import com.indoora.app.data.model.RoutineRead
import com.indoora.app.data.model.RoutineUpdate
import com.indoora.app.data.repository.RoutineRepository
import com.indoora.app.feature.auth.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoutinesViewModel(
    private val repository: RoutineRepository,
    private val homeId: Int
) : ViewModel() {

    private val _routinesState = MutableStateFlow<UiState<List<RoutineRead>>>(UiState.Idle)
    val routinesState: StateFlow<UiState<List<RoutineRead>>> = _routinesState.asStateFlow()

    private val _createState = MutableStateFlow<UiState<RoutineRead?>>(UiState.Idle)
    val createState: StateFlow<UiState<RoutineRead?>> = _createState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<RoutineRead?>>(UiState.Idle)
    val updateState: StateFlow<UiState<RoutineRead?>> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Boolean>> = _deleteState.asStateFlow()

    init {
        loadRoutines()
    }

    fun loadRoutines() {
        viewModelScope.launch {
            _routinesState.value = UiState.Loading
            val result = repository.getRoutines(homeId)
            _routinesState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error loading routines") }
            )
        }
    }

    fun createRoutine(routine: RoutineCreate) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            val result = repository.createRoutine(routine)
            _createState.value = result.fold(
                onSuccess = { created ->
                    loadRoutines()
                    UiState.Success(created)
                },
                onFailure = { UiState.Error(it.message ?: "Error creating routine") }
            )
        }
    }

    fun updateRoutine(routineId: Int, routine: RoutineUpdate) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val result = repository.updateRoutine(routineId, routine)
            _updateState.value = result.fold(
                onSuccess = { updated ->
                    loadRoutines()
                    UiState.Success(updated)
                },
                onFailure = { UiState.Error(it.message ?: "Error updating routine") }
            )
        }
    }

    fun deleteRoutine(routineId: Int) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            val result = repository.deleteRoutine(routineId)
            _deleteState.value = result.fold(
                onSuccess = {
                    loadRoutines()
                    UiState.Success(true)
                },
                onFailure = { UiState.Error(it.message ?: "Error deleting routine") }
            )
        }
    }

    fun resetCreateState() { _createState.value = UiState.Idle }
    fun resetUpdateState() { _updateState.value = UiState.Idle }
    fun resetDeleteState() { _deleteState.value = UiState.Idle }
}