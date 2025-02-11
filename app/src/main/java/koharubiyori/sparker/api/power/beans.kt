package koharubiyori.sparker.api.power

data class ShutdownReq(
  val force: Boolean? = null,
  val timeout: Int? = null,
  val reboot: Boolean? = null,
  val hybrid: Boolean? = null,
)

data class SleepReq(
  val hibernate: Boolean? = null
)