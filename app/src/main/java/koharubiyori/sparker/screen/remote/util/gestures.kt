package koharubiyori.sparker.screen.remote.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.remoteScreenTapGestures(
  onTap: (position: Offset) -> Unit,
  onDoubleTap: (position: Offset) -> Unit,
): Modifier {
  return this
    .pointerInput(onDoubleTap) {
      detectTapGestures(
        onDoubleTap = { onDoubleTap(it) }
      )
    }
    // A tap detector with faster reaction than detectTapGestures.onTap
    .pointerInput(onTap) {
      awaitEachGesture {
        val down = awaitFirstDown()
        withTimeoutOrNull(100) { waitForUpOrCancellation() }?.let {
          down.consume()
          it.consume()
          onTap(down.position)
        }
      }
    }
}