package org.geezer.routes

import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

class RequestContext(val request: HttpServletRequest, val response: HttpServletResponse) {
    val method: HttpMethod by lazy { HttpMethod.fromServletMethod(request.method) }

    val session: HttpSession by lazy { request.session }

    val path: RequestPath by lazy { RequestPath(request) }

    val parameters: RequestParameters by lazy { RequestParameters(request) }

    val requestedContentType: RequestedContentType by lazy { RequestedContentType(this) }

    val requestContent: RequestContent by lazy { RequestContent(request) }

    val requestUrl: String by lazy {
        var requestUrl = request.requestURL.toString()
        val queryString = request.queryString
        if (queryString != null && queryString.trim().isNotEmpty()) {
            requestUrl += "?$queryString"
        }
        requestUrl
    }

    val url: URL by lazy { URL(requestUrl) }
}
