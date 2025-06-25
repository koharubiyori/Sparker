package koharubiyori.sparker.screen.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import koharubiyori.sparker.screen.home.component.BottomSheetToPairDeviceState
import koharubiyori.sparker.screen.settings.ClickAction
import koharubiyori.sparker.screen.settings.PowerOffAction
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.util.DeviceStateCenter
import koharubiyori.sparker.store.SettingsStore
import koharubiyori.sparker.util.LoadStatus
import koharubiyori.sparker.util.RemoteDevicePowerActions
import koharubiyori.sparker.util.toast
import koharubiyori.sparker.util.tryToastAsRequestException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@HiltViewModel
class HomeScreenModel @Inject constructor() : ViewModel() {
  val localCoroutineScopeState = LocalCoroutineScopeState()
  val bottomSheetToAddDeviceState = BottomSheetToAddDeviceState()
  val bottomSheetForDeviceActionsState = BottomSheetForDeviceActionsState()
  val bottomSheetToPairDeviceState = BottomSheetToPairDeviceState()
  var deviceRefreshStatus by mutableStateOf(LoadStatus.INITIAL)

  fun testIsHostConnected(deviceConfig: DeviceConfig): Boolean {
    val deviceState = DeviceStateCenter.stateMap[deviceConfig.name]!!
    return when {
      !deviceState.pingOnline -> {
        toast(Globals.context.getString(R.string.s_device_offline))
        false
      }
      !deviceState.serverOnline -> {
        toast(Globals.context.getString(R.string.s_device_without_server))
        false
      }
      deviceState.paired != true -> {
        toast(Globals.context.getString(R.string.s_device_not_paired))
        false
      }
      else -> true
    }
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

    RemoteDevicePowerActions.wake(deviceConfig)
    toast(Globals.context.getString(R.string.s_woke))
  }


  suspend fun powerOff(deviceConfig: DeviceConfig, action: PowerOffAction? = null) {
    val preferenceSettings = SettingsStore.preference.getValue { this }.first()
    val deviceState = DeviceStateCenter.stateMap[deviceConfig.name]!!
    val finalAction = action ?: preferenceSettings.defaultPowerOffAction
    val actionName = Globals.context.getString(when (finalAction) {
      PowerOffAction.SHUTDOWN -> R.string.shutdown
      PowerOffAction.SLEEP -> R.string.sleep
      PowerOffAction.HIBERNATE -> R.string.hibernate
      PowerOffAction.REBOOT -> R.string.reboot
    })

    if (!testIsHostConnected(deviceConfig)) return
    if (finalAction == PowerOffAction.HIBERNATE && !deviceState.hibernateEnabled!!) {
      Globals.commonAlertDialog.showText(Globals.context.getString(R.string.s_disabled_hibernate_waring))
      return
    }
    try {
      when (finalAction) {
        PowerOffAction.SHUTDOWN -> RemoteDevicePowerActions.shutdown(
          deviceConfig = deviceConfig,
          force = preferenceSettings.forceShutdown != 0,
          timeout = preferenceSettings.forceShutdown,
          hybridShutdown = preferenceSettings.fastBoot
        )
        PowerOffAction.SLEEP -> RemoteDevicePowerActions.sleep(deviceConfig)
        PowerOffAction.HIBERNATE -> RemoteDevicePowerActions.hibernate(deviceConfig)
        PowerOffAction.REBOOT -> RemoteDevicePowerActions.shutdown(
          deviceConfig = deviceConfig,
          force = preferenceSettings.forceShutdown != 0,
          timeout = preferenceSettings.forceShutdown,
          hybridShutdown = preferenceSettings.fastBoot,
          reboot = true
        )
      }
      toast("[$actionName] ${Globals.context.getString(R.string.s_sent)}")
    } catch (ex: Exception) {
      if (!ex.tryToastAsRequestException()) throw ex
    }
  }

  suspend fun lock(deviceConfig: DeviceConfig) {
    if (!testIsHostConnected(deviceConfig)) return
    try {
      RemoteDevicePowerActions.lock(deviceConfig)
      toast(Globals.context.getString(R.string.s_locked))
    } catch (ex: Exception) {
      if (!ex.tryToastAsRequestException()) throw ex
    }
  }

  suspend fun unlock(deviceConfig: DeviceConfig) {
    if (!testIsHostConnected(deviceConfig)) return
    try {
      RemoteDevicePowerActions.unlock(deviceConfig)
      toast(Globals.context.getString(R.string.s_unlocked))
    } catch (ex: Exception) {
      if (!ex.tryToastAsRequestException()) throw ex
    }
  }

  suspend fun handleItemClick(deviceConfig: DeviceConfig) {
    val preferenceSettings = SettingsStore.preference.getValue { this }.first()
    val deviceState = DeviceStateCenter.stateMap[deviceConfig.name]

    when (preferenceSettings.clickAction) {
      ClickAction.POWER_ON -> powerOn(deviceConfig)
      ClickAction.POWER_OFF -> powerOff(deviceConfig)
      ClickAction.TOGGLE -> if (deviceState?.pingOnline == true) powerOff(deviceConfig) else powerOn(deviceConfig)
    }
  }

  suspend fun refreshDevices() {
    if (LoadStatus.isLoading(deviceRefreshStatus)) return
    deviceRefreshStatus = LoadStatus.LOADING
    DeviceStateCenter.testAllDevicesConnectionState()
    deviceRefreshStatus = LoadStatus.DONE
  }

  suspend fun reset() {
    bottomSheetToAddDeviceState.hide()
    bottomSheetForDeviceActionsState.hide()
    bottomSheetToPairDeviceState.hide()
    refreshDevices()
  }
}