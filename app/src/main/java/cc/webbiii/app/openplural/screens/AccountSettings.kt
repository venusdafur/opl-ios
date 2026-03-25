package cc.webbiii.app.openplural.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.webbiii.app.openplural.DEFAULT_RESOURCE_COLOR
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.helper.LocalStorageViewModel
import cc.webbiii.app.openplural.screens.helper.ColorPicker
import cc.webbiii.app.openplural.screens.helper.FullPageLoadingIndicator
import cc.webbiii.app.openplural.screens.helper.Prompt
import cc.webbiii.app.openplural.screens.helper.WebImage

@Composable
fun AccountSettingsScreen(viewModel: LocalStorageViewModel = viewModel()) {
    val localSelfUser by viewModel.selfUser
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(DEFAULT_RESOURCE_COLOR) }
    var friendCode by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    var editAvatar by remember { mutableStateOf(false) }
    var profileChanged by remember { mutableStateOf(false) }

    if (loading) {
        FullPageLoadingIndicator()
    } else {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            WebImage(
                avatarUrl,
                R.drawable.baseline_person_32,
                modifier = Modifier
                    .padding(32.dp)
                    .clickable(true, onClick = {
                        editAvatar = true
                    })
            )

            TextField(
                value = name,
                onValueChange = {
                    name = it
                    profileChanged = true
                },
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true
            )
            TextField(
                value = description,
                onValueChange = {
                    description = it
                    profileChanged = true
                },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = false
            )
            ColorPicker(
                color = color,
                onChange = {
                    color = it
                    profileChanged = true
                }
            )

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            TextField(
                value = friendCode,
                onValueChange = {},
                label = { Text(stringResource(R.string.friend_code)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true,
                readOnly = true
            )
        }
    }

    if (editAvatar) {
        Prompt(
            title = stringResource(R.string.avatar),
            label = R.string.avatar_url,
            prompt = R.string.url_prompt,
            initialValue = avatarUrl,
            onDismiss = { editAvatar = false },
            onSubmit = { newUrl ->
                editAvatar = false
                avatarUrl = newUrl
                profileChanged = true
            }
        )
    }


    LaunchedEffect(localSelfUser) {
        name = localSelfUser.name
        description = localSelfUser.description ?: ""
        avatarUrl = localSelfUser.avatar ?: ""
        color = localSelfUser.color
        friendCode = localSelfUser.friendCode ?: ""

        loading = false
    }
}