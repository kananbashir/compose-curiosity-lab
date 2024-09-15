package com.example.compose_curiosity_lab.splitthebill

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toIntRect
import androidx.core.graphics.toColorInt
import com.example.compose_curiosity_lab.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created on 8/31/2024
 * @author Kanan Bashir
 */

class ScreenState(scope: CoroutineScope, configuration: Configuration, density: Density, lazyGridState: LazyGridState) {
    var pickedItemGlobalOffset by mutableStateOf<Offset?>(null)
    var globalDragOffset by mutableStateOf(Animatable(Offset.Zero, Offset.VectorConverter))
    var selectedPersonItemKey by mutableStateOf<Int?>(null)
    val personItemList  = personList.toMutableStateList()
    val transactionItemList = transactionList.toMutableStateList()
    val stackedTransactionItemList = listOf<TransactionItem>().toMutableStateList()
    var itemState by mutableStateOf(ItemState.Idle)
    var dragState by mutableStateOf(DragState.Idle)
    private var higherIndex = -1f
    private var dropState by mutableStateOf(DropState.Idle)
    private var pickedTransactionItem: TransactionItem? = null
    private var flowRowLayoutCoordinates: LayoutCoordinates? = null
    private var rootLayoutCoordinates: LayoutCoordinates? = null
    private var flowRowHeight: Float? = null
    private var screenHeight: Float = with(density) { configuration.screenHeightDp.toDp().toPx() }
    private var lazyGridLayoutCoordinates: LayoutCoordinates? = null
    private var coroutineScope: CoroutineScope = scope
    private var gridState = lazyGridState

    enum class DragState {
        Idle,
        DragStarted;
    }

    enum class ItemState {
        Idle,
        Picked,
        Dropped;
    }

    enum class DropState {
        Idle,
        InDropBounds;
    }

    fun startDragging(item: TransactionItem) {
        if (item.overlayItemZIndex != higherIndex) item.overlayItemZIndex = higherIndex++
        if (item.isChecked.value.not()) {
            item.isChecked.value = true
            stackedTransactionItemList.add(item)
        }

        handlePickedItemsAlphas(0f)
        coroutineScope.launch { globalDragOffset.snapTo(Offset.Zero) }
        pickedTransactionItem = item
        pickedItemGlobalOffset = item.itemPositionInFlow
        dragState = DragState.DragStarted
        itemState = ItemState.Picked
    }

    fun onDrag(dragPosition: Offset, dragAmount: Offset) {
        pickedTransactionItem?.let { item ->
            coroutineScope.launch { globalDragOffset.snapTo(globalDragOffset.value + dragAmount) }

            applyWhenAllNotNull(
                flowRowHeight,
                lazyGridLayoutCoordinates,
                item.itemLayoutCoordinates
            ) {
                if (globalDragOffset.value.y < flowRowHeight!!) {
                    val position = lazyGridLayoutCoordinates!!.localPositionOf(
                        item.itemLayoutCoordinates!!,
                        dragPosition
                    )
                    findItemAtOffset(position)
                } else {
                    selectedPersonItemKey = null
                    dropState = DropState.Idle
                }
            }
        }
    }

    fun endDragging() {
        when (dropState) {
            DropState.InDropBounds -> dropItem()
            else -> cancelDragging()
        }
    }

    private fun dropItem() {
        coroutineScope.launch {
            pickedTransactionItem?.let { item ->
                if (selectedPersonItemKey == null) {
                    cancelDragging()
                    return@let
                }

                applyWhenAllNotNull(
                    selectedPersonItemKey,
                    flowRowLayoutCoordinates,
                    item.itemLayoutCoordinates
                ) {
                    //Just to make sure there will be not an IndexOutOfBounds exception..
                    val personItem: PersonItem? = personItemList.find { it.id == selectedPersonItemKey!! }
                    var position: Offset = Offset.Zero

                    if (personItem == null) {
                        cancelDragging()
                        return@let
                    }

                    personItem.itemBubbleLayoutCoordinates?.let {
                        position = item.itemLayoutCoordinates!!.localPositionOf(it, it.positionInParent())
                    }
                    itemState = ItemState.Dropped
                    globalDragOffset.animateWithResult(position, springOffset,
                        onAnimationEnd = {
                            transactionItemList.removeAll(stackedTransactionItemList)
                            resetValues()
                        })
                }
            }
        }
    }

