package koharubiyori.sparker.screen.remote.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import koharubiyori.sparker.component.HoverBgIndicationFactory
import koharubiyori.sparker.component.HoverBgIndicationProvider
import koharubiyori.sparker.util.BorderSide
import koharubiyori.sparker.util.noRippleClickable
import koharubiyori.sparker.util.onAllFingerRelease
import koharubiyori.sparker.util.sideBorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BoxScope.VirtualMouse(
  modifier: Modifier = Modifier,
  onLeftPress: (Offset) -> Unit,
  onLeftRelease: (Offset) -> Unit,
  onRightPress: (Offset) -> Unit,
  onRightRelease: (Offset) -> Unit,
  onScroll: (Float) -> Unit,
  cursorPosition: Offset,
  onCursorPositionChange: (Offset) -> Unit,
  onHide: () -> Unit,
) {
  val colorScheme = MaterialTheme.colorScheme
  val density = LocalDensity.current

  val currentCursorPosition by rememberUpdatedState(cursorPosition)
  var computedContainerSize by remember { mutableStateOf(IntSize.Zero) }
  var disabledCursorMovingForScrollCapsule by remember { mutableStateOf(false) }

  HoverBgIndicationProvider {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
    ) {
      val parentWidth = this.maxWidth.let { density.run { it.toPx() } }
      val parentHeight = this.maxHeight.let { density.run { it.toPx() } }

      Row(
        modifier = Modifier
          .align(Alignment.TopStart)
          .absoluteOffset(density.run { cursorPosition.x.toDp() }, density.run { cursorPosition.y.toDp() })
          .pointerInput(Unit) {
            awaitEachGesture {
              var downPointer = awaitFirstDown()
              while (true) {
                if (disabledCursorMovingForScrollCapsule) return@awaitEachGesture
                var event = awaitDragOrCancellation(downPointer.id)
                if (event == null) return@awaitEachGesture
                if (event.changedToUp()) return@awaitEachGesture

                val positionChange = event.positionChange()
                val newPosition = currentCursorPosition.copy(
                  x = (currentCursorPosition.x + positionChange.x).coerceIn(0f, parentWidth - computedContainerSize.width),
                  y = (currentCursorPosition.y + positionChange.y).coerceIn(0f, parentHeight - computedContainerSize.height)
                )
                onCursorPositionChange(newPosition)
              }
            }
          }
          .then(modifier)
          .onSizeChanged { computedContainerSize = it }
      ) {
        // Cursor
        Icon(
          modifier = Modifier
            .size(25.dp)
          ,
          imageVector = remember { createCursorImage(colorScheme.primary) },
          contentDescription = null,
          tint = Color.Unspecified
        )

        MouseBody(
          onLeftPress = { onLeftPress(cursorPosition) },
          onLeftRelease = { onLeftRelease(cursorPosition) },
          onRightPress = { onRightPress(cursorPosition) },
          onRightRelease = { onRightRelease(cursorPosition) },
          onScroll = { onScroll(it) },
          onRequestDisableCursorMoving = { disabledCursorMovingForScrollCapsule = it }
        )

        Icon(
          modifier = Modifier
            .padding(start = 10.dp)
            .size(20.dp)
            .noRippleClickable { onHide() },
          imageVector = Icons.Rounded.HighlightOff,
          contentDescription = null,
          tint = Color.White
        )
      }
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MouseBody(
  onLeftPress: () -> Unit,
  onLeftRelease: () -> Unit,
  onRightPress: () -> Unit,
  onRightRelease: () -> Unit,
  onScroll: (Float) -> Unit,
  onRequestDisableCursorMoving: (Boolean) -> Unit,
) {
  var scrollMode by remember { mutableStateOf(false) }
  val scrollCapsuleRef = remember { Ref<ScrollCapsuleRef>() }
  val roundedCornerValues = object {
    val top = 50.dp
    val end = 40.dp

    fun toBorderShape() = RoundedCornerShape(top, top, end, end)
    fun toBackgroundShape() = RoundedCornerShape(top - 1.dp, top - 1.dp, end - 1.dp, end - 1.dp)
  }

  LaunchedEffect(scrollMode) {
    onRequestDisableCursorMoving(scrollMode)
  }

  LaunchedEffect(scrollMode) {
    if (!scrollMode) return@LaunchedEffect
  }

  Box(
    modifier = Modifier
      .padding(start = 5.dp)
      .width(100.dp)
      .height(120.dp)
      .pointerInput(Unit) {
        awaitEachGesture {
          val down = awaitFirstDown()
          if (!scrollMode) return@awaitEachGesture
          val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
            scrollCapsuleRef.value!!.updateIndicatorPosition(change.positionChange().y)
          }
          if (drag == null) scrollMode = false
        }
      },
    contentAlignment = Alignment.Center
  ) {
    if (!scrollMode) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .clip(roundedCornerValues.toBorderShape())
          .background(
            color = MaterialTheme.colorScheme.background,
            shape = roundedCornerValues.toBackgroundShape()
          )
          .border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = roundedCornerValues.toBorderShape()
          ),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .weight(2f)
        ) {
          // Left button
          Spacer(
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .mouseButton(
                onPress = { onLeftPress() },
                onRelease = { onLeftRelease() },
              )
          )
          Spacer(
            Modifier
              .width(2.dp)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.onSurfaceVariant)
          )
          // Right button
          Spacer(
            Modifier
              .weight(1f)
              .fillMaxHeight()
              .mouseButton(
                onPress = { onRightPress() },
                onRelease = { onRightRelease() },
              )
          )
        }

        Spacer(
          Modifier
            .fillMaxWidth()
            .weight(1f)
            .sideBorder(BorderSide.TOP, 2.dp, MaterialTheme.colorScheme.onSurfaceVariant)
        )
      }

      // Wheel
      Column(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .zIndex(1f)
          .absoluteOffset(y = 25.dp)
          .width(20.dp)
          .height(25.dp)
          .clip(RoundedCornerShape(5.dp))
          .background(MaterialTheme.colorScheme.background)
          .border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(5.dp)
          )
          .padding(vertical = 2.dp)
          .pointerInput(Unit) {
            awaitEachGesture {
              awaitFirstDown()
              scrollMode = true
            }
          }
      ) {
        repeat(3) {
          Spacer(
            Modifier
              .fillMaxWidth()
              .weight(1f)
              .then(
                if (it < 2)
                  Modifier.sideBorder(
                    BorderSide.BOTTOM,
                    2.dp,
                    MaterialTheme.colorScheme.onSurfaceVariant
                  )
                else Modifier
              )
          )
        }
      }
    } else {
      ScrollCapsule(
        ref = scrollCapsuleRef,
        onScroll = onScroll
      )
    }
  }
}

