package koharubiyori.sparker.store

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import koharubiyori.sparker.Constants
import koharubiyori.sparker.api.info.InfoApi
import koharubiyori.sparker.util.IntervalTask
import koharubiyori.sparker.util.NetworkUtil
import koharubiyori.sparker.util.isFailedRequest
import koharubiyori.sparker.util.tryGettingServicePortFromHostServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.InetAddress
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.seconds

object DeviceConnectionStore {
  private val _connectionInfoMap = mutableStateMapOf<DeviceConfig, DeviceConnectionInfo>()
  private var activeDevice: DeviceConfig? = null

  private val activeConnectionInfo get() = _connectionInfoMap[activeDevice]

  val hostUrl get() = generateHostUrl(activeDevice, activeConnectionInfo)

  val composableConnectionInfoMap @Composable get() = _connectionInfoMap
  val connectionInfoMap get() = _connectionInfoMap.toMap()

  init {
    IntervalTask(5.seconds) { testAllDevice() }
  }

  private fun generateHostUrl(
    deviceConfig: DeviceConfig? = null,
    connectionInfo: DeviceConnectionInfo? = null,
    hostName: String? = null,
    serverPort: Int? = null
  ): String? {
    val finalHostName = hostName ?: deviceConfig?.hostName
    val finalServerPort = serverPort ?: connectionInfo?.serverPort

    return finalHostName?.let { "http://${it}:${finalServerPort ?: Constants.HOST_SERVER_PORT}" }
  }

  suspend fun testServer(deviceConfig: DeviceConfig?, connectionInfo: DeviceConnectionInfo?): NetworkTestResult {
    val url = generateHostUrl(deviceConfig, connectionInfo) ?: return NetworkTestResult(online = false)

    try {
      val time = measureTimeMillis {
        InfoApi.approach(url)
      }
      return NetworkTestResult(online = true, timeout = time)
    } catch (ex: Exception) {
      if (isFailedRequest(ex)) {
        Timber.w(ex)
        return NetworkTestResult(online = false)
      } else {
        throw ex
      }
    }
  }

  private suspend fun testPing(ip: String) = withContext(Dispatchers.IO) {
    val deviceTimeout = SettingsStore.preference.getValue {
      if (NetworkUtil.isLocalIP(ip)) localDeviceTimeout else removeDeviceTimeout
    }.first()

    val time = measureTimeMillis {
      InetAddress.getByName(ip).isReachable(deviceTimeout)
    }

    return@withContext NetworkTestResult(
      online = time < deviceTimeout,
      timeout = time
    )
  }

  suspend fun testAllDevice() {
    _connectionInfoMap.entries.forEach {
      val pingTestResult = testPing(it.key.hostName)
      val serverTestResult = testServer(it.key, it.value)

      _connectionInfoMap[it.key] = it.value.copy(
        online = pingTestResult.online,
        pingTimeout = pingTestResult.timeout,
        serverOnline = serverTestResult.online,
        serverTimeout = serverTestResult.timeout
      )
    }
  }

  private suspend fun getDeviceConnectionInfo(deviceConfig: DeviceConfig): DeviceConnectionInfo {
    val serverPort = tryGettingServicePortFromHostServer(deviceConfig.hostName)
    val basicInfo = serverPort?.let { InfoApi.getBasicInfo(generateHostUrl(deviceConfig, serverPort = serverPort)!!) }
    val pingTestResult = testPing(deviceConfig.hostName)
    return DeviceConnectionInfo(
      online = pingTestResult.online,
      serverPort = serverPort,
      serverOnline = serverPort != null,
      pingTimeout = pingTestResult.timeout,
      hibernateEnabled = basicInfo?.hibernateEnabled ?: false
    )
  }

  suspend fun registerDevice(deviceConfig: DeviceConfig) {
    if (_connectionInfoMap.containsKey(deviceConfig)) return
    _connectionInfoMap[deviceConfig] = getDeviceConnectionInfo(deviceConfig)
  }

  suspend fun resetRegisteredDevices(deviceConfigs: List<DeviceConfig>) = coroutineScope {
    val jobs = deviceConfigs.map {
      async { it to getDeviceConnectionInfo(it) }
    }

    val result = awaitAll(*jobs.toTypedArray())
    _connectionInfoMap.clear()
    result.forEach { _connectionInfoMap[it.first] = it.second }
  }

  fun unregisterDevice(deviceConfig: DeviceConfig) {
    _connectionInfoMap.remove(deviceConfig)
  }

  fun activate(deviceConfig: DeviceConfig) {
    activeDevice = deviceConfig
  }

  fun inactivateCurrentDevice() {
    activeDevice = null
  }

  suspend fun activeScope(deviceConfig: DeviceConfig, run: suspend () -> Unit) {
    activate(deviceConfig)
    run()
    inactivateCurrentDevice()
  }

  suspend fun refreshDevices() {
    resetRegisteredDevices(_connectionInfoMap.keys.toList())
  }
}

data class DeviceConnectionInfo(
  val online: Boolean,
  val serverPort: Int?,
  val serverOnline: Boolean,
  val pingTimeout: Long = -1,
  val serverTimeout: Long = -1,
  val hibernateEnabled: Boolean
)

class NetworkTestResult(
  val online: Boolean,
  val timeout: Long = -1
)