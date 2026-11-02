package com.example.bankofsarah.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bankofsarah.BankOfSarahApp
import com.example.bankofsarah.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class LoginUiState(
    val customerNumber: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
) {
    val canSubmit: Boolean
        get() = customerNumber.length == 9 && password.isNotEmpty() && !isLoading
}

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCustomerNumberChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(9)
        _uiState.update { it.copy(customerNumber = digits, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun login() {
        val state = _uiState.value
        if (!state.canSubmit) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                authRepository.login(state.customerNumber.toInt(), state.password)
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: HttpException) {
                val message = if (e.code() == 401) {
                    "Invalid customer number or password"
                } else {
                    "Something went wrong. Please try again."
                }
                _uiState.update { it.copy(isLoading = false, error = message) }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Can't reach the server. Check your connection.")
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BankOfSarahApp
                LoginViewModel(app.container.authRepository)
            }
        }
    }
}