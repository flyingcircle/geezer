package org.geezer.routes.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.security.Principal
import java.util.*
import jakarta.servlet.*
import jakarta.servlet.http.*

class TestHttpServletRequest : HttpServletRequest {
    var attributes: MutableMap<String, Any> = HashMap()

    var headers = mutableListOf<Pair<String, String>>()

    var parameters: MutableMap<String, Array<String>> = HashMap()

    var httpMethod = "GET"

    var hostName: String? = null

    var httpContextPath = "/"

    var requestUri: String? = null

    var httpContentType = "text/html"

    var httpContentLength = 0

    var servletInputStream: TestServletInputStream? = null

    var httpProtocol = "HTTP/1.1"

    var httpScheme = "http"

    var httpServerName = "localhost"

    var httpServerPort = 8080

    var requestDispatcher: TestRequestDispatcher? = null

    var session: TestHttpSession = TestHttpSession()

    var requestUrl: String? = null

    private val pathInfo: String? = null

    private val pathTranslated: String? = null

    var httpQueryString: String? = null

    private val servletPath: String? = null

    constructor() {}

    constructor(httpMethod: String, httpContextPath: String, requestUri: String?, vararg parameters: String) {
        this.httpMethod = httpMethod
        this.httpContextPath = httpContextPath
        this.requestUri = requestUri
        this.parameters = run {
            val p = parameters.asList().withIndex()
                .groupBy(keySelector = { it.index % 2 }, valueTransform = { it.value })
            if (p.isNotEmpty()) {
                p[0]!!.zip(p[1]!!.map { arrayOf(it) }).toMap().toMutableMap()
            } else {
                mutableMapOf()
            }
        }
        httpQueryString = parameters.asList().fold("") { b, it ->
            if (b.endsWith("=")) "$b${it}&" else "$b${it}=" }.dropLast(1)
    }

    override fun getAttribute(name: String): Any? {
        return attributes[name]
    }

    override fun getAttributeNames(): Enumeration<String>? {
        return Collections.enumeration(attributes.keys)
    }


    override fun getCharacterEncoding(): String? {
        return null
    }

    @Throws(UnsupportedEncodingException::class)
    override fun setCharacterEncoding(env: String?) {
    }

    override fun getContentLength(): Int {
        return httpContentLength
    }

    override fun getContentLengthLong(): Long = 0

    override fun getContentType(): String? {
        return httpContentType
    }

    @Throws(IOException::class)
    override fun getInputStream(): ServletInputStream? {
        return servletInputStream
    }

    override fun getParameter(name: String): String? {
        return if (parameters.containsKey(name)) parameters[name]!![0] else null
    }

    override fun getParameterNames(): Enumeration<String>? {
        return Collections.enumeration(parameters.keys)
    }

    override fun getParameterValues(name: String): Array<String>? {
        return parameters[name]
    }

    override fun getParameterMap(): Map<String, Array<String>>? {
        return HashMap(parameters)
    }

    override fun getProtocol(): String? {
        return httpProtocol
    }

    override fun getScheme(): String? {
        return httpScheme
    }

    override fun getServerName(): String {
        return httpServerName
    }

    override fun getServerPort(): Int {
        return httpServerPort
    }

    @Throws(IOException::class)
    override fun getReader(): BufferedReader? {
        return BufferedReader(InputStreamReader(servletInputStream))
    }

    override fun getRemoteAddr(): String? {
        throw RuntimeException("Not implemented.")
    }

    override fun getRemoteHost(): String? {
        throw RuntimeException("Not implemented.")
    }

    override fun setAttribute(name: String, value: Any) {
        attributes[name] = value
    }

    override fun removeAttribute(name: String) {
        attributes.remove(name)
    }

    override fun getLocale(): Locale? {
        throw RuntimeException("Not implemented.")
    }

    override fun getLocales(): Enumeration<Locale?>? {
        throw RuntimeException("Not implemented.")
    }

    override fun isSecure(): Boolean {
        return httpScheme.equals("https", ignoreCase = true)
    }

    override fun getRequestDispatcher(path: String): RequestDispatcher? {
        requestDispatcher = TestRequestDispatcher(path)
        return requestDispatcher
    }

    override fun getRealPath(path: String?): String? {
        throw RuntimeException("Not implemented.")
    }

