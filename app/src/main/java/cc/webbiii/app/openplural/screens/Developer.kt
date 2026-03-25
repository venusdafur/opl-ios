package cc.webbiii.app.openplural.screens

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.webbiii.app.openplural.appVersion
import cc.webbiii.app.openplural.helper.LocalStorage
import cc.webbiii.app.openplural.helper.local.syncDirtyResources
import cc.webbiii.app.openplural.helper.local.syncFully
import cc.webbiii.app.openplural.helper.web.getUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DeveloperScreen() {
    val ctx = LocalActivity.current
    val localStorage = remember { LocalStorage(ctx!!) }
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        val info = appVersion(ctx)
        if (info == null) {
            Text("<unknown package>")
        } else {
            Text(info.packageName)
            Text("${info.versionName} (v${info.versionCode})")
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    localStorage.syncDirtyResources()
                    ctx?.runOnUiThread {
                        Toast.makeText(ctx, "Sync complete", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Partial Sync")
        }
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    localStorage.syncFully()
                    ctx?.runOnUiThread {
                        Toast.makeText(ctx, "Sync complete", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Full Sync")
        }
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    try {
                        val resp = localStorage.webService.getUser(true)
                        ctx?.runOnUiThread {
                            localStorage.selfUser.value = resp
                            localStorage.save(true)
                            Toast.makeText(
                                ctx,
                                "n=${resp.name};i=${resp.id};f=${resp.folders?.size},m=${resp.members?.size},fr=${resp.front?.size}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ctx?.runOnUiThread {
                            Toast.makeText(ctx, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Force Sync (Dangerous)")
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        Button(
            onClick = {
                localStorage.dirty.folders = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mark folders dirty")
        }
        Button(
            onClick = {
                localStorage.dirty.members = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mark members dirty")
        }
        Button(
            onClick = {
                localStorage.dirty.front = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mark front dirty")
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        Button(
            onClick = {
                localStorage.save()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save local storage")
        }
    }
}