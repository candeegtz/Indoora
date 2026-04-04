package com.indoora.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.HomeRead
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.feature.auth.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow<UiState<HomeRead>>(UiState.Idle)
    val homeState: StateFlow<UiState<HomeRead>> = _homeState

    fun loadHome(homeId: Int) {
        viewModelScope.launch {
            _homeState.value = UiState.Loading
            val result = repository.getHome(homeId)
            _homeState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error loading home") }
            )
        }
    }
}