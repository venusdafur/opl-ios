package cc.webbiii.app.openplural.screens.members

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cc.webbiii.app.openplural.DEFAULT_RESOURCE_COLOR
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.helper.LocalStorageViewModel
import cc.webbiii.app.openplural.helper.local.deleteFolder
import cc.webbiii.app.openplural.helper.local.editFolder
import cc.webbiii.app.openplural.helper.local.getFolder
import cc.webbiii.app.openplural.model.FolderId
import cc.webbiii.app.openplural.screens.helper.ColorPicker
import cc.webbiii.app.openplural.screens.helper.FullPageLoadingIndicator
import cc.webbiii.app.openplural.screens.helper.NavControl
import cc.webbiii.app.openplural.screens.helper.back

@Composable
fun FolderEdit(navControl: NavControl, folderId: FolderId, viewModel: LocalStorageViewModel = viewModel()) {
    var saveListener by remember { mutableStateOf({}) }

    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    saveListener()
                    navControl.back()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_save_32),
                    contentDescription = stringResource(R.string.save)
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        viewModel.localStorage.deleteFolder(folderId)
                        navControl.back()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_delete_32),
                        contentDescription = stringResource(R.string.delete),
                        tint = Color.Red
                    )
                }
                IconButton(
                    onClick = {
                        navControl.back()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_cancel_32),
                        contentDescription = stringResource(R.string.cancel)
                    )
                }
            }
        }

        FolderEditPage(
            navControl = navControl,
            viewModel = viewModel,
            folderId = folderId,
            save = {
                saveListener = it
            }
        )
    }
}

@Composable
private fun FolderEditPage(navControl: NavControl, viewModel: LocalStorageViewModel, folderId: FolderId, save: (() -> Unit) -> Unit) {
    val localSelfUser by viewModel.selfUser
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(DEFAULT_RESOURCE_COLOR) }
    var memberCount by remember { mutableIntStateOf(-1) }
    var archivedMemberCount by remember { mutableIntStateOf(-1) }
    var loading by remember { mutableStateOf(true) }

    var changed by remember { mutableStateOf(false) }

    if (loading) {
        FullPageLoadingIndicator()
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextField(
                value = name,
                onValueChange = {
                    name = it
                    changed = true
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
                    changed = true
                },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = false
            )
            TextField(
                value = emoji,
                onValueChange = {
                    if (it.length <= 3) {
                        emoji = it
                        changed = true
                    }
                },
                label = { Text(stringResource(R.string.emoji)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true
            )
            ColorPicker(
                color = color,
                onChange = {
                    color = it
                    changed = true
                }
            )
            if (memberCount != -1 && archivedMemberCount != -1) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Text(stringResource(R.string.folder_member_count_title))
                Text(stringResource(R.string.folder_member_count, memberCount, archivedMemberCount))
            }
        }
    }

    LaunchedEffect(folderId, localSelfUser.folders, localSelfUser.members) {
        val folder = viewModel.localStorage.getFolder(folderId)
        if (folder == null) {
            navControl.back()
            return@LaunchedEffect
        }
        name = folder.name
        description = folder.description ?: ""
        emoji = folder.emoji ?: ""
        color = folder.color
        memberCount = localSelfUser.members!!.count {
            it.folders.contains(folderId)
        }
        archivedMemberCount = 0

        loading = false

        save {
            if (changed) {
                val folder = folder.copy(
                    name = name,
                    description = description.takeIf { it.isNotEmpty() },
                    emoji = emoji.takeIf { it.isNotEmpty() },
                    color = color
                )
                viewModel.localStorage.editFolder(folder)
            }
        }
    }
}