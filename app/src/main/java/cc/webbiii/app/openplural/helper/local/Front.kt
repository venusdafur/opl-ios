package cc.webbiii.app.openplural.helper.local

import cc.webbiii.app.openplural.concat
import cc.webbiii.app.openplural.dateFromIso8601
import cc.webbiii.app.openplural.model.FrontEntry
import cc.webbiii.app.openplural.model.FrontEntryId
import cc.webbiii.app.openplural.model.MemberId
import cc.webbiii.app.openplural.nowIso8601
import cc.webbiii.app.openplural.helper.LocalStorage
import cc.webbiii.app.openplural.helper.web.addFrontEntry
import cc.webbiii.app.openplural.helper.web.frontComment
import cc.webbiii.app.openplural.helper.web.frontStartTime
import cc.webbiii.app.openplural.helper.web.unfront
import java.util.Objects

fun LocalStorage.front(memberId: MemberId): FrontEntryId {
    val id = localId().toLong()
    val now = nowIso8601()
    val entry = FrontEntry(
        id = id,
        member = memberId,
        startedAt = now,
        endedAt = null,
        comment = null,
    )
    selfUser.value = selfUser.value.copy(front = concat(selfUser.value.front, entry))
    frontChanged()
    return id
}

fun LocalStorage.unfront(memberId: MemberId) {
    selfUser.value.front?.let { front ->
        selfUser.value = selfUser.value.copy(front = front.map { if (it.member == memberId && it.endedAt == null) it.copy(endedAt = nowIso8601()) else it }.toTypedArray())
        frontChanged()
    }
}

fun LocalStorage.frontComment(id: FrontEntryId, comment: String?) {
    selfUser.value.front?.let { front ->
        selfUser.value = selfUser.value.copy(front = front.map { if (it.id == id) it.copy(comment = comment) else it }.toTypedArray())
        frontChanged()
    }
}

internal suspend fun LocalStorage.syncFront(serverFront: Array<FrontEntry>) {
    val localFront = selfUser.value.front ?: emptyArray()
    var newestFront = sync.newestFront

    for (local in localFront) {
        val server = serverFront.find { it.id == local.id }
        if (server == null) {
            // Local only, create on server
            if (local.id < sync.newestFront && local.id > 0) {
                // Created before last sync, must have been deleted on server
                selfUser.value = selfUser.value.copy(front = localFront.filter { it.id != local.id }.toTypedArray())
                continue
            }
            val id = webService.addFrontEntry(local)
            selfUser.value = selfUser.value.copy(front = localFront.map { if (it.id == local.id) local.copy(id = id) else it }.toTypedArray())
            if (id > newestFront) {
                newestFront = id
            }
        } else if (local.endedAt != null) {
            // Entry ended locally, but server still has it

            webService.unfront(local.member, local.endedAt)
        } else {
            // Server has entry, check for updates
            // Local updates take priority

            val localTime = dateFromIso8601(local.startedAt).time / 1000L
            val serverTime = dateFromIso8601(server.startedAt).time / 1000L
            if (localTime != serverTime) {
                webService.frontStartTime(local.id, local.startedAt)
            }
            if (!Objects.equals(local.comment, server.comment)) {
                webService.frontComment(local.id, local.comment)
            }
        }
    }

    for (server in serverFront) {
        val local = localFront.find { it.id == server.id }
        if (local == null) {
            // Server only, add locally
            selfUser.value = selfUser.value.copy(front = concat(selfUser.value.front, server))
            newestFront = server.id
        }
    }

    // Delete ended local entries that we pushed
    selfUser.value.front?.let { front ->
        selfUser.value = selfUser.value.copy(front = front.filter { it.id > newestFront || it.endedAt == null }.toTypedArray())
    }
    sync.newestFront = newestFront
    dirty.front = false
    save()
}