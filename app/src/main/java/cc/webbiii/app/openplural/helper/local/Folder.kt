package cc.webbiii.app.openplural.helper.local

import cc.webbiii.app.openplural.DEFAULT_RESOURCE_COLOR
import cc.webbiii.app.openplural.concat
import cc.webbiii.app.openplural.dateFromIso8601
import cc.webbiii.app.openplural.model.FolderId
import cc.webbiii.app.openplural.model.Folder
import cc.webbiii.app.openplural.nowIso8601
import cc.webbiii.app.openplural.helper.LocalStorage
import cc.webbiii.app.openplural.helper.web.createFolder
import cc.webbiii.app.openplural.helper.web.deleteFolder
import cc.webbiii.app.openplural.helper.web.editFolder

fun LocalStorage.getFolder(id: FolderId): Folder? {
    return selfUser.value.folders?.find { it.id == id }
}

fun LocalStorage.createFolder(name: String, parentId: FolderId?): FolderId {
    val id = localId()
    val now = nowIso8601()
    val folder = Folder(
        id = id,
        parentId = parentId,
        name = name,
        description = null,
        emoji = null,
        color = DEFAULT_RESOURCE_COLOR,
        createdAt = now,
        updatedAt = now,
    )
    selfUser.value = selfUser.value.copy(folders = concat(selfUser.value.folders, folder))
    foldersChanged()
    return id
}

fun LocalStorage.deleteFolder(id: FolderId) {
    selfUser.value.folders?.let { folders ->
        selfUser.value = selfUser.value.copy(folders = folders.filter { it.id != id }.toTypedArray())
        foldersChanged()
    }
}

fun LocalStorage.editFolder(folder: Folder) {
    selfUser.value.folders?.let { folders ->
        selfUser.value = selfUser.value.copy(folders = folders.map { if (it.id == folder.id) folder else it }.toTypedArray())
        foldersChanged()
    }
}

internal suspend fun LocalStorage.syncFolders(serverFolders: Array<Folder>) {
    val localFolders = selfUser.value.folders ?: emptyArray()
    var newestFolder = sync.newestFolder

    for (local in localFolders) {
        val server = serverFolders.find { it.id == local.id }
        if (server == null) {
            // Local only, create on server
            if (local.id < sync.newestFolder && local.id > 0) {
                // Created before last sync, must have been deleted on server
                selfUser.value = selfUser.value.copy(
                    folders = localFolders.filter { it.id != local.id }.toTypedArray(),
                    members = selfUser.value.members?.map {
                        if (it.folders.contains(local.id)) {
                            val folders = it.folders.toMutableList()
                            folders.remove(local.id)
                            it.copy(folders = folders)
                        } else {
                            it
                        }
                    }?.toTypedArray()
                )
                continue
            }
            val id = webService.createFolder(local)
            selfUser.value = selfUser.value.copy(
                folders = localFolders.map { if (it.id == local.id) local.copy(id = id) else it }.toTypedArray(),
                members = selfUser.value.members?.map {
                    if (it.folders.contains(local.id)) {
                        val folders = it.folders.toMutableList()
                        folders.remove(local.id)
                        folders.add(id)
                        it.copy(folders = folders)
                    } else {
                        it
                    }
                }?.toTypedArray()
            )
            if (id > newestFolder) {
                newestFolder = id
            }
        } else {
            val localUpdated = dateFromIso8601(local.updatedAt)
            val serverUpdated = dateFromIso8601(server.updatedAt)

            if (localUpdated.after(serverUpdated)) {
                // Local newer, update on server
                webService.editFolder(local)
            } else if (localUpdated.before(serverUpdated)) {
                // Server newer, update locally
                selfUser.value = selfUser.value.copy(folders = localFolders.map { if (it.id == local.id) server else it }.toTypedArray())
            }
        }
    }

    for (server in serverFolders) {
        val local = localFolders.find { it.id == server.id }
        if (local == null) {
            // Server only, add locally
            if (server.id <= sync.newestFolder) {
                // Created before last sync, must have been deleted locally
                webService.deleteFolder(server.id)
                continue
            }
            selfUser.value = selfUser.value.copy(folders = concat(selfUser.value.folders, server))
            newestFolder = server.id
        }
    }

    sync.newestFolder = newestFolder
    dirty.folders = false
    save()
}