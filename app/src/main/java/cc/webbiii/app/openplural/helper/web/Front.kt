package cc.webbiii.app.openplural.helper.web

import cc.webbiii.app.openplural.model.FrontEntry
import cc.webbiii.app.openplural.model.FrontCommentRequest
import cc.webbiii.app.openplural.model.FrontEntryId
import cc.webbiii.app.openplural.model.FrontStartTimeRequest
import cc.webbiii.app.openplural.model.IdResponse
import cc.webbiii.app.openplural.model.MemberId
import cc.webbiii.app.openplural.helper.WebService
import java.io.IOException

@Throws(IOException::class, WebException::class)
suspend fun WebService.addFrontEntry(entry: FrontEntry): FrontEntryId {
    val (data, code) = request<IdResponse<FrontEntryId>>("PUT", "$baseUrl/api/front/", body = entry, expectCode = 200)

    if (code == 403) {
        throw WebException(code, "You may not add this member to front")
    }
    if (code == 409) {
        throw WebException(code, "This member is already fronting")
    }

    return data!!.id
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.front(memberId: MemberId): FrontEntryId {
    val (data, code) = request<IdResponse<FrontEntryId>>("PUT", "$baseUrl/api/front/$memberId", expectCode = 200)

    if (code == 403) {
        throw WebException(code, "You may not add this member to front")
    }
    if (code == 409) {
        throw WebException(code, "This member is already fronting")
    }

    return data!!.id
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.unfront(memberId: MemberId, endTime: String? = null) {
    val query = if (endTime != null) "?endedAt=$endTime" else ""
    val (_, code) = request<Unit>("DELETE", "$baseUrl/api/front/member/$memberId$query", expectCode = 204)

    if (code == 403) {
        throw WebException(code, "You may not remove this member from front")
    }
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.frontComment(entryId: FrontEntryId, comment: String?) {
    val req = FrontCommentRequest(comment)
    val (_, code) = request<Unit>("PATCH", "$baseUrl/api/front/$entryId/comment", body = req, expectCode = 204)

    if (code == 403) {
        throw WebException(code, "You may not edit this member at front")
    }
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.frontStartTime(entryId: FrontEntryId, startedAt: String) {
    val req = FrontStartTimeRequest(startedAt)
    val (_, code) = request<Unit>("PATCH", "$baseUrl/api/front/$entryId/startTime", body = req, expectCode = 204)

    if (code == 400) {
        throw WebException(code, "Invalid time format, must be RFC-3339")
    }
    if (code == 403) {
        throw WebException(code, "You may not edit this member at front")
    }
}