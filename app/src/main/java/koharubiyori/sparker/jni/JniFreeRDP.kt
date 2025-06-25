package koharubiyori.sparker.jni

import android.content.Context
import android.graphics.Bitmap
import timber.log.Timber
import java.util.Objects
import java.util.regex.Pattern

object JniFreeRDP {
  private val eventListeners = emptyMap<Long, EventListener>().toMutableMap()

  init {
    try {
      System.loadLibrary("freerdp-android")

      /* Load dependent libraries too to trigger JNI_OnLoad calls */
      val version: String = getVersion()!!
      val versions = version.split("[.-]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      if (versions.isNotEmpty()) {
        System.loadLibrary("freerdp-client" + versions[0])
        System.loadLibrary("freerdp" + versions[0])
        System.loadLibrary("winpr" + versions[0])
      }
      val pattern = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+).*")
      val matcher = pattern.matcher(version)
      if (!matcher.matches() || (matcher.groupCount() < 3)) throw RuntimeException(
        "APK broken: native library version " + version +
            " does not meet requirements!"
      )
      val major = Objects.requireNonNull(matcher.group(1)).toInt()
      val minor = Objects.requireNonNull(matcher.group(2)).toInt()
      val patch = Objects.requireNonNull(matcher.group(3)).toInt()

      val h264Supported =
        if (major > 2) hasH264()
        else if (minor > 5) hasH264()
        else if ((minor == 5) && (patch >= 1)) hasH264()
        else throw RuntimeException("APK broken: native library version $version does not meet requirements!")
      Timber.i("Successfully loaded native library. H264 is ${if (h264Supported) "supported" else "not available"}")
    } catch (ex: UnsatisfiedLinkError) {
      Timber.e("Failed to load library: $ex")
      throw ex
    }
  }

  fun addEventListener(instance: Long, listener: EventListener) {
    eventListeners[instance] = listener
  }

  fun removeEventListener(instance: Long) {
    eventListeners.remove(instance)
  }

  external fun hasH264(): Boolean

  external fun getJniVersion(): String?

  external fun getVersion(): String?

  external fun getBuildRevision(): String?

  external fun getBuildConfig(): String?

  external fun new(context: Context): Long

  external fun free(inst: Long)

  external fun parseArguments(inst: Long, args: Array<String>): Boolean

  external fun connect(inst: Long): Boolean

  external fun disconnect(inst: Long): Boolean

  external fun updateGraphics(
    inst: Long, bitmap: Bitmap, x: Int, y: Int,
    width: Int, height: Int
  ): Boolean

  external fun sendCursorEvent(inst: Long, x: Int, y: Int, flags: Int): Boolean

  external fun sendKeyEvent(inst: Long, keycode: Int, down: Boolean): Boolean

  external fun sendUnicodekeyEvent(
    inst: Long, keycode: Int,
    down: Boolean
  ): Boolean

  external fun sendClipboardData(inst: Long, data: String): Boolean

  external fun getLastErrorString(inst: Long): String?

  @JvmStatic
  private fun onConnectionSuccess(inst: Long) {
    eventListeners[inst]?.onConnectionSuccess()
  }

  @JvmStatic
  private fun onConnectionFailure(inst: Long) {
    eventListeners[inst]?.onConnectionFailure()
  }

  @JvmStatic
  private fun onPreConnect(inst: Long) {
    eventListeners[inst]?.onPreConnect()
  }

  @JvmStatic
  private fun onDisconnecting(inst: Long) {
    eventListeners[inst]?.onDisconnecting()
  }

  @JvmStatic
  private fun onDisconnected(inst: Long) {
    eventListeners[inst]?.onDisconnected()
  }

  @JvmStatic
  private fun onSettingsChanged(inst: Long, width: Int, height: Int, bpp: Int) {
    eventListeners[inst]?.onSettingsChanged(width, height, bpp)
  }

  @JvmStatic
  private fun onAuthenticate(
    inst: Long, username: StringBuilder, domain: StringBuilder,
    password: StringBuilder
  ) = eventListeners[inst]?.onAuthenticate(username, domain, password) ?: false

  @JvmStatic
  private fun onGatewayAuthenticate(
    inst: Long, username: StringBuilder,
    domain: StringBuilder, password: StringBuilder
  ) = eventListeners[inst]?.onGatewayAuthenticate(username, domain, password) ?: false

  @JvmStatic
  private fun onVerifyCertificateEx(
    inst: Long, host: String, port: Long, commonName: String,
    subject: String, issuer: String, fingerprint: String,
    flags: Long
  ) = eventListeners[inst]?.onVerifyCertificateEx(
    host,
    port,
    commonName,
    subject,
    issuer,
    fingerprint,
    flags
  ) ?: false

  @JvmStatic
  private fun onVerifyChangedCertificateEx(
    inst: Long, host: String, port: Long,
    commonName: String, subject: String,
    issuer: String, fingerprint: String,
    oldSubject: String, oldIssuer: String,
    oldFingerprint: String, flags: Long
  ) = eventListeners[inst]?.onVerifyChangedCertificateEx(
    host,
    port,
    commonName,
    subject,
    issuer,
    fingerprint,
    oldSubject,
    oldIssuer,
    oldFingerprint,
    flags
  ) ?: 0

  @JvmStatic
  private fun onGraphicsUpdate(inst: Long, x: Int, y: Int, width: Int, height: Int) {
    eventListeners[inst]?.onGraphicsUpdate(x, y, width, height)
  }

  @JvmStatic
  private fun onGraphicsResize(inst: Long, width: Int, height: Int, bpp: Int) {
    eventListeners[inst]?.onGraphicsResize(width, height, bpp)
  }

  @JvmStatic
  private fun onRemoteClipboardChanged(inst: Long, data: String) {
    eventListeners[inst]?.onRemoteClipboardChanged(data)
  }

  abstract class EventListener {
    open fun onPreConnect() {}
    open fun onConnectionSuccess() {}
    open fun onConnectionFailure() {}
    open fun onDisconnecting() {}
    open fun onDisconnected() {}

    open fun onSettingsChanged(width: Int, height: Int, bpp: Int) {}

    open fun onAuthenticate(
      username: StringBuilder?, domain: StringBuilder?,
      password: StringBuilder?
    ): Boolean = false

    open fun onGatewayAuthenticate(
      username: StringBuilder?, domain: StringBuilder?,
      password: StringBuilder?
    ): Boolean = false

    open fun onVerifyCertificateEx(
      host: String?, port: Long, commonName: String?, subject: String?, issuer: String?,
      fingerprint: String?, flags: Long
    ): Int = 0

    open fun onVerifyChangedCertificateEx(
      host: String?, port: Long, commonName: String?, subject: String?, issuer: String?,
      fingerprint: String?, oldSubject: String?, oldIssuer: String?,
      oldFingerprint: String?, flags: Long
    ): Int = 0

    open fun onGraphicsUpdate(x: Int, y: Int, width: Int, height: Int) {}
    open fun onGraphicsResize(width: Int, height: Int, bpp: Int) {}
    open fun onRemoteClipboardChanged(data: String?) {}
  }

  class CursorFlags {
    companion object {
      const val LBUTTON = 0x1000
      const val RBUTTON = 0x2000
      const val DOWN = 0x8000
      const val UP = 0

      const val MOVE = 0x0800

      const val WHEEL = 0x0200
      const val WHEEL_NEGATIVE = 0x0100
      const val WHEEL_DOWN = 0x0088
      const val WHEEL_UP = 0x0078
    }
  }
}