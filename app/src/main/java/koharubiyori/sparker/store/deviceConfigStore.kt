package koharubiyori.sparker.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import koharubiyori.sparker.DataStoreName
import koharubiyori.sparker.Globals
import koharubiyori.sparker.util.ProguardIgnore
import koharubiyori.sparker.util.debugPrint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

object DeviceConfigStore {
  private val Context.dataStore by preferencesDataStore(DataStoreName.DEVICE_CONFIG.name)
  private val dataStore get() = Globals.context.dataStore
  private val dataStoreKeys = object {
    val deviceConfig = stringPreferencesKey("deviceConfig")
  }

  val deviceConfigs get() = dataStore.data.map {
    val array = it[dataStoreKeys.deviceConfig]?.let { Gson().fromJson(it, Array<DeviceConfig>::class.java) } ?: emptyArray()
    array.toList()
  }

  private suspend fun writeConfig(value: List<DeviceConfig>) {
    dataStore.edit {
      it[dataStoreKeys.deviceConfig] = Gson().toJson(value)
    }
  }

  suspend fun addConfig(config: DeviceConfig) {
    val current = deviceConfigs.first()
    assert(current.none { it.name == config.name }) { "The device name already exists." }
    writeConfig(current + config)
  }

  suspend fun modifyConfig(deviceName: String, newConfig: DeviceConfig) {
    val deviceConfigsValue = deviceConfigs.first().toMutableList()
    val targetIndex = deviceConfigsValue.indexOfFirst { it.name == deviceName }
    assert(targetIndex != -1)
    deviceConfigsValue[targetIndex] = newConfig
    writeConfig(deviceConfigsValue)
  }

  suspend fun removeConfig(deviceName: String) {
    val currentValue = deviceConfigs.first().filter { it.name != deviceName }
    writeConfig(currentValue)
  }
}

@ProguardIgnore
data class DeviceConfig(
  val name: String,
  val hostName: String,
  val wakeOnLanPort: Int?,
  val macAddress: String?,
  val token: String?
)
