import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledPullToRefreshBox(
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  state: PullToRefreshState = rememberPullToRefreshState(),
  contentAlignment: Alignment = Alignment.TopStart,
  content: @Composable BoxScope.() -> Unit
) {
  PullToRefreshBox(
    modifier = Modifier
      .then(modifier),
    isRefreshing = isRefreshing,
    state = state,
    contentAlignment = contentAlignment,
    onRefresh = onRefresh,
    indicator = {
      Indicator(
        modifier = Modifier.align(Alignment.TopCenter),
        isRefreshing = isRefreshing,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        state = state
      )
    },
    content = content
  )
}