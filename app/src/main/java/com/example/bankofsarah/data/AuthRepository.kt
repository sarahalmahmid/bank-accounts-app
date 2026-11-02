package com.example.bankofsarah.data

import com.example.bankofsarah.data.local.TokenStore
import com.example.bankofsarah.data.remote.ApiService
import com.example.bankofsarah.data.remote.LoginRequest

class AuthRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) {
    suspend fun login(customerNumber: Int, password: String) {
        val response = api.login(LoginRequest(customerNumber, password))
        tokenStore.saveToken(response.accessToken)
    }

    suspend fun logout() {
        tokenStore.clearToken()
    }
}