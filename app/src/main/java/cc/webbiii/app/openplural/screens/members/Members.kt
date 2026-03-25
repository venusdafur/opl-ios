package cc.webbiii.app.openplural.screens.members

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.webbiii.app.openplural.R
import cc.webbiii.app.openplural.helper.LocalStorageViewModel
import cc.webbiii.app.openplural.helper.local.createFolder
import cc.webbiii.app.openplural.helper.local.createMember
import cc.webbiii.app.openplural.helper.local.front
import cc.webbiii.app.openplural.helper.local.syncDirtyResources
import cc.webbiii.app.openplural.helper.local.unfront
import cc.webbiii.app.openplural.model.Folder
import cc.webbiii.app.openplural.model.Member
import cc.webbiii.app.openplural.model.UserResponse
import cc.webbiii.app.openplural.opaque
import cc.webbiii.app.openplural.screens.helper.ListEntry
import cc.webbiii.app.openplural.screens.helper.NavControl
import cc.webbiii.app.openplural.screens.helper.Prompt
import cc.webbiii.app.openplural.screens.helper.WebImage
import cc.webbiii.app.openplural.screens.helper.navigate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MemberTree(navControl: NavControl, foreignUser: UserResponse?, viewModel: LocalStorageViewModel, custom: Boolean, root: Int = 0) {
    val localSelfUser by viewModel.selfUser
    var viewFolders by remember(custom) { mutableStateOf(!custom) }
    val effectiveUser by remember(foreignUser) {
        derivedStateOf {
            foreignUser ?: localSelfUser
        }
    }
    val folders by remember(effectiveUser.folders) {
        derivedStateOf {
            effectiveUser.folders
        }
    }
    val members by remember(effectiveUser.members) {
        derivedStateOf {
            effectiveUser.members?.filter { it.custom == custom }
        }
    }
    var currentRoot by remember(root) { mutableIntStateOf(root) }
    val currentPath by remember(currentRoot, folders) {
        derivedStateOf {
            return@derivedStateOf if (currentRoot != 0 && folders != null) {
                val folders = folders as Array<out Folder>
                folders.find { it.id == currentRoot }?.resolvePath(folders.toList(), "Root") ?: "Root"
            } else {
                "Root"
            }
        }
    }

    val currentFolders by remember(currentRoot, folders) {
        derivedStateOf {
            folders?.filter {
                if (currentRoot == 0) {
                    it.parentId == null
                } else {
                    it.parentId == currentRoot
                }
            }?.toList()
        }
    }
    val currentMembers by remember(currentRoot, folders, members) {
        derivedStateOf {
            members?.filter {
                if (currentRoot == 0) {
                    it.folders.isEmpty()
                } else {
                    it.folders.contains(currentRoot)
                }
            }?.toList()
        }
    }

    var newFolder by remember { mutableStateOf(false) }
    var newMember by remember { mutableStateOf(false) }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!custom) {
                    IconButton(
                        onClick = { viewFolders = !viewFolders }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_folder_32),
                            contentDescription = stringResource(R.string.view_folders),
                            tint = if (viewFolders) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (foreignUser == null) {
                    IconButton(
                        onClick = {
                            newMember = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_person_add_32),
                            contentDescription = stringResource(R.string.create_member)
                        )
                    }
                }
            }
        }
        if (viewFolders) {
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = {
                                newFolder = true
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_create_new_folder_32),
                                contentDescription = stringResource(R.string.create_folder)
                            )
                        }
                        if (currentRoot != 0) {
                            IconButton(
                                onClick = {
                                    currentRoot = folders?.first { it.id == currentRoot }?.parentId ?: 0
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_back_32),
                                    contentDescription = stringResource(R.string.go_back)
                                )
                            }
                        }
                    }
                    if (currentRoot != 0) {
                        IconButton(
                            onClick = {
                                navControl.navigate("editFolder/$currentRoot")
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_folder_managed_32),
                                contentDescription = stringResource(R.string.options)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = currentPath,
                    modifier = Modifier.fillMaxWidth()
                        .padding(8.dp)
                )
            }

            items(currentFolders as List<Folder>) {
                FolderEntry(it, onClick = {
                    currentRoot = it.id
                })
            }
            items(currentMembers as List<Member>) { member ->
                val scope = rememberCoroutineScope()
                val fronting = localSelfUser.front?.find { it.member == member.id } != null
                MemberEntry(member, fronting, foreignUser == null, onClick = {
                    navControl.navigate("editMember/${member.id}?custom=$custom")
                }, onFront = {
                    if (it) {
                        viewModel.localStorage.front(member.id)
                    } else {
                        viewModel.localStorage.unfront(member.id)
                    }
                    scope.launch(Dispatchers.IO) {
                        try {
                            viewModel.localStorage.syncDirtyResources()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }, modifier = Modifier.padding(4.dp))
            }
        } else {
            items(members as List<Member>) { member ->
                val scope = rememberCoroutineScope()
                val fronting = localSelfUser.front?.find { it.member == member.id } != null
                MemberEntry(member, fronting, foreignUser == null, onClick = {
                    navControl.navigate("editMember/${member.id}?custom=$custom")
                }, onFront = {
                    if (it) {
                        viewModel.localStorage.front(member.id)
                    } else {
                        viewModel.localStorage.unfront(member.id)
                    }
                    scope.launch(Dispatchers.IO) {
                        try {
                            viewModel.localStorage.syncDirtyResources()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }, modifier = Modifier.padding(4.dp))
            }
        }
    }

    if (newFolder) {
        Prompt(
            title = stringResource(R.string.create_folder),
            label = R.string.name,
            prompt = R.string.name_prompt,
            onDismiss = { newFolder = false },
            onSubmit = { name ->
                newFolder = false

                val id = viewModel.localStorage.createFolder(name, currentRoot.takeIf { it != 0 })
                navControl.navigate("editFolder/$id")
            }
        )
    }

    if (newMember) {
        Prompt(
            title = stringResource(R.string.create_member),
            label = R.string.name,
            prompt = R.string.name_prompt,
            onDismiss = { newMember = false },
            onSubmit = { name ->
                newMember = false

                val id = viewModel.localStorage.createMember(name, custom)
                navControl.navigate("editMember/$id?custom=$custom")
            }
        )
    }
}

@Composable
fun FolderEntry(folder: Folder, onClick: () -> Unit) {
    ListEntry(folder.color, onClick = onClick, modifier = Modifier.padding(4.dp).height(64.dp)) {
        if (folder.emoji != null) {
            Text(folder.emoji, fontSize = 24.sp)
        } else {
            Icon(
                painter = painterResource(R.drawable.baseline_folder_32),
                contentDescription = folder.name
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(folder.name)
    }
}

@Composable
fun MemberEntry(member: Member, fronting: Boolean, local: Boolean, onClick: () -> Unit, onFront: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    ListEntry(member.color, onClick = onClick, modifier = modifier.height(64.dp), button = {
        if (local) {
            IconButton(
                onClick = {
                    if (fronting) {
                        onFront(false)
                    } else {
                        onFront(true)
                    }
                },
                modifier = Modifier.border(2.dp, if (fronting) opaque(member.color) else MaterialTheme.colorScheme.onPrimaryContainer, shape = RoundedCornerShape(8.dp))
            ) {
                if (fronting) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_remove_32),
                        contentDescription = stringResource(R.string.remove_front)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.baseline_add_32),
                        contentDescription = stringResource(R.string.add_front)
                    )
                }
            }
        }
    }) {
        if (member.avatar != null) {
            WebImage(member.avatar, R.drawable.baseline_person_32, modifier = Modifier.width(48.dp))
        } else {
            Icon(
                painter = painterResource(R.drawable.baseline_person_32),
                contentDescription = member.name
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(member.name)
            member.pronouns?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}