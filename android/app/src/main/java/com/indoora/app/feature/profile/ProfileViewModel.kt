package com.indoora.app.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.UserRead
import com.indoora.app.data.model.UserUpdate
import com.indoora.app.data.repository.AuthRepository
import com.indoora.app.feature.auth.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<UserRead>>(UiState.Idle)
    val userState: StateFlow<UiState<UserRead>> = _userState

    private val _updateState = MutableStateFlow<UiState<UserRead>>(UiState.Idle)
    val updateState: StateFlow<UiState<UserRead>> = _updateState

    fun loadCurrentUser() {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            val result = repository.getCurrentUser()
            _userState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Error al cargar usuario") }
            )
        }
    }

    fun updateUser(userId: Int, data: UserUpdate) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val result = repository.updateUser(userId, data)
            _updateState.value = result.fold(
                onSuccess = {
                    // Actualizar también userState con los nuevos datos
                    _userState.value = UiState.Success(it)
                    UiState.Success(it)
                },
                onFailure = { UiState.Error(it.message ?: "Error al actualizar") }
            )
        }
    }

    fun resetUpdateState() {
        _updateState.value = UiState.Idle
    }
}