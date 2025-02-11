package koharubiyori.sparker.util

import koharubiyori.sparker.api.power.PowerApi
import koharubiyori.sparker.api.power.ShutdownReq
import koharubiyori.sparker.api.power.SleepReq
import koharubiyori.sparker.store.DeviceConfig

suspend fun DeviceConfig.wake() {
  val currentNetworkSegment = NetworkUtil.getSelfIpInLan("255")
  if (port == null) {
    NetworkUtil.sendWakeOnLan(currentNetworkSegment, macAddress!!, 7)
    NetworkUtil.sendWakeOnLan(currentNetworkSegment, macAddress, 9)
  } else {
    NetworkUtil.sendWakeOnLan(currentNetworkSegment, macAddress!!, port)
  }
}

// hybrid shutdown == fast boot
// Why don't name it as fast boot: hybridShutdown cannot be used with reboot, this will cause confusion
suspend fun DeviceConfig.shutdown(force: Boolean = false, timeout: Int = 0, reboot: Boolean = false, hybridShutdown: Boolean = false) {
  toastExceptionHandlerForRequest {
    PowerApi.shutdown(ShutdownReq(
      force = force,
      timeout = timeout,
      reboot = reboot,
      hybrid = hybridShutdown
    ))
  }
}

suspend fun DeviceConfig.sleep() {
  toastExceptionHandlerForRequest {
    PowerApi.sleep(SleepReq())
  }
}

suspend fun DeviceConfig.hibernate() {
  toastExceptionHandlerForRequest {
    PowerApi.sleep(SleepReq(hibernate = true))
  }
}
