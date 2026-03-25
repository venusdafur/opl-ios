package cc.webbiii.app.openplural.helper.local

import cc.webbiii.app.openplural.helper.LocalStorage
import cc.webbiii.app.openplural.helper.web.getFolders
import cc.webbiii.app.openplural.helper.web.getFront
import cc.webbiii.app.openplural.helper.web.getMembers
import cc.webbiii.app.openplural.helper.web.getUser

private const val FULL_SYNC_INTERVAL = 1000L * 60 * 60 * 24 // 24 hours

suspend fun LocalStorage.syncFully() {
    val time = System.currentTimeMillis()
    val user = webService.getUser(true)
    user.session = null

    syncFolders(user.folders ?: emptyArray())
    syncMembers(user.members ?: emptyArray())
    syncFront(user.front ?: emptyArray())

    selfUser.value = user.copy(
        folders = selfUser.value.folders ?: user.folders,
        members = selfUser.value.members ?: user.members,
        front = selfUser.value.front ?: user.front,
    )
    sync.lastFullSync = time
    save()
}

@Throws(Exception::class)
suspend fun LocalStorage.syncDirtyResources() {
    if (dirty.folders && dirty.members && dirty.front) {
        // If everything is dirty, do a full sync instead to save time and bandwidth
        syncFully()
        return
    }
    if (System.currentTimeMillis() - sync.lastFullSync > FULL_SYNC_INTERVAL) {
        // It the last sync was a long time ago, do a full sync to make sure everything is up to date
        syncFully()
        return
    }

    if (dirty.folders) {
        syncFolders(webService.getFolders())
    }
    if (dirty.members) {
        syncMembers(webService.getMembers())
    }
    if (dirty.front) {
        syncFront(webService.getFront())
    }
}