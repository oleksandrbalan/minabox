package eu.wewox.minabox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Lazy layout to display data on the two directional plane.
 * Items should be provided with [content] lambda.
 *
 * @param modifier The modifier instance for the root composable.
 * @param state The state which could be used to observe and change translation offset.
 * @param contentPadding A padding around the whole content. This will add padding for the content
 * after it has been clipped, which is not possible via modifier param.
 * @param scrollDirection Determines which directions are allowed to scroll.
 * @param content The lambda block which describes the content. Inside this block you can use
 * [MinaBoxScope.items] method to add items.
 */
@Composable
public fun MinaBox(
    modifier: Modifier = Modifier,
    outerPadding: PaddingValues = PaddingValues(0.dp),
    state: MinaBoxState = rememberMinaBoxState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    scrollDirection: MinaBoxScrollDirection = MinaBoxScrollDirection.BOTH,
    keyboardScrollingSpeed: Float = 50f,
    scrollBarData: ScrollbarData = ScrollbarData(
        color = Color.DarkGray,
        hoveredColor = Color.Gray,
        padding = 5.dp,
        thickness = 12.dp,
        shapeRadius = 16.dp,
        isOuterTable = true
    ),
    content: MinaBoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val contentPaddingPx = contentPadding.toPx()

    val itemProvider = rememberItemProvider(content)

    val visibilityState = remember {
        MutableTransitionState(false).apply {
            targetState = scrollBarData.thickness > 0.dp
        }
    }

    var positionProvider by remember { mutableStateOf<MinaBoxPositionProviderImpl?>(null) }


    var canvasX by remember { mutableStateOf(state.getCanvasX()) }
    var canvasY by remember { mutableStateOf(state.getCanvasY()) }

    var boxX by remember { mutableStateOf(1) }
    var boxY by remember { mutableStateOf(1) }

    var shouldShowScrollbarY by remember { mutableStateOf(false) }
    var shouldShowScrollbarX by remember { mutableStateOf(false) }


    val endPaddingForScrollbarY = if (scrollBarData.isOuterTable && shouldShowScrollbarY) ((scrollBarData.padding * 2) + scrollBarData.thickness) else 0.dp
    val bottomPaddingForScrollbarX = if (scrollBarData.isOuterTable && shouldShowScrollbarX) ((scrollBarData.padding * 2) + scrollBarData.thickness) else 0.dp

    Box(contentAlignment = Alignment.BottomEnd) {
        LazyLayout(
            modifier = Modifier
                .padding(outerPadding)
                .padding(
                    end = endPaddingForScrollbarY,
                    bottom = bottomPaddingForScrollbarX
                ).then(
                    modifier
                        .keysScrolling(speed = keyboardScrollingSpeed, state = state, scope = scope)
                        .clipToBounds()
                        .lazyLayoutPointerInput(state, scrollDirection)
                ),
            itemProvider = { itemProvider },
        ) { constraints ->
            val size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
            positionProvider = positionProvider.update(
                state = state,
                itemProvider = itemProvider,
                layoutDirection = layoutDirection,
                size = size,
                contentPaddingPx = contentPaddingPx,
                scope = scope
            )

            val items = itemProvider.getItems(
                state.translateX.value,
                state.translateY.value,
                contentPaddingPx,
                size,
            )

            val placeables = items.map { (index, bounds) ->
                measure(
                    index,
                    Constraints.fixed(bounds.width.toInt(), bounds.height.toInt())
                ) to bounds.topLeft
            }

            val itemsSize = itemProvider.getItemsSize(contentPaddingPx)
            val width = min(itemsSize.width.toInt(), constraints.maxWidth)
            val height = min(itemsSize.height.toInt(), constraints.maxHeight)

            shouldShowScrollbarX = itemsSize.width.toInt() > constraints.maxWidth
            shouldShowScrollbarY = itemsSize.height.toInt() > constraints.maxHeight


            boxX = (width + endPaddingForScrollbarY.toPx()).roundToInt()
            boxY = (height + bottomPaddingForScrollbarX.toPx()).roundToInt()

            layout(
                width, height
            ) {
                placeables.forEach { (itemPlaceables, position) ->
                    itemPlaceables.forEach { placeable ->
                        placeable.placeRelative(
                            x = position.x.toInt(),
                            y = position.y.toInt(),
                        )
                    }
                }
            }
        }

        Box(
            Modifier
                .padding(outerPadding)
                .size(
                    height = with(density) { boxY.toDp() },
                    width = with(density) { boxX.toDp() })
                .onPlaced {
                    canvasX = state.getCanvasX()
                    canvasY = state.getCanvasY()
                }

        ) {

            val xTimes = (canvasX / boxX).coerceAtLeast(0f)
            val scrollBarXPx = boxX / (xTimes + 1)

            val yTimes = (canvasY / boxY).coerceAtLeast(0f)
            val scrollBarYPx = boxY / (yTimes + 1)
            AnimatedVisibility(
                visibilityState, modifier = Modifier.fillMaxSize(),
                enter = fadeIn(tween(700)),
            ) {
                Box(Modifier.fillMaxSize().padding(scrollBarData.padding)) {
                    //VERTICAL ScrollBar
                    if (shouldShowScrollbarY) {
                        Box(Modifier.fillMaxHeight().align(Alignment.TopEnd)
                            .pointerInput(canvasX, canvasY) {
                                detectTapGestures(
                                    onPress = { position ->
                                        scope.launch {
                                            val newPos =
                                                (position.y) * (canvasY / boxY)
                                            state.animateTo(
                                                state.translateX.value,
                                                newPos
                                            )
                                        }
                                    }
                                )
                            }) {
                            val source =
                                remember { MutableInteractionSource() }

                            val isDragging = remember { mutableStateOf(false) }

                            val color =
                                animateColorAsState(if (source.collectIsHoveredAsState().value || isDragging.value) scrollBarData.hoveredColor else scrollBarData.color)

                            Box(Modifier
                                .height(with(density) { scrollBarYPx.toDp() })
                                .offset(y = if (state.isInitialized()) with(density) {
                                    ((state.translateY.value.toDp() / (yTimes + 1))).coerceIn(
                                        minimumValue = 0.dp,
                                        maximumValue = (boxY.toDp() - (3.dp + scrollBarData.thickness + scrollBarData.padding + scrollBarYPx.toDp())).coerceAtLeast(
                                            0.dp
                                        )
                                    )
                                } else 0.dp)
                                .pointerInput(canvasX, canvasY) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            scope.launch {
                                                val scaledDragAmountY =
                                                    ((change.position.y - (scrollBarYPx / 2)) * (canvasY / boxY))
                                                state.animateTo(
                                                    state.translateX.value,
                                                    state.translateY.value + scaledDragAmountY
                                                )
                                            }
                                        },
                                        onDragStart = {
                                            isDragging.value = true
                                        },
                                        onDragEnd = {
                                            isDragging.value = false
                                        }
                                    )
                                }) {
                                Box(
                                    Modifier
                                        .hoverable(source)
                                        .width(scrollBarData.thickness)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(scrollBarData.shapeRadius))
                                        .background(color.value)
                                ) {}
                            }
                        }

                    }
                    if (shouldShowScrollbarX) {
                        Box(Modifier.fillMaxWidth().align(Alignment.BottomStart)
                            .pointerInput(canvasX, canvasY) {
                                detectTapGestures(
                                    onPress = { position ->
                                        scope.launch {
                                            val newPos =
                                                (position.x) * (canvasX / boxX)
                                            state.animateTo(
                                                newPos,
                                                state.translateY.value
                                            )
                                        }
                                    }
                                )
                            }) {

                            val source =
                                remember { MutableInteractionSource() }

                            val isDragging = remember { mutableStateOf(false) }

                            val color =
                                animateColorAsState(if (source.collectIsHoveredAsState().value || isDragging.value) scrollBarData.hoveredColor else scrollBarData.color)


                            Box(Modifier
                                .width(with(density) { scrollBarXPx.toDp() })
                                .offset(x = if (state.isInitialized()) with(density) {
                                    ((state.translateX.value.toDp() / (xTimes + 1))).coerceIn(
                                        minimumValue = 0.dp,
                                        maximumValue = (boxX.toDp() - (3.dp + scrollBarData.thickness + scrollBarData.padding + scrollBarXPx.toDp())).coerceAtLeast(
                                            0.dp
                                        )
                                    )
                                } else 0.dp)
                                .pointerInput(canvasX, canvasY) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            scope.launch {
                                                val scaledDragAmountX =
                                                    ((change.position.x - (scrollBarXPx / 2)) * (canvasX / boxX))
                                                state.animateTo(
                                                    state.translateX.value + scaledDragAmountX,
                                                    state.translateY.value
                                                )
                                            }
                                        },
                                        onDragStart = {
                                            isDragging.value = true
                                        },
                                        onDragEnd = {
                                            isDragging.value = false
                                        }
                                    )
                                }) {
                                Box(
                                    Modifier
                                        .hoverable(source)
                                        .height(scrollBarData.thickness)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(scrollBarData.shapeRadius))
                                        .background(color.value)
                                ) {}
                            }
                        }
                    }
                }
            }

        }
    }


}

