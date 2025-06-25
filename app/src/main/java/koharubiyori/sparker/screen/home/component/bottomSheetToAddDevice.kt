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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import koharubiyori.sparker.component.VerifiableOutlinedTextFieldRef
import koharubiyori.sparker.component.InputFieldRule
import koharubiyori.sparker.component.VerifiableOutlinedTextField
import koharubiyori.sparker.screen.home.component.BottomSheetToAddDeviceState.TextFields
import koharubiyori.sparker.screen.scanDevices.ScanResultEx
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.util.DeviceStateCenter
import koharubiyori.sparker.util.PlaceholderTransformation
import koharubiyori.sparker.util.ScanResult
import koharubiyori.sparker.util.navigateByArguments
import koharubiyori.sparker.util.toast
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun BottomSheetToAddDevice(
  state: BottomSheetToAddDeviceState
) {
  val localCoroutine = rememberLocalCoroutineScope()
  val editMode = state.editMode

  BottomSheetContainer(
    visible = state.visible,
    onClickMask = { localCoroutine.launch { state.hide() } },
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
          onClick = { localCoroutine.launch { state.hide() } }
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
          onClick = { localCoroutine.launch { state.saveConfig() } }
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
                CompletableDeferred<ScanResultEx?>().apply {
                  Globals.navController.navigateByArguments(ScanDevicesRouteArguments(this))
                  val result = await() ?: return@apply
                  state.fillWithIpScanResult(result.scanResult)
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
        labelText = stringResource(R.string.wake_on_lan_port),
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

  // Cleaning this field and unregistering from DeviceConnectionStore is necessary after submitting the bottom sheet in edit mode
  var edit by mutableStateOf<DeviceConfig?>(null)

  val textFieldRefs = mapOf(
    TextFields.DEVICE_NAME to Ref<VerifiableOutlinedTextFieldRef>(),
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
      port = it.wakeOnLanPort?.let { it.toString() } ?: ""
      macAddress = it.macAddress ?: ""
    }

    visible = true
    this.edit = edit
  }

  suspend fun hide() = coroutineScope {
    launch {
      delay(500)
      deviceName = ""
      hostName = ""
      port = ""
      macAddress = ""
    }

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
      wakeOnLanPort = if (port.trim().isEmpty()) null else port.toInt(),
      macAddress = if (macAddress.trim().isEmpty()) null else macAddress,
      token = edit?.token
    )

    launch {
      if (edit == null) {
        DeviceConfigStore.addConfig(deviceConfig)
      } else {
        DeviceConfigStore.modifyConfig(edit!!.name, deviceConfig)
        DeviceStateCenter.unregisterDevice(edit!!)
        edit = null
        toast(Globals.context.getString(R.string.s_successful_edit))
      }

      DeviceStateCenter.registerDevice(deviceConfig)
    }

    hide()
  }

  enum class TextFields {
    DEVICE_NAME, HOST_NAME, PORT, MAC_ADDRESS
  }
}

@Composable
private fun ComposedTextField(
  value: String,
  labelText: String,
  inputFieldRules: List<InputFieldRule> = emptyList(),
  keyboardType: KeyboardType = KeyboardType.Text,
  imeAction: ImeAction = ImeAction.Next,
  ref: Ref<VerifiableOutlinedTextFieldRef>? = null,
  onValueChange: (String) -> Unit,
  placeholderText: String? = null,
  enabled: Boolean = true,
  suffix: (@Composable () -> Unit)? = null,
) {
  VerifiableOutlinedTextField(
    modifier = Modifier
      .padding(bottom = 7.dp)
      .fillMaxWidth(),
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
    label = { Text(labelText) },
    visualTransformation = if (value.isEmpty() && placeholderText != null) PlaceholderTransformation(placeholderText) else VisualTransformation.None,
    trailingIcon = suffix,
    inputFieldRules = inputFieldRules,
    ref = ref,
    value = value,
    onValueChange = onValueChange
  )
}
