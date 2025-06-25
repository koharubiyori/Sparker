package koharubiyori.sparker.component

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//@ExperimentalComposeUiApi
//@Composable
//fun HoverBgColorButton(
//  onClick: () -> Unit,
//  content: @Composable () -> Unit,
//) {
//
//}

@Composable
fun HoverBgIndicationProvider(
  backgroundColor: Color = Color.Black.copy(alpha = 0.2f),
  content: @Composable () -> Unit
) {
  CompositionLocalProvider(
    LocalIndication provides remember(backgroundColor) { HoverBgIndicationFactory(backgroundColor) },
    content = content
  )
}

class HoverBgIndicationFactory(
  val backgroundColor: Color
) : IndicationNodeFactory {
  override fun create(interactionSource: InteractionSource): DelegatableNode {
    return HoverBgIndicationNode(interactionSource, backgroundColor)
  }

  override fun equals(other: Any?): Boolean {
    return other is HoverBgIndicationFactory && other.backgroundColor == backgroundColor
  }

  override fun hashCode(): Int {
    return backgroundColor.hashCode()
  }

  private class HoverBgIndicationNode(
    private val interactionSource: InteractionSource,
    private val backgroundColor: Color,
  ) : Modifier.Node(), DrawModifierNode {
    private var isPressed = false
    private var isHovered = false
    private var isFocused = false

    /** the code based on [androidx.compose.foundation.DefaultDebugIndication.DefaultDebugIndicationInstance.onAttach] */
    override fun onAttach() {
      // the coroutineScope inherited from Modifier.Node
      coroutineScope.launch {
        var pressCount = 0
        var hoverCount = 0
        var focusCount = 0

        interactionSource.interactions.collect { interaction ->
          when (interaction) {
            is PressInteraction.Press -> pressCount++
            is PressInteraction.Release -> pressCount--
            is PressInteraction.Cancel -> pressCount--
            is HoverInteraction.Enter -> hoverCount++
            is HoverInteraction.Exit -> hoverCount--
            is FocusInteraction.Focus -> focusCount++
            is FocusInteraction.Unfocus -> focusCount--
          }
          val pressed = pressCount > 0
          val hovered = hoverCount > 0
          val focused = focusCount > 0
          var invalidateNeeded = false
          if (isPressed != pressed) {
            isPressed = pressed
            invalidateNeeded = true
          }
          if (isHovered != hovered) {
            isHovered = hovered
            invalidateNeeded = true
          }
          if (isFocused != focused) {
            isFocused = focused
            invalidateNeeded = true
          }
          if (invalidateNeeded) invalidateDraw()
        }
      }
    }

    override fun ContentDrawScope.draw() {
      drawContent()
      if (isPressed || isHovered || isFocused) {
        drawRect(color = backgroundColor, size = size)
      }
    }
  }
}