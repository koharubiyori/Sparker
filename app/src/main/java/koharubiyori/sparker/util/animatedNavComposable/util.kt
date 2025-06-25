import AnimatedNavUtil.getRouteName
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.get
import koharubiyori.sparker.Globals
import koharubiyori.sparker.util.animatedNavComposable.RouteMeta
import koharubiyori.sparker.util.animatedNavComposable.routeMetas

object AnimatedNavUtil {
  fun getRouteName(route: String): String {
    return route.split("?")[0]
  }

  @Composable
  fun isRoutePop(): Boolean {
    val composeNavigator = getComposeNavigator()
    val resultValue by remember {
      composeNavigator::class.java.getDeclaredField("isPop").let {
        it.isAccessible = true
        it.get(composeNavigator) as State<Boolean>
      }
    }

    return resultValue
  }

//  @Composable
//  fun getTransitionsInProgress(): Set<NavBackStackEntry> {
////    val navigator = getComposeNavigator()
////    val transitionsInProgress by remember(navigator) {
////      navigator.readSelfSecretProperty<StateFlow<Set<NavBackStackEntry>>>("transitionsInProgress")
////    }.collectAsState()
////
////    return transitionsInProgress
////  }
////
////  fun AnimatedContentScope.getSimpleRouteTransitionState(): SimpleRouteTransitionState {
////    val segment = transition.segment
////    return when {
////      segment.initialState.name == "PreEnter" && segment.targetState.name == "Visible" -> SimpleRouteTransitionState.ENTER
////      segment.initialState.name == "Visible" && segment.targetState.name == "PostExit" -> SimpleRouteTransitionState.EXIT
////      segment.initialState.name == "Visible" && segment.targetState.name == "Visible" -> SimpleRouteTransitionState.VISIBLE
////      else -> error("unknown transition")
////    }
//  }

  enum class SimpleRouteTransitionState {
    VISIBLE, ENTER, EXIT
  }
}

@ExperimentalAnimationApi
val AnimatedContentTransitionScope<NavBackStackEntry>.targetRouteMeta: RouteMeta
  get() {
    val routeName = getRouteName(targetState.destination.route!!)
    return routeMetas[routeName]!!
  }

@ExperimentalAnimationApi
val AnimatedContentTransitionScope<NavBackStackEntry>.initialRouteMeta: RouteMeta
  get() {
    val routeName = getRouteName(initialState.destination.route!!)
    return routeMetas[routeName]!!
  }

private fun getComposeNavigator(): ComposeNavigator {
  val composableName = ComposeNavigator::class.java.getDeclaredField("NAME").let {
    it.isAccessible = true
    it.get(null) as String
  }

  return Globals.navController.navigatorProvider.get(composableName)
}