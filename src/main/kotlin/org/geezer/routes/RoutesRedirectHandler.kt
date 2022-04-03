package org.geezer.routes

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

interface RoutesRedirectHandler {
    fun getRedirectUrl(url: String, request: HttpServletRequest, response: HttpServletResponse): String
}
