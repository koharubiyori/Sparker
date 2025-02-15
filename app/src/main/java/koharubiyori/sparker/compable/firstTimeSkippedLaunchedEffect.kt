package koharubiyori.sparker.compable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import koharubiyori.sparker.util.InitRef
import kotlinx.coroutines.CoroutineScope

@Composable
 fun FirstTimeSkippedLaunchedEffect(
  vararg keys: Any,
  block: suspend CoroutineScope.() -> Unit,
) {
  val firstRunningFlag = remember { InitRef(false) }

  LaunchedEffect(*keys) {
    if (!firstRunningFlag.value) {
      firstRunningFlag.value = true
    } else {
      block()
    }
  }
}