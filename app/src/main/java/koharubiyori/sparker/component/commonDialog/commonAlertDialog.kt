package koharubiyori.sparker.component.commonDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.util.visibility

class CommonAlertDialogProps(
  val title: String? = null,
  val primaryButtonText: String? = null,
  val secondaryButton: ButtonConfig? = null,
  val leftButton: ButtonConfig? = null,
  val closeOnDismiss: Boolean = true,
  val closeOnAction: Boolean = true,
  val hideTitle: Boolean = false,
  val onDismiss: (() -> Unit)? = null,
  val onPrimaryButtonClick: (() -> Unit)? = null,
  val content: (@Composable () -> Unit)? = null,
)

class CommonAlertDialogRef(
  val show: (props: CommonAlertDialogProps) -> Unit,
  val showText: (text: String) -> Unit,
  val hide: () -> Unit
)

@ExperimentalComposeUiApi
@Composable
fun CommonAlertDialog(
  ref: Ref<CommonAlertDialogRef>
) {
  var visible by remember { mutableStateOf(false) }
  var currentProps by remember { mutableStateOf<CommonAlertDialogProps?>(null) }

  fun show(props: CommonAlertDialogProps) {
    visible = true
    currentProps = props
  }

  fun hide() {
    visible = false
  }

  ref.value = remember {
    CommonAlertDialogRef(
      show = { show(it) },
      showText = {
        show(CommonAlertDialogProps(
          content = { Text(it) }
        ))
      },
      hide = { hide() }
    )
  }

  if (!visible || currentProps == null) {
    return
  }

//  BackHandler(
//    onBack = { visible = false }
//  )

  val props = currentProps!!
  CommonAlertDialogUI(
    title = props.title,
    secondaryButton = props.secondaryButton,
    leftButton = props.leftButton,
    hideTitle = props.hideTitle,
    onDismiss = {
      if (props.closeOnDismiss) visible = false
      props.onDismiss?.invoke()
    },
    primaryButtonText = props.primaryButtonText,
    onRequestClose = { visible = false },
    onPrimaryButtonClick = {
      if (currentProps!!.onPrimaryButtonClick != null) {
        currentProps!!.onPrimaryButtonClick?.invoke()
      }
      if (props.closeOnAction) visible = false
    },
  ) {
    props.content?.invoke()
  }
}

@ExperimentalComposeUiApi
@Composable
fun CommonAlertDialogUI(
  title: String? = null,
  primaryButtonText: String? = null,
  secondaryButton: ButtonConfig? = null,
  leftButton: ButtonConfig? = null,
  closeOnAction: Boolean = true,
  hideTitle: Boolean = false,
  onDismiss: () -> Unit,
  onPrimaryButtonClick: () -> Unit,
  onRequestClose: (() -> Unit)? = null,
  content: (@Composable () -> Unit)? = null,
) {
  val themeColors = MaterialTheme.colorScheme
  val configuration = LocalConfiguration.current
  val textStyle = LocalTextStyle.current
  val defaultTextStyle = remember {
    textStyle.copy(
      fontSize = 16.sp
    )
  }

  // AlertDialog在嵌套滚动视图时会出bug，必须用Dialog
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(5.dp)
    ) {
      Column(
        modifier = Modifier
          .width((configuration.screenWidthDp * 0.85).dp)
          .background(themeColors.surface)
      ) {
        Column(
          modifier = Modifier
            .padding(horizontal = 20.dp)
        ) {
          if (!hideTitle) {
            Text(
              modifier = Modifier
                .padding(vertical = 18.dp),
              text = title ?: stringResource(id = R.string.notice),
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
          } else {
            Spacer(modifier = Modifier.padding(18.dp))
          }

          CompositionLocalProvider(
            LocalTextStyle provides defaultTextStyle
          ) {
            content?.invoke()
          }
        }

        Row(
          modifier = Modifier
            .padding(horizontal = 10.dp)
            .padding(top = 15.dp, bottom = 5.dp)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          TextButton(
            modifier = Modifier
              .visibility(leftButton != null),
            enabled = leftButton != null,
            onClick = {
              leftButton?.onClick?.invoke()
              if (closeOnAction) onRequestClose?.invoke()
            },
          ) {
            Text(
              text = leftButton?.text ?: "",
              fontWeight = FontWeight.Bold,
              color = Color.Unspecified
            )
          }

          Row {
            if (secondaryButton != null) {
              TextButton(
                modifier = Modifier
                  .padding(end = 5.dp),
                onClick = {
                  secondaryButton.onClick?.invoke()
                  if (closeOnAction) onRequestClose?.invoke()
                }
              ) {
                Text(
                  text = secondaryButton.text,
                  fontWeight = FontWeight.Bold,
                  color = themeColors.primary
                )
              }
            }

            TextButton(
              onClick = onPrimaryButtonClick
            ) {
              Text(
                text = primaryButtonText ?: stringResource(R.string.ok),
                fontWeight = FontWeight.Bold,
                color = themeColors.primary
              )
            }
          }
        }
      }
    }
  }
}

class ButtonConfig(
  val text: String,
  val onClick: (() -> Unit)? = null
) {
  companion object {
    fun cancelButton(
      onClick: (() -> Unit)? = null
    ) = ButtonConfig(
      text = Globals.context.getString(R.string.cancel),
      onClick = onClick
    )
  }
}