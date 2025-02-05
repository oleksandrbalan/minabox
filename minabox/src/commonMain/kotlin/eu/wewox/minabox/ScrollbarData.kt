package eu.wewox.minabox

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

public data class ScrollbarData(
    val color: Color,
    val hoveredColor: Color,
    val padding: Dp,
    val thickness: Dp,
    val shapeRadius: Dp,
    val isOuterTable: Boolean
    )