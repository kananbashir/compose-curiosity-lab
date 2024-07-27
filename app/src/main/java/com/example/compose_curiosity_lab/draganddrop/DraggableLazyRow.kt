package com.example.compose_curiosity_lab.draganddrop

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun <T: DraggableItem>DraggableLazyRow(
    modifier: Modifier = Modifier,
    items: List<T>
) {

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {

        }
    }
}

abstract class DraggableItem() {

}

data class PersonItem(
    val nameAndSurname: String,
    @DrawableRes val photo: Int,
    val price: Int
): DraggableItem()

@Preview (showBackground = true)
@Composable
private fun DraggableLazyRowPreview() {
    DraggableLazyRow()
}