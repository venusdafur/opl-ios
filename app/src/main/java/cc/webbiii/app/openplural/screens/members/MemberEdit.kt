package cc.webbiii.app.openplural.screens.members

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import cc.webbiii.app.openplural.brighter
import cc.webbiii.app.openplural.helper.LocalStorageViewModel
import cc.webbiii.app.openplural.helper.local.deleteMember
import cc.webbiii.app.openplural.helper.local.editMember
import cc.webbiii.app.openplural.helper.local.getMember
import cc.webbiii.app.openplural.model.Folder
import cc.webbiii.app.openplural.model.MemberId
import cc.webbiii.app.openplural.opaque
import cc.webbiii.app.openplural.screens.helper.ColorPicker
import cc.webbiii.app.openplural.screens.helper.FullPageLoadingIndicator
import cc.webbiii.app.openplural.screens.helper.GroupSelectionPrompt
import cc.webbiii.app.openplural.screens.helper.NavControl
import cc.webbiii.app.openplural.screens.helper.Prompt
import cc.webbiii.app.openplural.screens.helper.WebImage
import cc.webbiii.app.openplural.screens.helper.back
import cc.webbiii.app.openplural.underline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MemberEdit(navControl: NavControl, memberId: MemberId, custom: Boolean, editable: Boolean, viewModel: LocalStorageViewModel = viewModel()) {    val page = rememberPagerState(pageCount = {
        if (custom) {
            if (editable) {
                3
            } else {
                2
            }
        } else if (editable) {
            6
        } else {
            2
        }
    }, initialPage = 0)

    var saveListeners by remember { mutableStateOf<Map<String, suspend () -> Unit>>(emptyMap()) }

    Column {
        Row(
            horizontalArrangement = if (editable) Arrangement.SpaceBetween else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val scope = rememberCoroutineScope()
            val ctx = LocalActivity.current
            if (editable) {
                IconButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            for (listener in saveListeners.values) {
                                listener()
                            }
                            ctx!!.runOnUiThread {
                                navControl.back()
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_save_32),
                        contentDescription = stringResource(R.string.save)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (editable) {
                    IconButton(
                        onClick = {
                            viewModel.localStorage.deleteMember(memberId)
                            navControl.back()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_delete_32),
                            contentDescription = stringResource(R.string.delete),
                            tint = Color.Red
                        )
                    }
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
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.brighter(30))
        ) {
            PageButton(R.drawable.baseline_notes_32, R.string.member_profile, 0, page)
            if (custom) {
                PageButton(R.drawable.baseline_front_history_32, R.string.member_history, 1, page)
                if (editable) {
                    PageButton(R.drawable.baseline_account_settings_32, R.string.member_options, 2, page)
                }
            } else {
                PageButton(R.drawable.baseline_note_alt_32, R.string.member_info, 1, page)
                if (editable) {
                    PageButton(R.drawable.baseline_chat_bubble_32, R.string.member_message_board, 2, page)
                    PageButton(R.drawable.baseline_front_history_32, R.string.member_history, 3, page)
                    PageButton(R.drawable.baseline_note_32, R.string.member_notes, 4, page)
                    PageButton(R.drawable.baseline_account_settings_32, R.string.member_options, 5, page)
                }
            }
        }

        HorizontalPager(
            state = page,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(Modifier
                        .background(
                            color = MaterialTheme.colorScheme.background.brighter(30),
                            shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                        )
                    ) {
                        Text(
                            text = stringResource(
                                when (page) {
                                    0 -> R.string.member_profile
                                    1 -> {
                                        if (custom) {
                                            R.string.member_history
                                        } else {
                                            R.string.member_info
                                        }
                                    }
                                    2 -> {
                                        if (custom) {
                                            R.string.member_options
                                        } else {
                                            R.string.member_message_board
                                        }
                                    }
                                    3 -> R.string.member_history
                                    4 -> R.string.member_notes
                                    5 -> R.string.member_options
                                    else -> R.string.empty
                                }
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }

                when (page) {
                    0 -> MemberProfilePage(
                        navControl = navControl,
                        viewModel = viewModel,
                        memberId = memberId,
                        custom = custom,
                        editable = editable,
                        save = { name, listener ->
                            saveListeners += name to listener
                        }
                    )
                    /*
                    1 -> MemberInfoPage(memberId)
                    2 -> MemberMessageBoardPage(memberId)
                    3 -> MemberHistoryPage(memberId)
                    4 -> MemberNotesPage(memberId)
                    5 -> MemberOptionsPage(memberId)
                     */
                }
            }
        }
    }
}

@Composable
fun PageButton(icon: Int, name: Int, page: Int, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    val selected by remember { derivedStateOf { pagerState.currentPage == page } }

    IconButton(
        onClick = {
            scope.launch {
                pagerState.scrollToPage(page)
            }
        },
        modifier = Modifier
            .padding(vertical = 4.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background.brighter(
                    30
                ),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = stringResource(name),
            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun MemberProfilePage(navControl: NavControl, viewModel: LocalStorageViewModel, memberId: MemberId, custom: Boolean, editable: Boolean, save: (String, suspend () -> Unit) -> Unit) {
    val localSelfUser by viewModel.selfUser
    var name by remember { mutableStateOf("") }
    var pronouns by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(DEFAULT_RESOURCE_COLOR) }
    var allFolders by remember { mutableStateOf<List<Folder>>(emptyList()) }
    var folders by remember { mutableStateOf<List<Folder>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var editAvatar by remember { mutableStateOf(false) }
    var editGroups by remember { mutableStateOf(false) }

    var profileChanged by remember { mutableStateOf(false) }
    var groupsChanged by remember { mutableStateOf(false) }

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
                    .clickable(editable, onClick = {
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
                singleLine = true,
                readOnly = !editable
            )
            if (!custom) {
                TextField(
                    value = pronouns,
                    onValueChange = {
                        pronouns = it
                        profileChanged = true
                    },
                    label = { Text(stringResource(R.string.pronouns)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    singleLine = true,
                    readOnly = !editable
                )
            }
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
                singleLine = false,
                readOnly = !editable
            )
            ColorPicker(
                color = color,
                onChange = {
                    color = it
                    profileChanged = true
                },
                clickable = editable
            )
            if (!custom) {
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                        .clickable(editable, onClick = {
                            editGroups = true
                        })
                ) {
                    if (editable) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_edit_32),
                            contentDescription = stringResource(R.string.edit)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.groups))
                }
                HorizontalDivider(Modifier.padding(bottom = 12.dp))
                if (folders.isEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.no_groups))
                    }
                } else {
                    LazyRow {
                        items(
                            items = folders,
                            key = {
                                it.id
                            }
                        ) {
                            var displayName = it.name
                            if (it.emoji != null) {
                                displayName = it.emoji + " " + displayName
                            }
                            Text(
                                text = displayName,
                                modifier = Modifier.underline(2.dp, opaque(it.color))
                            )
                        }
                    }
                }
            }
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

    if (editGroups) {
        GroupSelectionPrompt(
            title = stringResource(R.string.edit_groups, name),
            folders = allFolders,
            initialSelection = folders.map { it.id },
            onDismiss = {
                editGroups = false
            },
            onSubmit = { selection ->
                editGroups = false
                folders = allFolders.filter {
                    selection.contains(it.id)
                }
                groupsChanged = true
            }
        )
    }

    LaunchedEffect(memberId, localSelfUser.folders, localSelfUser.members) {
        val member = viewModel.localStorage.getMember(memberId)
        if (member == null) {
            navControl.back()
            return@LaunchedEffect
        }
        name = member.name
        pronouns = member.pronouns ?: ""
        description = member.description ?: ""
        avatarUrl = member.avatar ?: ""
        color = member.color

        allFolders = localSelfUser.folders!!.toList()
        folders = allFolders.filter {
            member.folders.contains(it.id)
        }

        loading = false

        save("member_profile") {
            if (profileChanged || groupsChanged) {
                val member = member.copy(
                    name = name,
                    pronouns = pronouns.takeIf { it.isNotEmpty() },
                    avatar = avatarUrl.takeIf { it.isNotEmpty() },
                    description = description.takeIf { it.isNotEmpty() },
                    color = color,
                    folders = folders.map { it.id }
                )
                viewModel.localStorage.editMember(member)
            }
        }
    }
}