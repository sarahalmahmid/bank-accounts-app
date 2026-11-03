package com.example.bankofsarah.data

import com.example.bankofsarah.data.remote.AccountDto
import com.example.bankofsarah.data.remote.ApiService
import com.example.bankofsarah.data.remote.UpdateNicknameRequest

class AccountsRepository(
    private val api: ApiService,
) {
    suspend fun getAccounts(): List<AccountDto> = api.getAccounts()
    suspend fun getAccount(accountNumber: String): AccountDto =
        api.getAccount(accountNumber)

    suspend fun updateNickname(accountNumber: String, nickname: String): AccountDto =
        api.updateNickname(accountNumber, UpdateNicknameRequest(nickname.trim().ifEmpty { null }))
}
