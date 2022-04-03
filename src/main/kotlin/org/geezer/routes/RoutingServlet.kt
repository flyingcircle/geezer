package org.geezer.routes

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

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
