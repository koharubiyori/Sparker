package koharubiyori.sparker.screen.remote.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import koharubiyori.sparker.compable.remember.CustomSavers
import koharubiyori.sparker.compable.remember.rememberSavableMutableStateOf
import koharubiyori.sparker.compable.remember.rememberFullScreenSize
import koharubiyori.sparker.screen.remote.util.remoteScreenTapGestures

class RemoteSessionCanvasRef(
  val convertToRemotePosition: (Offset) -> Offset,
)

@Composable
fun RemoteSessionCanvas(
  modifier: Modifier = Modifier,
  frame: ImageBitmap,
  onTap: (Offset) -> Unit,
  onDoubleTap: (Offset) -> Unit,
  ref: Ref<RemoteSessionCanvasRef>? = null
) {
  val density = LocalDensity.current
  val screenSize = rememberFullScreenSize()
  var scale by rememberSaveable { mutableFloatStateOf(1f) }
  var translation by rememberSavableMutableStateOf(CustomSavers.offset, Offset.Zero)
  val sightWidth = remember(frame) { screenSize.height * (frame.width / frame.height) }
  val sightHeight = screenSize.height

  fun Offset.convertToRemotePosition(): Offset {
    val topLeftPointer = translation.copy(
      x = (scale - 1) * sightWidth / 2 - translation.x,
      y = (scale - 1) * sightHeight / 2 - translation.y
    )

    return (topLeftPointer + this) / scale
  }

  SideEffect {
    ref?.value = RemoteSessionCanvasRef(
      convertToRemotePosition = { it.convertToRemotePosition() }
    )
  }

  Box(
    modifier = Modifier
      .width(density.run { sightWidth.toDp() })
      .height(density.run { sightHeight.toDp() })
  ) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .remoteScreenTapGestures(
          onTap = { onTap(it.convertToRemotePosition()) },
          onDoubleTap = { onDoubleTap(it.convertToRemotePosition()) },
        )
        .transformable(
          state = rememberTransformableState { zoom, offset, _ ->
            fun Float.coerceInTranslatableRange(value: Float): Float {
              val boundaryValue = (scale - 1) * value / 2
              return coerceIn(-boundaryValue, boundaryValue)
            }

            scale = (scale * zoom).coerceIn(1f, 2f)
            translation = translation.copy(
              x = (translation.x + offset.x).coerceInTranslatableRange(sightWidth),
              y = (translation.y + offset.y).coerceInTranslatableRange(sightHeight)
            )
          }
        )
        .graphicsLayer(
          scaleX = scale,
          scaleY = scale,
          translationX = translation.x,
          translationY = translation.y
        )
        .then(modifier)
    ) {
      size.width
      val canvasHeight = size.height
      frame.width.toFloat()
      val imageHeight = frame.height.toFloat()

      val scale = canvasHeight / imageHeight

      withTransform({
        scale(scale, scale)
      }) {
        drawImage(frame)
      }
      drawImage(frame)
    }
  }
}