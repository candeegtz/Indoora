package com.indoora.app.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.RoutineCreate
import com.indoora.app.data.model.RoutineRead
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

    init {
        loadRoutines()
    }

    fun loadRoutines() {
        viewModelScope.launch {
            _routinesState.value = UiState.Loading
            val result = repository.getRoutines(homeId) // ✅ usa homeId
            _routinesState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error loading routines") }
            )
        }
    }

    fun createRoutine(
        name: String,
        description: String?,
        startTime: String,
        endTime: String,
        days: List<String>,
        activityId: Int
    ) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            val routine = RoutineCreate(name, description, startTime, endTime, days, activityId)
            val result = repository.createRoutine(routine)
            _createState.value = result.fold(
                onSuccess = { created ->
                    loadRoutines() // recargar lista
                    UiState.Success(created)
                },
                onFailure = { UiState.Error(it.message ?: "Error creating routine") }
            )
        }
    }

    fun resetCreateState() {
        _createState.value = UiState.Idle
    }
}