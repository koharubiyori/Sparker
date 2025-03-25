package koharubiyori.sparker.compable.remember

import android.view.WindowManager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import koharubiyori.sparker.Globals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberFullScreenSize(): Size {
  val configuration = LocalConfiguration.current

  return remember(configuration.orientation) {
    val windowBounds = Globals.activity.getSystemService(WindowManager::class.java).currentWindowMetrics.bounds
    Size(windowBounds.width().toFloat(), windowBounds.height().toFloat())
  }
}