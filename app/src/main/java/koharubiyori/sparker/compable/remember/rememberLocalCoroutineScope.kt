package koharubiyori.sparker.compable.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

// Instance of CoroutineScopeState scoped
class LocalCoroutineScopeState(dispatcher: CoroutineContext = Dispatchers.Main) {
  private val coroutine = CoroutineScope(dispatcher)

  @Composable
  fun Provider(content: @Composable () -> Unit) {
    CompositionLocalProvider(
      LocalCoroutineScope provides coroutine,
      content = content
    )
  }

  fun cancel() {
    coroutine.cancel()
  }
}

// Composable function scoped
@Composable
fun LocalCoroutineScopeProvider(content: @Composable () -> Unit) {
  val coroutine = rememberCoroutineScope()

  CompositionLocalProvider(
    LocalCoroutineScope provides coroutine,
    content = content
  )
}

@Composable
fun rememberLocalCoroutineScope() = LocalCoroutineScope.current

val LocalCoroutineScope = staticCompositionLocalOf<CoroutineScope> { error("No LocalCoroutineScope provided") }