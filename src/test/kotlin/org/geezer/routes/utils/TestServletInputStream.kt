package org.geezer.routes.utils

import java.io.IOException
import java.io.InputStream
import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream

class TestServletInputStream : ServletInputStream {
    private var source: InputStream? = null

    constructor(source: InputStream?) {
        this.source = source
    }

    @Throws(IOException::class)
    override fun read(): Int {
        return source!!.read()
    }

    override fun isFinished(): Boolean = false

    override fun isReady(): Boolean = true

    override fun setReadListener(p0: ReadListener?) {}
}