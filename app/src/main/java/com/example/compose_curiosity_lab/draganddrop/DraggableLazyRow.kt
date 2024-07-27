package com.example.compose_curiosity_lab.draganddrop

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose_curiosity_lab.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T: DraggableItem>DraggableLazyRow(
    modifier: Modifier = Modifier,
    items: List<T>,
    draggableItemContent: @Composable (item: T) -> Unit
) {
    val rowItems = remember { items.toMutableStateList() }
    var pickedItem: T? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    var isItemPicked by remember { mutableStateOf(false) }
    var rowScrollEnabled by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(rowItems, key = {_, item -> item.key}) { index, item ->
                val paddingOffset = animateFloatAsState(
                    targetValue = when {
                        !isItemPicked -> 0f
                        index > rowItems.indexOf(pickedItem) -> 100f
                        else -> -100f
                    },
                    label = ""
                )
                var itemAlpha by remember { mutableFloatStateOf(1f) }

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = itemAlpha
                            if (pickedItem != item) translationX = paddingOffset.value
                        }
                        .animateItemPlacement(tween(easing = LinearOutSlowInEasing, durationMillis = 500))
                        .onGloballyPositioned { coordinates ->
                            item.startPosition = coordinates.positionInRoot()
                        }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    scope.launch {
                                        if (pickedItem == null) {
                                            pickedItem = item
                                            isItemPicked = true
                                            item.itemOverlayDragPosition.snapTo(item.startPosition)
                                            itemAlpha = 0f
                                            rowScrollEnabled = false
                                        }
                                    }
                                },

                                onDrag = { _, dragAmount ->
                                    if (pickedItem == item) {
                                        scope.launch {
                                            item.itemOverlayDragPosition.snapTo(item.itemOverlayDragPosition.value + dragAmount)
                                        }
                                    }
                                },

                                onDragEnd = {
                                    scope.launch {
                                        if (pickedItem == item) {
                                            isItemPicked = false
                                            item.itemOverlayDragPosition.animateWithResult(
                                                targetOffset = item.startPosition,
                                                animationSpec = tween(
                                                    easing = FastOutSlowInEasing,
                                                    durationMillis = 500
                                                ),
                                                onAnimationEnd = {
                                                    rowScrollEnabled = false
                                                    pickedItem = null
                                                }
                                            )
                                            itemAlpha = 1f
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    draggableItemContent(item)
                }
            }
        }
    }


    //If an item is picked we assign this item to pickedItem on onDragStart
    // and we compose the item overlay in order to be able to drag it globally.
    pickedItem?.let {
        ItemOverlay(
            item = it,
            draggableItemContent = { draggableItemContent(it) }
        )
    }
}


//We cannot just drag an item from row itself, that is because it has limited bounds.
//We need to create an item overlay to be able to drag the picked item globally.
@Composable
private fun ItemOverlay(
    item: DraggableItem,
    modifier: Modifier = Modifier,
    draggableItemContent: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    item.itemOverlayDragPosition.value.x.roundToInt(),
                    item.itemOverlayDragPosition.value.y.roundToInt()
                )
            }
    ) {
        draggableItemContent()
    }
}


//Extension function when we need to do something only if the animation has been finished.
private suspend fun <T, V: AnimationVector> Animatable<T, V>.animateWithResult(
    targetOffset: T,
    animationSpec: AnimationSpec<T>,
    onAnimationEnd: () -> Unit
) {
    when (animateTo(targetOffset, animationSpec).endReason) {
        AnimationEndReason.Finished -> { onAnimationEnd() }
        else -> {}
    }
}

abstract class DraggableItem(val key: Any) {
    //We want the dragged item to return its start position if it is not
    // in drop bounds.
    var startPosition: Offset = Offset.Zero
    val itemOverlayDragPosition: Animatable<Offset, AnimationVector2D> = Animatable(Offset.Zero, Offset.VectorConverter)
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