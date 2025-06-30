package koharubiyori.sparker.screen.home

import StyledPullToRefreshBox
import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.component.EmptyContent
import koharubiyori.sparker.component.TopAppBarIcon
import koharubiyori.sparker.component.styled.StyledTopAppBar
import koharubiyori.sparker.component.styled.TopAppBarTitle
import koharubiyori.sparker.screen.home.component.BottomSheetForDeviceActions
import koharubiyori.sparker.screen.home.component.BottomSheetToAddDevice
import koharubiyori.sparker.screen.home.component.BottomSheetToPairDevice
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.util.DeviceStateCenter
import koharubiyori.sparker.util.LoadStatus
import koharubiyori.sparker.util.VibrationType
import koharubiyori.sparker.util.debugPrint
import koharubiyori.sparker.util.vibrate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen() {
  val model: HomeScreenModel = hiltViewModel()
  val coroutine = rememberCoroutineScope()
//  var deviceConfigs by remember { mutableStateOf(emptyList<DeviceConfig>()) }
  // When using the following code, the screen will not update when deviceConfigs is added. Why?
  val deviceConfigs by DeviceConfigStore.deviceConfigs.collectAsStateWithLifecycle(emptyList())

  debugPrint("deviceConfigs", deviceConfigs.size)
//  LaunchedEffect(true) {
//    DeviceConfigStore.deviceConfigs.collect { deviceConfigs = it }
//  }

  BackHandler(model.bottomSheetToAddDeviceState.visible) {
    model.bottomSheetToAddDeviceState.visible = false
  }

  BackHandler(model.bottomSheetForDeviceActionsState.visible) {
    model.bottomSheetForDeviceActionsState.visible = false
  }

  model.localCoroutineScopeState.Provider {
    StyledPullToRefreshBox(
      isRefreshing = LoadStatus.isLoading(model.deviceRefreshStatus),
      onRefresh = { coroutine.launch { model.refreshDevices() } },
    ) {
      Scaffold(
        modifier = Modifier
          .fillMaxSize()
          .imePadding(),
        topBar = {
          StyledTopAppBar(
            title = { TopAppBarTitle(text = stringResource(R.string.app_name)) },
            navigationIcon = {},
            actions = {
              TopAppBarIcon(
                image = Icons.Rounded.Settings,
                onClick = { Globals.navController.navigate("settings") }
              )
            }
          )
        }
      ) {
        Box(
          modifier = Modifier
            .padding(it)
            .fillMaxSize(),
        ) {
          if (deviceConfigs.isNotEmpty()) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
            ) {
              for (item in deviceConfigs) {
                ComposedDeviceItem(
                  deviceConfig = item,
                  onClick = { coroutine.launch { model.handleItemClick(item) } },
                  onLongClick = {
                    vibrate(VibrationType.LongPress)
                    coroutine.launch {
                      model.bottomSheetForDeviceActionsState.show(item)
                    }
                  }
                )
              }
            }
          } else {
            EmptyContent(
              message = stringResource(R.string.no_devices),
              icon = Icons.Rounded.Computer
            )
          }

          FloatingActionButton(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .absoluteOffset((-30).dp, (-32).dp),
            elevation = FloatingActionButtonDefaults.elevation(
              defaultElevation = 2.dp,
            ),
            onClick = {
              model.bottomSheetToAddDeviceState.show()
            }
          ) {
            Icon(Icons.Filled.Add, null)
          }
        }
      }

      BottomSheetToAddDevice(
        state = model.bottomSheetToAddDeviceState
      )
      BottomSheetForDeviceActions(
        state = model.bottomSheetForDeviceActionsState
      )
      BottomSheetToPairDevice(
        state = model.bottomSheetToPairDeviceState
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ComposedDeviceItem(
  deviceConfig: DeviceConfig,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
) {
  val deviceState = DeviceStateCenter.composableStateMap[deviceConfig.name]
  val online = deviceState?.pingOnline == true
  val time = deviceState?.pingTimeout ?: -1

  Row(
    modifier = Modifier
      .padding(horizontal = 20.dp)
      .padding(bottom = 10.dp)
      .fillMaxWidth()
      .clip(MaterialTheme.shapes.medium)
      .background(MaterialTheme.colorScheme.primaryContainer)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      )
      .padding(15.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        modifier = Modifier
          .size(50.dp),
//          .clip(CircleShape)
//          .background(MaterialTheme.colorScheme.primary),
        imageVector = if(deviceState?.serverOnline == true) Icons.Rounded.Bolt else Icons.Rounded.Computer,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary
      )

      Column(
        modifier = Modifier
          .padding(start = 15.dp)
      ) {
        Text(
          text = deviceConfig.name,
          overflow = TextOverflow.Ellipsis
        )

        if (deviceConfig.hostName != deviceConfig.name) {
          Text(
            text = deviceConfig.hostName,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }

    Text(
      text = if (online) "${time}ms" else stringResource(R.string.offline),
      color = when {
        !online -> MaterialTheme.colorScheme.onSurfaceVariant
        time < 20 -> Color(0xff28A745)
        time < 40 -> Color(0xFFDC9404)
        else -> Color(0xffDC3545)
      }
    )
  }
}