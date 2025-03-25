package koharubiyori.sparker.compable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import koharubiyori.sparker.Globals

@Composable
fun StatusBar(
  mode: StatusBarMode = StatusBarMode.VISIBLE,
  backgroundColor: Color = Color.Transparent,
  darkIcons: Boolean = false,
) {
  val refStatusBarLocked = statusBarLocked

  fun syncConfig() {
    val window = Globals.activity.window
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

    when(mode) {
      StatusBarMode.VISIBLE -> {
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
      }
      StatusBarMode.HIDE -> windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
    }

    windowInsetsController.isAppearanceLightStatusBars = darkIcons

    with(CachedStatusBarConfig) {
      CachedStatusBarConfig.mode = mode
      CachedStatusBarConfig.backgroundColor = backgroundColor
      CachedStatusBarConfig.darkIcons = darkIcons
    }
  }

  LaunchedEffect(
    mode,
    backgroundColor,
    darkIcons,
    refStatusBarLocked
  ) {
    if (refStatusBarLocked) return@LaunchedEffect
    syncConfig()
  }

  LifecycleEventEffect(
    onResume = {
      if (refStatusBarLocked) return@LifecycleEventEffect
      syncConfig()
    }
  )
}

var statusBarLocked by mutableStateOf(false)
object CachedStatusBarConfig {
  var mode = StatusBarMode.VISIBLE
  var backgroundColor = Color.Transparent
  var darkIcons = false
}

enum class StatusBarMode {
  VISIBLE,
  HIDE,
}