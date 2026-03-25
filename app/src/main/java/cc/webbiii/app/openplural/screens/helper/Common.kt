package cc.webbiii.app.openplural.screens.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.brighter
import cc.webbiii.app.openplural.drawOneSideBorder
import cc.webbiii.app.openplural.opaque
import cc.webbiii.app.openplural.toRgb
import coil.compose.SubcomposeAsyncImage

@Composable
fun <T> Spinner(items: List<T>, selected: T, displayMapper: @Composable (T) -> String, onSelected: (T) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(selected) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button (
            onClick = {
                expanded = true
            },
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.background.brighter(10),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(displayMapper(selectedItem))
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_drop_down_32),
                    contentDescription = stringResource(R.string.dropdown)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(displayMapper(item)) },
                    onClick = {
                        selectedItem = item
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColorPicker(color: Int, onChange: (Int) -> Unit, clickable: Boolean = true) {
    var editColor by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (clickable) {
                editColor = true
            }
        },
        colors = ButtonColors(
            containerColor = opaque(color),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
        ),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text(" ")
    }

    if (editColor && clickable) {
        ColorPrompt(
            title = stringResource(R.string.color_prompt),
            initialValue = opaque(color),
            onDismiss = {
                editColor = false
            },
            onSubmit = {
                onChange(it.toRgb())
                editColor = false
            }
        )
    }
}

@Composable
fun WebImage(url: String, default: Int, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        loading = {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        },
        error = {
            Icon(
                painter = painterResource(default),
                contentDescription = stringResource(R.string.empty)
            )
        },
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .aspectRatio(1f)
    )
}

@Composable
fun FullPageLoadingIndicator(modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )
        content()
    }
}

@Composable
fun ListEntry(color: Int, onClick: () -> Unit, modifier: Modifier = Modifier, button: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonColors(
            containerColor = MaterialTheme.colorScheme.background.brighter(10),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .drawOneSideBorder(8.dp, opaque(color), RoundedCornerShape(8.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
            button?.let { it() }
        }
    }
}