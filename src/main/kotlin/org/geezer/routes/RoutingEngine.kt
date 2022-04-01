package org.geezer.routes

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RoutingEngine(val table: RoutingTable) {
    fun processRequest(request: HttpServletRequest, response: HttpServletResponse): Boolean {
        val context = RequestContext(request, response)
        val routeNode = table.find(context) ?: return false

        routeNode.invoke(context)
        return true
    }
}
