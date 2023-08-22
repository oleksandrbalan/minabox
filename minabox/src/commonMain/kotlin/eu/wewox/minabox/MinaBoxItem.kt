package eu.wewox.minabox

import eu.wewox.minabox.MinaBoxItem.Value.Absolute

/**
 * The layout data for item inside [MinaBox].
 *
 * @property x The offset on the plane in pixels on the X axis.
 * @property y The offset on the plane in pixels on the Y axis.
 * @property width The width of the item, may be absolute, or relative to the parent width.
 * @property height The height of the item, may be absolute, or relative to the parent height.
 * @property lockHorizontally Whether item should be displayed when user scrolls horizontally.
 * @property lockVertically Whether item should be displayed when user scrolls vertically.
 */
public class MinaBoxItem(
    public val x: Float,
    public val y: Float,
    public val width: Value,
    public val height: Value,
    public val lockHorizontally: Boolean = false,
    public val lockVertically: Boolean = false,
) {

    /**
     * The layout data for item inside [MinaBox].
     *
     * @param x The offset on the plane in pixels on the X axis.
     * @param y The offset on the plane in pixels on the Y axis.
     * @param width The width of the item in pixels.
     * @param height The height of the item in pixels.
     * @param lockHorizontally Whether item should be displayed when user scrolls horizontally.
     * @param lockVertically Whether item should be displayed when user scrolls vertically.
     */
    public constructor(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        lockHorizontally: Boolean = false,
        lockVertically: Boolean = false,
    ) : this(x, y, Absolute(width), Absolute(height), lockHorizontally, lockVertically)

    /**
     * An interface for [MinaBox] width and height.
     */
    public sealed interface Value {
        /**
         * The size in pixels.
         *
         * @property value The value in pixels.
         */
        public class Absolute(public val value: Float) : Value

        /**
         * The size should be relative to the parent's size.
         *
         * @property fraction The fraction of the parent size to occupy.
         */
        public class MatchParent(public val fraction: Float) : Value

        /**
         * Resolves the actual size of the [Value].
         *
         * @param parentSize The parent size of the dimension in pixels.
         * @return The size of the [MinaBox] item.
         */
        public fun resolve(parentSize: Float = 0f): Float =
            when (this) {
                is Absolute -> value
                is MatchParent -> parentSize * fraction
            }
    }
}
