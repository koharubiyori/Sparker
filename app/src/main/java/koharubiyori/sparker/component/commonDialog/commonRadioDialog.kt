package koharubiyori.sparker.component.commonDialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import koharubiyori.sparker.R

class CommonRadioItem(
  val label: String,
  val value: Any
)

class CommonRadioDialogProps<T>(
  val items: List<CommonRadioItem>,
  val defaultValue: Any,
  val closeOnDismiss: Boolean = true,
  val onDismiss: (() -> Unit)? = null,
  val onCheck: ((value: T) -> Unit),
)

class CommonRadioDialogRef(
  private val _show: (props: CommonRadioDialogProps<Any>) -> Any,
  val hide: () -> Unit
) {
  // Wraps `_show` in a function to support generic type T
  fun <T> show(props: CommonRadioDialogProps<T>) {
    _show(props as CommonRadioDialogProps<Any>)
  }
}

@Composable
fun CommonRadioDialog(
  ref: Ref<CommonRadioDialogRef>
) {
  var visible by remember { mutableStateOf(false) }
  var currentProps by remember { mutableStateOf<CommonRadioDialogProps<Any>?>(null) }
  var selectedValue by remember { mutableStateOf<Any?>(null) }

  fun show(props: CommonRadioDialogProps<Any>) {
    visible = true
    currentProps = props
    selectedValue = props.defaultValue
  }

  fun hide() {
    visible = false
  }

  ref.value = remember {
    CommonRadioDialogRef(
      _show = { show(it) },
      hide = { hide() }
    )
  }

  if (!visible) { return }

  BackHandler(
    onBack = { visible = false }
  )

  val props = currentProps!!
  CommonLoadingDialogUI(
    items = props.items,
    value = selectedValue,
    onClickItem = { selectedValue = it },
    onCheck = {
      visible = false
      props.onCheck(selectedValue!!)
    },
    onDismiss = {
      if (props.closeOnDismiss) {
        visible = false
        props.onDismiss?.invoke()
      }
    }
  )
}

@Composable
fun CommonLoadingDialogUI(
  value: Any?,
  items: List<CommonRadioItem>,
  onDismiss: () -> Unit,
  onClickItem: (value: Any) -> Unit,
  onCheck: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp),
      shape = MaterialTheme.shapes.medium
    ) {
      Column(
        modifier = Modifier
          .padding(horizontal = 10.dp)
          .padding(top = 10.dp, bottom = 5.dp)
      ) {
        for (item in items) {
          RadioItem(
            selected = item.value == value,
            config = item,
            onClick = { onClickItem(item.value) }
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          TextButton(
            onClick = { onCheck() }
          ) {
            Text(
              text = stringResource(R.string.select),
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              style = MaterialTheme.typography.titleMedium
            )
          }
        }
      }
    }
  }
}

@Composable
private fun RadioItem(
  selected: Boolean,
  config: CommonRadioItem,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    verticalAlignment = Alignment.CenterVertically
  ) {
    RadioButton(
      selected = selected,
      onClick = { onClick() }
    )

    Text(
      modifier = Modifier
        .padding(start = 10.dp),
      text = config.label
    )
  }
}