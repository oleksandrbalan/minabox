package eu.wewox.minabox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Main activity for demo application.
 * Contains simple "Crossfade" based navigation to various examples.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val sysUiController = rememberSystemUiController()
            SideEffect {
                sysUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = !darkTheme,
                    isNavigationBarContrastEnforced = false
                )
            }

            var example by rememberSaveable { mutableStateOf<Example?>(null) }
            BackHandler(enabled = example != null) {
                example = null
            }
            App(
                example = example,
                onChangeExample = { example = it },
            )
        }
    }
}
