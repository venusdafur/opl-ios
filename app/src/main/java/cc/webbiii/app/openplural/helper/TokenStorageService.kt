package cc.webbiii.app.openplural.helper

import android.content.Context
import androidx.core.content.edit

private const val AUTH_TOKEN: String = "auth_token"
private const val VALIDITY: String = "validity"
private const val TOKEN_VALIDITY_DURATION_MS: Long = 7 * 24 * 60 * 60 * 1000 // 7 days

class TokenStorageService(ctx: Context) {
    private val sharedPreferences = ctx.getSharedPreferences("openplural_web", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        sharedPreferences.edit {
            putString(AUTH_TOKEN, token)
            putLong(VALIDITY, System.currentTimeMillis() + TOKEN_VALIDITY_DURATION_MS)
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN, null)
    }

    fun revalidateToken() {
        sharedPreferences.edit { putLong(VALIDITY, System.currentTimeMillis() + TOKEN_VALIDITY_DURATION_MS) }
    }

    fun isTokenValid(): Boolean {
        val validity = sharedPreferences.getLong(VALIDITY, 0L)
        return System.currentTimeMillis() < validity
    }

    fun clearToken() {
        sharedPreferences.edit {
            remove(AUTH_TOKEN)
            remove(VALIDITY)
        }
    }
}