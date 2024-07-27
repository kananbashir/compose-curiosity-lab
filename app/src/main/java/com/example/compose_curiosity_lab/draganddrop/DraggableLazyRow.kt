package com.example.compose_curiosity_lab.draganddrop

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose_curiosity_lab.R

@Composable
fun <T: DraggableItem>DraggableLazyRow(
    modifier: Modifier = Modifier,
    items: List<T>,
    draggableItemContent: @Composable (item: T) -> Unit
) {
    val rowItems = remember { items.toMutableStateList() }
    var pickedItem: T? by remember { mutableStateOf(null) }

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(rowItems, key = {_, item -> item.key}) { index, item ->
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    pickedItem = item
                                },

                                onDrag = { _, dragAmount ->

                                },

                                onDragEnd = {
                                    pickedItem = null
                                }
                            )
                        }
                ) {
                    draggableItemContent(item)
                }
            }
        }
    }
}

abstract class DraggableItem(val key: Any) {

}

data class PersonItem(
    val nameAndSurname: String,
    @DrawableRes val photo: Int,
    val price: Int
): DraggableItem(nameAndSurname)

@Preview (showBackground = true)
@Composable
private fun DraggableLazyRowPreview() {
    DraggableLazyRow(
        items = personItems,
        draggableItemContent = { item ->
            MyDraggableItem(
                name = item.nameAndSurname,
                photo = item.photo
            )
        }
    )
}


//Preview related things
@Composable
fun MyDraggableItem(
    modifier: Modifier = Modifier,
    name: String,
    photo: Int
) {
    Box(
        modifier = modifier
            .size(100.dp, 140.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.LightGray,
                    cornerRadius = CornerRadius(40f, 40f)
                )
            }
            .padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .size(40.dp, 40.dp)
                    .clip(CircleShape),
                painter = painterResource(id = photo),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
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