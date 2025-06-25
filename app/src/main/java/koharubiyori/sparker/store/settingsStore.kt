package koharubiyori.sparker.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import koharubiyori.sparker.DataStoreName
import koharubiyori.sparker.Globals
import koharubiyori.sparker.screen.settings.ClickAction
import koharubiyori.sparker.screen.settings.PowerOffAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// All the settings are saved here, not only the setting items in settings screen
// Each sets of setting items have to extend Settings, and all members are defined using var, then add to SettingsStore
sealed class Settings

// Items in settings screen
data class PreferenceSettings(
  var defaultPowerOffAction: PowerOffAction = PowerOffAction.SHUTDOWN,
  var clickAction: ClickAction = ClickAction.POWER_ON,
  var forceShutdown: Int = 0,
  var fastBoot: Boolean = false,
  var localDeviceTimeout: Int = 3000,
  var removeDeviceTimeout: Int = 5000,
  var dynamicPowerActions: Boolean = false,
) : Settings()

// For saving persistent variables
data class ReminderSettings(
  var macFindingAlertChecked: Boolean = false
) : Settings()

object SettingsStore {
  val preference = SettingsStoreClient(PreferenceSettings::class.java)
  val reminder = SettingsStoreClient(ReminderSettings::class.java)
}

private val Context.dataStore by preferencesDataStore(DataStoreName.SETTINGS.name)
private val dataStore get() = Globals.context.dataStore

class SettingsStoreClient<T : Settings>(
  private val entity: Class<out T>
) {
  private val preferencesKey = stringPreferencesKey(entity.simpleName)

  fun <SelectedValue> getValue(
    getter: (T.() -> SelectedValue)
  ) = dataStore.data.map {
    val jsonStr = it[preferencesKey]
    val entityInstance = if (jsonStr != null) Gson().fromJson(jsonStr, entity) else entity.getConstructor().newInstance()
    getter.invoke(entityInstance)
  }

  suspend fun setValue(setter: T.() -> Unit) {
    val jsonStr = dataStore.data.first()[preferencesKey]
    val entityInstance = if (jsonStr != null) Gson().fromJson(jsonStr, entity) else entity.getConstructor().newInstance()
    setter.invoke(entityInstance)
    dataStore.edit {
      it[preferencesKey] = Gson().toJson(entityInstance)
    }
  }

  suspend fun setValue(value: T) {
    dataStore.edit {
      it[preferencesKey] = Gson().toJson(value)
    }
  }
}

