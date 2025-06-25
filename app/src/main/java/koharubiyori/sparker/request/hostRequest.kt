package koharubiyori.sparker.request

import com.google.gson.Gson
import koharubiyori.sparker.Globals
import koharubiyori.sparker.R
import koharubiyori.sparker.util.ActiveDeviceScope
import koharubiyori.sparker.store.DeviceConfig
import koharubiyori.sparker.store.DeviceConfigStore
import koharubiyori.sparker.util.HostErrorCode
import koharubiyori.sparker.util.ProguardIgnore
import koharubiyori.sparker.util.debounce
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

suspend inline fun <reified T : Any?> hostRequest(
  url: String,
  baseUrl: String,
  body: Any? = null,
  crossinline requestBuilder: Request.Builder.() -> Request.Builder = { this },
  crossinline clientBuilder: OkHttpClient.Builder.() -> OkHttpClient.Builder = { this },
): T = withContext(Dispatchers.IO) {
  val requestBody = (if (body == null) "" else Gson().toJson(body))
    .toRequestBody("application/json".toMediaType())
  val request = Request.Builder().run(requestBuilder)
    .url(URI.create(baseUrl).resolve(url).toURL())
    .post(requestBody)
    .build()
  val client = commonOkHttpClient.newBuilder().run(clientBuilder).build()

  val res = if (T::class != Unit::class) {
    request.send<HostResponse<T>>(client = client)!!
  } else {
    request.send<HostResponse<Any?>>(client = client)!!
  }

  if (!HostErrorCode.isSuccess(res.code)) throw HostException(res)
  (if (T::class == Unit::class || res.data == null) Unit else res.data) as T
}

suspend inline fun <reified T : Any?> ActiveDeviceScope.hostRequest(
  url: String,
  body: Any? = null,
  crossinline requestBuilder: Request.Builder.() -> Request.Builder = { this },
  crossinline clientBuilder: OkHttpClient.Builder.() -> OkHttpClient.Builder = { this },
): T {
  val deviceConfig = deviceConfig
  return __hostRequestExceptionHandler(deviceConfig) {
    hostRequest<T>(
      url,
      hostUrl,
      body,
      {
        requestBuilder()
        if (deviceConfig.token != null) header("Authorization", deviceConfig.token)
        this
      },
      clientBuilder
    )
  }
}

val debouncedAlertDialog = debounce<String>(500) { message ->
  Globals.commonAlertDialog.showText(message)
}
suspend fun <T> __hostRequestExceptionHandler(deviceConfig: DeviceConfig, block: suspend () -> T): T {
  try {
    return block()
  } catch (ex: Exception) {
    when {
      ex is HttpException && ex.code == 401 && deviceConfig.token != null -> {
        DeviceConfigStore.modifyConfig(deviceConfig.name, deviceConfig.copy(token = null))
        debouncedAlertDialog(Globals.context.getString(R.string.s_unauthorized_message))
      }
      ex is HostException && ex.code == HostErrorCode.GRPC_CONNECT_ERROR -> {

      }
    }
    throw ex
  }
}

@ProguardIgnore
data class HostResponse<T>(
  val code: Int,
  val message: String,
  val data: T
)

class HostException(res: HostResponse<*>) : Exception() {
  val code = HostErrorCode.fromCode(res.code)
  val data = res.data

  override val message: String = res.message
    get() = "Host Error($code): $field\nResponse Body: $data"
}