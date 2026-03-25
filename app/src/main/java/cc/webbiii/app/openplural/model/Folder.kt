package cc.webbiii.app.openplural.model

typealias FolderId = Int

data class Folder(
    val id: FolderId,
    val parentId: FolderId?,
    val name: String,
    val description: String?,
    val emoji: String?,
    val color: Int,
    val createdAt: String,
    val updatedAt: String,
) {
    fun resolvePath(folders: List<Folder>, basePath: String): String {
        var selfDisplayName = "";
        if (emoji != null) {
            selfDisplayName += "$emoji "
        }
        selfDisplayName += name

        return if (parentId == null) {
            "$basePath / $selfDisplayName"
        } else {
            val parent = folders.find { it.id == parentId }
            if (parent != null) {
                val parentPath = parent.resolvePath(folders, basePath)
                "$parentPath / $selfDisplayName"
            } else {
                "$basePath / ???"
            }
        }
    }
}