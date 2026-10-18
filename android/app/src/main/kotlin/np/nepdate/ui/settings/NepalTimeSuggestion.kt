package np.nepdate.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.KATHMANDU
import np.nepdate.R
import np.nepdate.data.DateFollows
import np.nepdate.data.Settings
import java.time.ZoneId

/** Whether the first-launch "Nepal time" suggestion should show (MR-06 AC1). */
internal fun shouldSuggestNepalTime(
    settings: Settings,
    deviceZone: ZoneId,
): Boolean =
    !settings.dateFollowsSuggestionHandled &&
        settings.dateFollows == DateFollows.DEVICE &&
        deviceZone != KATHMANDU

/** The once-only first-launch suggestion. Accepting switches the zone and both actions mark it handled. */
@Composable
internal fun NepalTimeSuggestion(
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("nepal_time_suggestion"),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_nepal_time_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_nepal_time_body),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier =
                        Modifier
                            .heightIn(min = 48.dp)
                            .testTag("nepal_time_not_now"),
                ) {
                    Text(stringResource(R.string.settings_nepal_time_not_now))
                }
                Button(
                    onClick = onAccept,
                    modifier =
                        Modifier
                            .heightIn(min = 48.dp)
                            .testTag("nepal_time_accept"),
                ) {
                    Text(stringResource(R.string.settings_nepal_time_accept))
                }
            }
        }
    }
}
