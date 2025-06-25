package koharubiyori.sparker.initialization

import koharubiyori.sparker.util.DeviceStateCenter
import koharubiyori.sparker.util.globalMainCoroutineScope
import kotlinx.coroutines.launch


fun onComposeCreated() {
  globalMainCoroutineScope.launch {
    DeviceStateCenter.registerAllDevices()
  }
}