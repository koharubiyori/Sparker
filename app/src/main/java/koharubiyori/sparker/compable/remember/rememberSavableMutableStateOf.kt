package koharubiyori.sparker.compable.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset

@Composable
fun <T> rememberSavableMutableStateOf(
  saver: Saver<T, out Any>,
  initialValue: T,
): MutableState<T> {
  return rememberSaveable(stateSaver = saver) {
    mutableStateOf(initialValue)
  }
}

object CustomSavers {
  val offset = listSaver<Offset, Float>(
    save = { listOf(it.x, it.y) },
    restore = { Offset(it[0], it[1]) }
  )
}