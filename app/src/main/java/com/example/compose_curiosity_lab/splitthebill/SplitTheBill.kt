package com.example.compose_curiosity_lab.splitthebill

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toIntRect
import androidx.core.graphics.toColorInt
import com.example.compose_curiosity_lab.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Created on 8/9/2024
 * @author Kanan Bashir
 */

const val PICKING_UP_ANIM_TRANSITION_DURATION = 300
const val TRANSACTION_ITEM_SCALE_ANIM_DURATION = 100
const val PERSON_ITEM_SCALE_ANIM_DURATION = 800

class ScreenState {
    //If it is not null, that means the drag is started.
    var pickedItemGlobalOffset by mutableStateOf<Offset?>(null)
    var globalDragOffset by mutableStateOf(Animatable(Offset.Zero, Offset.VectorConverter))
    var isDragStarted by mutableStateOf(false)
    var isDraggingCancelled by mutableStateOf(false)
    var isInDropBounds by mutableStateOf(false)
    var boxSize: IntSize? = null
    var selectedPersonItemKey by mutableStateOf<Int?>(null)
    var lazyGridLayoutCoordinates by mutableStateOf<LayoutCoordinates?>(null)
    var flowRowLayoutCoordinates by mutableStateOf<LayoutCoordinates?>(null)

