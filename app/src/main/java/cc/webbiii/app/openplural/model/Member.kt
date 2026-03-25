package cc.webbiii.app.openplural.model

typealias MemberId = Int

data class Member(
    val id: MemberId,
    val userId: UserId,
    val name: String,
    val pronouns: String?,
    val avatar: String?,
    val description: String?,
    val color: Int,
    val custom: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val folders: List<FolderId>,
)

fun Member.profilesMatch(other: Member): Boolean {
    return name == other.name &&
            pronouns == other.pronouns &&
            avatar == other.avatar &&
            description == other.description &&
            color == other.color
}

fun Member.foldersMatch(other: Member): Boolean {
    return folders.toSet() == other.folders.toSet()
}