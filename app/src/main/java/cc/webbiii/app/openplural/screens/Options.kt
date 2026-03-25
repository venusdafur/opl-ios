package cc.webbiii.app.openplural.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.screens.helper.Spinner
import cc.webbiii.app.openplural.helper.AppSettingsService
import cc.webbiii.app.openplural.helper.COLOR_WHEEL
import cc.webbiii.app.openplural.helper.DEFAULT_PAGE
import cc.webbiii.app.openplural.helper.DEVELOPER_MODE

@Composable
fun Options(settings: AppSettingsService) {
    val pages = listOf(
        ("dashboard" to R.string.dashboard),
        ("members" to R.string.members),
        ("front_history" to R.string.front_history),
        ("analytics" to R.string.analytics),
        ("chat" to R.string.chat),
        ("polls" to R.string.polls),
        ("friends" to R.string.friends),
        ("useful_links" to R.string.useful_links),
        ("app_reminders" to R.string.app_reminders),
        ("privacy_buckets" to R.string.privacy_buckets),
        ("tokens" to R.string.tokens),
        ("user_report" to R.string.user_report),
        ("notification_history" to R.string.notification_history),
        ("how_to" to R.string.how_to),
        ("custom_fields" to R.string.custom_fields),
        ("account_settings" to R.string.account_settings),
        ("accessibility" to R.string.accessibility),
        ("options" to R.string.options)
    )

    var selectedPage by remember {
        val defaultPage = settings.string(DEFAULT_PAGE, "dashboard")
        mutableStateOf(pages.find { it.first == defaultPage } ?: ("dashboard" to R.string.dashboard))
    }
    var colorWheel by remember { mutableStateOf(settings.bool(COLOR_WHEEL, false)) }
    var developerMode by remember { mutableStateOf(settings.bool(DEVELOPER_MODE, false)) }

    Column {
        Setting(R.string.default_page) {
            Spinner(
                items = pages,
                selected = selectedPage,
                displayMapper = { pair ->
                    stringResource(pair.second)
                },
                onSelected = {
                    selectedPage = it
                    settings.setString(DEFAULT_PAGE, it.first)
                }
            )
        }
        Setting(R.string.color_wheel) {
            Switch(colorWheel, onCheckedChange = {
                colorWheel = it
                settings.setBool(COLOR_WHEEL, it)
            })
        }
        Setting(R.string.developer_mode) {
            Switch(developerMode, onCheckedChange = {
                developerMode = it
                settings.setBool(DEVELOPER_MODE, it)
            })
        }
    }
}

@Composable
private fun Setting(label: Int, content: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(stringResource(label))
        content()
    }
}