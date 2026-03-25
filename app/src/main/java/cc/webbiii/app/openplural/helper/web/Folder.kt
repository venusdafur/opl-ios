package cc.webbiii.app.openplural.helper.web

import cc.webbiii.app.openplural.model.Folder
import cc.webbiii.app.openplural.model.FolderId
import cc.webbiii.app.openplural.model.IdResponse
import cc.webbiii.app.openplural.model.UserId
import cc.webbiii.app.openplural.helper.WebService
import okio.IOException

@Throws(IOException::class, WebException::class)
suspend fun WebService.getFolders(userId: UserId? = null): Array<Folder> {
    val userFilter = if (userId != null) "?userId=$userId" else ""
    val (data, code) = request<Array<Folder>>("GET", "$baseUrl/api/folder/$userFilter")

    if (code == 403) {
        throw WebException(code, "You do not have permission to view folders for this user")
    }

    return data!!
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.getFolder(id: FolderId): Folder {
    val (data, code) = request<Folder>("GET", "$baseUrl/api/folder/$id")

    if (code == 403) {
        throw WebException(code, "You do not have permission to view this folder")
    }
    if (code == 404) {
        throw WebException(code, "Folder not found")
    }

    return data!!
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.createFolder(folder: Folder): FolderId {
    val (data, _) = request<IdResponse<FolderId>>("PUT", "$baseUrl/api/folder/", body = folder)
    return data!!.id
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.deleteFolder(id: FolderId) {
    request<Unit>("DELETE", "$baseUrl/api/folder/$id", expectCode = 204)
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.editFolder(folder: Folder) {
    val (_, code) = request<Unit>("PATCH", "$baseUrl/api/folder/${folder.id}", body = folder, expectCode = 204)

    if (code == 403) {
        throw WebException(code, "You do not have permission to edit this folder")
    }
}