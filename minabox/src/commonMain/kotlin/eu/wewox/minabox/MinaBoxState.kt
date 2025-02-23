package eu.wewox.minabox

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Creates a [MinaBoxState] that is remembered across compositions.
 *
 * @param initialOffset The lambda to provide initial offset on the plane.
 * @return Instance of the [MinaBoxState].
 */
@Deprecated(
    message = "Use rememberSaveableMinaBoxState() which uses rememberSaveable API.",
    replaceWith = ReplaceWith(
        "rememberSaveableMinaBoxState(initialOffset)",
        "eu.wewox.minabox.rememberSaveableMinaBoxState"
    )
)
@Composable
public fun rememberMinaBoxState(
    initialOffset: MinaBoxPositionProvider.() -> Offset = { Offset.Zero }
): MinaBoxState {
    return remember { MinaBoxState(initialOffset) }
}

/**
 * Creates a [MinaBoxState] that is remembered across compositions and saved across activity or process recreation.
 *
 * @param initialOffset The lambda to provide initial offset on the plane.
 * @return Instance of the [MinaBoxState].
 */
@Composable
public fun rememberSaveableMinaBoxState(
    initialOffset: MinaBoxPositionProvider.() -> Offset = { Offset.Zero },
): MinaBoxState {
    return rememberSaveable(
        saver = MinaBoxState.Saver(),
        init = { MinaBoxState(initialOffset) }
    )
}

/**
 * A state object that can be hoisted to control and observe scrolling.
 *
 * @property initialOffset The lambda to provide initial offset on the plane.
 */
