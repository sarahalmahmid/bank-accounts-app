package com.example.bankofsarah

import android.content.Context
import com.example.bankofsarah.data.AuthRepository
import com.example.bankofsarah.data.local.TokenStore
import com.example.bankofsarah.data.remote.createApiService

class AppContainer(context: Context) {
    private val apiService = createApiService()
    val tokenStore = TokenStore(context)
    val authRepository = AuthRepository(apiService, tokenStore)
}