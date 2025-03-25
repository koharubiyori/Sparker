package koharubiyori.sparker.screen.home.component

import koharubiyori.sparker.screen.scanDevices.ScanDevicesRouteArguments
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.component.BottomSheetContainer
import koharubiyori.sparker.compable.remember.rememberLocalCoroutineScope
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.store.DeviceConnectionStore
import koharubiyori.sparker.util.PlaceholderTransformation
import koharubiyori.sparker.util.ScanResult
import koharubiyori.sparker.util.navigateByArguments
import koharubiyori.sparker.util.toast
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@Composable
fun BottomSheetToAddDevice(
  state: BottomSheetToAddDeviceState
) {
  val coroutine = rememberCoroutineScope()
  val localCoroutine = rememberLocalCoroutineScope()
  val editMode = state.editMode

  BottomSheetContainer(
    visible = state.visible,
    onClickMask = { state.visible = false },
  ) {
    Column (
      modifier = Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.large.copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize))
        .background(MaterialTheme.colorScheme.surface)
        .padding(15.dp)
    ) {
      // Header
      Row(
        modifier = Modifier
          .padding(bottom = 5.dp)
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Button(
          onClick = { state.visible = false }
        ) {
          Icon(
            modifier = Modifier
              .size(20.dp)
              .background(MaterialTheme.colorScheme.primary),
            imageVector = Icons.Rounded.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
          )
        }

        Text(
          text = stringResource(if (state.editMode) R.string.edit_device else R.string.add_device),
          style = MaterialTheme.typography.titleLarge
        )

        Button(
          onClick = { coroutine.launch { state.saveConfig() } }
        ) {
          Icon(
            modifier = Modifier
              .size(20.dp)
              .background(MaterialTheme.colorScheme.primary),
            imageVector = Icons.Rounded.Save,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
          )
        }
      }
      ComposedTextField(
        labelText = stringResource(R.string.device_name),
        value = state.deviceName,
        onValueChange = { state.deviceName = it },
        placeholderText = stringResource(R.string.s_device_name_description),
        inputFieldRules = listOf(InputFieldRule(
          required = true,
          validator = { !DeviceConfigStore.deviceConfigs.first().any { deviceConfig -> deviceConfig.name == it } || editMode },
          mismatchHelpingText = stringResource(R.string.s_name_already_exists)
        )),
        enabled = !editMode,
        ref = state.textFieldRefs[TextFields.DEVICE_NAME]
      )
      ComposedTextField(
        labelText = stringResource(R.string.host_name),
        value = state.hostName,
        placeholderText = stringResource(R.string.s_host_name_description),
        onValueChange = { state.hostName = it },
        inputFieldRules = InputFieldRule.requiredOnly(),
        enabled = !editMode,
        ref = state.textFieldRefs[TextFields.HOST_NAME],
        suffix = {
          IconButton(
            modifier = Modifier
              .padding(end = 5.dp),
            enabled = !editMode,
            onClick = {
              localCoroutine.launch {
                CompletableDeferred<ScanResult?>().apply {
                  Globals.navController.navigateByArguments(ScanDevicesRouteArguments(this))
                  val result = await() ?: return@apply
                  state.fillWithIpScanResult(result)
                }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Rounded.Search,
              contentDescription = null
            )
          }
        }
      )
      ComposedTextField(
        labelText = stringResource(R.string.port),
        value = state.port,
        keyboardType = KeyboardType.Number,
        placeholderText = stringResource(R.string.s_port_description),
        onValueChange = { state.port = it },
        inputFieldRules = listOf(
          InputFieldRule(
            validator = { Regex("""^\d+$""").matches(it) },
          )),
        ref = state.textFieldRefs[TextFields.PORT]
      )
      ComposedTextField(
        labelText = stringResource(R.string.mac_address),
        imeAction = ImeAction.Done,
        value = state.macAddress,
        placeholderText = stringResource(R.string.s_mac_address_description),
        onValueChange = { state.macAddress = it },
        inputFieldRules = listOf(
          InputFieldRule(
            validator = { Regex("""^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$""").matches(it) }
          )
        ),
        ref = state.textFieldRefs[TextFields.MAC_ADDRESS]
      )
    }
  }
}

class BottomSheetToAddDeviceState {
  var visible by mutableStateOf(false)

  var deviceName by mutableStateOf("")
  var hostName by mutableStateOf("")
  var port by mutableStateOf("")
  var macAddress by mutableStateOf("")

  // Unregistering from DeviceConnectionStore and cleaning this field is necessary after submitting the bottom sheet in edit mode
  var edit by mutableStateOf<DeviceConfig?>(null)

