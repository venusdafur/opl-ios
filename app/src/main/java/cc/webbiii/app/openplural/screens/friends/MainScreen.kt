package cc.webbiii.app.openplural.screens.friends

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavBackStackEntry
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.helper.WebService
import cc.webbiii.app.openplural.helper.local.frontComment
import cc.webbiii.app.openplural.helper.web.WebException
import cc.webbiii.app.openplural.helper.web.getFriends
import cc.webbiii.app.openplural.helper.web.sendFriendRequest
import cc.webbiii.app.openplural.model.UserResponse
import cc.webbiii.app.openplural.screens.helper.FullPageLoadingIndicator
import cc.webbiii.app.openplural.screens.helper.ListEntry
import cc.webbiii.app.openplural.screens.helper.NavControl
import cc.webbiii.app.openplural.screens.helper.Prompt
import cc.webbiii.app.openplural.screens.helper.WebImage
import cc.webbiii.app.openplural.screens.helper.navigate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen(navControl: NavControl, navEntry: NavBackStackEntry) {
    val scope = rememberCoroutineScope()
    val ctx = LocalActivity.current
    var friends by remember { mutableStateOf<Array<UserResponse>?>(null) }

    var addFriend by remember { mutableStateOf(false) }

    if (friends == null) {
        FullPageLoadingIndicator()
    } else if (friends?.isEmpty() == true) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(stringResource(R.string.empty_friends))
            Button(
                onClick = {
                    addFriend = true
                }
            ) {
                Text(stringResource(R.string.add_friend))
            }
        }
    } else {
        LazyColumn {
            items(friends as Array<UserResponse>) { friend ->
                ListEntry(friend.color, onClick = {
                    navControl.navigate("friend/${friend.id}")
                }) {
                    if (friend.avatar != null) {
                        WebImage(friend.avatar, R.drawable.baseline_person_32, modifier = Modifier.width(48.dp))
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.baseline_person_32),
                            contentDescription = friend.name
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(friend.name)
                }
            }
        }
        FloatingActionButton(
            onClick = {
                addFriend = true
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_person_add_32),
                contentDescription = stringResource(R.string.add_friend),
            )
        }
    }

    if (addFriend) {
        Prompt(
            title = stringResource(R.string.add_friend),
            label = R.string.friend_code,
            prompt = R.string.empty,
            explanation = R.string.friend_code_help,
            initialValue = "",
            onDismiss = {
                addFriend = false
            },
            onSubmit = {
                addFriend = false

                scope.launch(Dispatchers.IO) {
                    try {
                        val webService = WebService.fromContext(ctx!!)
                        webService.sendFriendRequest(it)
                    } catch (e: Exception) {
                        e.printStackTrace()

                        if (e is WebException) {
                            ctx?.runOnUiThread {
                                Toast.makeText(ctx, e.message ?: "An error occured", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        )
    }

    LaunchedEffect(navEntry) {
        scope.launch(Dispatchers.IO) {
            try {
                val webService = WebService.fromContext(ctx!!)
                friends = webService.getFriends()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}