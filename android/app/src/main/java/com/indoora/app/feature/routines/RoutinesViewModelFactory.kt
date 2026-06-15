package com.indoora.app.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.indoora.app.data.repository.RoutineRepository

class RoutinesViewModelFactory(
    private val routineRepository: RoutineRepository,
    private val homeId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutinesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoutinesViewModel(routineRepository, homeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}