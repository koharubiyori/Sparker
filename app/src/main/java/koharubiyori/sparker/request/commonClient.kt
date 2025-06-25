package koharubiyori.sparker.request

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
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
    get() = "HTTP Error($code): $field\nResponse Body: $responseBody"
}

suspend inline fun <reified T> Request.send(
  client: OkHttpClient = commonOkHttpClient
): T? = withContext(Dispatchers.IO) {
  val res = client.newCall(this@send).execute()
  if (res.isSuccessful) {
    if (T::class == Unit::class) return@withContext null
    res.body?.let {
      val bodyContent = it.string()
      if (bodyContent.isNotEmpty()) {
        val type = object : TypeToken<T>() {}.type
        Gson().fromJson(bodyContent, type)
      } else null
    }
  } else {
    throw HttpException(res)
  }
}