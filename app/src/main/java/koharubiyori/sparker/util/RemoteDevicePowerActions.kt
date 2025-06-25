package koharubiyori.sparker.util

import koharubiyori.sparker.api.power.ShutdownReq
import koharubiyori.sparker.api.power.SleepReq
import koharubiyori.sparker.api.power.powerApi
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.util.DeviceStateCenter

object RemoteDevicePowerActions {
  suspend fun wake(deviceConfig: DeviceConfig) {
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
    deviceConfig: DeviceConfig,
    force: Boolean = false,
    timeout: Int = 0,
    reboot: Boolean = false,
    hybridShutdown: Boolean = false
  ) {
    DeviceStateCenter.deviceScope(deviceConfig) {
      powerApi.shutdown(ShutdownReq(
        force = force,
        timeout = timeout,
        reboot = reboot,
        hybrid = hybridShutdown
      ))
    }
  }

  suspend fun sleep(deviceConfig: DeviceConfig) {
    DeviceStateCenter.deviceScope(deviceConfig) {
      powerApi.sleep(SleepReq(hibernate = false))
    }
  }

  suspend fun hibernate(deviceConfig: DeviceConfig) {
    DeviceStateCenter.deviceScope(deviceConfig) {
      powerApi.sleep(SleepReq(hibernate = true))
    }
  }

  suspend fun lock(deviceConfig: DeviceConfig) {
    DeviceStateCenter.deviceScope(deviceConfig) { powerApi.lock() }
  }

  suspend fun unlock(deviceConfig: DeviceConfig): Boolean {
    return DeviceStateCenter.deviceScope(deviceConfig) { powerApi.unlock().success }
  }
}