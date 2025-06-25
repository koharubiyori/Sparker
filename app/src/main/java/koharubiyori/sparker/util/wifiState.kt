package koharubiyori.sparker.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import koharubiyori.sparker.Globals

class SimpleNetworkStateObserver {
  private var wifiEnabled by mutableStateOf(NetworkUtil.isWifiEnabled())
  private var networkConnected by mutableStateOf(NetworkUtil.isWifiConnected())
  private val wifiStateMonitor = WifiStateMonitor { wifiEnabled = it }
  private val networkConnectionMonitor = NetworkConnectionMonitor { networkConnected = it }

  fun unregister() {
    wifiStateMonitor.unregister()
    networkConnectionMonitor.unregister()
  }

  @Composable
  fun Provider(content: @Composable () -> Unit) {
    val wifiState = remember(wifiEnabled, networkConnected) {
      when {
        wifiEnabled && networkConnected -> WifiState.CONNECTED
        wifiEnabled -> WifiState.ENABLED
        else -> WifiState.DISABLED
      }
    }

    CompositionLocalProvider(
      LocalWifiState provides wifiState,
      content = content
    )
  }
}

val LocalWifiState = compositionLocalOf { WifiState.DISABLED }

enum class WifiState {
  DISABLED, ENABLED, CONNECTED
}


private class WifiStateMonitor(
  val onWifiStatusChanged: (enabled: Boolean) -> Unit
) {
  private val wifiStateReceiver = WifiStateReceiver()

  init {
    val filter = IntentFilter()
    filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION) // WiFi 状态变化
//    filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) // WiFi 连接状态变化
    registerReceiver(Globals.context, wifiStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
  }

  fun unregister() {
    Globals.context.unregisterReceiver(wifiStateReceiver)
  }

  private inner class WifiStateReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      val action = intent?.action

      if (action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
        // 获取 WiFi 状态
        val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
        when (wifiState) {
          WifiManager.WIFI_STATE_ENABLED -> onWifiStatusChanged(true)
          WifiManager.WIFI_STATE_DISABLED -> onWifiStatusChanged(false)
        }
      }
    }
  }
}

private class NetworkConnectionMonitor(
  val onNetworkStatusChanged: (isConnected: Boolean) -> Unit
) {
  private val connectivityManager =
    Globals.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      onNetworkStatusChanged(true)
    }
    override fun onLost(network: Network) {
      onNetworkStatusChanged(false)
    }
  }

  init {
    register()
  }

  fun register() {
    val networkRequest = NetworkRequest.Builder()
      .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
      .build()
    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
  }

  fun unregister() {
    connectivityManager.unregisterNetworkCallback(networkCallback)
  }
}


//fun getWifiState(): WifiState {
//  val wifiManager = Globals.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//  val connectivityManager = Globals.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//  val network = connectivityManager.activeNetwork
//  val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
//
//  return when {
//    !wifiManager.isWifiEnabled -> WifiState.DISABLED
//    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> WifiState.CONNECTED
//    else -> WifiState.ENABLED
//  }
//}

