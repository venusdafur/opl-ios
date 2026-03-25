package cc.webbiii.app.openplural

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import cc.webbiii.app.openplural.helper.FULL_SYNC_INTERVAL
import cc.webbiii.app.openplural.helper.LocalStorage
import cc.webbiii.app.openplural.helper.TokenStorageService
import cc.webbiii.app.openplural.helper.local.syncFully
import cc.webbiii.app.openplural.helper.web.checkAppUpdate
import cc.webbiii.app.openplural.screens.helper.FullPageLoadingIndicator
import cc.webbiii.app.openplural.ui.theme.OpenPluralTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class LoadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenPluralTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LoadingScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier) {
    val status = remember { mutableIntStateOf(R.string.starting_app) }

    FullPageLoadingIndicator(modifier) {
        Text(stringResource(status.intValue))
    }

    val ctx = LocalActivity.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)

    val updatePrompt = stringResource(R.string.update_notice) + "\n" + stringResource(R.string.update_versions)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                status.intValue = R.string.checking_login
                val tokenStorageService = TokenStorageService(ctx!!)
                val token = tokenStorageService.getToken()
                if (token == null || !tokenStorageService.isTokenValid()) {
                    ctx.startActivity(Intent(ctx, LoginActivity::class.java))
                    return@LifecycleEventObserver
                }

                status.intValue = R.string.fetching_data

                val localStorage: LocalStorage
                try {
                    localStorage = LocalStorage(ctx)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
                    return@LifecycleEventObserver
                }
                scope.launch(Dispatchers.IO) {
                    try {
                        val time = System.currentTimeMillis()
                        if (time - localStorage.sync.lastFullSync >= FULL_SYNC_INTERVAL) {
                            localStorage.syncFully()

                            val version = localStorage.webService.checkAppUpdate()
                            if (version != null) {
                                val info = appVersion(ctx)
                                if (info != null && info.versionName != version) {
                                    ctx.runOnUiThread {
                                        Toast.makeText(
                                            ctx,
                                            String.format(updatePrompt, info.versionName, version),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tokenStorageService.clearToken()
                        ctx.startActivity(Intent(ctx, LoginActivity::class.java))
                        return@launch
                    }

                    ctx.startActivity(Intent(ctx, MainActivity::class.java))
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}