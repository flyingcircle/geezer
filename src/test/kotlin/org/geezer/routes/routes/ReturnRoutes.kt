package org.geezer.routes.routes

import org.geezer.routes.RedirectTo
import org.geezer.routes.RenderPath
import org.geezer.routes.ReturnStatus
import java.io.ByteArrayInputStream
import java.io.InputStream

class ReturnRoutes {

    fun testReturnStatus(): String {
        throw ReturnStatus.NotFound404
    }

    fun testForward(): String {
        return "forward.jsp"
    }

    fun testThrowRender(): String {
        throw  RenderPath("render.jsp")
    }

    fun testRedirect(): String {
        return "redirect:/redirect"
    }

    fun testThrowRedirect(): String {
        throw RedirectTo("/thrown")
    }

    fun testStringContent(): String {
        return "string"
    }

    fun testBytesContent(): ByteArray {
        return "bytes".toByteArray()
    }

    fun testStreamContent(): InputStream {
        return ByteArrayInputStream("stream".toByteArray())
    }
}