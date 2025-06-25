package koharubiyori.sparker.api.device

import koharubiyori.sparker.util.ProguardIgnore

@ProguardIgnore
data class GetPairingCodeReq(
  val sessionId: String
)

@ProguardIgnore
data class PairReq(
  val deviceId: String,
  val sessionId: String,
  val pairingCode: String,
  val username: String,
  val password: String
)

@ProguardIgnore
data class PairRes(
  val token: String
)

@ProguardIgnore
data class UnpairReq(val deviceId: String)