package org.geezer.routes

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class RoutingEngine(val table: RoutingTable) {
    fun processRequest(request: HttpServletRequest, response: HttpServletResponse): Boolean {
        val context = RequestContext(request, response)
        val routeNode = table.find(context) ?: return false

        routeNode.invoke(context)
        return true
    }
}
