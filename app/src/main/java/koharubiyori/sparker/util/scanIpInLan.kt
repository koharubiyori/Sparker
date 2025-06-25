package koharubiyori.sparker.util

import android.os.Build
import it.alessangiorgi.ipneigh30.ArpNDK
import koharubiyori.sparker.Constants
import koharubiyori.sparker.api.hostInfo.ApproachRes
import koharubiyori.sparker.api.hostInfo.HostInfoApi
import koharubiyori.sparker.store.SettingsStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

private const val forwardStepForFindingHostServerPort = 5

fun scanIpInLan(): Flow<ScanResult> = channelFlow {
  val macAddresses = getMacAddressesFromArp()
  val networkSegment = NetworkUtil.getSelfIpInLan("")   // This value will be like: 192.168.0.
  val customDispatcher = Executors.newFixedThreadPool(254).asCoroutineDispatcher()

  try {
    withContext(customDispatcher) {
      val jobs = (1..254).map {
        val ip = "$networkSegment$it"
        val localDeviceTimeout = SettingsStore.preference.getValue { localDeviceTimeout }.first()
        launch {
          val time = measureTimeMillis {
            if (!InetAddress.getByName(ip).isReachable(localDeviceTimeout)) return@launch
          }

          send(
            ScanResult(
              ip = ip,
              mac = macAddresses[ip],
              // The ping may be assigned to the next loop and the final time will be greater than maxTimeout if the thread pool is filled
              delay = time.let { if (it > localDeviceTimeout) it - localDeviceTimeout else it },
            )
          )
        }
      }

      joinAll(*jobs.toTypedArray())
    }
  } finally {
    (customDispatcher.executor as java.util.concurrent.ThreadPoolExecutor).shutdown()
  }
}

private fun getMacAddressesFromArp(): Map<String, String> {
  // There is no way to get arp table on android 13 and above.
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return emptyMap()
  val arpTable = ArpNDK.getARP()
  val spaceRegex = Regex("""\s+""")
  return arpTable.split("\n")
    .map { it.split(spaceRegex) }
    .filter { it.size >= 5 && it[4] != "FAILED" }
    .associate { it[0] to it[4] }
}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun tryApproachingHostServer(ip: String): Pair<ApproachRes, Int>? = coroutineScope {
  try {
    // First of all, it sends just one request to the default server port to avoid send too many requests in a short time
    val baseUrlWithDefaultServerPort = "http://$ip:${Constants.HOST_SERVER_PORT}"
    val res = HostInfoApi.approach(baseUrlWithDefaultServerPort)
    res to Constants.HOST_SERVER_PORT
  } catch (ex: Exception) {
    if (ex.isFailedRequest()) {
      val jobs = (1..<forwardStepForFindingHostServerPort).map { i ->
        CompletableDeferred<Pair<ApproachRes, Int>>().also {
          launch {
            val portToTry = Constants.HOST_SERVER_PORT + i
            val baseUrl = "http://$ip:${portToTry}"
            try {
              val res = HostInfoApi.approach(baseUrl, true)
              it.complete(res to portToTry)
            } catch (ex: Exception) {
              if (!ex.isFailedRequest()) throw ex
            }
          }
        }
      }

      val localDeviceTimeout = SettingsStore.preference.getValue { localDeviceTimeout }.first()
      select<Pair<ApproachRes, Int>?> {
        jobs.forEach { it.onAwait { it } }
        onTimeout(localDeviceTimeout.toLong()) { null }
      }
    } else {
      throw ex
    }
  }
}

suspend fun tryGettingMacAddressFromHostServer(ip: String): String? {
  return tryApproachingHostServer(ip)?.first?.macAddress
}

suspend fun tryGettingServicePortFromHostServer(ip: String): Int? {
  return tryApproachingHostServer(ip)?.second
}

data class ScanResult(val ip: String, val mac: String?, val delay: Long)