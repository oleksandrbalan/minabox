package eu.wewox.minabox.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The reusable component for top bar.
 *
 * @param title The text to show in top bar.
 * @param modifier The modifier instance for the root composable.
 */
@Composable
expect fun TopBar(title: String, modifier: Modifier = Modifier)
