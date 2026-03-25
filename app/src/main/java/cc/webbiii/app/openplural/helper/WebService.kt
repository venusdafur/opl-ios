package cc.webbiii.app.openplural.helper

import android.content.Context
import cc.webbiii.app.openplural.GSON
import cc.webbiii.app.openplural.helper.web.WebException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val DEFAULT_BASE_URL = "http://127.0.0.1:11675"
val JSON = "application/json".toMediaType()

class WebService(val baseUrl: String, var authToken: String?) {
    val http = OkHttpClient()

    companion object {
        fun fromContext(ctx: Context): WebService {
            val tokenStorage = TokenStorageService(ctx)
            val token = tokenStorage.getToken()

            val settings = AppSettingsService(ctx)
            val serverUrl = settings.string(SERVER_URL, DEFAULT_BASE_URL)

            return WebService(serverUrl, token)
        }
    }

    @Throws(IOException::class, WebException::class)
    suspend inline fun <reified T> request(method: String, url: String, expectCode: Int = 200, body: Any? = null, preHandleErrors: Boolean = true): Pair<T?, Int> {
        val body = if (body != null) {
            GSON.toJson(body)
                .toRequestBody(JSON)
        } else if (method == "POST") {
            "{}".toRequestBody(JSON)
        } else {
            null
        }
        val req = Request.Builder()
            .method(method, body)
            .url(url)
            .apply {
                if (authToken != null) {
                    header("Authorization", "Bearer $authToken")
                }
            }
            .header("User-Agent", "OpenPlural Android App")
            .build()

        val result = suspendCoroutine { cont ->
            this.http.newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        val body = resp.body?.string()
                        if (resp.isSuccessful && body != null && body.isNotEmpty()) {
                            val data = if (T::class == String::class) {
                                body
                            } else {
                                GSON.fromJson(body, T::class.java)
                            }
                            cont.resume(Pair(data as T, resp.code))
                        } else {
                            cont.resume(Pair(null, resp.code))
                        }
                    }
                }
            })
        }

        if (preHandleErrors) {
            if (result.second == 401) {
                throw WebException(result.second, "Session expired or invalid. Please log in again.")
            }
            if (result.second == 500) {
                throw WebException(result.second, "Server error")
            }
            if (result.second != expectCode) {
                throw WebException(result.second, "Unexpected HTTP Response: ${result.second}")
            }
        }

        return result
    }
}