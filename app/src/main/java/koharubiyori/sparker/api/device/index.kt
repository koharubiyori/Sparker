package koharubiyori.sparker.api.device

import koharubiyori.sparker.request.hostRequest
import koharubiyori.sparker.util.ActiveDeviceScope

class DeviceApi(val scope: ActiveDeviceScope) {
  suspend fun getPairingCode(reqBody: GetPairingCodeReq) {
    return scope.hostRequest(
      url = "/device/getPairingCode",
      body = reqBody
    )
  }

  suspend fun pair(reqBody: PairReq): PairRes {
    return scope.hostRequest(
      url = "/device/pair",
      body = reqBody
    )
  }

  suspend fun unpair(reqBody: UnpairReq) {
    scope.hostRequest<Unit>(
      url = "/device/unpair",
      body = reqBody
    )
  }
}

val ActiveDeviceScope.deviceApi: DeviceApi
  get() = DeviceApi(this)
