package koharubiyori.sparker.screen.remote

import android.content.res.Resources
import android.graphics.Bitmap.Config
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.node.Ref
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import androidx.lifecycle.ViewModel
import koharubiyori.sparker.Globals
import koharubiyori.sparker.jni.JniFreeRDP
import koharubiyori.sparker.jni.JniFreeRDP.CursorFlags
import koharubiyori.sparker.screen.remote.component.RemoteSessionCanvasRef
import koharubiyori.sparker.util.debugPrint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val metrics = Resources.getSystem().displayMetrics

class RemoteScreenModel @Inject constructor() : ViewModel() {
  val remoteSession = RemoteSession()
  var canvasScale by mutableFloatStateOf(1f)
  var canvasTranslation by mutableStateOf(Offset.Zero)
  var computedCanvasSize by mutableStateOf(IntSize.Zero)
  var visibleVirtualMouse by mutableStateOf(false)
  var computedVirtualMouseSize by mutableStateOf(IntSize.Zero)
  var currentCursorPosition by mutableStateOf(Offset.Zero)
  var cursorNearHorizontalEdge by mutableStateOf(false)
  var cursorNearVerticalEdge by mutableStateOf(false)

  val remoteSessionCanvasRef = Ref<RemoteSessionCanvasRef>()

  override fun onCleared() {
    super.onCleared()
    remoteSession.destroy()
  }
}

class RemoteSession {
  private var inst = (-1).toLong()
  private var bitmap = createBitmap(1, 1)
  private var currentPosition = Offset.Zero // Updated when left click is called

  val frameFlow = createFrameFlow()
  var connectionStatus by mutableStateOf(ConnectionStatus.PRE_CONNECT)
    private set

  private fun createFrameFlow() = callbackFlow {
    inst = JniFreeRDP.new(Globals.context)
    val arguments = "LibFreeRDP|/unmap-buttons|/gdi:sw|/client-hostname:aFreeRDP-8f30fd6b-f074-4068-985|/v:192.168.0.225|/port:3389|/u:Test|/p:1234|/size:${metrics.widthPixels}x${metrics.heightPixels}|/bpp:16|/gfx|/gfx:AVC444|-wallpaper|-window-drag|-menu-anims|-themes|-fonts|-aero|+async-channels|+async-update|/clipboard|/audio-mode:0|/sound|/kbd:unicode:on|/cert:ignore|/log-level:INFO"
      .split("|")
      .toTypedArray()
    JniFreeRDP.parseArguments(inst, arguments)

    JniFreeRDP.addEventListener(inst, object : JniFreeRDP.EventListener() {
      override fun onPreConnect() { connectionStatus = ConnectionStatus.PRE_CONNECT }
      override fun onConnectionSuccess() { connectionStatus = ConnectionStatus.SUCCESS }
      override fun onConnectionFailure() { connectionStatus = ConnectionStatus.FAILED }
      override fun onDisconnecting() { connectionStatus = ConnectionStatus.DISCONNECTING }
      override fun onDisconnected() { connectionStatus = ConnectionStatus.DISCONNECTED }

      override fun onGraphicsUpdate(x: Int, y: Int, width: Int, height: Int) {
        JniFreeRDP.updateGraphics(inst, bitmap, x, y, width, height)
        // Note that without asImageBitmap(), it will not be sent because the reference of the bitmap object itself hasn't changed.
        trySend(bitmap.asImageBitmap())
      }

      override fun onGraphicsResize(width: Int, height: Int, bpp: Int) {
        val oldBitmap = bitmap
        bitmap = createBitmap(width, height, if (bpp > 16) Config.ARGB_8888 else Config.RGB_565)
        oldBitmap.recycle()
      }
    })

    withContext(Dispatchers.Main) {
      connectionStatus = ConnectionStatus.CONNECTING
      JniFreeRDP.connect(inst)
    }

    awaitClose {
      JniFreeRDP.removeEventListener(inst)
    }
  }

  /** eventFlags: [CursorFlags] */
  fun sendCursorEvent(position: Offset, eventFlags: Int) {
    val result = JniFreeRDP.sendCursorEvent(inst, position.x.toInt(), position.y.toInt(), eventFlags)
    if (result) return
    debugPrint(JniFreeRDP.getLastErrorString(inst))
  }

  fun sendClick(position: Offset, leftButton: Boolean = true) {
    val buttonFlag = if (leftButton) CursorFlags.LBUTTON else CursorFlags.RBUTTON
    sendCursorEvent(position, buttonFlag or CursorFlags.DOWN)
    sendCursorEvent(position, buttonFlag or CursorFlags.UP)
    if (leftButton) currentPosition = position
  }

  fun sendDoubleClick(position: Offset, leftButton: Boolean = true) {
    repeat(2) { sendClick(position, leftButton) }
  }

  // The position of current cursor is the last position of left click.
  fun rightClickOnCurrentCursorPosition() {
    sendClick(currentPosition, false)
  }

  fun sendCursorMove(position: Offset) {
    sendCursorEvent(position, CursorFlags.MOVE)
  }

  fun sendScroll(down: Boolean = true) {
    sendCursorEvent(Offset.Zero, CursorFlags.WHEEL or if (!down) CursorFlags.WHEEL_UP else CursorFlags.WHEEL_DOWN or CursorFlags.WHEEL_NEGATIVE)
  }

  fun destroy() {
    if (connectionStatus != ConnectionStatus.DISCONNECTED) JniFreeRDP.disconnect(inst)
    JniFreeRDP.free(inst)
    bitmap.recycle()
  }

  enum class ConnectionStatus {
    PRE_CONNECT,
    CONNECTING,
    SUCCESS,
    FAILED,
    DISCONNECTING,
    DISCONNECTED
  }
}