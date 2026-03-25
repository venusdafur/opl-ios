package cc.webbiii.app.openplural.helper.local

import cc.webbiii.app.openplural.DEFAULT_RESOURCE_COLOR
import cc.webbiii.app.openplural.concat
import cc.webbiii.app.openplural.dateFromIso8601
import cc.webbiii.app.openplural.model.Member
import cc.webbiii.app.openplural.model.MemberId
import cc.webbiii.app.openplural.nowIso8601
import cc.webbiii.app.openplural.helper.LocalStorage
import cc.webbiii.app.openplural.helper.web.createMember
import cc.webbiii.app.openplural.helper.web.deleteMember
import cc.webbiii.app.openplural.helper.web.editMember
import cc.webbiii.app.openplural.helper.web.editMemberFolders
import cc.webbiii.app.openplural.model.foldersMatch
import cc.webbiii.app.openplural.model.profilesMatch

fun LocalStorage.getMember(id: MemberId): Member? {
    return selfUser.value.members?.find { it.id == id }
}

fun LocalStorage.createMember(name: String, custom: Boolean): MemberId {
    val id = localId()
    val now = nowIso8601()
    val member = Member(
        id = id,
        userId = selfUser.value.id,
        name = name,
        pronouns = null,
        avatar = null,
        description = null,
        color = DEFAULT_RESOURCE_COLOR,
        createdAt = now,
        updatedAt = now,
        custom = custom,
        folders = emptyList(),
    )
    selfUser.value = selfUser.value.copy(members = concat(selfUser.value.members, member))
    membersChanged()
    return id
}

fun LocalStorage.deleteMember(id: MemberId) {
    selfUser.value.members?.let { members ->
        selfUser.value = selfUser.value.copy(members = members.filter { it.id != id }.toTypedArray())
        membersChanged()
    }
}

fun LocalStorage.editMember(member: Member) {
    selfUser.value.members?.let { members ->
        selfUser.value = selfUser.value.copy(members = members.map { if (it.id == member.id) member else it }.toTypedArray())
        membersChanged()
    }
}

internal suspend fun LocalStorage.syncMembers(serverMembers: Array<Member>) {
    val localMembers = selfUser.value.members ?: emptyArray()
    var newestMember = sync.newestMember

    for (local in localMembers) {
        val server = serverMembers.find { it.id == local.id }
        if (server == null) {
            // Local only, create on server
            if (local.id < sync.newestMember && local.id > 0) {
                // Created before last sync, must have been deleted on server
                selfUser.value = selfUser.value.copy(
                    members = localMembers.filter { it.id != local.id }.toTypedArray(),
                    front = selfUser.value.front?.filter { it.member != local.id }?.toTypedArray()
                )
                continue
            }
            val id = webService.createMember(local)
            selfUser.value = selfUser.value.copy(
                members = localMembers.map { if (it.id == local.id) local.copy(id = id) else it }.toTypedArray(),
                front = selfUser.value.front?.map {
                    if (it.member == local.id) {
                        it.copy(member = id)
                    } else {
                        it
                    }
                }?.toTypedArray()
            )
            if (id > newestMember) {
                newestMember = id
            }
        } else {
            val localUpdated = dateFromIso8601(local.updatedAt)
            val serverUpdated = dateFromIso8601(server.updatedAt)

            if (localUpdated.after(serverUpdated)) {
                // Local newer, update on server
                if (!local.profilesMatch(server)) {
                    webService.editMember(local)
                }
                if (!local.foldersMatch(server)) {
                    webService.editMemberFolders(local.id, local.folders)
                }
            } else if (localUpdated.before(serverUpdated)) {
                // Server newer, update locally
                selfUser.value = selfUser.value.copy(members = localMembers.map { if (it.id == local.id) server else it }.toTypedArray())
            }
        }
    }

    for (server in serverMembers) {
        val local = localMembers.find { it.id == server.id }
        if (local == null) {
            // Server only, add locally
            if (server.id <= sync.newestMember) {
                // Created before last sync, must have been deleted locally
                webService.deleteMember(server.id)
                continue
            }
            selfUser.value = selfUser.value.copy(members = concat(selfUser.value.members, server))
            newestMember = server.id
        }
    }

    sync.newestMember = newestMember
    dirty.members = false
    save()
}