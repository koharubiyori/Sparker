package koharubiyori.sparker.screen.scanDevices

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import koharubiyori.sparker.util.LoadStatus
import koharubiyori.sparker.util.ScanResult
import koharubiyori.sparker.util.scanIpInLan
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
class ScanDevicesScreenModel @Inject() constructor() : ViewModel() {
  lateinit var routeArguments: ScanDevicesRouteArguments
  val pullToRefreshState = PullToRefreshState()
  val scannedDevices = mutableStateListOf<ScanResult>()
  var scanStatus by mutableStateOf(LoadStatus.INITIAL)

  suspend fun rescanDevices() {
    if (LoadStatus.isLoading(scanStatus)) return

    scannedDevices.clear()
    scanStatus = LoadStatus.LOADING
    scanIpInLan().collect { scannedDevices.add(it) }
    scanStatus = LoadStatus.DONE
  }
}