package cc.webbiii.app.openplural.helper.web

import cc.webbiii.app.openplural.model.FrontEntry
import cc.webbiii.app.openplural.model.UserId
import cc.webbiii.app.openplural.model.UserResponse
import cc.webbiii.app.openplural.helper.WebService
import java.io.IOException

@Throws(IOException::class, WebException::class)
suspend fun WebService.getUser(full: Boolean = false): UserResponse {
    val (data, _) = request<UserResponse>("GET", "$baseUrl/api/user/self?full=$full")
    return data!!
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.getFront(userId: UserId? = null): Array<FrontEntry> {
    val userFilter = if (userId != null) "?userId=$userId" else ""
    val (data, _) = request<Array<FrontEntry>>("GET", "$baseUrl/api/user/front$userFilter")
    return data!!
}