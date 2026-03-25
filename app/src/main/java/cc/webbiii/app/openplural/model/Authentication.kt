package cc.webbiii.app.openplural.model

typealias UserId = Int

data class RegisterRequest(
    val name: String,
    val password: String,
    val system: Boolean,
)

data class LoginRequest(
    val device: String,
    val name: String,
    val password: String,
)

data class SessionResponse(
    val token: String,
)

data class UserResponse(
    var session: SessionResponse?,
    val id: UserId,
    val name: String,
    val avatar: String?,
    val description: String?,
    val color: Int,
    val system: Boolean,
    val friendCode: String?,

    //these properties are only set if requested with full=true
    val folders: Array<Folder>?,
    val members: Array<Member>?,
    val front: Array<FrontEntry>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserResponse

        if (id != other.id) return false
        if (system != other.system) return false
        if (session != other.session) return false
        if (name != other.name) return false
        if (avatar != other.avatar) return false
        if (!folders.contentEquals(other.folders)) return false
        if (!members.contentEquals(other.members)) return false
        if (!front.contentEquals(other.front)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + system.hashCode()
        result = 31 * result + (session?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + (avatar?.hashCode() ?: 0)
        result = 31 * result + (folders?.contentHashCode() ?: 0)
        result = 31 * result + (members?.contentHashCode() ?: 0)
        result = 31 * result + (front?.contentHashCode() ?: 0)
        return result
    }
}