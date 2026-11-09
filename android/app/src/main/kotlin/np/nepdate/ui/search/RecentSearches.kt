package np.nepdate.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import np.nepdate.R
import np.nepdate.data.RecentSearchStore
import np.nepdate.data.withRecent
import np.nepdate.ui.common.SymbolIcon

/** The recent searches and how to change them. */
internal class RecentSearches(
    val queries: List<String>,
    val add: (String) -> Unit,
    val clear: () -> Unit,
)

/**
 * The recent searches from [store], kept on the device across launches. Without a store they last
 * only as long as the process, as before the store existed.
 */
@Composable
internal fun rememberRecentSearches(store: RecentSearchStore?): RecentSearches {
    val scope = rememberCoroutineScope()
    if (store == null) {
        return RecentSearches(
            queries = SessionRecent.queries.toList(),
            add = { query ->
                val updated = withRecent(SessionRecent.queries, query)
                SessionRecent.queries.clear()
                SessionRecent.queries.addAll(updated)
            },
            clear = { SessionRecent.queries.clear() },
        )
    }
    val queries by store.queries.collectAsStateWithLifecycle(initialValue = emptyList())
    return RecentSearches(
        queries = queries,
        add = { query -> scope.launch { store.remember(query) } },
        clear = { scope.launch { store.clear() } },
    )
}

/** Recent searches for this process only, used when no store is wired. */
private object SessionRecent {
    val queries = mutableStateListOf<String>()
}

/** The recent searches as assist chips, with a button that clears them. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecentSearchChips(
    recent: RecentSearches,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.search_recent),
                modifier = Modifier.padding(start = 4.dp).weight(1f).semantics { heading() },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            TextButton(
                onClick = recent.clear,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.heightIn(min = 48.dp).testTag("search_clear_recent"),
            ) {
                Text(stringResource(R.string.search_clear_recent))
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            recent.queries.forEach { query ->
                AssistChip(
                    onClick = { onPick(query) },
                    label = { Text(query) },
                    leadingIcon = {
                        SymbolIcon(R.drawable.ic_sym_history, size = AssistChipDefaults.IconSize)
                    },
                )
            }
        }
    }
}