@Composable
private fun Modifier.keysScrolling(speed: Float, state: MinaBoxState, scope: CoroutineScope) =
    if (speed > 0f) {
        val requester = remember { FocusRequester() }
        pointerInput(key1 = true) {
            detectTapGestures(onPress = {
                requester.requestFocus()
            })
        }
            .onPreviewKeyEvent {
                if ((it.key in listOf(
                        Key.DirectionUp,
                        Key.DirectionDown,
                        Key.DirectionLeft,
                        Key.DirectionRight
                    ) && it.type == KeyEventType.KeyDown)
                ) {
                    val yChange =
                        if (it.key == Key.DirectionUp) 50f else if (it.key == Key.DirectionDown) -50f else 0f
                    val xChange =
                        if (it.key == Key.DirectionLeft) 50f else if (it.key == Key.DirectionRight) -50f else 0f
                    scope.launch {
                        state.animateTo(
                            state.translateX.value - xChange,
                            state.translateY.value - yChange
                        )
                    }
                    true
                } else false

            }
            .focusRequester(requester).focusable()
    } else Modifier

private fun MinaBoxItemProvider.getItemsSize(contentPaddingPx: Rect): Size =
    size.let {
        Size(
            width = it.width + contentPaddingPx.left + contentPaddingPx.right,
            height = it.height + contentPaddingPx.top + contentPaddingPx.bottom,
        )
    }

