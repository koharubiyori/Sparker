package koharubiyori.sparker

import OnComposeWillCreate
import koharubiyori.sparker.screen.scanDevices.ScanDevicesRouteArguments
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dagger.hilt.android.AndroidEntryPoint
import koharubiyori.sparker.screen.home.HomeScreen
import koharubiyori.sparker.screen.remote.RemoteRouteArguments
import koharubiyori.sparker.screen.remote.RemoteScreen
import koharubiyori.sparker.screen.scanDevices.ScanDevicesScreen
import koharubiyori.sparker.screen.settings.SettingsScreen
import koharubiyori.sparker.theme.SparkerTheme
import koharubiyori.sparker.util.animatedNavComposable.Animation
import koharubiyori.sparker.util.animatedNavComposable.composable
import koharubiyori.sparker.util.RouteArguments.Companion.formattedArguments
import koharubiyori.sparker.util.RouteArguments.Companion.formattedRouteName
import koharubiyori.sparker.util.toRouteArguments

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    Globals.activity = this
    super.onCreate(savedInstanceState)

    @Composable
    fun ContentBody() {
      SparkerTheme {
        OnComposeWillCreate {
          Routes(it)
        }
      }
    }

    setContent { ContentBody() }
  }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Routes(navHostController: NavHostController) {
  NavHost(navController = navHostController, startDestination = "home") {
    composable(
      route = "home",
      animation = Animation.PUSH,
    ) { HomeScreen() }
    composable(
      route = ScanDevicesRouteArguments::class.java.formattedRouteName,
      arguments = ScanDevicesRouteArguments::class.java.formattedArguments,
      animation = Animation.SLIDE,
    ) { ScanDevicesScreen(it.arguments!!.toRouteArguments()) }
    composable(
      route = "settings",
      animation = Animation.SLIDE,
    ) { SettingsScreen() }
//    composable(
//      route = RemoteRouteArguments::class.java.formattedRouteName,
//      arguments = RemoteRouteArguments::class.java.formattedArguments,
//      animation = Animation.SLIDE,
//    ) { RemoteScreen(RemoteRouteArguments()) }
  }
}