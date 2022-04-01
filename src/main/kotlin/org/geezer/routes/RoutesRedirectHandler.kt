package org.geezer.routes

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface RoutesRedirectHandler {
    fun getRedirectUrl(url: String, request: HttpServletRequest, response: HttpServletResponse): String
}
