package com.example.compose_curiosity_lab.splitthebill

import androidx.annotation.DrawableRes

/**
 * Created on 8/9/2024
 * @author Kanan Bashir
 */

data class PersonItem(
    val id: Int,
    val name: String,
    val surname: String,
    @DrawableRes val photo: Int
)
