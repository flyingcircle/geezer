package org.geezer.layouts

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper

internal class HttpBufferedResponse(private val request: HttpServletRequest, response: HttpServletResponse?) : HttpServletResponseWrapper(response) {
    private var buffer: ByteArrayOutputStream? = null
    private var printWriter: PrintWriter? = null
    private var outputStream: ServletOutputStream? = null
    private var nonHtmlContent = false
    private var contentLength: Int? = null
    fun hasBufferedContent(): Boolean {
        return buffer != null && buffer!!.size() > 0
    }

    val isHtmlContent: Boolean
        get() = !nonHtmlContent

    val content: ByteArray?
        get() = buffer?.let {
            printWriter?.flush()
            it.toByteArray()
        }

    @Throws(IOException::class)
    fun pushContent() {
        buffer?.let {
            contentLength?.let {
                super.setContentLength(contentLength!!)
            }

            // If a PrintWriter is being used make sure all bytes have been pushed to our buffer
            printWriter?.flush()
            super.getOutputStream().write(content)
        }
    }

    override fun setContentLength(contentLength: Int) {
        // If this request ends up getting a layout, the actual Content-Length returned to the client will be different.
        this.contentLength = contentLength
    }

    override fun setContentType(contentType: String) {
        super.setContentType(contentType)
        nonHtmlContent = !isHtmlContent(contentType)
    }

    @Throws(IOException::class)
    override fun getWriter(): PrintWriter {
        if (printWriter == null) {
            if (inNonBufferState()) {
                printWriter = super.getWriter()
            } else {
                if (buffer == null) {
                    buffer = ByteArrayOutputStream()
                }
                printWriter = PrintWriter(OutputStreamWriter(buffer))
            }
        }
        return printWriter!!
    }

    @Throws(IOException::class)
    override fun getOutputStream(): ServletOutputStream {
        if (outputStream == null) {
            if (inNonBufferState()) {
                outputStream = super.getOutputStream()
            } else {
                buffer = ByteArrayOutputStream()
                outputStream = LayoutsOutputStream(buffer!!)
            }
        }
        return outputStream!!
    }

    private fun inNonBufferState(): Boolean {
        return nonHtmlContent || trueValue(request.getAttribute(NO_LAYOUT))
    }

    private class LayoutsOutputStream(private val outStream: OutputStream) : ServletOutputStream() {
        @Throws(IOException::class)
        override fun write(b: Int) {
            outStream.write(b)
        }

        @Throws(IOException::class)
        override fun write(buffer: ByteArray) {
            outStream.write(buffer)
        }

        @Throws(IOException::class)
        override fun write(buffer: ByteArray, offset: Int, length: Int) {
            outStream.write(buffer, offset, length)
        }

        override fun isReady(): Boolean {
            return true
        }

        override fun setWriteListener(writeListener: WriteListener) {}
    }
}
