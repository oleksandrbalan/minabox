package eu.wewox.minabox

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Lazy layout to display data on the two directional plane.
 * Items should be provided with [content] lambda.
 *
 * @param modifier The modifier instance for the root composable.
 * @param state The state which could be used to observe and change translation offset.
 * @param contentPadding A padding around the whole content. This will add padding for the content
 * after it has been clipped, which is not possible via modifier param.
 * @param content The lambda block which describes the content. Inside this block you can use
 * [MinaBoxScope.items] method to add items.
 */
@Composable
public fun MinaBox(
    modifier: Modifier = Modifier,
    state: MinaBoxState = rememberMinaBoxState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: MinaBoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val contentPaddingPx = contentPadding.toPx()

    val itemProvider = rememberItemProvider(content)

    var positionProvider by remember { mutableStateOf<MinaBoxPositionProviderImpl?>(null) }

    LazyLayout(
        modifier = modifier
            .clipToBounds()
            .lazyLayoutPointerInput(state),
        itemProvider = itemProvider,
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

        layout(width, height) {
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
}

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
            state.updateBounds(it, bounds, scope)
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

private fun Modifier.lazyLayoutPointerInput(state: MinaBoxState): Modifier =
    pointerInput(Unit) {
        val velocityTracker = VelocityTracker()
        coroutineScope {
            detectDragGestures(
                onDragEnd = {
                    val velocity = velocityTracker.calculateVelocity()
                    launch {
                        state.flingBy(velocity)
                    }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                    launch {
                        state.dragBy(dragAmount)
                    }
                }
            )
        }
    }
