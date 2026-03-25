package cc.webbiii.app.openplural.helper

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import cc.webbiii.app.openplural.GSON
import cc.webbiii.app.openplural.model.FolderId
import cc.webbiii.app.openplural.model.FrontEntryId
import cc.webbiii.app.openplural.model.MemberId
import cc.webbiii.app.openplural.model.UserResponse
import cc.webbiii.app.openplural.nowIso8601
import kotlin.math.absoluteValue

const val DATA_FORMAT_VERSION = 1
const val FULL_SYNC_INTERVAL = 18 * 60 * 60 * 1000L // 18 hours

data class SyncData(
    var lastFullSync: Long,
    var newestFolder: FolderId,
    var newestMember: MemberId,
    var newestFront: FrontEntryId,
)

data class DirtyData(
    var folders: Boolean,
    var members: Boolean,
    var front: Boolean,
)

class LocalStorage {
    val selfUser: MutableState<UserResponse>
    val sync: SyncData
    val dirty: DirtyData

    private val ctx: Context
    private val sharedPreferences: SharedPreferences

    private val _webService: Lazy<WebService> = lazy { WebService.fromContext(ctx) }
    internal val webService: WebService
        get() = _webService.value

    constructor(ctx: Context) {
        fun <T> fromJson(key: String, clazz: Class<T>): T {
            val json = sharedPreferences.getString(key, null)
            if (json == null) {
                throw IllegalStateException("Missing $key in local storage")
            }

            return GSON.fromJson(json, clazz)
        }

        println("Loading local storage")

        this.ctx = ctx
        this.sharedPreferences = ctx.getSharedPreferences("openplural_data", Context.MODE_PRIVATE)
        this.selfUser = mutableStateOf(fromJson("selfUser", UserResponse::class.java))
        this.sync = fromJson("sync", SyncData::class.java)
        this.dirty = fromJson("dirty", DirtyData::class.java)

        println("Loaded local storage: selfUser=${selfUser.value}, sync=$sync, dirty=$dirty")
    }

    constructor(ctx: Context, selfUser: UserResponse) {
        this.selfUser = mutableStateOf(selfUser)
        this.sync = SyncData(
            lastFullSync = 0,
            newestFolder = selfUser.folders?.maxOf { it.id } ?: 0,
            newestMember = selfUser.members?.maxOf { it.id } ?: 0,
            newestFront = selfUser.front?.maxOf { it.id } ?: 0,
        )
        this.dirty = DirtyData(
            folders = false,
            members = false,
            front = false,
        )
        this.ctx = ctx
        this.sharedPreferences = ctx.getSharedPreferences("openplural_data", Context.MODE_PRIVATE)
    }

    fun foldersChanged() {
        dirty.folders = true
        save()
    }

    fun membersChanged() {
        dirty.members = true
        save()
    }

    fun frontChanged() {
        dirty.front = true
        save()
    }

    fun save(commit: Boolean = false) {
        this.sharedPreferences.edit(commit = commit) {
            putInt("version", DATA_FORMAT_VERSION)
            putString("selfUser", GSON.toJson(selfUser.value))
            putString("sync", GSON.toJson(sync))
            putString("dirty", GSON.toJson(dirty))
        }
    }

    internal fun localId(): Int {
        return -(nowIso8601().hashCode().absoluteValue)
    }
}

class LocalStorageViewModel(app: Application) : AndroidViewModel(app) {
    val localStorage = LocalStorage(app)
    val selfUser = localStorage.selfUser
}