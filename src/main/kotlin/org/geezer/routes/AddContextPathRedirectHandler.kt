package org.geezer.routes

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class AddContextPathRedirectHandler : RoutesRedirectHandler {
    override fun getRedirectUrl(url: String, request: HttpServletRequest, response: HttpServletResponse): String {
        return if (url.startsWith("/") && !url.startsWith(request.contextPath)) {
            request.contextPath + url
        } else {
            url
        }
    }
}
