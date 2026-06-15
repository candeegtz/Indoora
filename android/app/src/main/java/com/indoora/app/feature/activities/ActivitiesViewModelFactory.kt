package com.indoora.app.feature.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.indoora.app.data.repository.ActivityRepository
import com.indoora.app.data.repository.HomeRepository

class ActivitiesViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val homeRepository: HomeRepository,
    private val homeId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivitiesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivitiesViewModel(activityRepository, homeRepository, homeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}