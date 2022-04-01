package org.geezer.routes.routes

import org.geezer.routes.RequestContext
import org.geezer.routes.RequestParameters
import org.geezer.routes.RequestPath
import org.geezer.routes.RequestedContentType
import java.net.URL
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

object Filters {
    var _beforeCalled: Boolean = false

    val wasBeforeCalled: Boolean
        get() = _beforeCalled.let { _beforeCalled = false; it }

    var _afterCalled: Boolean = false

    val wasAfterCalled: Boolean
        get() = _afterCalled.let { _afterCalled = false; it }

    @Suppress("UNUSED_PARAMETER")
    fun before(
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
        if (_beforeCalled) {
            throw IllegalStateException("Before was already called.")
        }
        _beforeCalled = true
    }

    @Suppress("UNUSED_PARAMETER")
    fun after(
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
        if (_afterCalled) {
            throw IllegalStateException("After was already called.")
        }
        _afterCalled = true
    }

}