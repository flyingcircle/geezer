package org.geezer.routes.utils

import java.io.IOException
import javax.servlet.RequestDispatcher
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class TestRequestDispatcher : RequestDispatcher {
    val path: String

    var forwardCalled = false

    var includeCalled = false

    var servletExceptionToThrow: ServletException? = null

    var ioExceptionToThrow: IOException? = null

    constructor(path: String) {
        this.path = path
    }

    @Throws(ServletException::class, IOException::class)
    override fun forward(request: ServletRequest?, response: ServletResponse?) {
        forwardCalled = true
        if (servletExceptionToThrow != null) {
            throw servletExceptionToThrow!!
        } else if (ioExceptionToThrow != null) {
            throw ioExceptionToThrow!!
        }
    }

    @Throws(ServletException::class, IOException::class)
    override fun include(request: ServletRequest?, response: ServletResponse?) {
        includeCalled = true
        if (servletExceptionToThrow != null) {
            throw servletExceptionToThrow!!
        } else if (ioExceptionToThrow != null) {
            throw ioExceptionToThrow!!
        }
    }
}