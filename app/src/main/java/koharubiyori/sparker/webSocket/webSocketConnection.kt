package koharubiyori.sparker.webSocket

import koharubiyori.sparker.webSocket.hostMessage.handleRawHostMessage
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.util.DeviceStateCenter
import koharubiyori.sparker.util.globalDefaultCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber
import java.util.concurrent.TimeUnit

private class HostWebSocketListener(
  val deviceConfig: DeviceConfig,
  val connection: WebSocketConnection,
) : WebSocketListener() {
  override fun onOpen(webSocket: WebSocket, response: Response) {
    Timber.i("[WebSocket] ${deviceConfig.name}: Connection Opened!")
  }

  override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    Timber.i("[WebSocket] ${deviceConfig.name}: Received message: $bytes")
//    connection.run { handleRawHostMessage(connection, bytes) }
  }

  override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    Timber.e(t, "[WebSocket] ${deviceConfig.name}: Failed!")
    globalDefaultCoroutineScope.launch {
      DeviceStateCenter.testDeviceConnectionState(deviceConfig.name)
    }
  }

  override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    Timber.i("[WebSocket] ${deviceConfig.name}: Connection Closed!")
    globalDefaultCoroutineScope.launch {
      DeviceStateCenter.testDeviceConnectionState(deviceConfig.name)
    }
  }
}

class WebSocketConnection(
  val url: String,
  val deviceName: String
) {
  private lateinit var webSocket: WebSocket

  suspend fun connect() = withContext(Dispatchers.IO) {
    val deviceConfig = DeviceConfigStore.getConfigByName(deviceName)!!
    val client = OkHttpClient.Builder()
      .connectTimeout(5, TimeUnit.SECONDS)
      .readTimeout(0, TimeUnit.MILLISECONDS)
      .build()
    val request = Request.Builder()
      .url(url)
      .apply { if (deviceConfig.token != null) header("Authorization", deviceConfig.token) }
      .build()

    val listener = HostWebSocketListener(deviceConfig, this@WebSocketConnection)
    webSocket = client.newWebSocket(request, listener)
  }

  fun cancel() = webSocket.cancel()
  fun close() = webSocket.close(1000, null)

  fun send() {

  }
}