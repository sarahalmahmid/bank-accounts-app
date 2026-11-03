package com.example.bankofsarah.data

import com.example.bankofsarah.data.remote.AccountDto
import com.example.bankofsarah.data.remote.ApiService

class AccountsRepository(
    private val api: ApiService,
) {
    suspend fun getAccounts(): List<AccountDto> = api.getAccounts()
}