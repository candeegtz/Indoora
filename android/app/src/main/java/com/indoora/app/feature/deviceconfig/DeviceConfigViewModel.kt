package com.indoora.app.feature.deviceconfig

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DeviceConfigViewModel : ViewModel() {

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep

    val totalSteps = DeviceConfigSteps.steps.size

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
}