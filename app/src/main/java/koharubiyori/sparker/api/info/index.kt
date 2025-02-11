package koharubiyori.sparker.api.info

import koharubiyori.sparker.api.info.bean.ApproachReq
import koharubiyori.sparker.api.info.bean.ApproachRes
import koharubiyori.sparker.api.info.bean.BasicInfoRes
import koharubiyori.sparker.request.hostRequest
import java.net.URI
import java.util.concurrent.TimeUnit

object InfoApi {
  suspend fun getBasicInfo(baseUrl: String): BasicInfoRes {
    return hostRequest(
      entity = BasicInfoRes::class.java,
      baseUrl = baseUrl,
      url = "/info/getBasicInfo",
      body = null
    )!!
  }

  suspend fun approach(baseUrl: String, withMacAddress: Boolean = false): ApproachRes {
    return hostRequest(
      entity = ApproachRes::class.java,
      baseUrl = baseUrl,
      url = "/info/approach",
      body = ApproachReq(withMacAddress),
      clientBuilder = { connectTimeout(3, TimeUnit.SECONDS) },
    )!!
  }
}