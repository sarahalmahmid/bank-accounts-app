package com.example.bankofsarah.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val accountNumber: String,
    // Double is used for display only. A production app would send the balance
    // as a decimal string and parse it into BigDecimal to keep money exact.
    val balance: Double,
    val nickname: String? = null,
)