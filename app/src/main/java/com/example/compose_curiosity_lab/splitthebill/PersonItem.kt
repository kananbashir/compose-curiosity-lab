package com.example.compose_curiosity_lab.splitthebill

import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.LayoutCoordinates

/**
 * Created on 8/9/2024
 * @author Kanan Bashir
 */

data class PersonItem(
    val id: Int,
    val name: String,
    val surname: String,
    @DrawableRes val photo: Int,
    var requestAmount: MutableState<Double> = mutableDoubleStateOf(0.0),
    var requestAmountCount: MutableState<Int> = mutableIntStateOf(0),
    var itemBubbleLayoutCoordinates: LayoutCoordinates? = null,
)
