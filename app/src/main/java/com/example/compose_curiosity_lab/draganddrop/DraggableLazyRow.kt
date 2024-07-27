package com.example.compose_curiosity_lab.draganddrop

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compose_curiosity_lab.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T: DraggableItem>DraggableLazyRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal =
        if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    draggableItemContent: @Composable (item: T) -> Unit,
    droppableItemContent: @Composable (item: T) -> Unit
) {
    val rowItems = remember { items.toMutableStateList() }
    val columnItems = remember { listOf(items[0]).toMutableStateList() }
    var pickedItem: T? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    var isItemPicked by remember { mutableStateOf(false) }
    var isInDropBounds by remember { mutableStateOf(false) }
    var rowScrollEnabled by remember { mutableStateOf(true) }
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = remember { with(density) { config.screenHeightDp.toDp().toPx() } }
    val screenWidth = remember { with(density) { config.screenWidthDp.toDp().toPx() } }
    var columnHeight: Float? = remember { null }
    var isItemDropped: Boolean by remember { mutableStateOf(false) }

    //We assign the height of the draggable item to make sure that the column items sliding down height
    // will be bigger that the draggable item's height.
    var draggableItemHeight: Float? by remember { mutableStateOf(null) }

    //When the picket item is in drop bounds we add an overlay item to the column. If the picked
    // item has been moved away from drop bounds we slide the other items to hide this overlay item instead of
    // deleting it and adding new item again if it is in drop bounds again. So, we need to define the
    // height of the droppable item in order to slide to correct position.
    var droppableItemHeight: Float? by remember { mutableStateOf(null) }

    //The offset of the first item in the column.
    var dropOffset: Offset? by remember { mutableStateOf(null) }
    val columnItemSpace = remember { Animatable(0f) }
    var columnScrollEnabled by remember { mutableStateOf(true) }
    val columnState = rememberLazyListState()

    LaunchedEffect(key1 = isInDropBounds) {
        pickedItem?.let {
            draggableItemHeight?.let {
                droppableItemHeight?.let {
                    val targetPosition = when {
                        isInDropBounds -> {
                            columnItems.safeAddToFirstIndex(pickedItem!!)
                            (draggableItemHeight!! - droppableItemHeight!!) + 20f
                        }
                        isItemDropped -> 0f
                        else -> -droppableItemHeight!!
                    }

                    columnItemSpace.animateTo(targetPosition, tween(
                        easing = CubicBezierEasing(0.07f, 0.07f, 0.07f, 0.96f),
                        durationMillis = 300))
                }
            }

        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            userScrollEnabled = rowScrollEnabled,
            flingBehavior = flingBehavior
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
                            if (index == 0 && draggableItemHeight == null) {
                                draggableItemHeight = with(density) { coordinates.size.height.toDp().toPx() }
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    scope.launch {
                                        if (pickedItem == null) {
                                            pickedItem = item
                                            isItemPicked = true
                                            isItemDropped = false
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
                                            columnHeight?.let { isInDropBounds = item.itemOverlayDragPosition.value.y > it }
                                        }
                                    }
                                },

                                onDragEnd = {
                                    scope.launch {
                                        if (pickedItem == item) {
                                            if (isInDropBounds) {
                                                columnState.animateScrollToItem(0)
                                                columnScrollEnabled = false
                                                item.itemOverlayDragPosition.animateWithResult(
                                                    targetOffset = dropOffset ?: Offset.Zero,
                                                    animationSpec = tween(600),
                                                    onAnimationEnd = {
                                                        isItemDropped = true
                                                        isInDropBounds = false
                                                    }
                                                )
                                                isItemPicked = false
                                                rowItems.remove(item)
                                            } else {
                                                isItemPicked = false
                                                item.itemOverlayDragPosition.animateWithResult(
                                                    targetOffset = item.startPosition,
                                                    animationSpec = tween(
                                                        easing = FastOutSlowInEasing,
                                                        durationMillis = 500
                                                    ),
                                                    onAnimationEnd = {
                                                        rowScrollEnabled = true
                                                        pickedItem = null
                                                    }
                                                )
                                                itemAlpha = 1f
                                            }
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    if (columnHeight == null) {
                        columnHeight = screenHeight - (screenHeight - coordinates.size.height)
                    }
                }
                .padding(20.dp)
                .drawBehind {
                    drawRoundRect(
                        color = if (isInDropBounds) Color.Cyan else Color.Gray,
                        style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 10f)),
                        cornerRadius = CornerRadius(40f, 40f)
                    )
                },
            userScrollEnabled = columnScrollEnabled,
            state = columnState,
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(columnItems.toList(), key = { _, item -> item.key }) { index, dropItem ->
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = dropItem.droppedItemAlpha.value
                            if (index > 0) translationY = columnItemSpace.value
                        }
                        .onGloballyPositioned { coordinates ->
                            if (index == 0 && dropOffset == null && droppableItemHeight == null) {
                                //I am getting the offset on the initial launch of the app.
                                dropOffset = Offset(
                                    x = screenWidth + 19, //I don't know why but I have to add 19 in order to get the center. I'll fix it later :)
                                    y = coordinates.positionInRoot().y
                                )
                                droppableItemHeight = with(density) { coordinates.size.height.toDp().toPx() }
                                columnItems.safeRemoveAt(0)
                            }
                        }
                        .animateItemPlacement()
                ) {
                    droppableItemContent(dropItem)
                }
            }
        }
    }


    //If an item is picked we assign this item to pickedItem on onDragStart
    // and we compose the item overlay in order to be able to drag it globally.
    pickedItem?.let {
        ItemOverlay(
            item = it,
            isItemDropped = isItemDropped,
            inDropBoundsItemPadding = 30.dp,
            onTransitionAnimationFinished = {
                columnItems[0].droppedItemAlpha.value = 1f
                rowScrollEnabled = true
                pickedItem = null
                columnScrollEnabled = true
            },
            draggableItemContent = { draggableItemContent(it) },
            droppableItemContent = { droppableItemContent(it) }
        )
    }
}


