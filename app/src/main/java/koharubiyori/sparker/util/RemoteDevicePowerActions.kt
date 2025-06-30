package koharubiyori.sparker.util

import koharubiyori.sparker.api.power.ShutdownReq
import koharubiyori.sparker.api.power.SleepReq
import koharubiyori.sparker.api.power.powerApi
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.util.DeviceStateCenter

object RemoteDevicePowerActions {
  suspend fun wake(deviceName: String) {
    val deviceConfig = DeviceConfigStore.getConfigByName(deviceName)!!
    val currentNetworkSegment = NetworkUtil.getSelfIpInLan("255")
    if (deviceConfig.wakeOnLanPort == null) {
      NetworkUtil.sendWakeOnLan(currentNetworkSegment, deviceConfig.macAddress!!, 7)
      NetworkUtil.sendWakeOnLan(currentNetworkSegment, deviceConfig.macAddress, 9)
    } else {
      NetworkUtil.sendWakeOnLan(currentNetworkSegment, deviceConfig.macAddress!!, deviceConfig.wakeOnLanPort)
    }
  }

  // hybrid shutdown == fast boot
  // Why don't name it as fast boot: hybridShutdown cannot be used with reboot, this will cause confusion
  suspend fun shutdown(
    deviceName: String,
    force: Boolean = false,
    timeout: Int = 0,
    reboot: Boolean = false,
    hybridShutdown: Boolean = false
  ) {
    DeviceStateCenter.deviceScope(deviceName) {
      powerApi.shutdown(ShutdownReq(
        force = force,
        timeout = timeout,
        reboot = reboot,
        hybrid = hybridShutdown
      ))
    }
  }

  suspend fun sleep(deviceName: String) {
    DeviceStateCenter.deviceScope(deviceName) {
      powerApi.sleep(SleepReq(hibernate = false))
    }
  }

  suspend fun hibernate(deviceName: String) {
    DeviceStateCenter.deviceScope(deviceName) {
      powerApi.sleep(SleepReq(hibernate = true))
    }
  }

  suspend fun lock(deviceName: String) {
    DeviceStateCenter.deviceScope(deviceName) { powerApi.lock() }
  }

  suspend fun unlock(deviceName: String): Boolean {
    return DeviceStateCenter.deviceScope(deviceName) { powerApi.unlock().success }
  }
}