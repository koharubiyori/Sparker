package koharubiyori.sparker.api.hostInfo

import koharubiyori.sparker.util.ProguardIgnore

@ProguardIgnore
data class ApproachReq(val withMacAddress: Boolean? = null)

@ProguardIgnore
data class ApproachRes(
  val reached: Boolean,
  val macAddress: String?
)

@ProguardIgnore
data class BasicInfoRes(
  val macAddress: String,
  val hibernateEnabled: Boolean
)