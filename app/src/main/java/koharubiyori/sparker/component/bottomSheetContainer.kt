package koharubiyori.sparker.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import koharubiyori.sparker.util.noRippleClickable

@Composable
fun BottomSheetContainer(
  visible: Boolean,
  onClickMask: () -> Unit,
  content: @Composable () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize(),
    contentAlignment = Alignment.BottomCenter
  ) {
    AnimatedVisibility(
      visible = visible,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Spacer(Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f))
        .noRippleClickable { onClickMask() }
      )
    }

    AnimatedVisibility(
      visible = visible,
      enter = slideInVertically { it },
      exit = slideOutVertically { it }
    ) {
      Box(
        modifier = Modifier
          .navigationBarsPadding()
          .imePadding()
      ) {
        content()
      }
    }
  }
}