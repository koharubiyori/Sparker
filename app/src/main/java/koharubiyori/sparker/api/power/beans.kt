package koharubiyori.sparker.api.power

import koharubiyori.sparker.util.ProguardIgnore

@ProguardIgnore
data class ShutdownReq(
  val force: Boolean? = null,
  val timeout: Int? = null,
  val reboot: Boolean? = null,
  val hybrid: Boolean? = null,
)

@ProguardIgnore
data class SleepReq(
  val hibernate: Boolean? = null
)

@ProguardIgnore
data class UnlockRes(
  val success: Boolean
)

@ProguardIgnore
data class IsLockedRes(
  val locked: Boolean
)