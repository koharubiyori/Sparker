package koharubiyori.sparker.request

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.IOException

val commonOkHttpClient = OkHttpClient.Builder()
  .addInterceptor(HttpLoggingInterceptor().apply {
    this.level = HttpLoggingInterceptor.Level.BASIC
  })
  .build()

class HttpException(response: Response) : IOException() {
  val code = response.code
  val responseBody = response.body?.string()

  override val message: String = response.message
    get() = "HTTP Error $code: $field\nResponse Body: $responseBody"
}

suspend fun <T> Request.send(
  entity: Class<T>? = null,
  client: OkHttpClient = commonOkHttpClient
): T? = withContext(Dispatchers.IO) {
  val res = client.newCall(this@send).execute()
  if (res.isSuccessful) {
    if (entity == null) return@withContext null
    res.body?.let {
      val bodyContent = it.string()
      if (bodyContent.isNotEmpty())
        Gson().fromJson(bodyContent, entity)
      else null
    }
  } else {
    throw HttpException(res)
  }
}