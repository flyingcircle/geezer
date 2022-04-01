package org.geezer.routes

import java.io.IOException

object TestRoutes {
    fun test(abc: String?) {
        if (true) {
            throw IOException("BOOM")
        }
    }
}