package koharubiyori.sparker.api.power

import koharubiyori.sparker.request.hostRequest

object PowerApi {
  suspend fun shutdown(reqBody: ShutdownReq) {
    hostRequest<Unit>(
      url = "/power/shutdown",
      body = reqBody
    )
  }

  suspend fun sleep(reqBody: SleepReq) {
    hostRequest<Unit>(
      url = "/power/sleep",
      body = reqBody
    )
  }

  suspend fun unlock() {
    hostRequest<Unit>(
      url = "/power/unlock"
    )
  }
}