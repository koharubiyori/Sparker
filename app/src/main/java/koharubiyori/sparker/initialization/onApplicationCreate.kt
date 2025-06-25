package koharubiyori.sparker.initialization

import android.app.Application
import koharubiyori.sparker.Globals
import koharubiyori.sparker.util.globalDefaultCoroutineScope
import koharubiyori.sparker.util.globalMainCoroutineScope
import koharubiyori.sparker.webSocket.WebSocketConnectionCenter
import kotlinx.coroutines.launch


fun Application.initializeOnCreate() {
  Globals.context = applicationContext
  WebSocketConnectionCenter
}