@Stable
public class MinaBoxState(
    private val initialOffset: MinaBoxPositionProvider.() -> Offset
) {
    internal lateinit var translateX: Animatable<Float, AnimationVector1D>
    internal lateinit var translateY: Animatable<Float, AnimationVector1D>

    private val isRtl: Boolean
        get() = positionProvider.layoutDirection == LayoutDirection.Rtl

    /**
     * The position provider used to get items offsets.
     */
    public lateinit var positionProvider: MinaBoxPositionProvider

    /**
     * Offset on the plane, if null state is not initialised.
     */
    public var translate: Translate? by mutableStateOf(null)
        private set

    /**
     * Updates bounds of the layout and initializes the position provider.
     *
     * @param positionProvider An instance of the position provider.
     * @param maxBounds The max size of the layout.
     * @param size The viewport size.
     * @param coroutineScope The scope to use for observing [translateX] and [translateY].
     */
    internal fun updateBounds(
        positionProvider: MinaBoxPositionProvider,
        maxBounds: Rect,
        size: Size,
        coroutineScope: CoroutineScope,
    ) {
        this.positionProvider = positionProvider

        if (!::translateX.isInitialized && !::translateY.isInitialized) {
            val (x, y) = positionProvider.initialOffset()

            translateX = Animatable(x)
            translateY = Animatable(y)

            snapshotFlow { translateX.value }
                .onEach { updateTranslate(size) }
                .launchIn(coroutineScope)

            snapshotFlow { translateY.value }
                .onEach { updateTranslate(size) }
                .launchIn(coroutineScope)
        }

        translateX.updateBounds(
            lowerBound = maxBounds.left,
            upperBound = maxBounds.right,
        )
        translateY.updateBounds(
            lowerBound = maxBounds.top,
            upperBound = maxBounds.bottom,
        )

        updateTranslate(size)
    }

    private fun updateTranslate(size: Size) {
        if (
            translate == null ||
            translateX.value != translate?.x ||
            translateY.value != translate?.y ||
            translateX.upperBound != translate?.maxX ||
            translateY.upperBound != translate?.maxY
        ) {
            translate = Translate(
                translateX.value,
                translateY.value,
                translateX.upperBound ?: 0f,
                translateY.upperBound ?: 0f,
                size.width,
                size.height,
            )
        }
    }

    /**
     * Translates the current offset by the given value.
     *
     * @param value The value to translate by.
     */
    public suspend fun dragBy(value: Offset) {
        coroutineScope {
            launch {
                if (isRtl) {
                    translateX.snapTo(translateX.value + value.x)
                } else {
                    translateX.snapTo(translateX.value - value.x)
                }
            }
            launch {
                translateY.snapTo(translateY.value - value.y)
            }
        }
    }

    /**
     * Animates current offset to the new value.
     *
     * @param x The new offset on the X axis.
     * @param y The new offset on the Y axis.
     */
    public suspend fun animateTo(x: Float = translateX.value, y: Float = translateY.value) {
        coroutineScope {
            launch {
                translateX.animateTo(x)
            }
            launch {
                translateY.animateTo(y)
            }
        }
    }

    /**
     * Snaps current offset to the new value.
     *
     * @param x The new offset on the X axis.
     * @param y The new offset on the Y axis.
     */
    public suspend fun snapTo(x: Float = translateX.value, y: Float = translateY.value) {
        coroutineScope {
            launch {
                translateX.snapTo(x)
            }
            launch {
                translateY.snapTo(y)
            }
        }
    }

    /**
     * Flings current offset by the given velocity.
     *
     * @param velocity The velocity to fling by.
     */
    public suspend fun flingBy(velocity: Velocity) {
        coroutineScope {
            launch {
                if (isRtl) {
                    translateX.animateDecay(velocity.x, exponentialDecay())
                } else {
                    translateX.animateDecay(-velocity.x, exponentialDecay())
                }
            }
            launch {
                translateY.animateDecay(-velocity.y, exponentialDecay())
            }
        }
    }

    /**
     * Stops current offset animations.
     */
    public suspend fun stopAnimation() {
        coroutineScope {
            launch {
                translateX.stop()
            }
            launch {
                translateY.stop()
            }
        }
    }

    /**
     * Animates current offset to the item with a given index.
     *
     * @param index The global index of the item.
     * @param alignment The alignment to align item inside the [MinaBox].
     * @param paddingStart An additional start padding to tweak alignment.
     * @param paddingTop An additional top padding to tweak alignment.
     * @param paddingEnd An additional end padding to tweak alignment.
     * @param paddingBottom An additional bottom padding to tweak alignment.
     */
    public suspend fun animateTo(
        index: Int,
        alignment: Alignment = Alignment.Center,
        paddingStart: Float = 0f,
        paddingTop: Float = 0f,
        paddingEnd: Float = 0f,
        paddingBottom: Float = 0f,
    ) {
        val offset = positionProvider.getOffset(
            index = index,
            alignment = alignment,
            paddingStart = paddingStart,
            paddingTop = paddingTop,
            paddingEnd = paddingEnd,
            paddingBottom = paddingBottom,
            currentX = translateX.value,
            currentY = translateY.value,
        )
        animateTo(offset.x, offset.y)
    }

    /**
     * Snaps current offset to the item with a given index.
     *
     * @param index The global index of the item.
     * @param alignment The alignment to align item inside the [MinaBox].
     * @param paddingStart An additional start padding to tweak alignment.
     * @param paddingTop An additional top padding to tweak alignment.
     * @param paddingEnd An additional end padding to tweak alignment.
     * @param paddingBottom An additional bottom padding to tweak alignment.
     */
    public suspend fun snapTo(
        index: Int,
        alignment: Alignment = Alignment.Center,
        paddingStart: Float = 0f,
        paddingTop: Float = 0f,
        paddingEnd: Float = 0f,
        paddingBottom: Float = 0f,
    ) {
        val offset = positionProvider.getOffset(
            index = index,
            alignment = alignment,
            paddingStart = paddingStart,
            paddingTop = paddingTop,
            paddingEnd = paddingEnd,
            paddingBottom = paddingBottom,
            currentX = translateX.value,
            currentY = translateY.value
        )
        snapTo(offset.x, offset.y)
    }

    /**
     * Represents the offset on the plane.
     *
     * @property x Offset on the X axis in pixels.
     * @property y Offset on the Y axis in pixels.
     * @property maxX The max offset on on the X axis in pixels.
     * @property maxY The max offset on on the Y axis in pixels.
     * @property viewportWidth The width of the plane viewport in pixels.
     * @property viewportHeight The height of the plane viewport in pixels.
     */
    public class Translate(
        public val x: Float,
        public val y: Float,
        public val maxX: Float,
        public val maxY: Float,
        public val viewportWidth: Float,
        public val viewportHeight: Float,
    )

    internal companion object {

        /**
         * Creates a [Saver] that can save and restore a [MinaBoxState].
         *
         * @return A [Saver] instance for saving and restoring [MinaBoxState].
         */
        fun Saver(): Saver<MinaBoxState, *> = listSaver(
            save = {
                listOf(
                    if (it::translateX.isInitialized) it.translateX.value else 0f,
                    if (it::translateY.isInitialized) it.translateY.value else 0f,
                )
            },
            restore = {
                MinaBoxState {
                    Offset(it[0], it[1])
                }
            }
        )
    }
}