private fun MinaBoxPositionProviderImpl?.update(
    state: MinaBoxState,
    itemProvider: MinaBoxItemProvider,
    layoutDirection: LayoutDirection,
    size: Size,
    contentPaddingPx: Rect,
    scope: CoroutineScope,
): MinaBoxPositionProviderImpl =
    if (
        this != null &&
        this.items == itemProvider.items &&
        this.layoutDirection == layoutDirection &&
        this.size == size
    ) {
        this
    } else {
        MinaBoxPositionProviderImpl(itemProvider.items, layoutDirection, size).also {
            val itemsSize = itemProvider.getItemsSize(contentPaddingPx)
            val bounds = Rect(
                left = 0f,
                top = 0f,
                right = (itemsSize.width - size.width).coerceAtLeast(0f),
                bottom = (itemsSize.height - size.height).coerceAtLeast(0f)
            )
            state.updateBounds(it, bounds, size, scope)
        }
    }

@Composable
private fun PaddingValues.toPx(): Rect {
    val layoutDirection = LocalLayoutDirection.current
    return LocalDensity.current.run {
        Rect(
            calculateLeftPadding(layoutDirection).toPx(),
            calculateTopPadding().toPx(),
            calculateRightPadding(layoutDirection).toPx(),
            calculateBottomPadding().toPx()
        )
    }
}

