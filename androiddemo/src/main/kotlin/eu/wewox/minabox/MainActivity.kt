package eu.wewox.minabox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat

/**
 * Main activity for demo application.
 * Contains simple "Crossfade" based navigation to various examples.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            App(backHandler = { example, onBackPressed ->
                BackHandler(enabled = example != null) {
                    onBackPressed()
                }
            })
        }
    }
}
