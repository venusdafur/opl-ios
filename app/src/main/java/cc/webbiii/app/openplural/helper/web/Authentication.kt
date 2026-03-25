package cc.webbiii.app.openplural.helper.web

import cc.webbiii.app.openplural.model.LoginRequest
import cc.webbiii.app.openplural.model.RegisterRequest
import cc.webbiii.app.openplural.model.UserResponse
import cc.webbiii.app.openplural.helper.WebService
import java.io.IOException

@Throws(IOException::class, WebException::class)
suspend fun WebService.register(name: String, password: String, system: Boolean) {
    val req = RegisterRequest(name, password, system)

    val (_, code) = request<Unit>("POST", "$baseUrl/auth/register", 204, req)
    if (code == 409) {
        throw WebException(code, "Username already exists")
    }
}

@Throws(IOException::class, WebException::class)
suspend fun WebService.login(device: String, name: String, password: String): UserResponse {
    val req = LoginRequest(device, name, password)

    val (data, code) = request<UserResponse>("POST", "$baseUrl/auth/login", body = req)
    if (code == 401) {
        throw WebException(code, "Invalid username or password")
    }
    if (code == 500) {
        throw WebException(code, "Server error")
    }

    authToken = data!!.session?.token
    return data
}