    private fun cancelDragging() {
        coroutineScope.launch {
            globalDragOffset.animateWithResult(Offset.Zero, springOffset,
                onAnimationEnd = {
                    handlePickedItemsAlphas(1f)
                    dragState = DragState.Idle
                }
            )
        }
        itemState = ItemState.Idle
    }

    private fun resetValues() {
        pickedTransactionItem = null
        pickedItemGlobalOffset = null
        selectedPersonItemKey = null
        dragState = DragState.Idle
        itemState = ItemState.Idle
        dropState = DropState.Idle
        stackedTransactionItemList.clear()
        higherIndex = -1f
    }

    private fun handlePickedItemsAlphas(alpha: Float) {
        stackedTransactionItemList.forEach { item ->
            transactionItemList.find { it.id == item.id }?.itemAlpha?.value = alpha
        }
    }

    fun onFlowRowItemClicked(item: TransactionItem) {
        item.apply {
            if (isChecked.value.not()) {
                isChecked.value = true
                overlayItemZIndex = higherIndex++
                stackedTransactionItemList.add(item)
            } else {
                isChecked.value = false
                stackedTransactionItemList.remove(item)
            }
        }
    }

    fun onLazyGridLayoutPositioned(layoutCoordinates: LayoutCoordinates) {
        lazyGridLayoutCoordinates ?: run { lazyGridLayoutCoordinates = layoutCoordinates }
        flowRowHeight ?: run { flowRowHeight = screenHeight - layoutCoordinates.size.height }
    }

    fun onRootPositioned(layoutCoordinates: LayoutCoordinates) {
        rootLayoutCoordinates ?: run { rootLayoutCoordinates = layoutCoordinates }
    }

    fun onFlowRowPositioned(layoutCoordinates: LayoutCoordinates) {
        flowRowLayoutCoordinates = layoutCoordinates
    }

    fun onFlowRowItemPositioned(flowItem: TransactionItem, layoutCoordinates: LayoutCoordinates) {
        flowItem.apply {
            itemLayoutCoordinates = layoutCoordinates
            itemPositionInFlow = layoutCoordinates.positionOnScreen()
            dragOffset = mutableStateOf(itemPositionInFlow!!)
        }
    }

    private fun findItemAtOffset(hitOffset: Offset) {
        lazyGridLayoutCoordinates?.let {
            val lazyGridItemInfo = gridState.layoutInfo.visibleItemsInfo.find { itemInfo ->
                itemInfo.size.toIntRect().contains(hitOffset.round() - itemInfo.offset)
            }

            try {
                if (lazyGridItemInfo?.key != null) {
                    selectedPersonItemKey = lazyGridItemInfo.key as Int
                    dropState = DropState.InDropBounds
                } else {
                    dropState = DropState.Idle
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getRotationValue(index: Int): Float {
        return when {
            index == stackedTransactionItemList.size - 1 -> 0f
            index % 2 == 0 -> 7f
            else -> -7f
        }
    }
}

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
        id = 5,
        transactionTitle = "Bacon Blue Cheese Burger (new receipt)",
        transactionAmount = 7.99
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
        id = 6,
        transactionTitle = "Fish",
        transactionAmount = 16.99
    ),
    TransactionItem(
        id = 1,
        transactionTitle = "Beer",
        transactionAmount = 9.50,
    ),
    TransactionItem(
        id = 7,
        transactionTitle = "Fries",
        transactionAmount = 8.50
    ),
)
val bubbleColor: Color = Color("#167cff".toColorInt())
val transactionItemChipColor: Color = Color("#cbe4fe".toColorInt())
val transactionItemChipColorDark: Color = Color("#466e99".toColorInt())
val transactionItemChipTitleColor: Color = Color("#06264e".toColorInt())
val transactionItemChipAmountColor: Color = Color("#0f335f".toColorInt())