//We cannot just drag an item from row itself, that is because it has limited bounds.
//We need to create an item overlay to be able to drag the picked item globally.
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ItemOverlay(
    item: DraggableItem,
    modifier: Modifier = Modifier,
    isItemDropped: Boolean,
    inDropBoundsItemPadding: Dp,
    onTransitionAnimationFinished: () -> Unit,
    draggableItemContent: @Composable () -> Unit,
    droppableItemContent: @Composable () -> Unit
) {
    var sharedTransitionAnimEndCounter = remember { 0 }

    SharedTransitionLayout(
        modifier = modifier
            .offset {
                IntOffset(
                    item.itemOverlayDragPosition.value.x.roundToInt(),
                    item.itemOverlayDragPosition.value.y.roundToInt()
                )
            }
    ) {
        AnimatedContent(
            targetState = isItemDropped,
            label = ""
        ) { state ->

            if (transition.animations.isNotEmpty()) {
                val targetValue: String = transition.animations.first().animation.targetValue.toString()
                if (targetValue == "1.0") {
                    sharedTransitionAnimEndCounter++

                    //When the target value we get from "transition" hits 1.0 twice that means
                    // that the transition animation has been finished. I didn't find another way to
                    // check the animation state.
                    if (sharedTransitionAnimEndCounter == 2) onTransitionAnimationFinished()
                }
            }

            when (state) {
                true -> {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = inDropBoundsItemPadding)
                            .offset { IntOffset(-430, 0) } //Again, I'll fix this hardcoded offset later :)
                            .sharedElement(
                                rememberSharedContentState(key = item.key),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = { _, _ ->
                                    spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessVeryLow
                                    )
                                }
                            )
                    ) {
                        droppableItemContent()
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .sharedElement(
                                rememberSharedContentState(key = item.key),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = { _, _ ->
                                    spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessVeryLow
                                    )
                                }
                            )
                    ) {
                        draggableItemContent()
                    }
                }
            }
        }
    }
}


//To safely remove item if the list is not empty
private fun <T: DraggableItem> SnapshotStateList<T>.safeRemoveAt(index: Int) { if (isNotEmpty()) removeAt(index) }

//Extension function to safely add an item to list if the first element is not the same with it.
private fun <T: DraggableItem>SnapshotStateList<T>.safeAddToFirstIndex(item: T): Boolean {
    return if (item !in this) {
        if (safeGetFirst()?.droppedItemAlpha?.value == 0f) set(0, item) else add(0, item)
        true
    } else {
        false
    }
}

//Extension function to get the first element of the list if it is not empty
private fun <T: DraggableItem>SnapshotStateList<T>.safeGetFirst(): T? { return if (isNotEmpty()) first() else null }

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
    var droppedItemAlpha: MutableState<Float> = mutableFloatStateOf(0f)
}







//-------------------------------- Preview related things --------------------------------
@Preview (showBackground = true)
@Composable
private fun DraggableLazyRowPreview() {
    DraggableLazyRow(
        items = personItems,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        draggableItemContent = { item ->
            MyDraggableItem(
                name = item.nameAndSurname,
                photo = item.photo
            )
        },
        droppableItemContent = { item ->
            MyDroppableItem(
                name = item.nameAndSurname,
                price = item.price,
                photo = item.photo
            )
        }
    )
}

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

@Composable
fun MyDroppableItem(
    modifier: Modifier = Modifier,
    name: String,
    price: Int,
    photo: Int
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Color.LightGray,
                    cornerRadius = CornerRadius(40f, 40f)
                )
            }
            .padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(30.dp, 30.dp)
                    .clip(CircleShape),
                painter = painterResource(id = photo),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                modifier = Modifier
                    .weight(0.5f),
                text = name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.weight(1.4f))

            Text(
                modifier = Modifier
                    .weight(0.3f),
                text = "$",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.Gray)
            )

            Text(
                modifier = Modifier
                    .weight(0.3f),
                text = price.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )
        }
    }
}

data class PersonItem(
    val nameAndSurname: String,
    @DrawableRes val photo: Int,
    val price: Int
): DraggableItem(nameAndSurname)

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