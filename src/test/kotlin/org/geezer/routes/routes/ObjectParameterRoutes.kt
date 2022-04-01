package org.geezer.routes.routes

import org.geezer.routes.RequestContext
import org.geezer.routes.RequestParameters
import org.geezer.routes.RequestPath
import org.geezer.routes.RequestedContentType
import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

object ObjectParameterRoutes {
    var called = false
    var context: RequestContext? = null
    var request: HttpServletRequest? = null
    var response: HttpServletResponse? = null
    var session: HttpSession? = null
    var parameters: RequestParameters? = null
    var parameterMap: Map<String, List<String>>? = null
    var contentType: RequestedContentType? = null
    var url: URL? = null
    var path: RequestPath? = null

    fun get(
        context: RequestContext,
        request: HttpServletRequest,
        response: HttpServletResponse,
        session: HttpSession,
        parameters: RequestParameters,
        parameterMap: Map<String, List<String>>,
        contentType: RequestedContentType,
        url: URL,
        path: RequestPath
    ) {
        called = true
        this.context = context
        this.request = request
        this.response = response
        this.session = session
        this.parameters = parameters
        this.parameterMap = parameterMap
        this.contentType = contentType
        this.url = url
        this.path = path
    }

    fun reset() {
        called = false
        this.context = null
        this.request = null
        this.response = null
        this.session = null
        this.parameters = null
        this.parameterMap = null
        this.contentType = null
        this.url = null
        this.path = null
    }
}