    override fun getRemotePort(): Int {
        throw RuntimeException("Not implemented.")
    }

    override fun getLocalName(): String? {
        throw RuntimeException("Not implemented.")
    }

    override fun getLocalAddr(): String? {
        throw RuntimeException("Not implemented.")
    }

    override fun getLocalPort(): Int {
        throw RuntimeException("Not implemented.")
    }

    override fun getServletContext(): ServletContext? = null

    override fun startAsync(): AsyncContext? = null

    override fun startAsync(p0: ServletRequest?, p1: ServletResponse?): AsyncContext? = null

    override fun isAsyncStarted(): Boolean = false

    override fun isAsyncSupported(): Boolean = false

    override fun getAsyncContext(): AsyncContext? = null

    override fun getDispatcherType(): DispatcherType? = null

    override fun getAuthType(): String? {
        throw RuntimeException("Not implemented.")
    }

    override fun getCookies(): Array<Cookie?>? {
        throw RuntimeException("Not implemented.")
    }

    override fun getDateHeader(name: String?): Long {
        throw RuntimeException("Not implemented.")
    }

    override fun getHeader(name: String?): String? {
        return headers.firstOrNull { it.first.equals(name, true) }?.second
    }

    override fun getHeaders(name: String?): Enumeration<String> {
        val headersValues = headers.firstOrNull { it.first.equals(name, true) }?.let { listOf(it) } ?: listOf()
        return object : Enumeration<String> {
            var count = 0

            override fun hasMoreElements(): Boolean {
                return this.count < headersValues.size
            }

            override fun nextElement(): String {
                if (this.count < headersValues.size) {
                    return headersValues[this.count++].first
                }
                throw NoSuchElementException("List enumeration asked for more elements than present")
            }
        }
    }

    override fun getHeaderNames(): Enumeration<String> {
        return object : Enumeration<String> {
            var count = 0

            override fun hasMoreElements(): Boolean {
                return this.count < headers.size
            }

            override fun nextElement(): String {
                if (this.count < headers.size) {
                    return headers[this.count++].first
                }
                throw NoSuchElementException("List enumeration asked for more elements than present")
            }
        }
    }

    override fun getIntHeader(name: String?): Int {
        return getHeader(name)?.toIntOrNull() ?: -1
    }

    override fun getMethod(): String? {
        return httpMethod
    }

    override fun getPathInfo(): String? {
        return pathInfo
    }

    override fun getPathTranslated(): String? {
        return pathTranslated
    }

    override fun getContextPath(): String? {
        return httpContextPath
    }

    override fun getQueryString(): String? {
        return httpQueryString
    }

    override fun getRemoteUser(): String? {
        // TODO Auto-generated httpMethod stub
        return null
    }

    override fun isUserInRole(role: String?): Boolean {
        throw RuntimeException("Not implemented.")
    }

    override fun getUserPrincipal(): Principal? {
        throw RuntimeException("Not implemented.")
    }

    override fun getRequestedSessionId(): String? {
        throw RuntimeException("Not implemented.")
    }

    override fun getRequestURI(): String? {
        return requestUri
    }

    override fun getRequestURL(): StringBuffer? {
        return StringBuffer(requestUrl)
    }

    override fun getServletPath(): String? {
        return servletPath
    }

    override fun getSession(create: Boolean): HttpSession? {
        return session
    }

    override fun getSession(): HttpSession? {
        return session
    }

    override fun changeSessionId(): String {
        TODO("not implemented")
    }

    override fun isRequestedSessionIdValid(): Boolean {
        return true
    }

    override fun isRequestedSessionIdFromCookie(): Boolean {
        return true
    }

    override fun isRequestedSessionIdFromURL(): Boolean {
        return false
    }

    override fun isRequestedSessionIdFromUrl(): Boolean {
        return false
    }

    override fun authenticate(p0: HttpServletResponse?): Boolean {
        TODO("not implemented")
    }

    override fun login(p0: String?, p1: String?) {
        TODO("not implemented")
    }

    override fun logout() {
        TODO("not implemented")
    }

    override fun getParts(): MutableCollection<Part> {
        TODO("not implemented")
    }

    override fun getPart(p0: String?): Part {
        TODO("not implemented")
    }

    override fun <T : HttpUpgradeHandler?> upgrade(p0: Class<T>?): T {
        TODO("not implemented")
    }
}