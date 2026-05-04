package com.indoora.app.feature.deviceconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.EstadoConfig
import com.indoora.app.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DeviceConfigViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep

    val totalSteps = DeviceConfigSteps.steps.size

    private val _event = MutableSharedFlow<DeviceConfigEvent>()
    val event = _event.asSharedFlow()

    fun nextStep() {
        if (_currentStep.value < totalSteps - 1) {
            _currentStep.value++
        }
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value--
        }
    }

    fun resetSteps() {
        _currentStep.value = 0
    }

    fun canGoNext() = _currentStep.value < totalSteps - 1
    fun canGoPrevious() = _currentStep.value > 0
    fun isLastStep() = _currentStep.value == totalSteps - 1

    fun finishConfiguration(homeId: Int) {
        viewModelScope.launch {
            try {
                val result = homeRepository.updateHomeConfigState(homeId, EstadoConfig.ONLY_DEVICES_CONFIG)
                if (result.isSuccess) {
                    _event.emit(DeviceConfigEvent.ConfigurationFinished)
                } else {
                    _event.emit(DeviceConfigEvent.Error("No se pudo actualizar el estado"))
                }
            } catch (e: Exception) {
                _event.emit(DeviceConfigEvent.Error(e.message ?: "Error inesperado"))
            }
        }
    }
}

sealed class DeviceConfigEvent {
    object ConfigurationFinished : DeviceConfigEvent()
    data class Error(val message: String) : DeviceConfigEvent()
}