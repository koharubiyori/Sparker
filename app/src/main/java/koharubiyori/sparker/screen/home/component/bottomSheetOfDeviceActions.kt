package koharubiyori.sparker.screen.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.compable.remember.rememberLocalCoroutineScope
import koharubiyori.sparker.component.BottomSheetContainer
import koharubiyori.sparker.component.commonDialog.ButtonConfig
import koharubiyori.sparker.component.commonDialog.CommonAlertDialogProps
import koharubiyori.sparker.screen.home.HomeScreenModel
import koharubiyori.sparker.screen.settings.PowerOffAction
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.store.SettingsStore
import koharubiyori.sparker.util.DeviceStateCenter
import koharubiyori.sparker.util.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomSheetForDeviceActions(
  state: BottomSheetForDeviceActionsState
) {
  val preferenceSettings by SettingsStore.preference.getValue { this }.collectAsStateWithLifecycle(null)
  var testingDeviceConnection by remember { mutableStateOf(false) }

  LaunchedEffect(state.visible) {
    if (!state.visible) return@LaunchedEffect
    testingDeviceConnection = true
    DeviceStateCenter.testDeviceConnectionState(state.deviceConfig!!)
    testingDeviceConnection = false
  }

  if (preferenceSettings == null) return
  BottomSheetContainer(
    visible = state.visible,
    onClickMask = { state.hide() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .clip(MaterialTheme.shapes.large.copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize))
        .padding(top = 15.dp)
        .padding(horizontal = 15.dp)
    ) {
      ComposedHeader(state)

      Box(
        contentAlignment = Alignment.Center
      ) {
        ComposedActionButtons(state)
        if (preferenceSettings?.dynamicPowerActions == true) {
          androidx.compose.animation.AnimatedVisibility(
            modifier = Modifier
              .matchParentSize(),
            visible = testingDeviceConnection,
            enter = EnterTransition.None,
            exit = fadeOut()
          ) {
            Box(
              modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surface),
              contentAlignment = Alignment.Center
            ) {
              ContainedLoadingIndicator(
                modifier = Modifier
                  .size(60.dp)
              )
            }
          }
        }
      }

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ComposedButton(
  text: String,
  icon: ImageVector,
  loading: Boolean = false,
  enabled: Boolean = true,
  onClick: () -> Unit
) {
  val animatedIconSize by animateDpAsState(if (loading) 15.dp else 30.dp)

  Button(
    modifier = Modifier
      .padding(bottom = 15.dp)
      .fillMaxWidth()
      .height(50.dp)
      .alpha(if (loading) 0.5f else 1f),
    enabled = enabled,
    onClick = { if(!loading) onClick() }
  ) {
    Box(
      contentAlignment = Alignment.Center
    ) {
      Icon(
        modifier = Modifier
          .size(animatedIconSize),
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimary
      )

      androidx.compose.animation.AnimatedVisibility(
        visible = loading,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        CircularWavyProgressIndicator(
          modifier = Modifier
            .size(30.dp),
          color = MaterialTheme.colorScheme.primaryFixed,
          trackColor = MaterialTheme.colorScheme.onPrimary,
          amplitude = 0f,
          wavelength = 2.dp,
          waveSpeed = 4.dp,
        )
      }
    }

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

@Composable
private fun ComposedHeader(
  state: BottomSheetForDeviceActionsState,
) {
  val model: HomeScreenModel = hiltViewModel()
  val coroutine = rememberLocalCoroutineScope()
  Row(
    modifier = Modifier
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
              state.hide()
              delay(500)
              DeviceConfigStore.removeConfig(state.deviceConfig!!.name)
              DeviceStateCenter.unregisterDevice(state.deviceConfig!!)
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
}

@Composable
private fun ComposedActionButtons(
  state: BottomSheetForDeviceActionsState,
) {
  val model: HomeScreenModel = hiltViewModel()
  val localCoroutine = rememberLocalCoroutineScope()
  val preferenceSettings by SettingsStore.preference.getValue { this }.collectAsStateWithLifecycle(null)

  var lockButtonLoading by remember { mutableStateOf(false) }
  var unlockButtonLoading by remember { mutableStateOf(false) }
  val deviceState = state.deviceConfig?.let { DeviceStateCenter.composableStateMap[it.name] }

  val buttons = object {
    val wake = @Composable {
      ComposedButton(
        text = stringResource(R.string.wake),
        icon = Icons.Rounded.Bolt,
        onClick = { localCoroutine.launch { model.powerOn(state.deviceConfig!!) } }
      )
    }
    val pair = @Composable {
      ComposedButton(
        text = stringResource(R.string.pair),
        icon = Icons.Rounded.Link,
        onClick = {
          if (deviceState?.serverOnline != true) return@ComposedButton toast(Globals.context.getString(R.string.s_device_without_server))
          localCoroutine.launch {
            state.hide()
            model.bottomSheetToPairDeviceState.show(state.deviceConfig!!)
          }
        }
      )
    }
    val unlock = @Composable {
      ComposedButton(
        text = stringResource(R.string.unlock),
        loading = lockButtonLoading,
        icon = Icons.Rounded.LockOpen,
        onClick = {
          localCoroutine.launch {
            lockButtonLoading = true
            model.unlock(state.deviceConfig!!)
            delay(1000)
            DeviceStateCenter.updateLockState(state.deviceConfig!!)
            lockButtonLoading = false
          }
        }
      )
    }
    val lock = @Composable {
      ComposedButton(
        text = stringResource(R.string.lock),
        loading = unlockButtonLoading,
        icon = Icons.Rounded.Lock,
        onClick = {
          localCoroutine.launch {
            unlockButtonLoading = true
            model.lock(state.deviceConfig!!)
            delay(1000)
            DeviceStateCenter.updateLockState(state.deviceConfig!!)
            unlockButtonLoading = false
          }
        }
      )
    }
    val shutdown = @Composable {
      ComposedButton(
        text = stringResource(R.string.shutdown),
        icon = Icons.Rounded.PowerSettingsNew,
        onClick = {
          localCoroutine.launch {
            model.powerOff(state.deviceConfig!!, PowerOffAction.SHUTDOWN)
          }
        }
      )
    }
    val sleep = @Composable {
      ComposedButton(
        text = stringResource(R.string.sleep),
        icon = Icons.Rounded.Bedtime,
        onClick = {
          localCoroutine.launch {
            model.powerOff(state.deviceConfig!!, PowerOffAction.SLEEP)
          }
        }
      )
    }
    val hibernate = @Composable {
      ComposedButton(
        text = stringResource(R.string.hibernate),
        icon = Icons.Rounded.AcUnit,
        onClick = {
          localCoroutine.launch {
            model.powerOff(state.deviceConfig!!, PowerOffAction.HIBERNATE)
          }
        }
      )
    }
    val reboot = @Composable {
      ComposedButton(
        text = stringResource(R.string.reboot),
        icon = Icons.Rounded.Refresh,
        onClick = {
          localCoroutine.launch {
            model.powerOff(state.deviceConfig!!, PowerOffAction.REBOOT)
          }
        }
      )
    }
  }

  Column(
    modifier = Modifier
      .padding(top = 15.dp)
  ) {
    if (preferenceSettings?.dynamicPowerActions == false) {
      buttons.wake()
      if (state.deviceConfig!!.token == null) {
        buttons.pair()
      } else {
        buttons.lock()
        buttons.unlock()
        buttons.shutdown()
        buttons.sleep()
        if (deviceState?.hibernateEnabled == true) buttons.hibernate()
        buttons.reboot()
      }
    } else {
      AnimatedContent(deviceState?.pingOnline == true) { online ->
        if (!online) {
          buttons.wake()
        } else {
          Column {
            if (state.deviceConfig!!.token == null) {
              buttons.pair()
            } else {
              CrossFlipHorizontal(deviceState?.locked == true) { locked ->
                if (locked) buttons.unlock() else buttons.lock()
              }

              buttons.shutdown()
              buttons.sleep()
              if (deviceState?.hibernateEnabled == true) buttons.hibernate()
              buttons.reboot()
            }
          }
        }
      }
    }
  }
}

@Composable
private fun <T> CrossFlipHorizontal(
  targetState: T,
  modifier: Modifier = Modifier,
  content: @Composable (T) -> Unit
) {
  val density = LocalDensity.current.density
  var current by remember { mutableStateOf(targetState) }
  val rotation = remember { Animatable(0f) }
  val isBack = remember { mutableStateOf(false) }

  LaunchedEffect(targetState) {
    if (targetState != current) {
      isBack.value = true
      // Flip to 90°
      rotation.animateTo(
        90f,
        animationSpec = tween(250, easing = FastOutLinearInEasing)
      )
      // Change the content
      current = targetState
      isBack.value = false
      // Flip the remaining 90°
      rotation.animateTo(
        0f,
        animationSpec = tween(250, easing = LinearOutSlowInEasing)
      )
    }
  }

  val displayedRotation = if (isBack.value) rotation.value else -rotation.value

  Box(
    modifier = modifier.graphicsLayer {
      rotationY = displayedRotation
      cameraDistance = 8 * density
    }
  ) {
    content(current)
  }
}

