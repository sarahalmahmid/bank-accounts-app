package com.example.bankofsarah.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val customerNumber: Int,
    val password: String,
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val expiresIn: Int,
)