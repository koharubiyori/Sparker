package koharubiyori.sparker.screen.home.component

import android.annotation.SuppressLint
import android.provider.Settings
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
import androidx.compose.material.icons.rounded.AddLink
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.node.Ref
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.api.device.GetPairingCodeReq
import koharubiyori.sparker.api.device.PairReq
import koharubiyori.sparker.api.device.deviceApi
import koharubiyori.sparker.compable.remember.rememberLocalCoroutineScope
import koharubiyori.sparker.component.BottomSheetContainer
import koharubiyori.sparker.component.InputFieldRule
import koharubiyori.sparker.component.VerifiableOutlinedTextField
import koharubiyori.sparker.component.VerifiableOutlinedTextFieldRef
import koharubiyori.sparker.request.HostException
import koharubiyori.sparker.screen.home.component.BottomSheetToPairDeviceState.TextFields
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.util.DeviceStateCenter
import koharubiyori.sparker.util.HostErrorCode
import koharubiyori.sparker.util.PlaceholderTransformation
import koharubiyori.sparker.util.toast
import koharubiyori.sparker.util.tryToastAsRequestException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BottomSheetToPairDevice(
  state: BottomSheetToPairDeviceState
) {
  val localCoroutine = rememberLocalCoroutineScope()

  BottomSheetContainer(
    visible = state.visible,
    onClickMask = { state.visible = false }
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
          text = stringResource(R.string.pair_device),
          style = MaterialTheme.typography.titleLarge
        )

        Button(
          onClick = { localCoroutine.launch { state.submit() } }
        ) {
          Icon(
            modifier = Modifier
              .size(20.dp)
              .background(MaterialTheme.colorScheme.primary),
            imageVector = Icons.Rounded.AddLink,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
          )
        }
      }

      ComposedTextField(
        labelText = stringResource(R.string.pairing_code),
        value = state.pairingCode,
        keyboardType = KeyboardType.Number,
        inputFieldRules = listOf(InputFieldRule(
          required = true,
          validator = { it.length == 4 },
          mismatchHelpingText = stringResource(R.string.s_pairing_code_input_helping_text)
        )),
        onValueChange = {
          state.pairingCode = it.filter { it.isDigit() }.take(4)
        },
        ref = state.textFieldRefs[TextFields.PAIRING_CODE]
      )
      ComposedTextField(
        labelText = stringResource(R.string.username),
        value = state.username,
        placeholderText = stringResource(R.string.s_username_description),
        keyboardType = KeyboardType.Email,
        inputFieldRules = InputFieldRule.requiredOnly(),
        onValueChange = { state.username = it },
        ref = state.textFieldRefs[TextFields.USERNAME]
      )
      ComposedTextField(
        labelText = stringResource(R.string.password),
        value = state.password,
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done,
        placeholderText = stringResource(R.string.s_password_description),
        inputFieldRules = InputFieldRule.requiredOnly(),
        onValueChange = { state.password = it },
        ref = state.textFieldRefs[TextFields.PASSWORD]
      )
    }
  }
}

class BottomSheetToPairDeviceState {
  var visible by mutableStateOf(false)
  var deviceConfig by mutableStateOf<DeviceConfig?>(null)

  var pairingCode by mutableStateOf("")
  var username by mutableStateOf("")
  var password by mutableStateOf("")

  val textFieldRefs = mapOf(
    TextFields.PAIRING_CODE to Ref<VerifiableOutlinedTextFieldRef>(),
    TextFields.USERNAME to Ref(),
    TextFields.PASSWORD to Ref(),
  )

  suspend fun show(deviceConfig: DeviceConfig) {
    this.deviceConfig = deviceConfig
    visible = true
    getPairingCode()
  }

  suspend fun hide() = coroutineScope {
    launch {
      delay(500)
      pairingCode = ""
      username = ""
      password = ""
    }
    visible = false
  }

  // show a pairing code on specific device
  suspend fun getPairingCode() {
    try {
      @SuppressLint("HardwareIds")
      DeviceStateCenter.deviceScope(deviceConfig!!) {
        deviceApi.getPairingCode(GetPairingCodeReq(
          sessionId = Settings.Secure.getString(Globals.context.contentResolver, Settings.Secure.ANDROID_ID),
        ))
      }
    } catch (ex: Exception) {
      if (!ex.tryToastAsRequestException()) throw ex
    }
  }

  suspend fun submit() {
    val result = textFieldRefs.values.all { it.value!!.verify() }
    if (!result) return

    try {
      @SuppressLint("HardwareIds")
      val androidId = Settings.Secure.getString(Globals.context.contentResolver, Settings.Secure.ANDROID_ID)
      val res = DeviceStateCenter.deviceScope(deviceConfig!!) {
        deviceApi.pair(PairReq(
          deviceId = androidId,
          pairingCode = pairingCode,
          sessionId = androidId,
          username = username,
          password = password
        ))
      }
      val newDeviceConfig = deviceConfig!!.copy(token = res.token)
      DeviceConfigStore.modifyConfig(deviceConfig!!.name, newDeviceConfig)
      DeviceStateCenter.refreshDeviceState(newDeviceConfig)
      hide()
      toast(Globals.context.getString(R.string.s_successful_device_pairing_message))
    } catch (ex: HostException) {
      when (ex.code) {
        HostErrorCode.DEVICE_INVALID_PAIRING_CODE -> {
          toast(Globals.context.getString(R.string.s_invalid_pairing_code_message))
        }
        HostErrorCode.DEVICE_USERNAME_OR_PASSWORD_INVALID -> {
          toast(Globals.context.getString(R.string.s_invalid_username_or_password_message))
        }
        else -> {}
      }
    }
  }

  enum class TextFields {
    PAIRING_CODE,
    USERNAME,
    PASSWORD,
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
    visualTransformation = if (value.isEmpty() && placeholderText != null)
      PlaceholderTransformation(placeholderText) else
      if (keyboardType == KeyboardType.Password) PasswordVisualTransformation() else VisualTransformation.None,
    trailingIcon = suffix,
    inputFieldRules = inputFieldRules,
    ref = ref,
    value = value,
    onValueChange = onValueChange
  )
}
