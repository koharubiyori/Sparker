package koharubiyori.sparker.request

import com.google.gson.Gson
import koharubiyori.sparker.store.DeviceConnectionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.net.URI


suspend fun <E> hostRequest(
  url: String,
  baseUrl: String? = null,  // DeviceConnectingStore.hostUrl will be used when the value is null. if DeviceConnectingStore.hostUrl is null, an exception will be thrown.
  body: Any? = null,
  entity: Class<E>? = null,
  requestBuilder: Request.Builder.() -> Request.Builder = { this },
  clientBuilder: OkHttpClient.Builder.() -> OkHttpClient.Builder = { this },
): E? = withContext(Dispatchers.IO) {
  val finalBaseUrl = baseUrl ?: DeviceConnectionStore.hostUrl
  assert(finalBaseUrl != null) { "Make sure DeviceConnectingStore.activeDevice has been set." }
  val requestBody = (if (body == null) "" else Gson().toJson(body))
    .toRequestBody("application/json".toMediaType())
  val request = Request.Builder().run(requestBuilder)
    .url(URI.create(finalBaseUrl).resolve(url).toURL())
    .post(requestBody)
    .build()
  val client = commonOkHttpClient.newBuilder().run(clientBuilder).build()

  return@withContext request.send(entity = entity, client = client)
}