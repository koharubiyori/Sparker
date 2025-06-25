package koharubiyori.sparker.api.hostInfo

import koharubiyori.sparker.request.hostRequest
import koharubiyori.sparker.util.ActiveDeviceScope
import java.util.concurrent.TimeUnit

class HostInfoApi(private val scope: ActiveDeviceScope) {
  suspend fun getBasicInfo(): BasicInfoRes {
    return scope.hostRequest(
      url = "/hostInfo/getBasicInfo",
      body = null
    )
  }

  companion object {
    suspend fun approach(baseUrl: String, withMacAddress: Boolean = false): ApproachRes {
      return hostRequest(
        baseUrl = baseUrl,
        url = "/hostInfo/approach",
        body = ApproachReq(withMacAddress),
        clientBuilder = { connectTimeout(3, TimeUnit.SECONDS) },
      )
    }
  }
}

val ActiveDeviceScope.hostInfoApi: HostInfoApi
  get() = HostInfoApi(this)