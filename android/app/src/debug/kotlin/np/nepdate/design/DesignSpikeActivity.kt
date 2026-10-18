package np.nepdate.design

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import np.nepdate.R
import np.nepdate.ui.theme.NepdateTheme

/**
 * Debug-only entry point for the M0 design spike. It is deliberately not a launcher icon; run
 * `adb shell am start -n np.nepdate/.design.DesignSpikeActivity`.
 */
class DesignSpikeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NepdateTheme {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .systemBarsPadding(),
                ) {
                    var selected by rememberSaveable { mutableIntStateOf(0) }
                    PrimaryTabRow(selectedTabIndex = selected) {
                        SpikePart.entries.forEachIndexed { index, part ->
                            Tab(
                                selected = selected == index,
                                onClick = { selected = index },
                                text = { Text(tabLabel(part)) },
                            )
                        }
                    }
                    DesignSpikeScreen(part = SpikePart.entries[selected])
                }
            }
        }
    }
}

@Composable
private fun tabLabel(part: SpikePart): String =
    stringResource(
        when (part) {
            SpikePart.CELL -> R.string.design_spike_tab_cell
            SpikePart.HERO -> R.string.design_spike_tab_hero
            SpikePart.COLOUR -> R.string.design_spike_tab_colour
        },
    )
