package eu.wewox.minabox

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import eu.wewox.minabox.screens.MinaBoxAdvancedScreen
import eu.wewox.minabox.screens.MinaBoxContentPaddingScreen
import eu.wewox.minabox.screens.MinaBoxSimpleScreen
import eu.wewox.minabox.ui.theme.MinaBoxTheme

@Composable
fun App() {
    var example by rememberSaveable { mutableStateOf<Example?>(null) }
    App(
        example = example,
        onChangeExample = { example = it },
    )
}

@Composable
fun App(
    example: Example?,
    onChangeExample: (Example?) -> Unit,
) {
    MinaBoxTheme {
        val reset = { onChangeExample(null) }

        Crossfade(example) { selected ->
            when (selected) {
                null -> RootScreen(onExampleClick = onChangeExample)
                Example.MinaBoxSimple -> MinaBoxSimpleScreen(reset)
                Example.MinaBoxContentPadding -> MinaBoxContentPaddingScreen(reset)
                Example.MinaBoxAdvanced -> MinaBoxAdvancedScreen(reset)
            }
        }
    }
}
