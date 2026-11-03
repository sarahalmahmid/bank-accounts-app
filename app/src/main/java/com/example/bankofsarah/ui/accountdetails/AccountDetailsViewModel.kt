package com.example.bankofsarah.ui.accountdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.bankofsarah.BankOfSarahApp
import com.example.bankofsarah.data.AccountsRepository
import com.example.bankofsarah.data.remote.AccountDto
import com.example.bankofsarah.ui.navigation.AccountDetailsRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

const val MAX_NICKNAME_LENGTH = 255

data class NicknameEditorState(
    val text: String,
    val isSaving: Boolean = false,
    val error: String? = null,
)

data class AccountDetailsUiState(
    val isLoading: Boolean = true,
    val account: AccountDto? = null,
    val loadError: String? = null,
    val editor: NicknameEditorState? = null,
)

class AccountDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountsRepository: AccountsRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AccountDetailsRoute>()
    private var openEditorAfterLoad = route.openEditor

    private val _uiState = MutableStateFlow(AccountDetailsUiState())
    val uiState: StateFlow<AccountDetailsUiState> = _uiState.asStateFlow()

    init {
        loadAccount()
    }

    fun loadAccount() {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val account = accountsRepository.getAccount(route.accountNumber)
                _uiState.update { it.copy(isLoading = false, account = account) }
                if (openEditorAfterLoad) {
                    openEditorAfterLoad = false
                    openEditor()
                }
            } catch (e: HttpException) {
                val message = if (e.code() == 404) {
                    "This account couldn't be found."
                } else {
                    "Couldn't load this account. Please try again."
                }
                _uiState.update { it.copy(isLoading = false, loadError = message) }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(isLoading = false, loadError = "Can't reach the server. Check your connection.")
                }
            }
        }
    }

    fun openEditor() {
        val account = _uiState.value.account ?: return
        _uiState.update {
            it.copy(editor = NicknameEditorState(text = account.nickname.orEmpty()))
        }
    }

    fun onNicknameChange(text: String) {
        _uiState.update { state ->
            state.copy(
                editor = state.editor?.copy(text = text.take(MAX_NICKNAME_LENGTH), error = null),
            )
        }
    }

    fun dismissEditor() {
        if (_uiState.value.editor?.isSaving == true) return
        _uiState.update { it.copy(editor = null) }
    }

    fun saveNickname() {
        val editor = _uiState.value.editor ?: return
        if (editor.isSaving) return

        _uiState.update { it.copy(editor = editor.copy(isSaving = true, error = null)) }

        viewModelScope.launch {
            try {
                val updated = accountsRepository.updateNickname(route.accountNumber, editor.text)
                _uiState.update { it.copy(account = updated, editor = null) }
            } catch (e: HttpException) {
                showEditorError("Couldn't save the nickname. Please try again.")
            } catch (e: IOException) {
                showEditorError("Can't reach the server. Check your connection.")
            }
        }
    }

    private fun showEditorError(message: String) {
        _uiState.update { state ->
            state.copy(editor = state.editor?.copy(isSaving = false, error = message))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BankOfSarahApp
                AccountDetailsViewModel(createSavedStateHandle(), app.container.accountsRepository)
            }
        }
    }
}