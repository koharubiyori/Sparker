package koharubiyori.sparker.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.node.Ref
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@Composable
fun VerifiableOutlinedTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  prefix: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = false,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  interactionSource: MutableInteractionSource? = null,
  shape: Shape = OutlinedTextFieldDefaults.shape,
  colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),

  inputFieldRules: List<InputFieldRule> = emptyList(),
  ref: Ref<VerifiableOutlinedTextFieldRef>? = null,
) {
  val coroutine = rememberCoroutineScope()
  var wasFocused by rememberSaveable { mutableStateOf(false) }
  var hitRuleIndex by rememberSaveable { mutableIntStateOf(-1) }
  val hitRule = inputFieldRules.getOrNull(hitRuleIndex)
  val supportingText = flow { emit(hitRule?.getHelpingText(value)) }.collectAsStateWithLifecycle(null).value

  suspend fun validate(): Boolean {
    hitRuleIndex = inputFieldRules.indexOfFirst { !it.test(value) }
    return hitRuleIndex == -1
  }

  SideEffect {
    ref?.value = VerifiableOutlinedTextFieldRef(
      verify = { validate() }
    )
  }

  OutlinedTextField(
    modifier = modifier
      .onFocusChanged {
        // The last value needs to be checked because the hook will be called during initialization
        if (wasFocused) coroutine.launch { validate() }
        wasFocused = it.isFocused
      }
      .then(modifier),
    value = value,
    onValueChange = onValueChange,
    enabled = enabled,
    readOnly = readOnly,
    textStyle = textStyle,
    label = label,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    prefix = prefix,
    suffix = suffix,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    maxLines = maxLines,
    minLines = minLines,
    interactionSource = interactionSource,
    shape = shape,
    colors = colors,

    isError = hitRuleIndex > -1,
    supportingText = supportingText?.let { { Text(it) } }
  )
}

class VerifiableOutlinedTextFieldRef(
  val verify: suspend () -> Boolean
)

class InputFieldRule(
  val required: Boolean = false,
  val validator: (suspend (String) -> Boolean)? = null,
  val emptyHelpingText: String = Globals.context.getString(R.string.s_required_field),
  val mismatchHelpingText: String = Globals.context.getString(R.string.invalid_format)
) {
  suspend fun validate(text: String): HitType {
    if (text.trim().isEmpty()) return if (required) HitType.EMPTY else HitType.NONE
    if (validator?.invoke(text) == false) return HitType.MISMATCH
    return HitType.NONE
  }

  suspend fun test(text: String): Boolean {
    return validate(text) == HitType.NONE
  }

  suspend fun getHelpingText(text: String) = when(validate(text)) {
    HitType.EMPTY -> emptyHelpingText
    HitType.MISMATCH -> mismatchHelpingText
    HitType.NONE -> null
  }

  companion object {
    fun requiredOnly(
      emptyHelpingText: String = Globals.context.getString(R.string.s_required_field)
    ) = listOf(InputFieldRule(
      required = true,
      emptyHelpingText = emptyHelpingText
    ))
  }

  enum class HitType {
    EMPTY, MISMATCH, NONE
  }
}


