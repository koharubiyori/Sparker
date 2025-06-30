package koharubiyori.sparker.webSocket

import androidx.compose.runtime.snapshotFlow
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.util.ActiveDeviceScope
import koharubiyori.sparker.util.DeviceStateCenter
import koharubiyori.sparker.util.globalDefaultCoroutineScope
import koharubiyori.sparker.util.toWebSocketURI
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URI
import java.net.URL

object WebSocketConnectionCenter {
  private const val webSocketPath = "/ws"
  private val webSocketConnectionMap = mutableMapOf<String, WebSocketConnection>()  // key: device name

  init {
    globalDefaultCoroutineScope.launch {
      snapshotFlow { DeviceStateCenter.stateMap }.collect {
        it
        refreshConnections()
      }
    }
  }

  private suspend fun refreshConnections() {
    val activeDeviceMap = DeviceStateCenter.stateMap.filter { it.value.serverOnline && it.value.paired == true }
    // Clean the connections that are no longer active
    webSocketConnectionMap.filter { !activeDeviceMap.keys.contains(it.key) }.forEach {
      it.value.cancel()
      webSocketConnectionMap.remove(it.key)
    }

    val newDeviceMap = activeDeviceMap.filter { !webSocketConnectionMap.keys.contains(it.key) }
    newDeviceMap.forEach {
      val deviceConfig = DeviceConfigStore.deviceConfigs.first().first { deviceConfig -> deviceConfig.name == it.key }
      val hostUrl = ActiveDeviceScope(deviceConfig, it.value).hostUrl
      val webSocketUrl = URI.create(hostUrl).resolve(webSocketPath).toWebSocketURI().toString()
      val connection = WebSocketConnection(webSocketUrl, deviceConfig.name)
      webSocketConnectionMap[it.key] = connection
      Timber.i("Trying connect to $webSocketUrl ...")
      connection.connect()
    }
  }

  fun get(deviceName: String): WebSocketConnection? {
    return webSocketConnectionMap[deviceName]
  }
}