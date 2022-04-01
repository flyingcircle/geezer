package org.geezer.routes

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RoutingServlet : HttpServlet() {

    override fun service(request: HttpServletRequest, response: HttpServletResponse) {
        if (!engine.processRequest(request, response)) {
            response.status = 404
        }
    }

    companion object {
        lateinit var engine: RoutingEngine
    }
}
