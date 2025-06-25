package koharubiyori.sparker.screen.scanDevices

import koharubiyori.sparker.util.LocalWifiState
import StyledPullToRefreshBox
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.SignalWifiStatusbarConnectedNoInternet4
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.component.BackButton
import koharubiyori.sparker.component.EmptyContent
import koharubiyori.sparker.component.animation.AnimatedAppearing
import koharubiyori.sparker.component.styled.StyledTopAppBar
import koharubiyori.sparker.component.styled.TopAppBarTitle
import koharubiyori.sparker.store.SettingsStore
import koharubiyori.sparker.util.LoadStatus
import koharubiyori.sparker.util.NetworkUtil
import koharubiyori.sparker.util.ScanResult
import koharubiyori.sparker.util.WifiState
import koharubiyori.sparker.util.toast
import koharubiyori.sparker.util.tryApproachingHostServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDevicesScreen(arguments: ScanDevicesRouteArguments) {
  val model: ScanDevicesScreenModel = hiltViewModel()
  val coroutine = rememberCoroutineScope()
  val wifiState = LocalWifiState.current

  fun popBackWitEmptyResult() {
    arguments.returner.complete(null)
    Globals.navController.popBackStack()
  }

  LaunchedEffect(Unit) {
    model.routeArguments = arguments
  }

  LaunchedEffect(Unit) {
    var macFindingAlertChecked = SettingsStore.reminder.getValue { macFindingAlertChecked }.first()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !macFindingAlertChecked) {
      delay(1000)
      Globals.commonAlertDialog.showText(Globals.context.getString(R.string.s_mac_finding_alert))
      SettingsStore.reminder.setValue { macFindingAlertChecked = true }
    }
  }

  LaunchedEffect(wifiState) {
    if (wifiState == WifiState.CONNECTED) model.rescanDevices()
  }

  BackHandler { popBackWitEmptyResult() }

  StyledPullToRefreshBox(
    isRefreshing = model.scanStatus == LoadStatus.LOADING,
    state = model.pullToRefreshState,
    onRefresh = {
      if (wifiState != WifiState.CONNECTED) {
        toast(Globals.context.getString(R.string.s_request_connect_to_wifi))
        coroutine.launch { model.pullToRefreshState.animateToHidden() }
      } else {
        coroutine.launch { model.rescanDevices() } }
      }
  ) {
    Scaffold(
      modifier = Modifier
        .imePadding()
        .fillMaxSize(),
      topBar = {
        StyledTopAppBar(
          title = { TopAppBarTitle(text = stringResource(R.string.scan_devices)) },
          navigationIcon = {
            BackButton { popBackWitEmptyResult() }
          }
        )
      }
    ) {
      BoxWithConstraints(
        modifier = Modifier
          .padding(it)
          .fillMaxSize()
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ) {
          if (wifiState == WifiState.CONNECTED) {
            for (item in model.scannedDevices) {
              AnimatedAppearing(
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
              ) {
                ComposedDeviceItem(
                  deviceScanResult = item,
                  onClick = {
                    arguments.returner.complete(it)
                    Globals.navController.popBackStack()
                  }
                )
              }
            }
          } else {
            EmptyContent(
              height = this@BoxWithConstraints.maxHeight,
              message = stringResource(R.string.s_click_to_open_wifi_settings),
              icon = Icons.Rounded.SignalWifiStatusbarConnectedNoInternet4,
              onClick = { NetworkUtil.openWifiSettings() }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ComposedDeviceItem(
  deviceScanResult: ScanResult,
  onClick: (ScanResultEx) -> Unit
) {
  var macAddress by remember { mutableStateOf(deviceScanResult.mac) }
  var hostApproachStatus by remember { mutableStateOf(LoadStatus.INITIAL) }
  var hostAppRunning by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    if (LoadStatus.isAllLoaded(hostApproachStatus)) return@LaunchedEffect
    hostApproachStatus = LoadStatus.LOADING
    val result = tryApproachingHostServer(deviceScanResult.ip)
    macAddress = macAddress ?: result?.first?.macAddress
    hostAppRunning = result != null
    hostApproachStatus = LoadStatus.DONE
  }

  Column(
    modifier = Modifier
      .padding(horizontal = 20.dp)
      .padding(bottom = 10.dp)
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .background(MaterialTheme.colorScheme.primaryContainer)
      .clickable { onClick(ScanResultEx(
        scanResult = deviceScanResult.copy(mac = macAddress),
        supportedServices = if (hostAppRunning) listOf(SupportedService.SPARKER) else emptyList()
      )) }
      .padding(15.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "IP: " + deviceScanResult.ip,
      )
      Text(
        text = deviceScanResult.delay.toString() + "ms",
      )
    }

    Row(
      modifier = Modifier
        .fillMaxSize(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (LoadStatus.isLoading(hostApproachStatus)) {
          "MAC: " + stringResource(R.string.finding)
        } else {
          "MAC: ${macAddress ?: stringResource(R.string.not_found)}"
        },
      )

      Row {
        if (hostAppRunning) {
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              modifier = Modifier.
                size(15.dp),
              imageVector = Icons.Rounded.Bolt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary
            )
          }
        }
      }
    }
  }
}

enum class SupportedService {
  SPARKER, RDP,
}

data class ScanResultEx(
  val scanResult: ScanResult,
  val supportedServices: List<SupportedService>
)