  val textFieldRefs = mapOf(
    TextFields.DEVICE_NAME to Ref<ComposedTextFieldRef>(),
    TextFields.HOST_NAME to Ref(),
    TextFields.PORT to Ref(),
    TextFields.MAC_ADDRESS to Ref(),
  )

  val editMode @Composable get() = edit != null

  fun show(
    edit: DeviceConfig? = null,
  ) {
    edit?.let {
      deviceName = it.name
      hostName = it.hostName
      port = it.port?.let { it.toString() } ?: ""
      macAddress = it.macAddress ?: ""
    }

    visible = true
    this.edit = edit
  }

  fun hide() {
    visible = false
  }

  fun fillWithIpScanResult(scanResult: ScanResult) {
    if (deviceName == "" || deviceName == hostName) deviceName = scanResult.ip
    hostName = scanResult.ip
    if (scanResult.mac != null) macAddress = scanResult.mac
  }

  suspend fun saveConfig() = coroutineScope {
    val result = textFieldRefs.values.map { it.value!!.verify() }.all { it }
    if (!result) return@coroutineScope
    val deviceConfig = DeviceConfig(
      name = deviceName.trim(),
      hostName = hostName.trim(),
      port = if (port.trim().isEmpty()) null else port.toInt(),
      macAddress = if (macAddress.trim().isEmpty()) null else macAddress
    )

    launch {
      if (edit == null) {
        DeviceConfigStore.addConfig(deviceConfig)
      } else {
        DeviceConfigStore.modifyConfig(edit!!, deviceConfig)
        DeviceConnectionStore.unregisterDevice(edit!!)
        edit = null
        toast(Globals.context.getString(R.string.s_successful_edit))
      }

      DeviceConnectionStore.registerDevice(deviceConfig)
    }

    launch {
      delay(500)
      deviceName = ""
      hostName = ""
      port = ""
      macAddress = ""
    }

    visible = false
  }
}

enum class TextFields {
  DEVICE_NAME, HOST_NAME, PORT, MAC_ADDRESS
}

@Composable
private fun ComposedTextField(
  value: String,
  labelText: String,
  inputFieldRules: List<InputFieldRule> = emptyList(),
  keyboardType: KeyboardType = KeyboardType.Text,
  imeAction: ImeAction = ImeAction.Next,
  ref: Ref<ComposedTextFieldRef>? = null,
  onValueChange: (String) -> Unit,
  placeholderText: String? = null,
  enabled: Boolean = true,
  suffix: (@Composable () -> Unit)? = null,
) {
  val coroutine = rememberCoroutineScope()
  var wasFocused by rememberSaveable { mutableStateOf(false) }
  var hitRuleIndex by rememberSaveable { mutableIntStateOf(-1) }
  val hitRule = inputFieldRules.getOrNull(hitRuleIndex)
  val supportingText = flow { emit(hitRule?.getHelpingText(value)) }.collectAsState(null).value

  suspend fun validate(): Boolean {
    hitRuleIndex = inputFieldRules.indexOfFirst { !it.test(value) }
    return hitRuleIndex == -1
  }

  SideEffect {
    ref?.value = ComposedTextFieldRef(
      verify = { validate() }
    )
  }

  OutlinedTextField(
    modifier = Modifier
      .padding(bottom = 7.dp)
      .fillMaxWidth()
      .onFocusChanged {
        // The last value needs to be checked because the hook will be called during initialization
        if (wasFocused) coroutine.launch { validate() }
        wasFocused = it.isFocused
      },
    enabled = enabled,
    singleLine = true,
    keyboardOptions = KeyboardOptions(
      keyboardType = keyboardType,
      imeAction = imeAction
    ),
    // Makes the label and the placeholder style look focused, whether or not
    colors = PlaceholderTransformation.textFieldColors(
      inputValueEmpty = value.isEmpty(),
      focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
      errorTextColor = MaterialTheme.colorScheme.error
    ),
    label = { Text(labelText); },
    isError = hitRuleIndex > -1,
    visualTransformation = if (value.isEmpty() && placeholderText != null) PlaceholderTransformation(placeholderText) else VisualTransformation.None,
    supportingText = supportingText?.let { { Text(it) } },
    trailingIcon = suffix,
    value = value,
    onValueChange = onValueChange
  )
}

class ComposedTextFieldRef(
  val verify: suspend () -> Boolean
)

private class InputFieldRule(
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
}

private enum class HitType {
  EMPTY, MISMATCH, NONE
}
