package com.example.bankofsarah.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bankofsarah.BankOfSarahApp
import com.example.bankofsarah.data.AccountsRepository
import com.example.bankofsarah.data.AuthRepository
import com.example.bankofsarah.data.remote.AccountDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface AccountsUiState {
    data object Loading : AccountsUiState
    data class Success(val accounts: List<AccountDto>) : AccountsUiState
    data class Error(val message: String) : AccountsUiState
}

class AccountsViewModel(
    private val accountsRepository: AccountsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountsUiState>(AccountsUiState.Loading)
    val uiState: StateFlow<AccountsUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        _uiState.value = AccountsUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                AccountsUiState.Success(accountsRepository.getAccounts())
            } catch (e: HttpException) {
                AccountsUiState.Error("Couldn't load your accounts. Please try again.")
            } catch (e: IOException) {
                AccountsUiState.Error("Can't reach the server. Check your connection.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BankOfSarahApp
                AccountsViewModel(app.container.accountsRepository, app.container.authRepository)
            }
        }
    }
}