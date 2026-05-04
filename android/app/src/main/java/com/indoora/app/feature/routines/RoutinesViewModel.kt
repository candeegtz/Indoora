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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
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

    // Estado del filtro por día (null = "Todos")
    private val _selectedDay = MutableStateFlow<String?>(null)
    val selectedDay: StateFlow<String?> = _selectedDay.asStateFlow()

    // Lista filtrada y ordenada (derivada de routinesState y selectedDay)
    val filteredRoutines: StateFlow<List<RoutineRead>> = combine(
        routinesState,
        _selectedDay
    ) { state, day ->
        when (state) {
            is UiState.Success -> {
                val all = state.data
                // Filtrar por día
                val filtered = if (day == null) {
                    all
                } else {
                    all.filter { routine -> routine.days.contains(day) }
                }
                // Ordenar por startTime (formato "HH:MM:SS" o "HH:MM")
                filtered.sortedBy { it.startTime.take(5) }
            }
            else -> emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

    fun setSelectedDay(day: String?) {
        _selectedDay.value = day
    }

    fun resetCreateState() { _createState.value = UiState.Idle }
    fun resetUpdateState() { _updateState.value = UiState.Idle }
    fun resetDeleteState() { _deleteState.value = UiState.Idle }
}