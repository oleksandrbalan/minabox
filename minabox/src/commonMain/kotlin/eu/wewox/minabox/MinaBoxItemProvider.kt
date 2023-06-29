@file:OptIn(ExperimentalFoundationApi::class)

package eu.wewox.minabox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.IntervalList
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.getDefaultLazyLayoutKey
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.max

/**
 * Remembers the item provider.
 *
 * @param content The lambda block which describes the content.
 * @return An instance of the [MinaBoxItemProvider].
 */
@Composable
internal fun rememberItemProvider(
    content: MinaBoxScope.() -> Unit
): MinaBoxItemProvider =
    run {
        val scope = MinaBoxScopeImpl().apply(content)
        MinaBoxItemProvider(scope.intervals)
    }

/**
 * Implementation of the [LazyLayoutItemProvider]
 *
 * @property intervals The intervals with items registered in [MinaBoxScope].
 */
internal class MinaBoxItemProvider(
    private val intervals: IntervalList<MinaBoxItemContent>
) : LazyLayoutItemProvider {

    /**
     * The cache map with items, associated by its global index.
     */
    val items: Map<Int, MinaBoxItem> =
        intervals.mapAll { index, localIndex, item -> index to item.layoutInfo(localIndex) }.toMap()

    /**
     * The total size of the place occupied by items.
     */
    val size: Size = run {
        var maxX = 0f
        var maxY = 0f

        items.forEach { (_, info) ->
            maxX = max(maxX, info.x + info.width.resolve())
            maxY = max(maxY, info.y + info.height.resolve())
        }

        Size(maxX, maxY)
    }

    override val itemCount: Int =
        intervals.size

    override fun getContentType(index: Int): Any? =
        withLocalIntervalIndex(index) { localIndex, content ->
            content.contentType.invoke(localIndex)
        }

    override fun getKey(index: Int): Any =
        withLocalIntervalIndex(index) { localIndex, content ->
            content.key?.invoke(localIndex) ?: getDefaultLazyLayoutKey(index)
        }

    @Composable
    override fun Item(index: Int) {
        withLocalIntervalIndex(index) { localIndex, content ->
            content.item.invoke(localIndex)
        }
    }

    /**
     * Filters only visible items.
     * Returns a map of item indices with its position and size.
     *
     * @param translateX The current translation along X axis.
     * @param translateY The current translation along Y axis.
     * @param contentPadding The content padding in pixels.
     * @param size The size of the viewport.
     * @return A map of visible item indices with its position and size.
     */
    fun getItems(
        translateX: Float,
        translateY: Float,
        contentPadding: Rect,
        size: Size
    ): Map<Int, Rect> {
        val viewport = Rect(
            left = translateX - contentPadding.left,
            top = translateY - contentPadding.top,
            right = translateX - contentPadding.left + size.width,
            bottom = translateY - contentPadding.top + size.height,
        )

        return items
            .filterValues { it.overlaps(viewport) }
            .mapValues { (_, info) ->
                info.translate(translateX, translateY, contentPadding, viewport)
            }
    }

    private inline fun <T> withLocalIntervalIndex(
        index: Int,
        block: (localIndex: Int, content: MinaBoxItemContent) -> T
    ): T {
        val interval = intervals[index]
        val localIntervalIndex = index - interval.startIndex
        return block(localIntervalIndex, interval.value)
    }

    private fun <T, R> IntervalList<T>.mapAll(block: (Int, Int, T) -> R): List<R> =
        buildList {
            this@mapAll.forEach { interval ->
                repeat(interval.size) { index ->
                    add(block(index + interval.startIndex, index, interval.value))
                }
            }
        }
}

private fun MinaBoxItem.overlaps(other: Rect): Boolean {
    if (lockVertically && lockHorizontally) {
        return true
    }

    val width = width.resolve(other.width)
    val height = height.resolve(other.height)
    fun overlapsHorizontally(): Boolean = x + width > other.left && x < other.right
    fun overlapsVertically(): Boolean = y + height > other.top && y < other.bottom

    return if (lockHorizontally) {
        overlapsVertically()
    } else if (lockVertically) {
        overlapsHorizontally()
    } else {
        overlapsHorizontally() && overlapsVertically()
    }
}

private fun MinaBoxItem.translate(
    translateX: Float,
    translateY: Float,
    contentPadding: Rect,
    viewport: Rect
): Rect {
    val itemTranslateX = translateX.takeUnless { lockHorizontally } ?: 0f
    val itemTranslateY = translateY.takeUnless { lockVertically } ?: 0f
    val newX = this.x - itemTranslateX + contentPadding.left
    val newY = this.y - itemTranslateY + contentPadding.top
    val width = width.resolve(viewport.width - contentPadding.left - contentPadding.right)
    val height = height.resolve(viewport.height - contentPadding.top - contentPadding.bottom)
    return Rect(
        left = newX,
        top = newY,
        right = newX + width,
        bottom = newY + height
    )
}
