package com.example.bankofsarah.ui.common

import java.text.NumberFormat
import java.util.Locale

fun formatBalance(balance: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 3
        maximumFractionDigits = 3
    }
    return "${format.format(balance)} KWD"
}