package cc.webbiii.app.openplural.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.brighter
import cc.webbiii.app.openplural.screens.helper.NavControl
import cc.webbiii.app.openplural.screens.helper.navigate

private val DASHBOARD_ROUTES = mapOf(
    "members" to (R.string.members to R.drawable.baseline_members_32),
    "front_history" to (R.string.front_history to R.drawable.baseline_front_history_32),
    "analytics" to (R.string.analytics to R.drawable.baseline_analytics_32),
    "chat" to (R.string.chat to R.drawable.baseline_chat_32),
    "polls" to (R.string.polls to R.drawable.baseline_polls_32),
    "friends" to (R.string.friends to R.drawable.baseline_friends_32),
    "useful_links" to (R.string.useful_links to R.drawable.baseline_useful_links_32),
    "app_reminders" to (R.string.app_reminders to R.drawable.baseline_app_reminders_32),
    "privacy_buckets" to (R.string.privacy_buckets to R.drawable.baseline_privacy_buckets_32),
    "tokens" to (R.string.tokens to R.drawable.baseline_tokens_32),
    "user_report" to (R.string.user_report to R.drawable.baseline_user_report_32),
    "notification_history" to (R.string.notification_history to R.drawable.baseline_notification_history_32),
    "how_to" to (R.string.how_to to R.drawable.baseline_how_to_32),
    "custom_fields" to (R.string.custom_fields to R.drawable.baseline_custom_fields_32),
    "editAccount" to (R.string.account_settings to R.drawable.baseline_account_settings_32),
    "accessibility" to (R.string.accessibility to R.drawable.baseline_accessibility_32),
    "options" to (R.string.options to R.drawable.baseline_options_32),
).toList()

@Composable
fun Dashboard(controller: NavControl, showDeveloperSection: Boolean) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize()
    ) {
        items(DASHBOARD_ROUTES, key = { it.first }) { (route, info) ->
            val text = info.first
            val icon = info.second
            NavButton(controller, route, text, icon)
        }
        if (showDeveloperSection) {
            item { NavButton(controller, "developer", R.string.developer, R.drawable.baseline_developer_32) }
        }
    }
}

@Composable
fun NavButton(controller: NavControl, route: String, @StringRes text: Int, @DrawableRes icon: Int) {
    Surface(
        onClick = {
            controller.navigate(route)
        },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.background.brighter(10),
        modifier = Modifier.fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val label = stringResource(text)
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = label,
                textAlign = TextAlign.Center
            )
        }
    }
}