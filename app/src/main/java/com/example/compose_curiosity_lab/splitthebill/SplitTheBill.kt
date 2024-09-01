package com.example.compose_curiosity_lab.splitthebill

import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SplitTheBill(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val lazyGridState = rememberLazyGridState()
    val screenState = remember { ScreenState(scope, config, density, lazyGridState) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { screenState.onRootPositioned(it) }
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            text = "Fair Share new184",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = FontFamily.SansSerif
        )

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { screenState.onLazyGridLayoutPositioned(it) },
            state = lazyGridState,
            columns = GridCells.Fixed(3)
        ) {
            items(screenState.personItemList, key = { it.id }) { person ->
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
                .onGloballyPositioned { screenState.onFlowRowPositioned(it) },
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            screenState.transactionItemList.forEachIndexed { _, flowItem ->
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = flowItem.itemAlpha.value
                        }
                        .onGloballyPositioned { screenState.onFlowRowItemPositioned(flowItem, it) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { _ ->
                                    screenState.startDragging(flowItem)
                                },

                                onDrag = { change, dragAmount ->
                                    screenState.onDrag(change.position, dragAmount)
                                },

                                onDragEnd = {
                                    screenState.endDragging()
                                }
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            screenState.onFlowRowItemClicked(flowItem)
                        },
                ) {
                    TransactionItem(flowItem)
                }
            }
        }
    }

    if (screenState.isDragStarted) {
        Box(
            modifier = Modifier.offset { screenState.globalDragOffset.value.toIntOffset() }
        ) {
            screenState.stackedTransactionItemList.reversed().forEachIndexed { index, stackedItem ->
                scope.launch { screenState.transactionItemList.find { it.id == stackedItem.id }?.itemAlpha?.snapTo(0f) }
                val tempDragOffset by remember {
                    mutableStateOf(
                        Animatable(
                            stackedItem.itemPositionInFlow ?: Offset.Zero,
                            Offset.VectorConverter
                        )
                    )
                }
                val rotationValue = remember {
                    when {
                        index == screenState.stackedTransactionItemList.size - 1 -> 0f
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

                TransactionItem(
                    transactionItem = stackedItem,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                tempDragOffset.value.x.roundToInt(),
                                tempDragOffset.value.y.roundToInt()
                            )
                        }
                )

                LaunchedEffect(key1 = screenState.isDraggingCancelled) {
                    if (screenState.isDraggingCancelled) {
                        scope.launch { stackedItem.parentScale.animateTo(1f, tween(PICKING_UP_ANIM_TRANSITION_DURATION)) }
                        scope.launch { stackedItem.shadowAlpha.animateTo(0f, tween(PICKING_UP_ANIM_TRANSITION_DURATION)) }
                        stackedItem.itemBorderSize.value = 0.dp
                        scope.launch { stackedItem.overlayItemRotation.animateTo(0f, tween(PICKING_UP_ANIM_TRANSITION_DURATION)) }
                        scope.launch {
                            tempDragOffset.animateWithResult(
                                stackedItem.itemPositionInFlow ?: Offset.Zero,
                                tween(PICKING_UP_ANIM_TRANSITION_DURATION),
                                onAnimationEnd = {
                                    scope.launch {
                                        screenState.transactionItemList.find { it.id == stackedItem.id }?.itemAlpha?.snapTo(1f)
                                        screenState.isDragStarted = false
                                        screenState.selectedPersonItemKey = null
                                        screenState.isDraggingCancelled = false
                                    }
                                }
                            )
                        }
                    }
                }

                LaunchedEffect(key1 = screenState.isItemDropped) {
                    if (screenState.isItemDropped) {
                        scope.launch { stackedItem.parentScale.animateTo(0f, tween(400)) }
                        scope.launch {
                            screenState.transactionItemList.remove(stackedItem)
                            stackedItem.itemAlpha.animateWithResult(
                                0f,
                                tween(1800),
                                onAnimationEnd = {
                                    screenState.isDragStarted = false
                                    screenState.isItemDropped = false
                                    screenState.selectedPersonItemKey = null
                                    screenState.stackedTransactionItemList.clear()
                                }
                            )
                        }
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
            }
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

        SplitAmountBubble(item)
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
            .padding(end = 3.dp)
            .onGloballyPositioned {
                if (item.itemBubbleLayoutCoordinates == null) {
                    item.itemBubbleLayoutCoordinates = it
                }
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        item.requestAmount?.let {
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
                        text = "$${item.requestAmount}",
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
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
                color = transactionItemChipAmountColor
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

@Preview(showBackground = true)
@Composable
private fun SplitTheBillPreview() {
    SplitTheBill()
}