private fun Modifier.lazyLayoutPointerInput(
    state: MinaBoxState,
    scrollDirection: MinaBoxScrollDirection,
): Modifier {

    val velocityTracker = VelocityTracker()
    return this.pointerInput(Unit) {
        coroutineScope {
            when (scrollDirection) {
                MinaBoxScrollDirection.BOTH -> {
                    detectDragGestures(
                        onDragEnd = {
                            onDragEnd(state, velocityTracker, scrollDirection, this)
                        },
                        onDrag = { change, dragAmount ->
                            onDrag(state, change, dragAmount, velocityTracker, this)
                        }
                    )
                }

                MinaBoxScrollDirection.HORIZONTAL -> detectHorizontalDragGestures(
                    onDragEnd = { onDragEnd(state, velocityTracker, scrollDirection, this) },
                    onHorizontalDrag = { change, dragAmount ->
                        onDrag(state, change, Offset(dragAmount, 0f), velocityTracker, this)
                    }
                )

                MinaBoxScrollDirection.VERTICAL -> detectVerticalDragGestures(
                    onDragEnd = { onDragEnd(state, velocityTracker, scrollDirection, this) },
                    onVerticalDrag = { change, dragAmount ->
                        onDrag(state, change, Offset(0f, dragAmount), velocityTracker, this)
                    }
                )
            }
        }
    }.pointerInput(Unit) {

        coroutineScope {
            awaitPointerEventScope {

                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Scroll) {
                        val change = event.changes.first()

                        // for WEB || JVM controls this automatically
                        val isHorizontal =
                            isWeb() && event.keyboardModifiers.isShiftPressed //&& !state.translateY.isRunning
                        when (scrollDirection) {
                            MinaBoxScrollDirection.BOTH ->
                                onMouseScroll(
                                    state,
                                    change,
                                    change.scrollDelta.reversed()
                                        .horizontalSupport(isHorizontal),
                                    velocityTracker,
                                    this@coroutineScope
                                )


                            MinaBoxScrollDirection.HORIZONTAL -> onMouseScroll(
                                state,
                                change,
                                change.scrollDelta.copy(y = 0f).reversed()
                                    .horizontalSupport(isHorizontal),
                                velocityTracker,
                                this@coroutineScope
                            )

                            MinaBoxScrollDirection.VERTICAL -> onMouseScroll(
                                state,
                                change,
                                change.scrollDelta.copy(x = 0f).reversed()
                                    .horizontalSupport(isHorizontal),
                                velocityTracker,
                                this@coroutineScope
                            )
                        }
                    }
                }
            }
        }
    }
}

public expect fun isWeb(): Boolean


private fun Offset.horizontalSupport(isHorizontal: Boolean) =
    Offset(x = if (isHorizontal) y else x, y = if (isHorizontal) 0f else y)

private fun Offset.reversed() = Offset(x = -x, y = -y)


private fun onMouseScroll(
    state: MinaBoxState,
    change: PointerInputChange,
    dragAmount: Offset,
    velocityTracker: VelocityTracker,
    scope: CoroutineScope
) {
    change.consume()
    velocityTracker.addPosition(change.uptimeMillis, change.position)
    scope.launch {
        if (change.scrollDelta.getDistance() >= 50f) state.animateTo(
            state.translateX.value - dragAmount.x,
            state.translateY.value - dragAmount.y
        )
        else state.dragBy(dragAmount)
    }
}

private fun onDrag(
    state: MinaBoxState,
    change: PointerInputChange,
    dragAmount: Offset,
    velocityTracker: VelocityTracker,
    scope: CoroutineScope
) {
    change.consume()
    velocityTracker.addPosition(change.uptimeMillis, change.position)
    scope.launch {
        state.dragBy(dragAmount)
    }
}

private fun onDragEnd(
    state: MinaBoxState,
    velocityTracker: VelocityTracker,
    scrollDirection: MinaBoxScrollDirection,
    scope: CoroutineScope
) {
    var velocity = velocityTracker.calculateVelocity()
    velocity = when (scrollDirection) {
        MinaBoxScrollDirection.BOTH -> velocity
        MinaBoxScrollDirection.HORIZONTAL -> velocity.copy(velocity.x, 0f)
        MinaBoxScrollDirection.VERTICAL -> velocity.copy(0f, velocity.y)
    }
    scope.launch { state.flingBy(velocity) }
}
