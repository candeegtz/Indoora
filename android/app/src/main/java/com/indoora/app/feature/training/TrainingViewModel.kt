package com.indoora.app.feature.training

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.EstadoConfig
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.network.mqtt.MqttManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class TrainingStep(
    val stepNumber: Int,
    val room: String,
    val position: String,
    val completed: Boolean = false
)

sealed class TrainingUiState {
    object Loading : TrainingUiState()
    data class Ready(
        val sequence: List<TrainingStep>,
        val totalSteps: Int
    ) : TrainingUiState()
    object Training : TrainingUiState()
    object TrainingComplete : TrainingUiState()
    object ModelTraining : TrainingUiState()
    object ModelReady : TrainingUiState()
    data class Error(val message: String) : TrainingUiState()
}

class TrainingViewModel(
    private val homeRepository: HomeRepository,
    private val mqttManager: MqttManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrainingUiState>(TrainingUiState.Loading)
    val uiState: StateFlow<TrainingUiState> = _uiState

    private val _isStepConfirmed = MutableStateFlow(false)
    val isStepConfirmed: StateFlow<Boolean> = _isStepConfirmed

    // Flujos separados para la UI (actualización directa y confiable)
    private val _instruction = MutableStateFlow("")
    val instruction: StateFlow<String> = _instruction

    private val _progressReadings = MutableStateFlow(0)
    val progressReadings: StateFlow<Int> = _progressReadings

    private val _progressTotal = MutableStateFlow(0)
    val progressTotal: StateFlow<Int> = _progressTotal

    // Mantenemos la variable por compatibilidad con la vista (para cancelaciones o salida manual)
    private val _navigateToHome = MutableStateFlow(false)
    val navigateToHome: StateFlow<Boolean> = _navigateToHome

    // Variables internas
    private var homeId: Int = -1
    private var isMqttReady = false
    private var currentStepIndex = 0
    private var totalSteps = 0
    private var sequenceBackup: List<TrainingStep> = emptyList()

    private var isModelAlreadyReady = false

    fun onMqttConnected() {
        isMqttReady = true
        mqttManager.subscribe("training/instruction")
        mqttManager.subscribe("training/progress")
        mqttManager.subscribe("training/complete")
        mqttManager.subscribe("training/model_ready")

        viewModelScope.launch {
            mqttManager.messages.collect { message ->
                when (message.topic) {
                    "training/instruction" -> handleInstruction(message.message)
                    "training/progress" -> handleProgress(message.message)
                    "training/complete" -> handleComplete()
                    "training/model_ready" -> handleModelReady()
                }
            }
        }
    }

    fun loadTrainingSequence(homeId: Int) {
        this.homeId = homeId
        viewModelScope.launch {
            _uiState.value = TrainingUiState.Loading
            try {
                val roomsResult = homeRepository.getRooms(homeId)
                if (roomsResult.isFailure) {
                    _uiState.value = TrainingUiState.Error(roomsResult.exceptionOrNull()?.message ?: "Error al cargar habitaciones")
                    return@launch
                }
                val rooms = roomsResult.getOrNull() ?: emptyList()
                val sequence = mutableListOf<TrainingStep>()
                var stepCounter = 1
                for (room in rooms) {
                    val positionsResult = homeRepository.getPositions(room.id)
                    if (positionsResult.isFailure) continue
                    val positions = positionsResult.getOrNull() ?: emptyList()
                    for (position in positions) {
                        sequence.add(TrainingStep(stepCounter++, room.name, position.name))
                    }
                }
                if (sequence.isEmpty()) {
                    _uiState.value = TrainingUiState.Error("No hay habitaciones o posiciones configuradas")
                } else {
                    totalSteps = sequence.size
                    sequenceBackup = sequence
                    _uiState.value = TrainingUiState.Ready(sequence, totalSteps)
                }
            } catch (e: Exception) {
                _uiState.value = TrainingUiState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun startTraining() {
        val state = _uiState.value
        if (state !is TrainingUiState.Ready) return
        if (!isMqttReady) {
            _uiState.value = TrainingUiState.Error("Esperando conexión MQTT...")
            return
        }

        val sequenceArray = JSONArray()
        state.sequence.forEach { step ->
            val obj = JSONObject().apply {
                put("room", step.room)
                put("position", step.position)
            }
            sequenceArray.put(obj)
        }
        val startMessage = JSONObject().apply {
            put("type", "start_training")
            put("sequence", sequenceArray)
        }.toString()

        mqttManager.publish("training/start", startMessage)

        currentStepIndex = 0
        _instruction.value = ""
        _progressReadings.value = 0
        _progressTotal.value = 0
        isModelAlreadyReady = false // Resetear la bandera

        _uiState.value = TrainingUiState.Training
    }

    fun confirmStep() {
        if (_uiState.value != TrainingUiState.Training) return
        if (_isStepConfirmed.value) return
        mqttManager.publish("training/confirm", "{\"type\":\"confirm\"}")
        _isStepConfirmed.value = true
    }

    fun cancelTraining() {
        mqttManager.publish("training/cancel", "{\"type\":\"cancel\"}")
        resetToReady()
    }

    private fun handleInstruction(payload: String) {
        try {
            val json = JSONObject(payload)
            if (json.optString("type") == "move_to") {
                val room = json.getString("room")
                val position = json.getString("position")
                val needed = json.getInt("readings_needed")
                val instructionText = "Ve a $room - $position\n(Se requieren $needed lecturas)"
                _instruction.value = instructionText
                _progressReadings.value = 0
                _isStepConfirmed.value = false
            }
        } catch (e: Exception) {
            Log.e("TrainingViewModel", "Error en handleInstruction", e)
        }
    }

    private fun handleProgress(payload: String) {
        try {
            val json = JSONObject(payload)
            val current = json.getInt("current")
            val total = json.getInt("total")
            val percent = if (total > 0) (current * 100 / total) else 0
            _progressReadings.value = percent

            if (current == total) {
                currentStepIndex++
                if (totalSteps > 0) {
                    val totalPercent = (currentStepIndex * 100 / totalSteps).coerceIn(0, 100)
                    _progressTotal.value = totalPercent
                }
            }
        } catch (e: Exception) {
            Log.e("TrainingViewModel", "Error en handleProgress", e)
        }
    }

    private fun handleComplete() {
        _uiState.value = TrainingUiState.TrainingComplete
        viewModelScope.launch {
            delay(1500)
            if (!isModelAlreadyReady) {
                _uiState.value = TrainingUiState.ModelTraining
            }
        }
    }

    private fun handleModelReady() {
        isModelAlreadyReady = true
        _uiState.value = TrainingUiState.ModelReady

        viewModelScope.launch {
            updateHomeToCompleted()
        }
    }

    private suspend fun updateHomeToCompleted() {
        val result = homeRepository.updateHomeConfigState(homeId, EstadoConfig.CONFIG_COMPLETED)
        if (result.isSuccess) {
            Log.d("TrainingViewModel", "Estado de la casa actualizado a CONFIG_COMPLETED")
        } else {
            Log.e("TrainingViewModel", "Error al actualizar estado: ${result.exceptionOrNull()?.message}")
        }
    }

    private fun resetToReady() {
        _uiState.value = TrainingUiState.Ready(sequenceBackup, totalSteps)
        _instruction.value = ""
        _progressReadings.value = 0
        _progressTotal.value = 0
        currentStepIndex = 0
        _isStepConfirmed.value = false
        isModelAlreadyReady = false
    }
}