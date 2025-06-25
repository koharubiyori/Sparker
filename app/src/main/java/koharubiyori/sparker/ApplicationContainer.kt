package koharubiyori.sparker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import koharubiyori.sparker.initialization.initializeOnCreate
import timber.log.Timber


@HiltAndroidApp
class ApplicationContainer : Application() {
  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
    initializeOnCreate()
  }
}