    fun findItemAtOffset(lazyGridState: LazyGridState, hitOffset: Offset) {
        lazyGridLayoutCoordinates?.let {
            flowRowLayoutCoordinates?.let {
                //We are using the grid's coordinates to convert hit offset relative to source (flow row, in this case) to an offset
                // relative to the grid.
                val localHitOffset = lazyGridLayoutCoordinates!!.localPositionOf(flowRowLayoutCoordinates!!, hitOffset)
                val lazyGridItemInfo = lazyGridState.layoutInfo.visibleItemsInfo.find { itemInfo ->
                    itemInfo.size.toIntRect().contains(localHitOffset.round() - itemInfo.offset)
                }

                try {
                    lazyGridItemInfo?.key?.let { itemInfo ->
                        val foundKey = itemInfo as Int
                        selectedPersonItemKey = foundKey
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SplitTheBill(modifier: Modifier = Modifier) {

    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeight = remember { with(density) { config.screenHeightDp.toDp().toPx() } }
    var flowRowHeight: Float? = remember { null }
    val personItemList = remember { personList.toMutableStateList() }
    val transactionItemList = remember { transactionList.toMutableStateList() }
    val stackedTransactionItemList = remember { listOf<TransactionItem>().toMutableStateList() }
    val screenState = remember { ScreenState() }
    val lazyGridState = rememberLazyGridState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            text = "Fair Share new124",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = FontFamily.SansSerif
        )

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    if (screenState.lazyGridLayoutCoordinates == null) {
                        screenState.lazyGridLayoutCoordinates = coordinates
                    }
                },
            state = lazyGridState,
            columns = GridCells.Fixed(3)
        ) {
            items(personItemList, key = { it.id }) { person ->
                PersonItem(person, screenState)
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
            thickness = 0.3.dp
        )

        FlowRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(25.dp)
                .onGloballyPositioned { coordinates ->
                    if (screenState.flowRowLayoutCoordinates == null) {
                        screenState.flowRowLayoutCoordinates = coordinates
                    }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            transactionItemList.forEachIndexed { _, flowItem ->
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = flowItem.itemAlpha.value
                        }
                        .onGloballyPositioned {
                            if (flowItem.itemPositionInFlow == null) {
                                flowItem.itemPositionInFlow = it.positionOnScreen()
                            }

                            if (flowRowHeight == null) {
                                flowRowHeight = it.size.height - screenHeight
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { _ ->
                                    scope.launch {
                                        screenState.isDraggingCancelled = false
                                        if (flowItem.isChecked.value.not()) {
                                            flowItem.isChecked.value = true
                                            stackedTransactionItemList.add(flowItem)
                                        }
                                        stackedTransactionItemList.moveItem(flowItem, 0)
                                        screenState.pickedItemGlobalOffset = flowItem.itemPositionInFlow
                                        screenState.globalDragOffset.snapTo(Offset.Zero)
                                        screenState.isDragStarted = true
                                    }
                                },

                                onDrag = { change, dragAmount ->
                                    scope.launch {
                                        screenState.globalDragOffset.snapTo(screenState.globalDragOffset.value + dragAmount)
                                        flowRowHeight?.let {
                                            if (screenState.globalDragOffset.value.y < it) {
                                                screenState.isInDropBounds = true
                                                screenState.findItemAtOffset(lazyGridState, change.position)
                                            }
                                        }
                                    }
                                },

                                onDragEnd = {
                                    scope.launch {
                                        screenState.globalDragOffset.animateTo(Offset.Zero, tween(PICKING_UP_ANIM_TRANSITION_DURATION))
                                    }
                                    screenState.isDraggingCancelled = true
                                }
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (flowItem.isChecked.value.not()) {
                                flowItem.isChecked.value = true
                                stackedTransactionItemList.add(flowItem)
                            } else {
                                flowItem.isChecked.value = false
                                stackedTransactionItemList.remove(flowItem)
                            }
                        },
                ) {
                    TransactionItem(flowItem)
                }
            }
        }
    }

    if (screenState.isDragStarted) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        screenState.globalDragOffset.value.x.roundToInt(),
                        screenState.globalDragOffset.value.y.roundToInt(),
                    )
                }
        ) {
            stackedTransactionItemList.reversed().forEachIndexed { index, stackedItem ->
                transactionItemList.find { it.id == stackedItem.id }?.itemAlpha?.value = 0f
                val tempDragOffset by remember { mutableStateOf(Animatable(stackedItem.itemPositionInFlow ?: Offset.Zero, Offset.VectorConverter)) }
                val rotationValue = remember {
                    when {
                        index == stackedTransactionItemList.size-1 -> 0f
                        index % 2 == 0 -> 7f
                        else -> -7f
                    }
                }

                stackedItem.itemBorderSize.value = 1.dp
                scope.launch {
                    tempDragOffset.animateTo(
                        screenState.pickedItemGlobalOffset ?: Offset.Zero,
                        tween(PICKING_UP_ANIM_TRANSITION_DURATION)
                    )
                }
                scope.launch { stackedItem.overlayItemRotation.animateTo(rotationValue, tween(PICKING_UP_ANIM_TRANSITION_DURATION)) }
                scope.launch { stackedItem.parentScale.animateTo(1.2f, tween(TRANSACTION_ITEM_SCALE_ANIM_DURATION)) }
                scope.launch { stackedItem.shadowAlpha.animateTo(1f) }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                tempDragOffset.value.x.roundToInt(),
                                tempDragOffset.value.y.roundToInt()
                            )
                        }
                ) {
                    TransactionItem(transactionItem = stackedItem)
                }

                LaunchedEffect(key1 = screenState.isDraggingCancelled) {
                    if (screenState.isDraggingCancelled) {
                        scope.launch {
                            tempDragOffset.animateWithResult(
                                stackedItem.itemPositionInFlow ?: Offset.Zero,
                                tween(PICKING_UP_ANIM_TRANSITION_DURATION),
                                onAnimationEnd = {
                                    transactionItemList.find { it.id == stackedItem.id }?.itemAlpha?.value = 1f
                                    screenState.isDragStarted = false
                                }
                            )
                        }
                        scope.launch { stackedItem.parentScale.animateTo(1f, tween(PICKING_UP_ANIM_TRANSITION_DURATION)) }
                        scope.launch { stackedItem.shadowAlpha.animateTo(0f, tween(PICKING_UP_ANIM_TRANSITION_DURATION)) }
                        stackedItem.itemBorderSize.value = 0.dp
                        scope.launch { stackedItem.overlayItemRotation.animateTo(0f, tween(PICKING_UP_ANIM_TRANSITION_DURATION)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonItem(
    item: PersonItem,
    screenState: ScreenState,
    modifier: Modifier = Modifier
) {
    val itemScale by animateFloatAsState(
        targetValue = if (screenState.selectedPersonItemKey == item.id) 1.1f else 1f,
        animationSpec = tween(PERSON_ITEM_SCALE_ANIM_DURATION),
        label = ""
    )

    Box(
        modifier = modifier
            .scale(itemScale)
            .onGloballyPositioned {
                if (screenState.boxSize == null) {
                    screenState.boxSize = it.size
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    .size(100.dp)
                    .shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        borderRadius = 70.dp,
                        blurRadius = 20.dp,
                        offsetY = 80.dp,
                        spread = 3.dp
                    )
                    .clip(RoundedCornerShape(90f)),
                painter = painterResource(id = item.photo),
                contentDescription = "Person photo",
                contentScale = ContentScale.Crop
            )

            Text(
                text = item.name,
                color = Color.Gray
            )
        }
    }

    item.requestAmount?.let {
        SplitAmountBubble(item)
    }
}

@Composable
private fun TransactionItem(
    transactionItem: TransactionItem,
    modifier: Modifier = Modifier
) {
    val checkIconAlpha by animateFloatAsState(
        targetValue = if (transactionItem.isChecked.value) 1f else 0f, label = ""
    )

    Box(
        modifier = modifier
            .graphicsLayer { transformOrigin = TransformOrigin.Center }
            .rotate(transactionItem.overlayItemRotation.value)
            .scale(transactionItem.parentScale.value)
            .shadow(
                color = transactionItemChipColorDark,
                borderRadius = 30.dp,
                blurRadius = 16.dp,
                offsetY = 50.dp,
                alpha = transactionItem.shadowAlpha.value,
                spread = 0.dp
            )
            .clip(RoundedCornerShape(35f))
            .background(transactionItemChipColor)
            .border(transactionItem.itemBorderSize.value, transactionItemChipColorDark.copy(0.3f), RoundedCornerShape(35f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f, false),
                text = transactionItem.transactionTitle,
                fontSize = 18.sp,
                color = transactionItemChipTitleColor,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            Text(
                text = "$${transactionItem.transactionAmount}",
                fontSize = 18.sp,
                color = transactionItemChipTitleColor
            )

            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = transactionItemChipTitleColor,
                        shape = RoundedCornerShape(20f)
                    )
            ) {
                Icon(
                    modifier = Modifier
                        .graphicsLayer {
                            this.alpha = checkIconAlpha
                        }
                        .size(19.dp)
                        .padding(3.4.dp),
                    painter = painterResource(id = R.drawable.check_circlesvg),
                    contentDescription = "Transaction item checked state",
                    tint = transactionItemChipTitleColor
                )
            }
        }
    }
}

@Composable
fun SplitAmountBubble(
    item: PersonItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 3.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(top = 13.dp)
                .clip(CircleShape)
                .background(bubbleColor),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 3.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .offset { IntOffset(0, -7) },
                        text = item.requestAmountCount.toString(),
                        color = bubbleColor,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    modifier = Modifier
                        .offset(y = 1.dp),
                    text = "$${item.requestAmount.toString()}",
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}


//EXTENSION FUNCTIONS
private fun Modifier.shadow(
    color: Color = Color.Black,
    borderRadius: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    spread: Dp = 0f.dp,
    alpha: Float = 1f,
    modifier: Modifier = Modifier
) = this.then(
    modifier.drawBehind {
        this.drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            val spreadPixel = spread.toPx()
            val leftPixel = (0f - spreadPixel) + offsetX.toPx()
            val topPixel = (0f - spreadPixel) + offsetY.toPx()
            val rightPixel = (this.size.width + spreadPixel)
            val bottomPixel = (this.size.height + spreadPixel)

            if (blurRadius != 0.dp) {
                frameworkPaint.maskFilter =
                    (BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL))
            }

            val shadowColor = color.copy(alpha = color.alpha * alpha)
            frameworkPaint.color = shadowColor.toArgb()

            it.drawRoundRect(
                left = leftPixel,
                top = topPixel,
                right = rightPixel,
                bottom = bottomPixel,
                radiusX = borderRadius.toPx(),
                radiusY = borderRadius.toPx(),
                paint
            )
        }
    }
)

