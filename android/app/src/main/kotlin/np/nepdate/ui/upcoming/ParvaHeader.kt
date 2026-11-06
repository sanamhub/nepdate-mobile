package np.nepdate.ui.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import np.nepdate.R
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.settings.Choice
import np.nepdate.ui.settings.ConnectedToggleGroup
import np.nepdate.ui.settings.Haptics

/** The title, the settings button, the search field and the All / Public holidays group. */
@Composable
internal fun ParvaHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: ParvaFilter,
    onFilterChange: (ParvaFilter) -> Unit,
    haptics: Haptics,
    onSettings: (() -> Unit)?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.parva_title),
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .semantics { heading() },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
            )
            if (onSettings != null) {
                val settingsLabel = stringResource(R.string.settings_title)
                IconButton(
                    onClick = onSettings,
                    shapes = IconButtonDefaults.shapes(),
                    modifier = Modifier.semantics { contentDescription = settingsLabel },
                ) {
                    SymbolIcon(icon = R.drawable.ic_sym_settings, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        ParvaSearchField(query = query, onQueryChange = onQueryChange)
        ConnectedToggleGroup(
            options =
                listOf(
                    Choice(ParvaFilter.ALL, stringResource(R.string.parva_filter_all)),
                    Choice(ParvaFilter.HOLIDAYS, stringResource(R.string.parva_filter_holidays)),
                ),
            selected = filter,
            onSelect = onFilterChange,
            haptics = haptics,
            tags = listOf("parva_filter_all", "parva_filter_holidays"),
        )
    }
}

/** The 56 dp search bar: a full-radius pill with the search symbol and a clear button. */
@Composable
private fun ParvaSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val hint = stringResource(R.string.parva_search_hint)
    val clearLabel = stringResource(R.string.parva_clear_search)
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(start = 18.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SymbolIcon(icon = R.drawable.ic_sym_search, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { contentDescription = hint }
                    .testTag("parva_search"),
            textStyle = textStyle,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = hint,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = textStyle,
                            maxLines = 1,
                        )
                    }
                    inner()
                }
            },
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.semantics { contentDescription = clearLabel },
            ) {
                SymbolIcon(icon = R.drawable.ic_sym_close, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Spacer(Modifier.width(14.dp))
        }
    }
}
