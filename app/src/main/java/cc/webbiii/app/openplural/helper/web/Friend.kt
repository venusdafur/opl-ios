package cc.webbiii.app.openplural.helper.web

import cc.webbiii.app.openplural.helper.WebService
import cc.webbiii.app.openplural.model.FriendRequest
import cc.webbiii.app.openplural.model.UserId
import cc.webbiii.app.openplural.model.UserResponse
import java.io.IOException

@Throws(IOException::class, WebException::class)
suspend fun WebService.getFriends(): Array<UserResponse> {
    val (data, _) = request<Array<UserResponse>>("GET", "$baseUrl/api/friend/", expectCode = 200)

    return data!!
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.getIncomingFriendRequests(): Array<FriendRequest> {
    val (data, _) = request<Array<FriendRequest>>("GET", "$baseUrl/api/friend/requests/incoming", expectCode = 200)

    return data!!
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.getOutgoingFriendRequests(): Array<FriendRequest> {
    val (data, _) = request<Array<FriendRequest>>("GET", "$baseUrl/api/friend/requests/outgoing", expectCode = 200)

    return data!!
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.sendFriendRequest(friendCode: String) {
    val (_, code) = request<Unit>("PUT", "$baseUrl/api/friend/requests/$friendCode", expectCode = 204)

    if (code == 404) {
        throw WebException(code, "No user found with this friend code")
    }
    if (code == 409) {
        throw WebException(code, "You have already sent a friend request to this user, have received a friend request from this user, or are already friends with this user")
    }
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.acceptFriendRequest(friendCode: String) {
    val (_, code) = request<Unit>("POST", "$baseUrl/api/friend/requests/$friendCode/accept", expectCode = 204)

    if (code == 404) {
        throw WebException(code, "No user found with this friend code")
    }
    if (code == 409) {
        throw WebException(code, "You have not received a friend request from this user")
    }
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.declineFriendRequest(friendCode: String) {
    val (_, code) = request<Unit>("POST", "$baseUrl/api/friend/requests/$friendCode/decline", expectCode = 204)

    if (code == 404) {
        throw WebException(code, "No user found with this friend code")
    }
    if (code == 409) {
        throw WebException(code, "You have not received a friend request from this user")
    }
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.unfriend(userId: UserId) {
    request<Unit>("DELETE", "$baseUrl/api/friend/$userId", expectCode = 204)
}