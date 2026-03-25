package cc.webbiii.app.openplural.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.brighter
import cc.webbiii.app.openplural.dateFromIso8601
import cc.webbiii.app.openplural.duration
import cc.webbiii.app.openplural.helper.LocalStorageViewModel
import cc.webbiii.app.openplural.helper.local.frontComment
import cc.webbiii.app.openplural.helper.local.syncDirtyResources
import cc.webbiii.app.openplural.helper.local.unfront
import cc.webbiii.app.openplural.model.FrontEntry
import cc.webbiii.app.openplural.screens.helper.NavControl
import cc.webbiii.app.openplural.screens.helper.Prompt
import cc.webbiii.app.openplural.screens.helper.navigate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun FronterScreen(navControl: NavControl, viewModel: LocalStorageViewModel) {
    val localSelfUser by viewModel.selfUser
    val frontEntries by remember {
        derivedStateOf {
            localSelfUser.front!!
        }
    }

    if (frontEntries.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
                .padding(top = 48.dp)
        ) {
            Text(stringResource(R.string.empty_front))
        }
    } else {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            items(frontEntries, key = { it.id }) { entry ->
                FrontEntry(
                    navControl = navControl,
                    entry = entry,
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
fun FrontEntry(navControl: NavControl, entry: FrontEntry, viewModel: LocalStorageViewModel) {
    val localSelfUser by viewModel.selfUser
    val member by remember(entry.member) {
        derivedStateOf {
            localSelfUser.members?.find { entry.member == it.id }
        }
    }
    var comment by remember { mutableStateOf<String?>(null) }
    var time by remember { mutableStateOf<Date?>(null) }
    var timer by remember { mutableStateOf("00:00:00") }

    //var editDate by remember { mutableStateOf(false) }
    //var editTime by remember { mutableStateOf(false) }
    var editComment by remember { mutableStateOf(false) }

    member?.let { member ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            val scope = rememberCoroutineScope()
            MemberEntry(member, fronting = true, local = true, onClick = {
                navControl.navigate("editMember/${member.id}")
            }, onFront = {
                viewModel.localStorage.unfront(entry.member)
                scope.launch(Dispatchers.IO) {
                    try {
                        viewModel.localStorage.syncDirtyResources()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background.brighter(30),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 8.dp,
                            bottomEnd = 8.dp
                        )
                    )
            ) {
                Box(
                    modifier = Modifier.padding(start = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background.brighter(10),
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 8.dp,
                                bottomEnd = 8.dp
                            )
                        )
                ) {
                    Text(
                        text = timer,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    /*
                    IconButton(
                        onClick = {
                            editDate = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_calendar_32),
                            contentDescription = stringResource(R.string.edit_date)
                        )
                    }
                    IconButton(
                        onClick = {
                            editTime = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_time_32),
                            contentDescription = stringResource(R.string.edit_time)
                        )
                    }
                     */
                    IconButton(
                        onClick = {
                            editComment = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_chat_bubble_32),
                            contentDescription = stringResource(R.string.edit_custom_status)
                        )
                    }
                }
            }
            comment?.let { Text(it) }
        }
    }

    if (editComment) {
        Prompt(
            title = stringResource(R.string.edit_custom_status),
            label = R.string.custom_status,
            prompt = R.string.custom_status_prompt,
            initialValue = comment ?: "",
            onDismiss = {
                editComment = false
            },
            onSubmit = {
                editComment = false
                comment = it.takeIf { it.isNotEmpty() }
                viewModel.localStorage.frontComment(entry.id, comment)
            }
        )
    }

    LaunchedEffect(entry.member) {
        comment = entry.comment
        time = dateFromIso8601(entry.startedAt)

        while (true) {
            delay(500)
            time?.let { timer = it.duration() }
        }
    }
}