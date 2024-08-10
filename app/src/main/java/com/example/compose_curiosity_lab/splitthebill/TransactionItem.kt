package com.example.compose_curiosity_lab.splitthebill

import androidx.annotation.DrawableRes

/**
 * Created on 8/9/2024
 * @author Kanan Bashir
 */

data class TransactionItem(
    val id: Int,
    val transactionTitle: String,
    val transactionAmount: Double,
    val isChecked: Boolean
)
