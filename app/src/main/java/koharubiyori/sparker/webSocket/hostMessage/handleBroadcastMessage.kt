package koharubiyori.sparker.webSocket.hostMessage

import sparker.server.BroadcastMessage

fun handleBroadcastMessage(broadcastMessage: BroadcastMessage.BroadcastMessageEnvelope) = when (broadcastMessage.contentCase) {
  BroadcastMessage.BroadcastMessageEnvelope.ContentCase.CONTENT_NOT_SET -> throw IllegalArgumentException("Empty BroadcastMessageEnvelope")
  BroadcastMessage.BroadcastMessageEnvelope.ContentCase.REQUESTTOUNLOCKMESSAGE -> {
    broadcastMessage.requestToUnlockMessage
  }
}


