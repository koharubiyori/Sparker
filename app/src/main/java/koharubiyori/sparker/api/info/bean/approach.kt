package koharubiyori.sparker.api.info.bean

data class ApproachReq(val withMacAddress: Boolean? = null)

data class ApproachRes(
  val reached: Boolean,
  val macAddress: String?
)