package koharubiyori.sparker.api.power

import koharubiyori.sparker.request.hostRequest
import koharubiyori.sparker.util.ActiveDeviceScope

class PowerApi(val scope: ActiveDeviceScope) {
  suspend fun shutdown(reqBody: ShutdownReq) {
    scope.hostRequest<Unit>(
      url = "/power/shutdown",
      body = reqBody
    )
  }

  suspend fun sleep(reqBody: SleepReq) {
    scope.hostRequest<Unit>(
      url = "/power/sleep",
      body = reqBody
    )
  }

  suspend fun unlock(): UnlockRes {
    return scope.hostRequest(
      url = "/power/unlock"
    )
  }

  suspend fun lock() {
    scope.hostRequest<Unit>(
      url = "/power/lock"
    )
  }

  suspend fun isLocked(): IsLockedRes {
    return scope.hostRequest(
      url = "/power/isLocked"
    )
  }
}

val ActiveDeviceScope.powerApi: PowerApi
  get() = PowerApi(this)