package com.example.compose_curiosity_lab.draganddrop

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.compose_curiosity_lab.R

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
    DraggableLazyRow(
        items = personItems
    )
}

val personItems = listOf(
    PersonItem(
        nameAndSurname = "Jennifer Logan",
        photo = R.drawable.photo1,
        price = 60
    ),
    PersonItem(
        nameAndSurname = "Adam Barth",
        photo = R.drawable.photo2,
        price = 43
    ),
    PersonItem(
        nameAndSurname = "Sarah Hracek",
        photo = R.drawable.photo3,
        price = 18
    ),
    PersonItem(
        nameAndSurname = "Ian Hickson",
        photo = R.drawable.photo4,
        price = 74
    ),
    PersonItem(
        nameAndSurname = "Eric Seidel",
        photo = R.drawable.photo5,
        price = 74
    ),
    PersonItem(
        nameAndSurname = "Nolan Hudson",
        photo = R.drawable.photo6,
        price = 74
    ),
    PersonItem(
        nameAndSurname = "Noah Walker",
        photo = R.drawable.photo7,
        price = 74
    ),
)