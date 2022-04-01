package org.geezer.routes.utils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.util.*
import javax.servlet.ServletOutputStream
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

class TestHttpServletResponse : HttpServletResponse {
    var outputStream = ByteArrayOutputStream()

    var httpWriter = PrintWriter(outputStream)

    var httpContentType: String? = null

    var contentLength: Int? = null

    var httpBufferSize = 0

    var cookies: MutableList<Cookie> = ArrayList()

    var responseCode: Int? = null

    var redirect: String? = null

    fun getContentAsString(): String {
        httpWriter.flush()
        return String(outputStream.toByteArray())
    }

    override fun getCharacterEncoding(): String {
        return "UTF-8"
    }

    override fun getContentType(): String? {
        return httpContentType
    }

    @Throws(IOException::class)
    override fun getOutputStream(): ServletOutputStream {
        return TestServletOutputStream(outputStream)
    }

    @Throws(IOException::class)
    override fun getWriter(): PrintWriter {
        return httpWriter
    }

    override fun setCharacterEncoding(charset: String?) {
        throw RuntimeException("Not implemented.")
    }

    override fun setContentLength(len: Int) {
        contentLength = len
    }

    override fun setContentLengthLong(p0: Long) {}

    override fun setContentType(type: String?) {
        httpContentType = type
    }

    override fun setBufferSize(size: Int) {
        httpBufferSize = size
    }

    override fun getBufferSize(): Int {
        return httpBufferSize
    }

    @Throws(IOException::class)
    override fun flushBuffer() {
        outputStream.flush()
    }

    override fun resetBuffer() {
        outputStream.reset()
    }

    override fun isCommitted(): Boolean {
        return false
    }

    override fun reset() {
        outputStream.reset()
    }

    override fun setLocale(loc: Locale?) {
        throw RuntimeException("Not implemented.")
    }

    override fun getLocale(): Locale? {
        throw RuntimeException("Not implemented.")
    }

    override fun addCookie(cookie: Cookie) {
        cookies.add(cookie)
    }

    override fun containsHeader(name: String?): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    override fun encodeURL(url: String?): String? {
        // TODO Auto-generated method stub
        return null
    }

    override fun encodeRedirectURL(url: String?): String? {
        throw RuntimeException("Not implemented.")
    }

    override fun encodeUrl(url: String?): String? {
        throw RuntimeException("Not implemented.")
    }

    override fun encodeRedirectUrl(url: String?): String? {
        throw RuntimeException("Not implemented.")
    }

    @Throws(IOException::class)
    override fun sendError(sc: Int, msg: String?) {
        responseCode = sc
    }

    @Throws(IOException::class)
    override fun sendError(sc: Int) {
        responseCode = sc
    }

    @Throws(IOException::class)
    override fun sendRedirect(location: String?) {
        redirect = location
    }

    override fun setDateHeader(name: String?, date: Long) {}

    override fun addDateHeader(name: String?, date: Long) {}

    override fun setHeader(name: String?, value: String?) {}

    override fun addHeader(name: String?, value: String?) {}

    override fun setIntHeader(name: String?, value: Int) {}

    override fun addIntHeader(name: String?, value: Int) {}

    override fun setStatus(sc: Int) {
        responseCode = sc
    }

    override fun setStatus(sc: Int, sm: String?) {
        responseCode = sc
    }

    override fun getStatus(): Int {
        return if (responseCode == null) 200 else responseCode!!
    }

    override fun getHeader(name: String?): String? = null

    override fun getHeaders(name: String?): Collection<String?>? = null

    override fun getHeaderNames(): Collection<String?>? = null
}