@Composable
private fun Modifier.mouseButton(
  onPress: () -> Unit,
  onRelease: () -> Unit,
) = composed {
  val indication = LocalIndication.current as HoverBgIndicationFactory
  val coroutine = rememberCoroutineScope()
  val currentOnPress by rememberUpdatedState(onPress)
  val currentOnRelease by rememberUpdatedState(onRelease)
  val leftButtonInteractionSource = remember { MutableInteractionSource() }
  val firstDownInteractionRef = remember { Ref<PressInteraction.Press>() }

  this
    .indication(leftButtonInteractionSource, indication)
    .pointerInput(currentOnPress, currentOnRelease) {
      awaitEachGesture {
        val down = awaitFirstDown()
        firstDownInteractionRef.value = PressInteraction.Press(down.position)
        coroutine.launch { leftButtonInteractionSource.emit(firstDownInteractionRef.value!!) }
        currentOnPress()
      }
    }
    // For unknown reasons, awaitPointerEvent() after currentOnPress() is not working. It can't wait for any events.
    .onAllFingerRelease {
      coroutine.launch {
        leftButtonInteractionSource.emit(PressInteraction.Release(firstDownInteractionRef.value!!))
      }
      currentOnRelease()
    }
}

class ScrollCapsuleRef(
  val updateIndicatorPosition: (Float) -> Unit,
)

@Composable
private fun ScrollCapsule(
  modifier: Modifier = Modifier,
  ref: Ref<ScrollCapsuleRef>? = null,
  onScroll: (Float) -> Unit
) {
  val density = LocalDensity.current
  var indicatorPosition by remember { mutableFloatStateOf(0f) }

  val containerHeightDp = 70.dp
  val indicatorSizeDp = 35.dp
  val containerHeight = density.run { containerHeightDp.toPx() }
  val indicatorSize = density.run { indicatorSizeDp.toPx() }
  val indicatorMargin = density.run { 2.5.dp.toPx() }
  val maxIndicatorPosition = (containerHeight - indicatorSize) / 2 - indicatorMargin

  ref?.value = ScrollCapsuleRef(
    updateIndicatorPosition = {
      indicatorPosition = (it + indicatorPosition).coerceIn(-maxIndicatorPosition, maxIndicatorPosition)
    }
  )

  LaunchedEffect(true) {
    while (true) {
      delay(100)
      val speed = indicatorPosition / maxIndicatorPosition
      if (speed != 0f) onScroll(-indicatorPosition / maxIndicatorPosition)
    }
  }

  Box(
    modifier = Modifier
      .width(40.dp)
      .height(containerHeightDp)
      .clip(CircleShape)
      .alpha(0.75f)
      .background(MaterialTheme.colorScheme.background)
      .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    Spacer(Modifier
      .size(indicatorSizeDp)
      .graphicsLayer(
        translationY = indicatorPosition
      )
      .clip(CircleShape)
      .background(MaterialTheme.colorScheme.primary)
    )
  }
}

