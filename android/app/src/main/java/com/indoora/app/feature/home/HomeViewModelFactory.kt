package com.indoora.app.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.network.mqtt.MqttManager

class HomeViewModelFactory(
    private val homeRepository: HomeRepository,
    private val mqttManager: MqttManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(homeRepository, mqttManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}