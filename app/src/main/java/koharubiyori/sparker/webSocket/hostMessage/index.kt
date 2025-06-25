package koharubiyori.sparker.webSocket.hostMessage

import com.google.protobuf.GeneratedMessageLite
import koharubiyori.sparker.webSocket.WebSocketConnection
import sparker.server.BroadcastMessage
import sparker.server.PackagedEnvelopeOuterClass.PackagedEnvelope
import java.nio.ByteBuffer

fun handleRawHostMessage(connection: WebSocketConnection, rawMessage: ByteBuffer) {
  val packagedEnvelope = PackagedEnvelope.parseFrom(rawMessage)
  when (packagedEnvelope.contentCase) {
    PackagedEnvelope.ContentCase.CONTENT_NOT_SET -> throw IllegalArgumentException("Empty PackagedEnvelope")
    PackagedEnvelope.ContentCase.FUNCTIONS -> handleBroadcastMessage(packagedEnvelope.functions)
  }
}