// To allow setting colors for each part of the cursor image
private fun createCursorImage(
  borderColor: Color
) = ImageVector.Builder(
  name = "Cursor",
  defaultWidth = 200.dp,
  defaultHeight = 200.dp,
  viewportWidth = 1024f,
  viewportHeight = 1024f,
).apply {
  addPath(
    fill = SolidColor(Color.White),
    pathData = PathParser().pathStringToNodes(fillerPath)
  )
  addPath(
    fill = SolidColor(borderColor),
    pathData = PathParser().pathStringToNodes(borderPath)
  )
}.build()

private const val fillerPath = "M 91.606 93.29 L 89.422 50.859 C 72.546 38.822 50.27 51.605 49.334 73.868 C 49.293 74.767 49.293 75.67 49.334 76.569 L 91.606 93.29 Z M 116.784 743.117 L 77.781 774.49 C 78.668 796.756 100.919 809.601 117.82 797.611 C 118.951 796.81 120.027 795.915 121.027 794.931 L 103.833 773.159 L 116.784 743.117 Z M 262.159 618.554 L 284.72 604.078 C 276.505 588.25 257.316 584.729 244.965 596.782 L 262.159 618.525 L 262.159 618.554 Z M 375.847 837.346 L 353.287 851.821 C 360.485 865.668 376.417 870.409 388.873 862.42 L 375.847 837.346 Z M 501.739 745.48 L 532.066 770.553 C 544.522 762.558 548.792 744.851 541.599 731.004 L 501.739 745.48 Z M 405.368 526.66 L 397.031 499.213 C 381.44 505.06 374.569 525.305 382.808 541.135 L 405.368 526.66 Z M 605.034 451.586 L 613.377 479.004 C 632.371 471.879 637.313 444.565 622.259 429.839 C 621.265 428.864 620.199 427.976 619.075 427.18 L 605.034 451.586 Z M 49.358 76.569 L 77.781 774.49 L 441.252 503.918 L 101.439 73.934 L 49.383 76.54 L 49.358 76.569 Z M 121.004 794.931 L 279.355 640.268 L 244.965 596.782 L 86.613 751.415 L 121.004 794.931 Z M 239.6 633 L 353.287 851.821 L 398.416 822.869 L 388.525 512.724 L 239.6 633 Z M 388.873 862.42 L 532.066 770.553 L 385.988 530.547 L 362.821 812.273 L 388.873 862.42 Z M 541.599 730.975 L 427.928 512.184 L 382.808 541.135 L 496.479 759.956 L 541.599 731.004 L 541.599 730.975 Z M 413.704 554.077 L 613.393 479.004 L 596.697 424.168 L 397.007 499.213 L 413.704 554.077 Z M 619.075 427.18 L 89.422 50.859 L 91.595 640.414 L 590.973 475.964 L 619.06 427.18 L 619.075 427.18 Z"

private const val borderPath = "M 32.816 32.538 L 50.064 5.562 C 29.332 -7.742 1.972 6.386 0.817 30.993 C 0.77 31.988 0.77 32.984 0.816 33.978 L 32.816 32.538 Z M 67.728 803.898 L 35.76 805.37 C 36.853 829.98 64.177 844.177 84.943 830.925 C 86.329 830.04 87.646 829.05 88.88 827.962 L 67.76 803.898 L 67.728 803.898 Z M 262.224 633.018 L 289.936 617.018 C 279.849 599.525 256.28 595.633 241.104 608.954 L 262.224 632.986 L 262.224 633.018 Z M 401.872 874.842 L 374.16 890.842 C 382.997 906.147 402.567 911.391 417.872 902.554 L 401.872 874.842 Z M 577.744 773.306 L 593.744 801.018 C 609.048 792.182 614.292 772.611 605.456 757.306 L 577.744 773.306 Z M 438.128 531.45 L 427.888 501.114 C 408.738 507.577 400.3 529.953 410.416 547.45 L 438.128 531.45 Z M 683.376 448.474 L 693.616 478.778 C 716.956 470.903 723.019 440.713 704.528 424.438 C 703.303 423.36 701.998 422.377 700.624 421.498 L 683.376 448.474 Z M 0.848 33.978 L 35.76 805.37 L 99.696 802.49 L 64.816 31.066 L 0.88 33.946 L 0.848 33.978 Z M 88.848 827.962 L 283.344 657.018 L 241.104 608.954 L 46.608 779.866 L 88.848 827.962 Z M 234.512 648.986 L 374.16 890.842 L 429.584 858.842 L 289.936 616.986 L 234.512 648.986 Z M 417.872 902.554 L 593.744 801.018 L 561.744 745.594 L 385.872 847.13 L 417.872 902.554 Z M 605.456 757.274 L 465.84 515.45 L 410.416 547.45 L 550.032 789.306 L 605.456 757.306 L 605.456 757.274 Z M 448.368 561.754 L 693.648 478.778 L 673.136 418.17 L 427.856 501.114 L 448.368 561.754 Z M 700.624 421.498 L 50.064 5.562 L 15.536 59.514 L 666.096 475.418 L 700.592 421.498 L 700.624 421.498 Z"