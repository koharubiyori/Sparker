package koharubiyori.sparker.screen.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.component.commonDialog.CommonAlertDialogProps
import koharubiyori.sparker.component.commonDialog.CommonRadioDialogProps
import koharubiyori.sparker.component.commonDialog.CommonRadioItem
import koharubiyori.sparker.component.styled.StyledTopAppBar
import koharubiyori.sparker.component.styled.TopAppBarTitle
import koharubiyori.sparker.screen.settings.component.SettingsScreenItem
import koharubiyori.sparker.store.SettingsStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
  val coroutine = rememberCoroutineScope()
  val preferenceSettings by SettingsStore.preference.getValue { this }.collectAsStateWithLifecycle(null)

  suspend fun showSliderConfig(state: SliderState): Int? {
    val completableDeferred = CompletableDeferred<Int?>()
//    var currentValue = state.value
//
//    state.onValueChangeFinished = {
//      currentValue = state.value
//    }

    Globals.commonAlertDialog.show(CommonAlertDialogProps(
      hideTitle = true,
      onPrimaryButtonClick = {
        completableDeferred.complete((state.value * 1000).toInt())
      },
      onDismiss = {
        completableDeferred.complete(null)
      }
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Slider(state = state)
        Box(
          modifier = Modifier
            .padding(top = 10.dp)
            .width(60.dp)
            .height(30.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
          contentAlignment = Alignment.Center
        ) {
          Text(
            modifier = Modifier,
            text = String.format("%.1f", state.value) + "s",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    })

    return completableDeferred.await()
  }

  if (preferenceSettings == null) return
  Scaffold(
    modifier = Modifier
      .imePadding(),
    topBar = {
      StyledTopAppBar(
        title = { TopAppBarTitle(text = "Settings") },
      )
    }
  ) {
    Column(
      modifier = Modifier
        .padding(it)
        .verticalScroll(rememberScrollState())
    ) {
      Title(stringResource(R.string.actions))
      SettingsScreenItem(
        title = stringResource(R.string.item_click_action),
        subtext = stringResource(R.string.s_item_click_action_description),
        onClick = {
          Globals.commonRadioDialog.show<ClickAction>(CommonRadioDialogProps(
            defaultValue = preferenceSettings!!.clickAction,
            items = listOf(
              CommonRadioItem(
                label = Globals.context.getString(R.string.toggle),
                value = ClickAction.TOGGLE
              ),
              CommonRadioItem(
                label = Globals.context.getString(R.string.power_on),
                value = ClickAction.POWER_ON
              ),
              CommonRadioItem(
                label = Globals.context.getString(R.string.power_off),
                value = ClickAction.POWER_OFF
              ),
            ),
            onCheck = {
              coroutine.launch { SettingsStore.preference.setValue { clickAction = it } }
            }
          ))
        }
      ) {
        Text(
          color = MaterialTheme.colorScheme.primary,
          text = stringResource(when (preferenceSettings!!.clickAction) {
            ClickAction.TOGGLE -> R.string.toggle
            ClickAction.POWER_ON -> R.string.power_on
            ClickAction.POWER_OFF -> R.string.power_off
          })
        )
      }
      SettingsScreenItem(
        title = stringResource(R.string.default_power_off_action),
        subtext = stringResource(R.string.s_default_power_off_action_description),
        onClick = {
          Globals.commonRadioDialog.show<PowerOffAction>(CommonRadioDialogProps(
            defaultValue = preferenceSettings!!.defaultPowerOffAction,
            items = listOf(
              CommonRadioItem(
                label = Globals.context.getString(R.string.shutdown),
                value = PowerOffAction.SHUTDOWN
              ),
              CommonRadioItem(
                label = Globals.context.getString(R.string.sleep),
                value = PowerOffAction.SLEEP
              ),
              CommonRadioItem(
                label = Globals.context.getString(R.string.hibernate),
                value = PowerOffAction.HIBERNATE
              ),
              CommonRadioItem(
                label = Globals.context.getString(R.string.reboot),
                value = PowerOffAction.REBOOT
              ),
            ),
            onCheck = {
              coroutine.launch { SettingsStore.preference.setValue { defaultPowerOffAction = it } }
            }
          ))
        }
      ) {
        Text(
          color = MaterialTheme.colorScheme.primary,
          text = stringResource(when (preferenceSettings!!.defaultPowerOffAction) {
            PowerOffAction.SHUTDOWN -> R.string.shutdown
            PowerOffAction.SLEEP -> R.string.sleep
            PowerOffAction.HIBERNATE -> R.string.hibernate
            PowerOffAction.REBOOT -> R.string.reboot
          })
        )
      }

      Title(stringResource(R.string.power))
      SettingsScreenItem(
        title = stringResource(R.string.force_shutdown),
        subtext = stringResource(R.string.s_force_shutdown_warning),
        onClick = {
          fun item(seconds: Int) = CommonRadioItem(label = getOptionTextOfForceShutdown(seconds), value = seconds)
          Globals.commonRadioDialog.show<Int>(CommonRadioDialogProps(
            defaultValue = preferenceSettings!!.forceShutdown,
            items = listOf(
              item(0),
              item(10),
              item(30),
              item(60),
              item(180),
            ),
            onCheck = {
              coroutine.launch { SettingsStore.preference.setValue { forceShutdown = it } }
            }
          ))
        }
      ) {
        Text(
          color = MaterialTheme.colorScheme.primary,
          text = getOptionTextOfForceShutdown(preferenceSettings!!.forceShutdown)
        )
      }

      SettingsScreenItem(
        title = stringResource(R.string.fast_boot),
        onClick = {
          coroutine.launch { SettingsStore.preference.setValue { fastBoot = !fastBoot } }
        }
      ) {
        Switch(
          checked = preferenceSettings!!.fastBoot,
          onCheckedChange = {
            coroutine.launch { SettingsStore.preference.setValue { fastBoot = it } }
          }
        )
      }

      Title(stringResource(R.string.timeout))
      SettingsScreenItem(
        title = stringResource(R.string.local_device_timeout),
        compact = true,
        onClick = {
          val state = SliderState(
            valueRange = 1f..10f,
            value = (preferenceSettings!!.localDeviceTimeout / 1000).toFloat(),
            steps = 17,
          )

          coroutine.launch {
            val result = showSliderConfig(state) ?: return@launch
            SettingsStore.preference.setValue { localDeviceTimeout = result }
          }
        }
      ) {
        Text(
          color = MaterialTheme.colorScheme.primary,
          text = String.format("%.1f", preferenceSettings!!.localDeviceTimeout.toFloat() / 1000) + "s"
        )
      }
      SettingsScreenItem(
        title = stringResource(R.string.remote_device_timeout),
        compact = true,
        onClick = {
          val state = SliderState(
            valueRange = 1f..10f,
            value = (preferenceSettings!!.removeDeviceTimeout / 1000).toFloat(),
            steps = 17,
          )

          coroutine.launch {
            val result = showSliderConfig(state) ?: return@launch
            SettingsStore.preference.setValue { removeDeviceTimeout = result }
          }
        }
      ) {
        Text(
          color = MaterialTheme.colorScheme.primary,
          text = String.format("%.1f", (preferenceSettings!!.removeDeviceTimeout / 1000).toFloat()) + "s"
        )
      }

      Title(stringResource(R.string.experimental))
      SettingsScreenItem(
        title = stringResource(R.string.dynamic_power_actions),
        subtext = stringResource(R.string.s_dynamic_power_actions_description),
        onClick = {
          coroutine.launch { SettingsStore.preference.setValue { dynamicPowerActions = !dynamicPowerActions } }
        }
      ) {
        Switch(
          checked = preferenceSettings!!.dynamicPowerActions,
          onCheckedChange = {
            coroutine.launch { SettingsStore.preference.setValue { dynamicPowerActions = it } }
          }
        )
      }
    }
  }
}

@Composable
private fun Title(text: String) {
  Text(
    modifier = Modifier
      .padding(top = 10.dp, start = 20.dp, bottom = 5.dp),
    text = text,
    fontSize = 16.sp,
    color = MaterialTheme.colorScheme.primary,
    fontWeight = FontWeight.Bold
  )
}

private fun getOptionTextOfForceShutdown(seconds: Int) = when(seconds) {
  0 -> Globals.context.getString(R.string.off)
  10 -> Globals.context.getString(R.string.f_after_seconds, "10")
  20 -> Globals.context.getString(R.string.f_after_seconds, "20")
  30 -> Globals.context.getString(R.string.f_after_seconds, "30")
  60 -> Globals.context.getString(R.string.f_after_minutes, "1")
  180 -> Globals.context.getString(R.string.f_after_minutes, "3")
  else -> throw Exception("There is no text for $seconds seconds")
}