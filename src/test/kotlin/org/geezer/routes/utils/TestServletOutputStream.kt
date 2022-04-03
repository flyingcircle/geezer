package org.geezer.routes.utils

import java.io.IOException
import java.io.OutputStream
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener

class TestServletOutputStream : ServletOutputStream {
    val outStream: OutputStream?

    constructor(outStream: OutputStream?) {
        this.outStream = outStream
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        outStream!!.write(b)
    }

    override fun isReady(): Boolean = true

    override fun setWriteListener(p0: WriteListener?) {}
}