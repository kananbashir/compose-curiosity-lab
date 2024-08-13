package com.example.compose_curiosity_lab.splitthebill

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset

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
    var itemPositionInFlow: Offset? = null,
    var dragOffset: Animatable<Offset, AnimationVector2D> = Animatable(Offset.Zero, Offset.VectorConverter),
    var parentScale: Animatable<Float, AnimationVector1D> = Animatable(1f),
    var shadowAlpha: Animatable<Float, AnimationVector1D> = Animatable(0f),
)
