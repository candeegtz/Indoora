package com.indoora.app.feature.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.network.mqtt.MqttManager

class TrainingViewModelFactory(
    private val homeRepository: HomeRepository,
    private val mqttManager: MqttManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainingViewModel(homeRepository, mqttManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}