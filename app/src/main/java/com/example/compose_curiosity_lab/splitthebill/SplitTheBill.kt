package com.example.compose_curiosity_lab.splitthebill

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.key
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.compose_curiosity_lab.R

/**
 * Created on 8/9/2024
 * @author Kanan Bashir
 */

const val STIFFNESS_TRANSACTION_ITEM = 80f
const val STIFFNESS_PERSON_ITEM = 30f
val springFloatPersonItem: FiniteAnimationSpec<Float> = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = STIFFNESS_PERSON_ITEM)
val springFloat: FiniteAnimationSpec<Float> = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = STIFFNESS_TRANSACTION_ITEM)
val springOffset: FiniteAnimationSpec<Offset> = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = STIFFNESS_TRANSACTION_ITEM)
val springDp: FiniteAnimationSpec<Dp> = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = STIFFNESS_TRANSACTION_ITEM)

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
            text = "Fair Share new",
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
            screenState.transactionItemList.forEach { flowItem ->
                //Using key composable to properly update flow row when the list updates.
                //Otherwise it could cache a state even an item is deleted.
                key(flowItem.id) {
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
    }

    Box(
        modifier = Modifier
            .graphicsLayer { alpha = if (screenState.dragState == ScreenState.DragState.DragStarted) 1f else 0f }
            .offset { screenState.globalDragOffset.value.toIntOffset() }
    ) {
        val overlayItemTransition = updateTransition(
            targetState = screenState.itemState,
            label = "overlayItemTransition"
        )

        screenState.stackedTransactionItemList.forEachIndexed { index, stackedItem ->
            key (stackedItem.id){
                val itemDragOffset by remember {
                    mutableStateOf(
                        Animatable(
                            stackedItem.itemPositionInFlow ?: Offset.Zero,
                            Offset.VectorConverter
                        )
                    )
                }

                stackedItem.apply {
                    overlayItemRotation = overlayItemTransition.animateFloat(
                        label = "overlayItemRotation",
                        transitionSpec = { springFloat }
                    ) { itemState ->
                        when (itemState) {
                            ScreenState.ItemState.Picked -> screenState.getRotationValue(index)
                            else -> 0f
                        }
                    }

                    parentScale = overlayItemTransition.animateFloat(
                        label = "parentScale",
                        transitionSpec = { springFloat }
                    ) { itemState ->
                        when (itemState) {
                            ScreenState.ItemState.Picked -> 1.2f
                            ScreenState.ItemState.Idle -> 1f
                            else -> 0f //Dropped
                        }
                    }

                    shadowAlpha = overlayItemTransition.animateFloat(
                        label = "shadowAlpha",
                        transitionSpec = { springFloat }
                    ) { itemState ->
                        when (itemState) {
                            ScreenState.ItemState.Picked -> 1f
                            else -> 0f
                        }
                    }

                    itemBorderSize = overlayItemTransition.animateDp(
                        label = "itemBorderSize",
                        transitionSpec = { springDp }
                    ) { itemState ->
                        when (itemState) {
                            ScreenState.ItemState.Picked -> 1.dp
                            else -> 0.dp
                        }
                    }

                    TransactionItem(
                        transactionItem = stackedItem,
                        modifier = Modifier
                            .zIndex(stackedItem.overlayItemZIndex)
                            .offset { itemDragOffset.value.toIntOffset() }
                    )

                    //For some reason, the drag animation doesn't work with update transition. So, I decided
                    // to use the traditional way..
                    LaunchedEffect(key1 = screenState.itemState) {
                        val offset = when (screenState.itemState) {
                            ScreenState.ItemState.Picked -> screenState.pickedItemGlobalOffset ?: Offset.Zero
                            ScreenState.ItemState.Idle -> stackedItem.itemPositionInFlow ?: Offset.Zero
                            else -> return@LaunchedEffect
                        }

                        itemDragOffset.animateTo(offset, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = STIFFNESS_TRANSACTION_ITEM))
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
        animationSpec = springFloatPersonItem,
        label = ""
    )

    Box(
        modifier = modifier.scale(itemScale)
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

        SplitAmountBubble(item, screenState)
    }
}

@Composable
fun SplitAmountBubble(
    item: PersonItem,
    screenState: ScreenState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 3.dp)
            .onGloballyPositioned { screenState.onSplitAmountBubblePositioned(item, it) },
        contentAlignment = Alignment.CenterEnd
    ) {
        AnimatedVisibility(
            visible = item.requestAmount.value != 0.0,
            enter = scaleIn(
                animationSpec = tween(delayMillis = 250, easing = FastOutSlowInEasing),
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300, delayMillis = 100, easing = FastOutSlowInEasing)
            ),
            exit = scaleOut() + fadeOut()
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
                        AnimatedContent(
                            targetState = item.requestAmountCount.value,
                            transitionSpec = { slideInVertically(
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                            ).togetherWith(fadeOut(tween(200))) },
                            label = "requestAmountCount"
                        ) { state ->
                            Text(
                                modifier = Modifier.offset { IntOffset(0, -7) },
                                text = state.toString(),
                                color = bubbleColor,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = item.requestAmount.value,
                        transitionSpec = { slideInVertically(
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ).togetherWith(fadeOut(tween(200))) },
                        label = "requestAmount"
                    ) { state ->
                        Text(
                            modifier = Modifier.offset(y = 1.dp),
                            text = "$$state",
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