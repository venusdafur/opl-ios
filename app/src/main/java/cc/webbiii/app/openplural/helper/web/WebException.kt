package cc.webbiii.app.openplural.helper.web

class WebException(
    val code: Int,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)