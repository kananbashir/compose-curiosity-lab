package com.example.compose_curiosity_lab.splitthebill

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Created on 8/9/2024
 * @author Kanan Bashir
 */

data class TransactionItem(
    val id: Int,
    val transactionTitle: String,
    val transactionAmount: Double,
    var isChecked: MutableState<Boolean> = mutableStateOf(false)
)
