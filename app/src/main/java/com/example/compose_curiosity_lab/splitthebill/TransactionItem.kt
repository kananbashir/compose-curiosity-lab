package com.example.compose_curiosity_lab.splitthebill

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Created on 8/9/2024
 * @author Kanan Bashir
 */

data class TransactionItem(
    val id: Int,
    val transactionTitle: String,
    val transactionAmount: Double,
    var isChecked: MutableState<Boolean> = mutableStateOf(false),
    var isPicked: MutableState<Boolean> = mutableStateOf(false),
    var itemAlpha: MutableState<Float> = mutableFloatStateOf(1f),
    var itemPositionInFlow: Offset? = null,
    var dragOffset: State<Offset> = mutableStateOf(itemPositionInFlow ?: Offset.Zero),
    var parentScale: State<Float> = mutableFloatStateOf(1f),
    var shadowAlpha: State<Float> = mutableFloatStateOf(0f),
    var overlayItemRotation: State<Float> = mutableFloatStateOf(0f),
    var itemBorderSize: State<Dp> = mutableStateOf(0.dp),
    var itemLayoutCoordinates: LayoutCoordinates? = null
)
