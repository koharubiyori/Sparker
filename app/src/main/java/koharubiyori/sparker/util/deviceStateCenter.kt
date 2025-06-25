package koharubiyori.sparker.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import koharubiyori.sparker.Constants
import koharubiyori.sparker.api.hostInfo.HostInfoApi
import koharubiyori.sparker.api.hostInfo.hostInfoApi
import koharubiyori.sparker.api.power.powerApi
import koharubiyori.sparker.request.HttpException
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.store.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.InetAddress
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.seconds

object DeviceStateCenter {
  private val _stateMap = mutableStateMapOf<String, DeviceState>()  // key: device name

  var autoRefresh = true
  val composableStateMap @Composable get() = _stateMap
  val stateMap get() = _stateMap.toMap()

  init {
    IntervalTask(5.seconds) {
      if (!autoRefresh) return@IntervalTask
      testAllDevicesConnectionState()
    }
  }

  private fun generateHostUrl(
    deviceConfig: DeviceConfig? = null,
    deviceState: DeviceState? = null,
    hostName: String? = null,
    serverPort: Int? = null
  ): String? {
    val finalHostName = hostName ?: deviceConfig?.hostName
    val finalServerPort = serverPort ?: deviceState?.serverPort

    return finalHostName?.let { "http://${it}:${finalServerPort ?: Constants.HOST_SERVER_PORT}" }
  }

  private suspend fun testServer(deviceConfig: DeviceConfig?, deviceState: DeviceState?): Long {
    val url = generateHostUrl(deviceConfig, deviceState) ?: return -1

    try {
      return measureTimeMillis {
        HostInfoApi.Companion.approach(url)
      }
    } catch (ex: Exception) {
      if (ex.isFailedRequest()) {
        Timber.Forest.e(ex)
        return -1
      } else {
        throw ex
      }
    }
  }

  private suspend fun testPing(ip: String): Long = withContext(Dispatchers.IO) {
    val deviceTimeout = SettingsStore.preference.getValue {
      if (NetworkUtil.isLocalIP(ip)) localDeviceTimeout else removeDeviceTimeout
    }.first()

    val time = measureTimeMillis {
      InetAddress.getByName(ip).isReachable(deviceTimeout)
    }

    return@withContext if (time < deviceTimeout) time else -1
  }

  suspend fun testDeviceConnectionState(deviceConfig: DeviceConfig) {
    val state = _stateMap[deviceConfig.name] ?: return
    val pingTestResult = testPing(deviceConfig.hostName)
    val serverTestResult = testServer(deviceConfig, state)

    val newDeviceState = state.copy(
      pingTimeout = pingTestResult,
      serverTimeout = serverTestResult
    )
    _stateMap[deviceConfig.name] = newDeviceState

    if (newDeviceState.pingOnline && (!newDeviceState.serverOnline || newDeviceState.serverOnline && newDeviceState.paired == null)) refreshDeviceState(deviceConfig)
  }

  suspend fun testAllDevicesConnectionState() = coroutineScope {
    val deviceConfigs = DeviceConfigStore.deviceConfigs.first()
    _stateMap.entries.forEach {
      val foundItem = deviceConfigs.firstOrNull { deviceConfig -> deviceConfig.name == it.key } ?: return@forEach
      launch { testDeviceConnectionState(foundItem) }
    }
  }

  suspend fun refreshDeviceState(deviceConfig: DeviceConfig) {
    val serverPort = tryGettingServicePortFromHostServer(deviceConfig.hostName)
    with(ActiveDeviceScope(deviceConfig, DeviceState(serverPort = serverPort))) {
      var paired: Boolean? = null
      val basicInfo = serverPort?.let {
        try {
          val result = hostInfoApi.getBasicInfo()
          paired = true
          result
        } catch (ex: Exception) {
          if (ex.isFailedRequest()) {
            if (ex is HttpException && ex.code == 401) {
              paired = false
            } else {
              Timber.Forest.e(ex)
            }
            return@let null
          }

          throw ex
        }
      }

      val locked = serverPort?.let {
        try {
          val result = ActiveDeviceScope(deviceConfig, DeviceState(serverPort = serverPort)).run {
            powerApi.isLocked()
          }
          result.locked
        } catch (ex: Exception) {
          if (!ex.isFailedRequest()) throw ex
          Timber.Forest.e(ex)
          null
        }
      }

      _stateMap[deviceConfig.name] = (_stateMap[deviceConfig.name] ?: DeviceState()).copy(
        serverPort = serverPort,
        paired = paired,
        hibernateEnabled = basicInfo?.hibernateEnabled,
        locked = locked
      )
    }
  }

  suspend fun registerDevice(deviceConfig: DeviceConfig) {
    if (_stateMap.containsKey(deviceConfig.name)) return
    _stateMap[deviceConfig.name] = DeviceState()
    testDeviceConnectionState(deviceConfig)
  }

  suspend fun registerAllDevices() = coroutineScope {
    val deviceConfigs = DeviceConfigStore.deviceConfigs.first()
    val jobs = deviceConfigs.map {
      launch { registerDevice(it) }
    }

    joinAll(*jobs.toTypedArray())
  }

  fun unregisterDevice(deviceConfig: DeviceConfig) {
    _stateMap.remove(deviceConfig.name)
  }

  suspend fun <T> deviceScope(deviceConfig: DeviceConfig, run: suspend ActiveDeviceScope.() -> T): T {
    return ActiveDeviceScope(deviceConfig, _stateMap[deviceConfig.name]!!).run { run() }
  }

  suspend fun updateLockState(deviceConfig: DeviceConfig) {
    val targetState = _stateMap[deviceConfig.name] ?: return

    val locked = try {
      if (targetState.paired == true) {
        val result = deviceScope(deviceConfig) { powerApi.isLocked() }
        result.locked
      } else null
    } catch (ex: Exception) {
      if (!ex.isFailedRequest()) throw ex
      Timber.Forest.e(ex)
      null
    }
    _stateMap[deviceConfig.name] = targetState.copy(locked = locked)
  }
}

data class DeviceState(
  val serverPort: Int? = Constants.HOST_SERVER_PORT,
  val pingTimeout: Long = -1,
  val serverTimeout: Long = -1,
  // The following properties are null if the device is offline
  val paired: Boolean? = null,
  val hibernateEnabled: Boolean? = null,
  val locked: Boolean? = null
) {
  val pingOnline: Boolean get() = pingTimeout != -1L
  val serverOnline: Boolean get() = serverTimeout != -1L
}

class ActiveDeviceScope(
  val deviceConfig: DeviceConfig,
  val deviceState: DeviceState
) {
  val hostUrl: String get() = "http://${deviceConfig.hostName}:${deviceState.serverPort ?: Constants.HOST_SERVER_PORT}"
}