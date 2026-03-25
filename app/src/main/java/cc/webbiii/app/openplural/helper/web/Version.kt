package cc.webbiii.app.openplural.helper.web

import cc.webbiii.app.openplural.helper.WebService

suspend fun WebService.checkAppUpdate(): String? {
    val (data, _) = request<String>("GET", "$baseUrl/app_update", expectCode = 200)
    return data
}