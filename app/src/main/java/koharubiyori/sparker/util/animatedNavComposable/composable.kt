package koharubiyori.sparker.util.animatedNavComposable


import AnimatedNavUtil
import AnimatedNavUtil.getRouteName
import AnimatedNavUtil.isRoutePop
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import koharubiyori.sparker.util.debugPrint
import kotlin.collections.set

@ExperimentalAnimationApi
fun NavGraphBuilder.composable(
  route: String,
  animation: Animation,
  arguments: List<NamedNavArgument> = emptyList(),
  deepLinks: List<NavDeepLink> = emptyList(),
  content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
  routeMetas[getRouteName(route)] = RouteMeta(
    animation = animation
  )

  val transitions = getTransitions(animation)

  composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = transitions.enterTransition,
    exitTransition = transitions.exitTransition,
    popEnterTransition = transitions.popEnterTransition,
    popExitTransition = transitions.popExitTransition,
    content = { currentEntry ->
      content(currentEntry)
    // The code here achieves appending an animated container to the screens during transition,
    // such as showing a gradual black mask while the transition is playing.
    // There is a bug: if the user presses the back button while the transition is in progress, the container will not be appended.
    // Then and performance issues cause by reflection

//      val isPush = !isRoutePop()
//      val transitionsInProgress = AnimatedNavUtil.getTransitionsInProgress().toList()
//      val targetEntry = transitionsInProgress.lastOrNull()
//
//      if (targetEntry == null) {
//        content(currentEntry)
//        return@composable
//      }
//
//      val isTargetEntry = currentEntry == targetEntry
//      val prevEntryIndex = transitionsInProgress.indexOfFirst { it == currentEntry } - 1
//      val prevEntry = transitionsInProgress.getOrNull(prevEntryIndex)
//
//      val currentEntryTransitions = transitions
//      val targetEntryTransitions = getTransitions(routeMetas[getRouteName(targetEntry.destination.route!!)]!!.animation)
//      val prevEntryTransitions = prevEntry?.let { getTransitions(routeMetas[getRouteName(it.destination.route!!)]!!.animation) }
//
//      // If the user presses press back, isPush will be false but the order of transitionsInProgress array will not change following the transition orders.
//      // It breaks the logic that depends on the the order of transitionsInProgress
//      val DecorationContainer = when {
//        isPush && isTargetEntry -> currentEntryTransitions.decorationContainers?.enterTransition
//        isPush && !isTargetEntry -> targetEntryTransitions.decorationContainers?.pairedExitTransitionOnEnter
//        !isPush && isTargetEntry -> prevEntryTransitions?.decorationContainers?.pairedPopEnterTransitionOnExit
//        else -> currentEntryTransitions.decorationContainers?.popExitTransition
//      }
//
//      if (DecorationContainer != null) {
//        DecorationContainer { content(currentEntry) }
//      } else {
//        content(currentEntry)
//      }
    },
  )
}

