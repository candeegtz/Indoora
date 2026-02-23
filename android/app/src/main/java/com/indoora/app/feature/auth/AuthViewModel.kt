package com.indoora.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indoora.app.data.model.LoginResponse
import com.indoora.app.data.model.UserCreate
import com.indoora.app.data.model.UserRead
import com.indoora.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<LoginResponse>> = _loginState

    private val _registerState = MutableStateFlow<UiState<UserRead>>(UiState.Idle)
    val registerState: StateFlow<UiState<UserRead>> = _registerState

    private val _registerAndLoginState = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val registerAndLoginState: StateFlow<UiState<LoginResponse>> = _registerAndLoginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = repository.login(username, password)
            _loginState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun register(data: UserCreate) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            val result = repository.register(data)
            _registerState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun registerAndLogin(data: UserCreate) {
        viewModelScope.launch {
            _registerAndLoginState.value = UiState.Loading

            // Primero registrar
            val registerResult = repository.register(data)

            if (registerResult.isSuccess) {
                val loginResult = repository.login(data.username, data.password)
                _registerAndLoginState.value = loginResult.fold(
                    onSuccess = { UiState.Success(it) },
                    onFailure = { UiState.Error(it.message ?: "Login failed after registration") }
                )
            } else {
                _registerAndLoginState.value = UiState.Error(
                    registerResult.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = UiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = UiState.Idle
        _registerAndLoginState.value = UiState.Idle
    }
}

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}