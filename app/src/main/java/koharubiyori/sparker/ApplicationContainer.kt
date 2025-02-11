package koharubiyori.sparker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import koharubiyori.sparker.initialization.initializeOnCreate


@HiltAndroidApp
class ApplicationContainer : Application() {
  override fun onCreate() {
    super.onCreate()
    initializeOnCreate()
  }
}
