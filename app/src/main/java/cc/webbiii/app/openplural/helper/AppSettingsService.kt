package cc.webbiii.app.openplural.helper

import android.content.Context
import androidx.core.content.edit

const val SERVER_URL = "server_url"
const val DEVELOPER_MODE = "developer_mode"
const val DEFAULT_PAGE = "default_page"
const val COLOR_WHEEL = "color_wheel"

class AppSettingsService(ctx: Context) {
    private val sharedPreferences = ctx.getSharedPreferences("openplural_app", Context.MODE_PRIVATE)

    fun initializeDefaultSettings() {
        if (!sharedPreferences.contains(SERVER_URL)) {
            sharedPreferences.edit { putString(SERVER_URL, DEFAULT_BASE_URL) }
        }
        if (!sharedPreferences.contains(DEVELOPER_MODE)) {
            sharedPreferences.edit { putBoolean(DEVELOPER_MODE, false) }
        }
        if (!sharedPreferences.contains(DEFAULT_PAGE)) {
            sharedPreferences.edit { putString(DEFAULT_PAGE, "dashboard") }
        }
        if (!sharedPreferences.contains(COLOR_WHEEL)) {
            sharedPreferences.edit { putBoolean(COLOR_WHEEL, false) }
        }
    }

    fun bool(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun string(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun setBool(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }

    fun setString(key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
    }
}