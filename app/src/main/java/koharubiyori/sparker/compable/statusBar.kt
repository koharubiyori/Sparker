package koharubiyori.sparker.compable

import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat
import koharubiyori.sparker.Globals

@Composable
fun StatusBar(
  mode: StatusBarMode = StatusBarMode.VISIBLE,
//  sticky: Boolean = false,
  backgroundColor: Color = Color.Transparent,
  darkIcons: Boolean = false,
) {
  val refStatusBarLocked = statusBarLocked   // state必须出现在composable函数的上下文中，才能正确触发组件重渲染

  fun syncConfig() {
    val window = Globals.activity.window
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

    when(mode) {
      StatusBarMode.VISIBLE -> {
        windowInsetsController.show(Type.statusBars())
      }
      StatusBarMode.HIDE -> windowInsetsController.hide(Type.statusBars())
//      StatusBarMode.STICKY -> {
//        Globals.activity.useStickyStatusBarLayout()
//      }
    }

    window.statusBarColor = backgroundColor.toArgb()
    windowInsetsController.isAppearanceLightStatusBars = darkIcons

    with(CachedStatusBarConfig) {
      CachedStatusBarConfig.mode = mode
//      CachedStatusBarConfig.sticky = sticky
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
  var sticky = false
  var backgroundColor = Color.Transparent
  var darkIcons = false
}

enum class StatusBarMode {
  VISIBLE,
  HIDE,
//  STICKY
}