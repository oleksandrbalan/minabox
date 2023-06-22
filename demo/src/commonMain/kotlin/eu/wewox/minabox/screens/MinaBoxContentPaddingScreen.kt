package eu.wewox.minabox.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import eu.wewox.minabox.Example
import eu.wewox.minabox.MinaBox
import eu.wewox.minabox.MinaBoxItem
import eu.wewox.minabox.MinaBoxScrollDirection
import eu.wewox.minabox.ui.components.TopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mina Box example with content padding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinaBoxContentPaddingScreen() {
    Scaffold(
        topBar = {
            TopBar(
                title = Example.MinaBoxContentPadding.label,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
            )
        }
    ) { padding ->
        BoxWithConstraints {
            val gapPx = LocalDensity.current.run { ItemsGap.roundToPx() }
            val itemWidth = (constraints.maxWidth - gapPx * 3) / 2f
            val itemHeight = itemWidth * 2

            MinaBox(
                scrollDirection = MinaBoxScrollDirection.VERTICAL,
                contentPadding = PaddingValues(
                    top = ItemsGap + padding.calculateTopPadding(),
                    bottom = ItemsGap + padding.calculateBottomPadding(),
                    start = ItemsGap,
                    end = ItemsGap,
                ),
            ) {
                items(
                    count = ItemsCount,
                    layoutInfo = { createLayoutInfo(it, gapPx, itemWidth, itemHeight) },
                    itemContent = { ScaleUpItem(index = it) }
                )
            }
        }
    }
}

private fun createLayoutInfo(
    index: Int,
    gapPx: Int,
    itemWidth: Float,
    itemHeight: Float,
): MinaBoxItem {
    val x = if (index % 2 != 0) {
        itemWidth + gapPx
    } else {
        0f
    }

    val y = if (index % 2 != 0) {
        (index / 2) * (itemHeight + gapPx)
    } else {
        ((index / 2 - 1) * (itemHeight + gapPx) + itemHeight / 2f + gapPx).coerceAtLeast(0f)
    }

    val height = if (index == 0 || index == ItemsCount - 1) {
        itemHeight / 2f
    } else {
        itemHeight
    }

    return MinaBoxItem(
        x = x,
        y = y,
        width = itemWidth,
        height = height,
    )
}

@Composable
private fun ScaleUpItem(index: Int, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(0.75f) }
    LaunchedEffect(Unit) {
        delay(100)
        launch {
            scale.animateTo(1f)
        }
    }
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        modifier = modifier.scale(scale.value)
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = "#$index",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

private val ItemsGap = 16.dp
private const val ItemsCount = 50
