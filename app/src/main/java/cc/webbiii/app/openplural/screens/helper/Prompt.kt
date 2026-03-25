package cc.webbiii.app.openplural.screens.helper

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.htmlColor
import cc.webbiii.app.openplural.model.Folder
import cc.webbiii.app.openplural.model.FolderId
import cc.webbiii.app.openplural.helper.AppSettingsService
import cc.webbiii.app.openplural.helper.COLOR_WHEEL
import cc.webbiii.app.openplural.toHtml
import cc.webbiii.app.openplural.toRgb
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker

@Composable
fun Prompt(title: String, label: Int, prompt: Int, initialValue: String = "", onDismiss: () -> Unit, onSubmit: (String) -> Unit, explanation: Int? = null, modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf(initialValue) }

    GenericPrompt(
        title = title,
        onDismiss = onDismiss,
        onSubmit = {
            onSubmit(input)
        },
        modifier = modifier
    ) {
        if (explanation != null) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(4.dp))
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_info_32),
                    contentDescription = stringResource(R.string.info),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = stringResource(explanation),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        TextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(text = stringResource(label)) },
            placeholder = { Text(text = stringResource(prompt)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ColorPrompt(title: String, initialValue: Color, onDismiss: () -> Unit, onSubmit: (Color) -> Unit, modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf(HsvColor.from(initialValue)) }
    var html by remember { mutableStateOf(initialValue.toHtml()) }

    val ctx = LocalContext.current
    val settings = AppSettingsService(ctx)

    GenericPrompt(
        title = title,
        onDismiss = onDismiss,
        onSubmit = {
            onSubmit(input.toColor())
        },
        modifier = modifier
    ) {
        if (settings.bool(COLOR_WHEEL, false)) {
            HarmonyColorPicker(
                harmonyMode = ColorHarmonyMode.NONE,
                color = input,
                onColorChanged = {
                    input = it
                    html = it.toColor().toHtml()
                },
                showBrightnessBar = true,
                modifier = Modifier.aspectRatio(1f)
                    .padding(8.dp)
            )
        } else {
            ClassicColorPicker(
                color = input,
                onColorChanged = {
                    input = it
                    html = it.toColor().toHtml()
                },
                showAlphaBar = false,
                modifier = Modifier.height(192.dp)
            )
        }
        ColorPicker(
            color = input.toColor().toRgb(),
            onChange = {},
            clickable = false
        )
        TextField(
            value = html,
            onValueChange = {
                if (it.length <= 7) {
                    html = it
                }
                if (it.length >= 6) {
                    try {
                        input = HsvColor.from(htmlColor(it))
                    } catch (_: NumberFormatException) {
                    }
                }
            },
            singleLine = true
        )
    }
}

@Composable
fun GroupSelectionPrompt(title: String, folders: List<Folder>, initialSelection: List<FolderId>, onDismiss: () -> Unit, onSubmit: (List<FolderId>) -> Unit, modifier: Modifier = Modifier) {
    var selection by remember { mutableStateOf(initialSelection) }

    GenericPrompt(
        title = title,
        onDismiss = onDismiss,
        onSubmit = {
            onSubmit(selection)
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.75f)
                .scrollable(rememberScrollState(), Orientation.Vertical)
        ) {
            folders.filter {
                it.parentId == null
            }.forEach {
                FolderSelection(it, folders, selection, onSelectChange = { id, selected ->
                    if (selected) {
                        selection += id
                    } else {
                        selection = selection.filter { folderId ->
                            folderId != id
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun FolderSelection(folder: Folder, folders: List<Folder>, initialSelection: List<FolderId>, nested: Boolean = false, onSelectChange: (FolderId, Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        if (nested) {
            VerticalDivider()
        }
        Column {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(40.dp)
            ) {
                Checkbox(
                    checked = initialSelection.contains(folder.id),
                    onCheckedChange = {
                        onSelectChange(folder.id, it)
                    }
                )
                Text(folder.name)
            }
            folders.filter {
                it.parentId == folder.id
            }.forEach {
                FolderSelection(it, folders, initialSelection, nested = true, onSelectChange = onSelectChange, modifier = Modifier.padding(start = 16.dp))
            }
        }
    }
}

@Composable
private fun GenericPrompt(title: String, onDismiss: () -> Unit, onSubmit: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card {
            Column(modifier = modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = title)
                    IconButton(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_cancel_32),
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                }
                content()
                Button (
                    onClick = { onSubmit() },
                    modifier = Modifier.padding(top = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.confirm))
                }
            }
        }
    }
}