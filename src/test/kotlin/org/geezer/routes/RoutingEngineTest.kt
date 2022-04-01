package org.geezer.routes

import org.geezer.routes.routes.BaseOneRoutes
import org.geezer.routes.routes.OneRoutes
import org.geezer.routes.utils.TestHttpServletRequest
import org.geezer.routes.utils.TestHttpServletResponse
import kotlin.test.Test
import kotlin.test.assertTrue

class RoutingEngineTest {
    @Test
    fun testClassOne() {
        val table = RoutingTable(RoutesConfiguration()).context("/test", beforeRoute = BaseOneRoutes::before, afterRoute = BaseOneRoutes::after) {table ->
            table.add(BaseOneRoutes::get)
        }

        val engine = RoutingEngine(table)

        val invoked = engine.processRequest(TestHttpServletRequest("GET", "/", "/test"), TestHttpServletResponse())
        assertTrue(invoked)
        assertTrue(BaseOneRoutes.called.contains("before"))
        assertTrue(BaseOneRoutes.called.contains("get"))
        assertTrue(BaseOneRoutes.called.contains("after"))

        BaseOneRoutes.called.clear()
    }

    @Test
    fun testObjectOne() {
        val table = RoutingTable(RoutesConfiguration()).context("/test", beforeRoute = OneRoutes::before, afterRoute = OneRoutes::after) {table ->
            table.add(OneRoutes::get)
        }

        val engine = RoutingEngine(table)

        val invoked = engine.processRequest(TestHttpServletRequest("GET", "/", "/test"), TestHttpServletResponse())
        assertTrue(invoked)
        assertTrue(BaseOneRoutes.called.contains("before"))
        assertTrue(BaseOneRoutes.called.contains("get"))
        assertTrue(BaseOneRoutes.called.contains("after"))

        BaseOneRoutes.called.clear()
    }

}