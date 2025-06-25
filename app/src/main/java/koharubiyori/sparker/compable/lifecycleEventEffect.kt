package koharubiyori.sparker.compable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun LifecycleEventEffect(
  onResume: (() -> Unit)? = null,
  onCreate: (() -> Unit)? = null,
  onStart: (() -> Unit)? = null,
  onPause: (() -> Unit)? = null,
  onStop: (() -> Unit)? = null,
  onDestroy: (() -> Unit)? = null,
  onAny: (() -> Unit)? = null,
) {
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when(event) {
        Lifecycle.Event.ON_RESUME -> onResume?.invoke()
        Lifecycle.Event.ON_CREATE -> onCreate?.invoke()
        Lifecycle.Event.ON_START -> onStart?.invoke()
        Lifecycle.Event.ON_PAUSE -> onPause?.invoke()
        Lifecycle.Event.ON_STOP -> onStop?.invoke()
        Lifecycle.Event.ON_DESTROY -> onDestroy?.invoke()
        Lifecycle.Event.ON_ANY -> onAny?.invoke()
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
}