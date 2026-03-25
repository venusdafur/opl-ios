package cc.webbiii.app.openplural.model

typealias FrontEntryId = Long

data class FrontEntry(
    val id: FrontEntryId,
    val member: MemberId,
    val startedAt: String,
    val endedAt: String?,
    val comment: String?,
)

data class FrontCommentRequest(
    val comment: String?,
)

data class FrontStartTimeRequest(
    val startedAt: String
)