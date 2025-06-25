package koharubiyori.sparker.screen.remote

import android.content.pm.ActivityInfo
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mouse
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import koharubiyori.sparker.Globals
import koharubiyori.sparker.compable.remember.rememberFullScreenSize
import koharubiyori.sparker.component.ExpandedCenter
import koharubiyori.sparker.jni.JniFreeRDP.CursorFlags
import koharubiyori.sparker.screen.remote.component.RemoteSessionCanvas
import koharubiyori.sparker.screen.remote.component.VirtualMouse
import koharubiyori.sparker.util.getScreenRoundedCornerRadius
import koharubiyori.sparker.util.noRippleClickable
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteScreen(arguments: RemoteRouteArguments) {
  val model: RemoteScreenModel = hiltViewModel()
  val currentFrame = model.remoteSession.frameFlow.collectAsStateWithLifecycle(null).value
  val edgePaddingDistance = 50f  // Relax the trigger condition to prevent the mouse from needing to reach the edge before being triggered
  val animatedOffsetOnCursorNearEdge by animateOffsetAsState(
    Offset(
      x = if (model.cursorNearHorizontalEdge) model.computedVirtualMouseSize.width.toFloat() + edgePaddingDistance else 0f,
      y = if (model.cursorNearVerticalEdge) model.computedVirtualMouseSize.height.toFloat() + edgePaddingDistance else 0f
    )
  )

  DisposableEffect(Unit) {
    Globals.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    val window = Globals.activity.window
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    windowInsetsController.systemBarsBehavior =
      WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    onDispose {
      windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
      windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }
  }

  ExpandedCenter(
    modifier = Modifier
      .background(Color.Black)
  ) {
    if (currentFrame == null) return@ExpandedCenter
    RemoteSessionCanvas(
      modifier = Modifier
        .onSizeChanged { model.computedCanvasSize = it }
        .graphicsLayer(
          translationX = -animatedOffsetOnCursorNearEdge.x,
          translationY = -animatedOffsetOnCursorNearEdge.y
        ),
      frame = currentFrame,
      onTap = { model.remoteSession.sendClick(it) },
      onDoubleTap = { model.remoteSession.sendDoubleClick(it) },
      ref = model.remoteSessionCanvasRef
    )

    if (!model.visibleVirtualMouse) {
      Icon(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .absoluteOffset(x = -(30).dp)
          .alpha(0.5f)
          .noRippleClickable { model.visibleVirtualMouse = true },
        imageVector = Icons.Rounded.Mouse,
        contentDescription = null,
        tint = Color.White
      )
    } else {
      ComposedVirtualMouse(edgePaddingDistance, animatedOffsetOnCursorNearEdge)
    }
  }
}

@Composable
private fun BoxScope.ComposedVirtualMouse(
  edgePaddingDistance: Float,
  extraCursorOffset: Offset,
) {
  val model: RemoteScreenModel = hiltViewModel()
  val screenSize = rememberFullScreenSize()
  val cursorOffsetForScreenCornerRadius = remember { Offset(getScreenRoundedCornerRadius().toFloat(), 0f) }

  LaunchedEffect(model.computedVirtualMouseSize, model.currentCursorPosition) {
    val remotePosition = model.remoteSessionCanvasRef.value!!.convertToRemotePosition(model.currentCursorPosition)


    model.cursorNearHorizontalEdge = when {
      !model.cursorNearHorizontalEdge &&
          remotePosition.x >= model.computedCanvasSize.width - model.computedVirtualMouseSize.width - edgePaddingDistance -> true

      model.cursorNearHorizontalEdge &&
          model.currentCursorPosition.x <= screenSize.width - model.computedVirtualMouseSize.width * 3 -> false

      else -> model.cursorNearHorizontalEdge
    }

    model.cursorNearVerticalEdge = when {
      !model.cursorNearVerticalEdge &&
          remotePosition.y >= model.computedCanvasSize.height - model.computedVirtualMouseSize.height - edgePaddingDistance -> true

      model.cursorNearVerticalEdge &&
          model.currentCursorPosition.y <= screenSize.height - model.computedVirtualMouseSize.height * 2.5 -> false

      else -> model.cursorNearVerticalEdge
    }
  }

  fun Offset.convertToRemotePosition(): Offset {
    return model.remoteSessionCanvasRef.value!!.convertToRemotePosition(this - cursorOffsetForScreenCornerRadius) +
      Offset(
        x = if (model.cursorNearHorizontalEdge) extraCursorOffset.x else 0f,
        y = if (model.cursorNearVerticalEdge) extraCursorOffset.y else 0f
      )
  }

  VirtualMouse(
    modifier = Modifier
      .zIndex(1f)
      .onSizeChanged { model.computedVirtualMouseSize = it },
    cursorPosition = model.currentCursorPosition,
    onCursorPositionChange = {
      model.currentCursorPosition = it
      model.remoteSession.sendCursorMove(it.convertToRemotePosition())
    },
    onLeftPress = {
      model.remoteSession.sendCursorEvent(
        it.convertToRemotePosition(),
        CursorFlags.LBUTTON or CursorFlags.DOWN
      )
    },
    onLeftRelease = {
      model.remoteSession.sendCursorEvent(
        it.convertToRemotePosition(),
        CursorFlags.LBUTTON or CursorFlags.UP
      )
    },
    onRightPress = {
      model.remoteSession.sendCursorEvent(
        it.convertToRemotePosition(),
        CursorFlags.RBUTTON or CursorFlags.DOWN
      )
    },
    onRightRelease = {
      model.remoteSession.sendCursorEvent(
        it.convertToRemotePosition(),
        CursorFlags.RBUTTON or CursorFlags.UP
      )
    },
    onHide = { model.visibleVirtualMouse = false },
    onScroll = { speed ->
      repeat(abs(speed * 5).roundToInt()) {
        model.remoteSession.sendScroll(speed < 0)
      }
    }
  )
}