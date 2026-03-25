package cc.webbiii.app.openplural

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import cc.webbiii.app.openplural.helper.AppSettingsService
import cc.webbiii.app.openplural.helper.DEFAULT_PAGE
import cc.webbiii.app.openplural.helper.DEVELOPER_MODE
import cc.webbiii.app.openplural.screens.AccountSettingsScreen
import cc.webbiii.app.openplural.screens.Dashboard
import cc.webbiii.app.openplural.screens.DeveloperScreen
import cc.webbiii.app.openplural.screens.members.FolderEdit
import cc.webbiii.app.openplural.screens.members.MemberEdit
import cc.webbiii.app.openplural.screens.Options
import cc.webbiii.app.openplural.screens.friends.FriendsScreen
import cc.webbiii.app.openplural.screens.helper.navControl
import cc.webbiii.app.openplural.screens.members.MembersScreen
import cc.webbiii.app.openplural.ui.theme.OpenPluralTheme
import cc.webbiii.app.openplural.worker.WebSyncWorker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenPluralTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val controller = rememberNavController()
    val state = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by controller.currentBackStackEntryAsState()

    val navControl = navControl(controller)

    val ctx = LocalContext.current
    val settings = remember { AppSettingsService(ctx) }

    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                WorkManager.getInstance(ctx).enqueueUniqueWork(
                    uniqueWorkName = "web_sync",
                    existingWorkPolicy = ExistingWorkPolicy.KEEP,
                    request = OneTimeWorkRequestBuilder<WebSyncWorker>()
                        .setConstraints(Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                        .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
                        .build()
                )
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ModalNavigationDrawer(
        drawerState = state,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = false,
                    onClick = {
                        scope.launch { state.close() }
                        controller.navigate("dashboard")
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                val route by remember(navBackStackEntry) {
                    derivedStateOf {
                        navBackStackEntry?.destination?.route ?: ""
                    }
                }
                if (!route.startsWith("edit")) {
                    TopAppBar(
                        title = {
                            Text(text = "OpenPlural")
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (state.isClosed) {
                                            state.open()
                                        } else {
                                            state.close()
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = controller,
                startDestination = "dashboard",
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("dashboard") { Dashboard(navControl, settings.bool(DEVELOPER_MODE, false)) }
                composable("members?root={root}") { MembersScreen(navControl) }
                composable("editFolder/{folderId}") { FolderEdit(navControl, folderId = it.arguments!!.getString("folderId")!!.toInt()) }
                composable("editMember/{memberId}?custom={custom}") {
                    MemberEdit(
                        navControl,
                        memberId = it.arguments!!.getString("memberId")!!.toInt(),
                        custom = it.arguments!!.getString("custom", "false").toBooleanStrict(),
                        editable = true
                    )
                }
                composable("friends") { FriendsScreen(navControl, it) }
                composable("editAccount") { AccountSettingsScreen() }
                composable("options") { Options(settings) }
                composable("developer") { DeveloperScreen() }
            }
        }
    }

    LaunchedEffect(ctx) {
        val defaultPage = settings.string(DEFAULT_PAGE, "dashboard")
        if (defaultPage != "dashboard") {
            controller.navigate(defaultPage)
        }
    }
}