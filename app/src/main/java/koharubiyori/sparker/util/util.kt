package koharubiyori.sparker.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.RoundedCorner
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.request.HostException
import koharubiyori.sparker.request.HttpException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI

@Target(AnnotationTarget.CLASS)
annotation class ProguardIgnore

val globalMainCoroutineScope = CoroutineScope(Dispatchers.Main)
val globalDefaultCoroutineScope = CoroutineScope(Dispatchers.Default)

private const val dividerLine = "----------------------"

fun debugPrint(vararg value: Any?, divider: Boolean = false) {
  if (divider) Timber.w("$dividerLine DEBUG PRINT START $dividerLine")
  Timber.w(value.joinToString(separator = ", ") { it.toString() })
  if (divider) Timber.w("$dividerLine DEBUG PRINT END $dividerLine")
}

fun Exception.isFailedRequest() = when(this) {
  is SocketTimeoutException, is ConnectException, is HttpException -> true
  is HostException -> code == HostErrorCode.GRPC_CONNECT_ERROR
  else -> false
}

fun Exception.tryToastAsRequestException(): Boolean {
  when (this) {
    is SocketTimeoutException, is ConnectException -> {
      toast(Globals.context.getString(R.string.request_timeout))
      Timber.w(this)
      return true
    }
    is HttpException -> {
      val message = when (this.code) {
        401 -> Globals.context.getString(R.string.s_unauthorized_message)
        else -> this.message
      }
      toast(message)
      Timber.e(this)
      return true
    }
    is HostException -> {
      if (this.code == HostErrorCode.GRPC_CONNECT_ERROR) {
        toast(this.message)
        Timber.e(this)
      }
      return true
    }
    else -> return false
  }
}

enum class VibrationType {
  Click,
  LongPress,
  Success,
  Error,
  Custom
}

fun vibrate(type: VibrationType = VibrationType.Click) {
  val vibrator = Globals.context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
  if (!vibrator.hasVibrator()) return

  val effect = when (type) {
    VibrationType.Click ->
      VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)

    VibrationType.LongPress ->
      VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)

    VibrationType.Success ->
      VibrationEffect.createWaveform(longArrayOf(0, 40, 50, 40), -1)

    VibrationType.Error ->
      VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 30, 50, 60), -1)

    VibrationType.Custom ->
      VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
  }

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    vibrator.vibrate(effect)
  } else {
    @Suppress("DEPRECATION")
    vibrator.vibrate(50)
  }
}

fun getScreenRoundedCornerRadius(position: Int = RoundedCorner.POSITION_TOP_LEFT): Int {
  if (Build.VERSION.SDK_INT < 31) return 0
  return Globals.activity.window.decorView.rootWindowInsets.getRoundedCorner(position)?.radius ?: 0
}

fun <T> debounce(
  waitMs: Long = 300L,
  coroutineScope: CoroutineScope = globalMainCoroutineScope,
  action: (T) -> Unit
): (T) -> Unit {
  var job: Job? = null
  return { param: T ->
    job?.cancel()
    job = coroutineScope.launch {
      delay(waitMs)
      action(param)
    }
  }
}

fun URI.toWebSocketURI(): URI {
  val newScheme = when (scheme) {
    "http" -> "ws"
    "https" -> "wss"
    else -> throw IllegalArgumentException("Unsupported scheme: $scheme")
  }

  return URI(newScheme, userInfo, host, port, path, query, fragment)
}