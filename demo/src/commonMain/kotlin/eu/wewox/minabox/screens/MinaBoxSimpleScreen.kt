package eu.wewox.minabox.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import eu.wewox.minabox.Example
import eu.wewox.minabox.MinaBox
import eu.wewox.minabox.MinaBoxItem
import eu.wewox.minabox.ui.components.TopBar

/**
 * Simple Mina Box layout example.
 */
@Composable
fun MinaBoxSimpleScreen(
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBar(
                title = Example.MinaBoxSimple.label,
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        val itemSizePx = with(LocalDensity.current) { ItemSize.toSize() }
        MinaBox(
            outerPadding = padding, modifier = Modifier
                .border(
                    1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
        ) {
            items(
                count = ColumnsCount * RowsCount,
                layoutInfo = {
                    val column = it % ColumnsCount
                    val row = it / ColumnsCount
                    MinaBoxItem(
                        x = itemSizePx.width * column,
                        y = itemSizePx.height * row,
                        width = itemSizePx.width,
                        height = itemSizePx.height,
                    )
                }
            ) { index ->
                Text(
                    text = "Index #$index",
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary)
                        .padding(8.dp)
                )
            }
        }
    }
}

private const val ColumnsCount = 50
private const val RowsCount = 50
private val ItemSize = DpSize(144.dp, 48.dp)
