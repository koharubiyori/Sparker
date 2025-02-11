package koharubiyori.sparker.initialization

import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.store.DeviceConnectionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val coroutine = CoroutineScope(Dispatchers.Main)

fun onComposeCreated() {
  coroutine.launch {
    val deviceConfigs = DeviceConfigStore.deviceConfigs.first()
    DeviceConnectionStore.resetRegisteredDevices(deviceConfigs)
  }
}