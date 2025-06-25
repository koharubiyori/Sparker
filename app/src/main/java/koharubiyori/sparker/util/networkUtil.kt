package koharubiyori.sparker.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.provider.Settings
import koharubiyori.sparker.Globals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.UnknownHostException

object NetworkUtil {
  // This is inaccurate when called immediately after the network status changes.
  // In the case, you should obtain WifiState from LocalWifiState.current in @Composable function
  fun isWifiConnected(): Boolean {
    val connectivityManager = Globals.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
  }

  fun isWifiEnabled(): Boolean {
    val wifiManager = Globals.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return wifiManager.isWifiEnabled
  }

  @SuppressLint("DefaultLocale")
  fun getSelfIpInLan(replaceLastPart: String? = null): String {
    val wifiManager = Globals.context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    val ipAddress = wifiInfo.ipAddress
    return String.format(
      "%d.%d.%d.%s",
      ipAddress and 0xFF,
      ipAddress shr 8 and 0xFF,
      ipAddress shr 16 and 0xFF,
      replaceLastPart ?: (ipAddress shr 24 and 0xFF).toString()
    )
  }

  fun macStringToBytes(macAddress: String): ByteArray {
    val macParts = macAddress.split(":")
    return ByteArray(6) { index ->
      macParts[index].toInt(16).toByte()
    }
  }

  fun openWifiSettings() {
    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
    Globals.activity.startActivity(intent)
  }

  suspend fun sendWakeOnLan(ipAddress: String, macAddress: String, port: Int): Boolean = withContext(Dispatchers.IO) {
    val ffBytes = (0..5).map { (0xff).toByte() }
    val macBytes = macAddress.split(":")
      .map { it.toInt(16).toByte() }
      .let { macByte -> (0..15).map { macByte } }
      .flatten()

    val magicPacket = (ffBytes + macBytes).toByteArray()
    val address = InetAddress.getByName(ipAddress)
    val packet = DatagramPacket(magicPacket, magicPacket.size, address, port)

    try {
      DatagramSocket().apply {
        broadcast = true
        send(packet)
        close()
      }

      return@withContext true
    } catch (ex: IOException) {
      Timber.e(ex, "Failed to send magic packet for Wake-On-Lan")
      return@withContext false
    }
  }

  fun isLocalIP(ipAddress: String): Boolean {
    return try {
      val inetAddress = InetAddress.getByName(ipAddress)
      val bytes = inetAddress.address

      when {
        (bytes[0].toInt() and 0xFF) == 10 -> true  // 10.x.x.x
        (bytes[0].toInt() and 0xFF) == 172 && (bytes[1].toInt() and 0xFF) in 16..31 -> true  // 172.16.x.x - 172.31.x.x
        (bytes[0].toInt() and 0xFF) == 192 && (bytes[1].toInt() and 0xFF) == 168 -> true  // 192.168.x.x
        else -> false
      }
    } catch (ex: UnknownHostException) {
      false
    }
  }
}
