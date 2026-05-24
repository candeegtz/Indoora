package com.indoora.app.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.AlertResponse
import com.indoora.app.data.model.EstadoConfig
import com.indoora.app.data.model.HomeRead
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.feature.auth.UiState
import com.indoora.app.network.mqtt.MqttManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class HomeViewModel(
    private val homeRepository: HomeRepository,
    private val mqttManager: MqttManager,
    private val context: Context
) : ViewModel() {

    private val _homeState = MutableStateFlow<UiState<HomeRead>>(UiState.Loading)
    val homeState: StateFlow<UiState<HomeRead>> = _homeState

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger

    // Lista de alertas no leídas
    private val _unreadAlerts = MutableStateFlow<List<AlertResponse>>(emptyList())
    val unreadAlerts: StateFlow<List<AlertResponse>> = _unreadAlerts

    private var isMotorActivated = false
    private var currentHomeId: Int = -1

    private var isSessionActive = true

    fun loadHome(homeId: Int) {
        viewModelScope.launch {
            _homeState.value = UiState.Loading
            val result = homeRepository.getHome(homeId)
            _homeState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()!!)
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun clearSession() {
        _homeState.value = UiState.Idle
        _unreadAlerts.value = emptyList()
    }

    fun refreshHome() {
        _refreshTrigger.value += 1
    }

    fun checkAndActivateMotor(homeId: Int) {
        currentHomeId = homeId
        viewModelScope.launch {
            homeState.collect { state ->
                if (state is UiState.Success && state.data.estadoConfig == EstadoConfig.CONFIG_COMPLETED && !isMotorActivated) {
                    isMotorActivated = true
                    publishHouseConfigToMotor(homeId)
                    fetchUnreadAlerts(homeId)   // Cargar alertas al activar
                } else if (state is UiState.Success && state.data.estadoConfig != EstadoConfig.CONFIG_COMPLETED && isMotorActivated) {
                    isMotorActivated = false
                    _unreadAlerts.value = emptyList()
                }
            }
        }
    }

    suspend fun publishHouseConfigToMotor(homeId: Int) {
        val result = homeRepository.getRoomsAndPositions(homeId)
        if (result.isSuccess) {
            val config = result.getOrNull() ?: emptyMap()
            val roomsJson = JSONObject()
            config.forEach { (room, positions) ->
                roomsJson.put(room, JSONArray(positions))
            }
            val payload = JSONObject().apply {
                put("home_id", homeId)
                put("rooms_positions", roomsJson)
            }.toString()
            println("Payload a enviar: $payload")
            mqttManager.publish("motor/config", payload)
        } else {
            //TODO: Manejar error 
        }
    }

    private fun fetchUnreadAlerts(homeId: Int) {
        viewModelScope.launch {
            val result = homeRepository.getUnreadAlerts(homeId)
            if (result.isSuccess) {
                _unreadAlerts.value = result.getOrNull() ?: emptyList()
            } else {
                println("Error fetching unread alerts: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    // Marcar una alerta como leída y eliminarla de la lista local
    fun dismissAlert(alertId: Int) {
        viewModelScope.launch {
            homeRepository.markAlertAsRead(alertId)
            _unreadAlerts.value = _unreadAlerts.value.filter { it.id != alertId }
        }
    }

    fun refreshAlerts() {
        if (currentHomeId != -1) {
            fetchUnreadAlerts(currentHomeId)
        }
    }
}