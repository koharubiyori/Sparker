package koharubiyori.sparker.util.animatedNavComposable

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import initialRouteMeta
import targetRouteMeta
import kotlin.math.roundToInt

val routeMetas = mutableMapOf<String, RouteMeta>()

enum class Animation {
  SLIDE,
  PUSH,
  FADE,
  EXPANDED,
  NONE,
}

class RouteMeta(
  val animation: Animation
)

@ExperimentalAnimationApi
fun getTransitions(animation: Animation): Transitions = when(animation) {
  Animation.SLIDE -> {
    val animationSpec = tween<IntOffset>(400)

    Transitions(
      enterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec)
      },
      popExitTransition = {
        slideOutOfContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.Right,
          animationSpec = animationSpec,
        )
      },
      pairedExitTransitionOnEnter = {
        slideOutOfContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.Left,
          animationSpec = animationSpec,
//          targetOffset = { (0.2 * it).roundToInt() }
        )
      },
      pairedPopEnterTransitionOnExit = {
        slideIntoContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.Right,
          animationSpec = animationSpec,
//          initialOffset = { (0.2 * it).roundToInt() }
        )
      },
//      decorationContainers = TransitionDecorationContainers(
//        pairedExitTransitionOnEnter = createMaskContainer(tween(animationSpec.durationMillis)),
//        pairedPopEnterTransitionOnExit = createMaskContainer(
//          tween(animationSpec.durationMillis),
//          reversed = true
//        )
//      ),
    )
  }

  Animation.PUSH -> {
    val durationMillis = 350
    val enterEasing = CubicBezierEasing(0.25f,0.1f,0.25f,1f)
    val exitEasing = CubicBezierEasing(1f,0f,0.54f,0.97f)
    val fadeAnimationSpec = { easing: Easing ->
      TweenSpec<Float>(
        durationMillis = durationMillis,
        easing = easing
      )
    }
    val slideAnimationSpec = { easing: Easing ->
      TweenSpec<IntOffset>(
        durationMillis = durationMillis,
        easing = easing
      )
    }

    Transitions(
      enterTransition = {
        fadeIn(fadeAnimationSpec(enterEasing)) +
            slideInVertically(
              animationSpec = slideAnimationSpec(enterEasing),
              initialOffsetY = { fullHeight -> fullHeight }
            )
      },
      popExitTransition = {
        fadeOut(fadeAnimationSpec(exitEasing)) +
            slideOutVertically(
              animationSpec = slideAnimationSpec(exitEasing),
              targetOffsetY = { fullHeight -> fullHeight }
            )
      },
      pairedExitTransitionOnEnter = {
        fadeOut(
          TweenSpec(
            durationMillis = 1,
            delay = durationMillis,
          )
        )
      },
      pairedPopEnterTransitionOnExit = {
        fadeIn(
          TweenSpec(
            durationMillis = durationMillis,
          )
        )
      }
    )
  }

  Animation.FADE -> {
    val animationSpec = TweenSpec<Float>(
      durationMillis = 350,
    )

    Transitions(
      enterTransition = {
        fadeIn(animationSpec)
      },
      popExitTransition = {
        fadeOut(animationSpec)
      },
      pairedExitTransitionOnEnter = {
        fadeOut(animationSpec)
      },
      pairedPopEnterTransitionOnExit = {
        fadeIn(animationSpec)
      },
    )
  }

  Animation.EXPANDED -> {
    val animationSpec = TweenSpec<Float>(
      durationMillis = 350,
    )

    Transitions(
      enterTransition = {
        fadeIn(animationSpec) +
            scaleIn(
              animationSpec = animationSpec,
              initialScale = 0.5f
            )
      },
      popExitTransition = {
        fadeOut(animationSpec) +
            scaleOut(
              animationSpec = animationSpec,
              targetScale = 0.5f
            )
      },
      pairedExitTransitionOnEnter = {
        fadeOut(snap(animationSpec.durationMillis))
      },
      pairedPopEnterTransitionOnExit = {
        fadeIn(snap(0))
      },
    )
  }

  Animation.NONE -> Transitions(
    enterTransition = {
      EnterTransition.None
    },
    popExitTransition = {
      ExitTransition.None
    },
    pairedExitTransitionOnEnter = {
      ExitTransition.None
    },
    pairedPopEnterTransitionOnExit = {
      EnterTransition.None
    },
  )
}

typealias DecorationContainer = @Composable (content: @Composable () -> Unit) -> Unit

data class TransitionDecorationContainers(
  val enterTransition: DecorationContainer? = null,
  val popExitTransition: DecorationContainer? = null,
  val pairedExitTransitionOnEnter: DecorationContainer? = null,
  val pairedPopEnterTransitionOnExit: DecorationContainer? = null
)

@ExperimentalAnimationApi
class Transitions(
  val enterTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?,
  val exitTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?,
  val popEnterTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?,
  val popExitTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?,

  // The transition for current screen to exit, while new screen applied this transitions is entering
  val pairedExitTransitionOnEnter: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?),
  // While current screen applied this transitions is exiting
  val pairedPopEnterTransitionOnExit: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?),
  val decorationContainers: TransitionDecorationContainers? = null,
) {
  constructor(
    enterTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?,
    popExitTransition: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?,
    pairedExitTransitionOnEnter: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?),
    pairedPopEnterTransitionOnExit: @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?),
    decorationContainers: TransitionDecorationContainers? = null,
  ) : this(
    enterTransition = enterTransition,
    exitTransition = {
      getTransitions(targetRouteMeta.animation).pairedExitTransitionOnEnter(this)
    },
    popEnterTransition = {
      getTransitions(initialRouteMeta.animation).pairedPopEnterTransitionOnExit(this)
    },
    popExitTransition = popExitTransition,
    pairedExitTransitionOnEnter = pairedExitTransitionOnEnter,
    pairedPopEnterTransitionOnExit = pairedPopEnterTransitionOnExit,
    decorationContainers = decorationContainers
  )
}

private fun createMaskContainer(
  animationSpec: AnimationSpec<Float>,
  reversed: Boolean = false,
): DecorationContainer {
  return {
    val animationValue = remember { Animatable(if (reversed) 1f else 0f) }

    LaunchedEffect(Unit) {
      animationValue.animateTo(
        animationSpec = animationSpec,
        targetValue = if (reversed) 0f else 1f
      )
    }

    Box {
      it()
      Spacer(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.7f * animationValue.value))
      )
    }
  }
}

