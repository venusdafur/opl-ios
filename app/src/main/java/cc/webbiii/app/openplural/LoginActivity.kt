package cc.webbiii.app.openplural

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.webbiii.app.openplural.helper.AppSettingsService
import cc.webbiii.app.openplural.helper.DEFAULT_BASE_URL
import cc.webbiii.app.openplural.helper.LocalStorage
import cc.webbiii.app.openplural.helper.SERVER_URL
import cc.webbiii.app.openplural.helper.TokenStorageService
import cc.webbiii.app.openplural.helper.WebService
import cc.webbiii.app.openplural.helper.web.login
import cc.webbiii.app.openplural.helper.web.register
import cc.webbiii.app.openplural.ui.theme.OpenPluralTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenPluralTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LoginScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier) {
    var registering by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var system by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf(DEFAULT_BASE_URL) }
    var developerClicks by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val ctx = LocalActivity.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.login_prompt),
            modifier = Modifier.clickable(true, onClick = { developerClicks++ })
        )

        Spacer(Modifier.height(32.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(R.string.username)) },
            placeholder = { Text(stringResource(R.string.username)) }
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            placeholder = { Text(stringResource(R.string.password)) }
        )

        if (registering) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = system,
                    onCheckedChange = { system = it },
                )
            }
        }

        if (developerClicks >= 10) {
            TextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text(stringResource(R.string.server_url)) },
                placeholder = { Text(stringResource(R.string.server_url)) }
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val webService = WebService(serverUrl, null)
                scope.launch(Dispatchers.IO) {
                    try {
                        if (registering) {
                            webService.register(username, password, system)
                        }
                        val resp = webService.login(getDeviceName(), username, password)

                        val settings = AppSettingsService(ctx!!)
                        settings.string(SERVER_URL, serverUrl)

                        TokenStorageService(ctx.applicationContext).saveToken(resp.session!!.token)

                        LocalStorage(ctx.applicationContext, resp).save()

                        ctx.finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ctx?.runOnUiThread {
                            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        ) {
            Text(stringResource(if (registering) R.string.register else R.string.login))
        }

        if (!registering) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.register_instead),
                modifier = Modifier.clickable { registering = true }
            )
        }
    }
}