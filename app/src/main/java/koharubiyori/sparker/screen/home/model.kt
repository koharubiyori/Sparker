package koharubiyori.sparker.screen.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.compable.remember.LocalCoroutineScopeState
import koharubiyori.sparker.component.commonDialog.ButtonConfig
import koharubiyori.sparker.component.commonDialog.CommonAlertDialogProps
import koharubiyori.sparker.screen.home.component.BottomSheetForDeviceActionsState
import koharubiyori.sparker.screen.home.component.BottomSheetToAddDeviceState
import koharubiyori.sparker.screen.settings.ClickAction
import koharubiyori.sparker.screen.settings.PowerOffAction
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConnectionStore
import koharubiyori.sparker.store.SettingsStore
import koharubiyori.sparker.util.LoadStatus
import koharubiyori.sparker.util.hibernate
import koharubiyori.sparker.util.shutdown
import koharubiyori.sparker.util.sleep
import koharubiyori.sparker.util.toast
import koharubiyori.sparker.util.wake
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@HiltViewModel
class HomeScreenModel @Inject constructor() : ViewModel() {
  val localCoroutineScopeState = LocalCoroutineScopeState()
  val bottomSheetToAddDeviceState = BottomSheetToAddDeviceState()
  val bottomSheetForDeviceActionsState = BottomSheetForDeviceActionsState()
  var deviceRefreshStatus by mutableStateOf(LoadStatus.INITIAL)

  private fun toastAboutPower(actionName: String, message: String) {
    toast("[$actionName]: $message")
  }

  suspend fun powerOn(deviceConfig: DeviceConfig) {
    if (deviceConfig.macAddress == null) {
      Globals.commonAlertDialog.show(CommonAlertDialogProps(
        leftButton = ButtonConfig(
          text = Globals.context.getString(R.string.go_to_edit),
          onClick = {
            bottomSheetForDeviceActionsState.hide()
            bottomSheetToAddDeviceState.show(edit = deviceConfig)
          }
        ),
        content = {
          Text(stringResource(R.string.s_cannot_wake_for_missing_mac))
        }
      ))
      return
    }

    deviceConfig.wake()
    toastAboutPower(Globals.context.getString(R.string.wake), Globals.context.getString(R.string.instruction_has_been_sent))
  }


  suspend fun powerOff(deviceConfig: DeviceConfig, action: PowerOffAction? = null) {
    DeviceConnectionStore.activeScope(deviceConfig) {
      val preferenceSettings = SettingsStore.preference.getValue { this }.first()
      val connectionInfo = DeviceConnectionStore.connectionInfoMap[deviceConfig]!!
      val finalAction = action ?: preferenceSettings.defaultPowerOffAction
      val actionName = Globals.context.getString(when (finalAction) {
        PowerOffAction.SHUTDOWN -> R.string.shutdown
        PowerOffAction.SLEEP -> R.string.sleep
        PowerOffAction.HIBERNATE -> R.string.hibernate
        PowerOffAction.REBOOT -> R.string.reboot
      })

      if (!connectionInfo.online) return@activeScope toastAboutPower(actionName, Globals.context.getString(R.string.s_device_offline))
      if (!connectionInfo.serverOnline) return@activeScope toastAboutPower(actionName, Globals.context.getString(R.string.s_device_without_server))
      if (finalAction == PowerOffAction.HIBERNATE && !connectionInfo.hibernateEnabled) {
        Globals.commonAlertDialog.showText(Globals.context.getString(R.string.s_disabled_hibernate_waring))
        return@activeScope
      }
      when (finalAction) {
        PowerOffAction.SHUTDOWN -> deviceConfig.shutdown(
          force = preferenceSettings.forceShutdown != 0,
          timeout = preferenceSettings.forceShutdown,
          hybridShutdown = preferenceSettings.fastBoot
        )
        PowerOffAction.SLEEP -> deviceConfig.sleep()
        PowerOffAction.HIBERNATE -> deviceConfig.hibernate()
        PowerOffAction.REBOOT ->deviceConfig.shutdown(
          force = preferenceSettings.forceShutdown != 0,
          timeout = preferenceSettings.forceShutdown,
          hybridShutdown = preferenceSettings.fastBoot,
          reboot = true
        )
      }

      toastAboutPower(actionName, Globals.context.getString(R.string.instruction_has_been_sent))
    }
  }

  suspend fun handleItemClick(deviceConfig: DeviceConfig) {
    val preferenceSettings = SettingsStore.preference.getValue { this }.first()
    val connectionInfo = DeviceConnectionStore.connectionInfoMap[deviceConfig]

    when (preferenceSettings.clickAction) {
      ClickAction.POWER_ON -> powerOn(deviceConfig)
      ClickAction.POWER_OFF -> powerOff(deviceConfig)
      ClickAction.TOGGLE -> if (connectionInfo?.online == true) powerOff(deviceConfig) else powerOn(deviceConfig)
    }
  }

  suspend fun refreshDevices() {
    if (LoadStatus.isLoading(deviceRefreshStatus)) return
    deviceRefreshStatus = LoadStatus.LOADING
    DeviceConnectionStore.refreshDevices()
    deviceRefreshStatus = LoadStatus.DONE
  }
}