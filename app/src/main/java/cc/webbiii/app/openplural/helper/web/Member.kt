package cc.webbiii.app.openplural.helper.web

import cc.webbiii.app.openplural.model.FolderId
import cc.webbiii.app.openplural.model.IdResponse
import cc.webbiii.app.openplural.model.Member
import cc.webbiii.app.openplural.model.MemberId
import cc.webbiii.app.openplural.model.UserId
import cc.webbiii.app.openplural.helper.WebService
import java.io.IOException

@Throws(IOException::class, WebException::class)
suspend fun WebService.getMembers(userId: UserId? = null): Array<Member> {
    val userFilter = if (userId != null) "?userId=$userId" else ""
    val (data, code) = request<Array<Member>>("GET", "$baseUrl/api/member/$userFilter")

    if (code == 403) {
        throw WebException(code, "You do not have permission to view members for this user")
    }

    return data!!
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.getMember(id: MemberId): Member {
    val (data, code) = request<Member>("GET", "$baseUrl/api/member/$id")

    if (code == 403) {
        throw WebException(code, "You do not have permission to view this member")
    }
    if (code == 404) {
        throw WebException(code, "Member not found")
    }

    return data!!
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.createMember(member: Member): MemberId {
    val (data, _) = request<IdResponse<MemberId>>("PUT", "$baseUrl/api/member/", body = member)
    return data!!.id
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.deleteMember(id: MemberId) {
    request<Unit>("DELETE", "$baseUrl/api/member/$id", expectCode = 204)
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.editMember(member: Member) {
    val (_, code) = request<Unit>("PATCH", "$baseUrl/api/member/${member.id}", body = member, expectCode = 204)

    if (code == 403) {
        throw WebException(code, "You do not have permission to edit this member")
    }
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.editMemberFolders(id: MemberId, folders: List<FolderId>) {
    val (_, code) = request<Unit>("PATCH", "$baseUrl/api/member/$id/folders", body = folders, expectCode = 204)

    if (code == 403) {
        throw WebException(code, "You do not have permission to edit this member")
    }
}