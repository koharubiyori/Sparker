package koharubiyori.sparker.initialization

import android.app.Application
import koharubiyori.sparker.Globals
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.store.DeviceConnectionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


fun Application.initializeOnCreate() = coroutine.launch {
  Globals.context = applicationContext
}

private val coroutine = CoroutineScope(Dispatchers.Default)