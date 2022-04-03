package org.geezer.routes.routes

import org.geezer.routes.RequestContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

open class BaseOneRoutes {

    fun before(request: HttpServletRequest) {
        called.add("before")
    }

    fun get(context: RequestContext) {
        called.add("get")
    }

    fun after(response: HttpServletResponse) {
        called.add("after")
    }

    companion object {
        var called = mutableListOf<String>()
    }
}

object OneRoutes : BaseOneRoutes() {
    fun testNullParameter(id: Long, name: String?): String {
        called.add("testParameter")
        return "test"
    }
}