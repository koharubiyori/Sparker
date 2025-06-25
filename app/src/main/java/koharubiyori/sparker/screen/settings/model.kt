package koharubiyori.sparker.screen.settings

import androidx.lifecycle.ViewModel
import javax.inject.Inject

class SettingsScreenModel @Inject() constructor() : ViewModel()

enum class PowerOffAction {
  SHUTDOWN, SLEEP, HIBERNATE, REBOOT
}

enum class ClickAction {
  TOGGLE, POWER_ON, POWER_OFF
}