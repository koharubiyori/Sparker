package koharubiyori.sparker.screen.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.component.BottomSheetContainer
import koharubiyori.sparker.component.commonDialog.ButtonConfig
import koharubiyori.sparker.component.commonDialog.CommonAlertDialogProps
import koharubiyori.sparker.screen.home.HomeScreenModel
import koharubiyori.sparker.screen.settings.PowerOffAction
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.store.DeviceConnectionStore
import kotlinx.coroutines.launch

@Composable
fun BottomSheetForDeviceActions(
  state: BottomSheetForDeviceActionsState
) {
  val model: HomeScreenModel = hiltViewModel()
  val coroutine = rememberCoroutineScope()
  val connectionInfo = state.deviceConfig.let { DeviceConnectionStore.composableConnectionInfoMap[it] }

  BottomSheetContainer(
    visible = state.visible,
    onClickMask = { state.hide() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.large.copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize))
        .background(MaterialTheme.colorScheme.surface)
        .padding(top = 15.dp)
        .padding(horizontal = 15.dp)
    ) {
      Row(
        modifier = Modifier
          .padding(bottom = 15.dp)
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Delete button
        OutlinedButton(
          modifier = Modifier
            .weight(1f),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
          ),
          border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error
          ),
          onClick = {
            Globals.commonAlertDialog.show(CommonAlertDialogProps(
              content = {
                Text(stringResource(R.string.f_check_to_delete_device, state.deviceConfig!!.name))
              },
              secondaryButton = ButtonConfig.cancelButton(),
              onPrimaryButtonClick = {
                coroutine.launch {
                  DeviceConfigStore.removeConfig(state.deviceConfig!!.name)
                  DeviceConnectionStore.unregisterDevice(state.deviceConfig!!)
                  state.hide()
                }
              }
            ))
          }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              modifier = Modifier
                .size(30.dp),
              imageVector = Icons.Rounded.Delete,
              contentDescription = null,
            )

            Text(
              modifier = Modifier
                .padding(start = 10.dp),
              text = stringResource(R.string.delete),
              style = MaterialTheme.typography.titleMedium
            )
          }
        }

        Spacer(modifier = Modifier
          .weight(0.5f))

        // Edit Button
        OutlinedButton(
          modifier = Modifier
            .weight(1f),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
          ),
          border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
          ),
          onClick = {
            state.hide()
            model.bottomSheetToAddDeviceState.show(state.deviceConfig)
          }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              modifier = Modifier
                .size(30.dp),
              imageVector = Icons.Rounded.Edit,
              contentDescription = null,
            )

            Text(
              modifier = Modifier
                .padding(start = 10.dp),
              text = stringResource(R.string.edit),
              style = MaterialTheme.typography.titleMedium
            )
          }
        }
      }

      ComposedButton(
        text = stringResource(R.string.wake),
        icon = Icons.Rounded.Bolt,
        onClick = { coroutine.launch { model.powerOn(state.deviceConfig!!) } }
      )
      ComposedButton(
        text = stringResource(R.string.shutdown),
        icon = Icons.Rounded.PowerSettingsNew,
        onClick = {
          coroutine.launch {
            model.powerOff(state.deviceConfig!!, PowerOffAction.SHUTDOWN)
          }
        }
      )
      ComposedButton(
        text = stringResource(R.string.sleep),
        icon = Icons.Rounded.Bedtime,
        onClick = {
          coroutine.launch {
            model.powerOff(state.deviceConfig!!, PowerOffAction.SLEEP)
          }
        }
      )
      if (connectionInfo?.hibernateEnabled == true) {
        ComposedButton(
          text = stringResource(R.string.hibernate),
          icon = Icons.Rounded.AcUnit,
          onClick = {
            coroutine.launch {
              model.powerOff(state.deviceConfig!!, PowerOffAction.HIBERNATE)
            }
          }
        )
      }
      ComposedButton(
        text = stringResource(R.string.reboot),
        icon = Icons.Rounded.Refresh,
        onClick = {
          coroutine.launch {
            model.powerOff(state.deviceConfig!!, PowerOffAction.REBOOT)
          }
        }
      )
    }
  }
}

class BottomSheetForDeviceActionsState {
  var visible by mutableStateOf(false)
  var deviceConfig: DeviceConfig? = null

  fun show(deviceConfig: DeviceConfig) {
    visible = true
    this.deviceConfig = deviceConfig
  }

  fun hide() {
    visible = false
  }
}

@Composable
private fun ComposedButton(
  text: String,
  icon: ImageVector,
  enabled: Boolean = true,
  onClick: () -> Unit
) {
  Button(
    modifier = Modifier
      .padding(bottom = 15.dp)
      .fillMaxWidth()
      .height(50.dp),
    enabled = enabled,
    onClick = onClick
  ) {
    Icon(
      modifier = Modifier
        .size(30.dp),
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onPrimary
    )

    Text(
      modifier = Modifier
        .padding(start = 10.dp),
      text = text,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onPrimary,
      style = MaterialTheme.typography.titleMedium
    )
  }
}