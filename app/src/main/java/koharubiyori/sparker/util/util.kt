package koharubiyori.sparker.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.RoundedCorner
import androidx.compose.runtime.State
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.request.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException

@Target(AnnotationTarget.CLASS)
annotation class ProguardIgnore

private const val dividerLine = "----------------------"

fun debugPrint(vararg value: Any?, divider: Boolean = false) {
  if (divider) Timber.w("$dividerLine DEBUG PRINT START $dividerLine")
  Timber.w(value.joinToString(separator = ", ") { it.toString() })
  if (divider) Timber.w("$dividerLine DEBUG PRINT END $dividerLine")
}

suspend fun <T> toastExceptionHandlerForRequest(tryRun: suspend () -> T) {
  try {
    tryRun()
  } catch (ex: Exception) {
    when (ex) {
      is SocketTimeoutException, is ConnectException -> {
        toast(Globals.context.getString(R.string.request_timeout))
        Timber.w(ex)
      }
      is HttpException -> {
        toast(ex.message)
        Timber.e(ex)
      }
      else -> throw ex
    }
  }
}

fun vibrate() {
  val vibrator = Globals.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
  if (Build.VERSION.SDK_INT >= 26) {
    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
  } else {
    vibrator.vibrate(50)
  }
}

fun isFailedRequest(ex: Exception) = when(ex) {
  is SocketTimeoutException, is ConnectException, is HttpException -> true
  else -> false
}

fun getScreenRoundedCornerRadius(position: Int = RoundedCorner.POSITION_TOP_LEFT): Int {
  if (Build.VERSION.SDK_INT < 31) return 0
  return Globals.activity.window.decorView.rootWindowInsets.getRoundedCorner(position)?.radius ?: 0
}