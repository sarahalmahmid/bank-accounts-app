package com.example.bankofsarah

import android.content.Context
import com.example.bankofsarah.data.AccountsRepository
import com.example.bankofsarah.data.AuthRepository
import com.example.bankofsarah.data.local.TokenStore
import com.example.bankofsarah.data.remote.createApiService

class AppContainer(context: Context) {
    val tokenStore = TokenStore(context)
    private val apiService = createApiService(tokenStore)
    val authRepository = AuthRepository(apiService, tokenStore)
    val accountsRepository = AccountsRepository(apiService)
}