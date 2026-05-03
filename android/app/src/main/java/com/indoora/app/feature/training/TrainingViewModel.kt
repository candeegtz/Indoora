package com.indoora.app.feature.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.network.mqtt.MqttManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

data class TrainingStep(
    val stepNumber: Int,
    val room: String,
    val position: String,
    val completed: Boolean = false
)

data class TrainingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sequence: List<TrainingStep> = emptyList(),
    val totalSteps: Int = 0,
    val isTraining: Boolean = false,
    val trainingComplete: Boolean = false
)

class TrainingViewModel(
    private val homeRepository: HomeRepository,
    private val mqttManager: MqttManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex: StateFlow<Int> = _currentStepIndex

    var homeId: Int = -1

    private val _instruction = MutableSharedFlow<String>()
    val instruction: SharedFlow<String> = _instruction.asSharedFlow()

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress

    private val _isTrainingActive = MutableStateFlow(false)
    val isTrainingActive: StateFlow<Boolean> = _isTrainingActive

    init {
        mqttManager.subscribe("training/instruction")
        mqttManager.subscribe("training/progress")
        mqttManager.subscribe("training/complete")

        viewModelScope.launch {
            mqttManager.messages.collect { message ->
                when (message.topic) {
                    "training/instruction" -> handleInstruction(message.message)
                    "training/progress"   -> handleProgress(message.message)
                    "training/complete"   -> handleComplete()
                }
            }
        }
    }

    fun loadTrainingSequence(homeId: Int) {
        this.homeId = homeId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 1. Obtener habitaciones (Result)
                val roomsResult = homeRepository.getRooms(homeId)
                if (roomsResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = roomsResult.exceptionOrNull()?.message ?: "Error al cargar habitaciones"
                    )
                    return@launch
                }
                val rooms = roomsResult.getOrNull() ?: emptyList()

                val sequence = mutableListOf<TrainingStep>()
                var stepCounter = 1

                for (room in rooms) {
                    // 2. Obtener posiciones de la habitación (Result)
                    val positionsResult = homeRepository.getPositions(room.id)
                    if (positionsResult.isFailure) {
                        // Opcional: mostrar error pero continuar con otras habitaciones
                        continue
                    }
                    val positions = positionsResult.getOrNull() ?: emptyList()

                    for (position in positions) {
                        sequence.add(
                            TrainingStep(
                                stepNumber = stepCounter++,
                                room = room.name,
                                position = position.name
                            )
                        )
                    }
                }

                if (sequence.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No hay habitaciones o posiciones configuradas"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sequence = sequence,
                        totalSteps = sequence.size
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error inesperado"
                )
            }
        }
    }

    fun startTraining() {
        val state = _uiState.value
        if (state.sequence.isEmpty()) return

        val sequenceJson = state.sequence.map { step ->
            mapOf("room" to step.room, "position" to step.position)
        }
        val startMessage = JSONObject().apply {
            put("type", "start_training")
            put("readings_per_position", 300)
            put("sequence", sequenceJson)
        }.toString()

        mqttManager.publish("training/start", startMessage)
        _isTrainingActive.value = true
        _uiState.value = state.copy(isTraining = true)
    }

    fun confirmStep() {
        mqttManager.publish("training/confirm", "{\"type\":\"confirm\"}")
    }

    fun cancelTraining() {
        mqttManager.publish("training/cancel", "{\"type\":\"cancel\"}")
        resetTraining()
    }

    private fun handleInstruction(payload: String) {
        try {
            val json = JSONObject(payload)
            if (json.optString("type") == "move_to") {
                val room = json.getString("room")
                val position = json.getString("position")
                val needed = json.getInt("readings_needed")
                viewModelScope.launch {
                    _instruction.emit("📍 Ve a $room - $position\n(Se requieren $needed lecturas)")
                }
            }
        } catch (e: Exception) {
            // ignorar
        }
    }

    private fun handleProgress(payload: String) {
        try {
            val json = JSONObject(payload)
            val current = json.getInt("current")
            val total = json.getInt("total")
            _progress.value = if (total > 0) (current * 100 / total) else 0
        } catch (e: Exception) {
            // ignorar
        }
    }

    private fun handleComplete() {
        resetTraining()
        _uiState.value = _uiState.value.copy(
            isTraining = false,
            trainingComplete = true
        )
    }

    private fun resetTraining() {
        _isTrainingActive.value = false
        _progress.value = 0
        _currentStepIndex.value = 0
    }

    fun nextStep() {
        val idx = _currentStepIndex.value + 1
        if (idx < _uiState.value.totalSteps) {
            _currentStepIndex.value = idx
        }
    }

    fun previousStep() {
        val idx = _currentStepIndex.value - 1
        if (idx >= 0) {
            _currentStepIndex.value = idx
        }
    }

    fun canGoNext(): Boolean = _currentStepIndex.value < _uiState.value.totalSteps - 1
    fun canGoPrevious(): Boolean = _currentStepIndex.value > 0
    fun isLastStep(): Boolean = _currentStepIndex.value == _uiState.value.totalSteps - 1
}