private fun <T> SnapshotStateList<T>.moveItem(item: T, toIndex: Int) {
    if (isNotEmpty() && indexOf(item) != 0) {
        remove(item)
        add(toIndex, item)
    }
}

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

private val bubbleColor: Color = Color("#195FEB".toColorInt())
private val transactionItemChipColor: Color = Color("#D0E3FC".toColorInt())
private val transactionItemChipColorDark: Color = Color("#2f5a99".toColorInt())
private val transactionItemChipTitleColor: Color = Color("#18365B".toColorInt())







//-------------------------------- Preview related things --------------------------------
val personList: List<PersonItem> = listOf(
    PersonItem(
        id = 1,
        name = "Emily",
        surname = "Carter",
        photo = R.drawable.stb_photo_emily
    ),

    PersonItem(
        id = 2,
        name = "John",
        surname = "Mitchell",
        photo = R.drawable.stb_photo_john
    ),

    PersonItem(
        id = 3,
        name = "Sofia",
        surname = "Hernandez",
        photo = R.drawable.stb_photo_sofia,
        requestAmount = 20.48,
        requestAmountCount = 3
    ),

    PersonItem(
        id = 4,
        name = "Michael",
        surname = "Thompson",
        photo = R.drawable.stb_photo_michael
    ),

    PersonItem(
        id = 5,
        name = "Olivia",
        surname = "Garcia",
        photo = R.drawable.stb_photo_olivia,
        requestAmount = 12.99,
        requestAmountCount = 2
    ),

    PersonItem(
        id = 6,
        name = "James",
        surname = "Smith",
        photo = R.drawable.stb_photo_james
    ),

    PersonItem(
        id = 7,
        name = "Isabella",
        surname = "Roberts",
        photo = R.drawable.stb_photo_isabella
    ),

    PersonItem(
        id = 8,
        name = "David",
        surname = "Johnson",
        photo = R.drawable.stb_photo_david
    ),
)
val transactionList: List<TransactionItem> = listOf(
    TransactionItem(
        id = 1,
        transactionTitle = "Beer",
        transactionAmount = 9.50,
    ),
    TransactionItem(
        id = 2,
        transactionTitle = "Chicken",
        transactionAmount = 14.99
    ),
    TransactionItem(
        id = 3,
        transactionTitle = "Coke Zero 2x",
        transactionAmount = 5.99
    ),
    TransactionItem(
        id = 4,
        transactionTitle = "Coffee",
        transactionAmount = 6.50
    ),
    TransactionItem(
        id = 5,
        transactionTitle = "Bacon Blue Cheese Burger (new receipt)",
        transactionAmount = 7.99
    ),
    TransactionItem(
        id = 6,
        transactionTitle = "Fish",
        transactionAmount = 16.99
    ),
    TransactionItem(
        id = 7,
        transactionTitle = "Fries",
        transactionAmount = 8.50
    ),
)

@Preview(showBackground = true)
@Composable
private fun SplitTheBillPreview() {
    SplitTheBill()
}