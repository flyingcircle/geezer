package org.geezer.routes

import java.net.URL
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession

class RequestContext(val request: HttpServletRequest, val response: HttpServletResponse) {
    val method: HttpMethod by lazy { HttpMethod.fromServletMethod(request.method) }

    val session: HttpSession by lazy { request.session }

    val path: RequestPath by lazy { RequestPath(request) }

    val parameters: RequestParameters by lazy { RequestParameters(request) }

    val requestedContentType: RequestedContentType by lazy { RequestedContentType(this) }

    val requestContent: RequestContent by lazy { RequestContent(request) }

    val requestUrl: String by lazy {
        val queryString = request.queryString ?: ""
        if (queryString.isNotBlank()) {
            "${request.requestURL}?$queryString"
        } else {
            request.requestURL.toString()
        }
    }

    val url: URL by lazy { URL(requestUrl) }
}
