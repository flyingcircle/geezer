package org.geezer.routes

import org.geezer.routes.routes.BaseOneRoutes
import org.geezer.routes.routes.OneRoutes
import org.geezer.routes.utils.TestHttpServletRequest
import org.geezer.routes.utils.TestHttpServletResponse
import kotlin.test.Test
import kotlin.test.assertNotNull

class RoutingTableTest {
    @Test
    fun testClass() {
        val table = RoutingTable(RoutesConfiguration()).context("/test", beforeRoute = BaseOneRoutes::before, afterRoute = BaseOneRoutes::after) {table ->
            table.add(BaseOneRoutes::get)
        }

        val routeNode = table.find(RequestContext(TestHttpServletRequest("GET", "/", "/test"), TestHttpServletResponse()))
        assertNotNull(routeNode)
    }

    @Test
    fun testObject() {
        val table = RoutingTable(RoutesConfiguration()).context("/test", beforeRoute = OneRoutes::before, afterRoute = OneRoutes::after) {table ->
            table.add(OneRoutes::get)
        }

        val routeNode = table.find(RequestContext(TestHttpServletRequest("GET", "/", "/test"), TestHttpServletResponse()))
        assertNotNull(routeNode)
    }

    @Test
    fun testNullParameter() {
        var table = RoutingTable(RoutesConfiguration()) { table ->
            table.add("/{}?(name={})?", OneRoutes::testNullParameter)
        }

        var routeNode = table.find(RequestContext(TestHttpServletRequest("GET", "/", "/1"), TestHttpServletResponse()))
        assertNotNull(routeNode)

        routeNode = table.find(RequestContext(TestHttpServletRequest("GET", "/", "/1", "name", "test"), TestHttpServletResponse()))
        assertNotNull(routeNode)

        table = RoutingTable(RoutesConfiguration()) { table ->
            table.add("/{}?name={}", OneRoutes::testNullParameter)
        }

        routeNode = table.find(RequestContext(TestHttpServletRequest("GET", "/", "/1"), TestHttpServletResponse()))
        assertNotNull(routeNode)

        routeNode = table.find(RequestContext(TestHttpServletRequest("GET", "/", "/1", "name", "test"), TestHttpServletResponse()))
        assertNotNull(routeNode)
    }

}