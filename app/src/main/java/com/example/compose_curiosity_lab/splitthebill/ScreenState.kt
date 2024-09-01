package com.example.compose_curiosity_lab.splitthebill

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.IntSize
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
    var isDragStarted by mutableStateOf(false)
    var isDraggingCancelled by mutableStateOf(false)
    var isItemDropped by mutableStateOf(false)
    var boxSize: IntSize? = null
    var selectedPersonItemKey by mutableStateOf<Int?>(null)
    val personItemList  = personList.toMutableStateList()
    val transactionItemList = transactionList.toMutableStateList()
    val stackedTransactionItemList = listOf<TransactionItem>().toMutableStateList()
    private var pickedTransactionItem: TransactionItem? = null
    private var isInDropBounds by mutableStateOf(false)
    private var flowRowLayoutCoordinates: LayoutCoordinates? = null
    private var rootLayoutCoordinates: LayoutCoordinates? = null
    private var flowRowHeight: Float? = null
    private var screenHeight: Float = with(density) { configuration.screenHeightDp.toDp().toPx() }
    private var lazyGridLayoutCoordinates: LayoutCoordinates? = null
    private var coroutineScope: CoroutineScope = scope
    private var gridState = lazyGridState

    fun startDragging(item: TransactionItem) {
        coroutineScope.launch {
            pickedTransactionItem = item

            if (item.isChecked.value.not()) {
                item.isChecked.value = true
                stackedTransactionItemList.add(item)
            }
            stackedTransactionItemList.moveItem(item, 0)
            pickedItemGlobalOffset = item.itemPositionInFlow
            globalDragOffset.snapTo(Offset.Zero)
            isDragStarted = true
        }
    }

    fun onDrag(dragPosition: Offset, dragAmount: Offset) {
        pickedTransactionItem?.let { item ->
            coroutineScope.launch {
                globalDragOffset.snapTo(globalDragOffset.value + dragAmount)

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
                        isInDropBounds = true
                        findItemAtOffset(position)
                    } else {
                        isInDropBounds = false
                        selectedPersonItemKey = null
                    }
                }
            }
        }
    }

    fun endDragging() {
        when (isInDropBounds) {
            true -> { dropItem() }
            else -> { cancelDragging() }
        }
    }

    private fun dropItem() {
        pickedTransactionItem?.let { item ->
            coroutineScope.launch {
                if (selectedPersonItemKey == null) {
                    cancelDragging()
                    return@launch
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
                        return@launch
                    }

                    personItem.itemBubbleLayoutCoordinates?.let {
                        position = item.itemLayoutCoordinates!!.localPositionOf(it, it.positionInParent())
                    }

                    globalDragOffset.animateTo(position, tween(2000))
                }
                pickedTransactionItem = null
                isItemDropped = true
            }
        }
    }

    private fun cancelDragging() {
        coroutineScope.launch {
            globalDragOffset.animateTo(Offset.Zero, tween(PICKING_UP_ANIM_TRANSITION_DURATION))
        }
        pickedTransactionItem = null
        isDraggingCancelled = true
    }

    fun onFlowRowItemClicked(item: TransactionItem) {
        if (item.isChecked.value.not()) {
            item.isChecked.value = true
            stackedTransactionItemList.add(item)
        } else {
            item.isChecked.value = false
            stackedTransactionItemList.remove(item)
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
        flowRowLayoutCoordinates ?: run { flowRowLayoutCoordinates = layoutCoordinates }
    }

    fun onFlowRowItemPositioned(flowItem: TransactionItem, layoutCoordinates: LayoutCoordinates) {
        flowItem.itemLayoutCoordinates = layoutCoordinates
        flowItem.itemPositionInFlow = layoutCoordinates.positionOnScreen() //TODO: CAN BE DELETED
    }

    private fun findItemAtOffset(hitOffset: Offset) {
        lazyGridLayoutCoordinates?.let {
            val lazyGridItemInfo = gridState.layoutInfo.visibleItemsInfo.find { itemInfo ->
                itemInfo.size.toIntRect().contains(hitOffset.round() - itemInfo.offset)
            }

            try {
                lazyGridItemInfo?.key?.let { itemInfo ->
                    selectedPersonItemKey = itemInfo as Int
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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