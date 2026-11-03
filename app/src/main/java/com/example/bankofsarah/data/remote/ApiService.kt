package com.example.bankofsarah.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/accounts")
    suspend fun getAccounts(): List<AccountDto>

    @GET("api/accounts/{accountNumber}")
    suspend fun getAccount(@Path("accountNumber") accountNumber: String): AccountDto

    @PATCH("api/accounts/{accountNumber}")
    suspend fun updateNickname(
        @Path("accountNumber") accountNumber: String,
        @Body body: UpdateNicknameRequest,
    ): AccountDto
}
