package np.nepdate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import np.nepdate.ui.theme.NepdateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as NepdateApplication).container
        setContent {
            NepdateTheme {
                NepdateApp(container = container, onFirstGridDrawn = { reportFullyDrawn() })
            }
        }
    }
}
