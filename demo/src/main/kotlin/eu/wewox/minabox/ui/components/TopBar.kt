package eu.wewox.minabox.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.wewox.minabox.ui.theme.SpacingMedium

/**
 * The reusable component for top bar.
 *
 * @param title The text to show in top bar.
 * @param modifier The modifier instance for the root composable.
 */
@Composable
fun TopBar(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .padding(SpacingMedium)
            .statusBarsPadding()
    )
}
