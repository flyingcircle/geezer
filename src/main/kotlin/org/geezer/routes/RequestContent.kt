package org.geezer.routes

import java.io.InputStream
import jakarta.servlet.http.HttpServletRequest

class RequestContent(request: HttpServletRequest) {
    var contentType: String?

    val contentLength: Int?

    val inputStream: InputStream
        get() = inputStreamProvider()

    val bytes: ByteArray by lazy { inputStream.readAllBytes() }

    val text: String by lazy { bytes.decodeToString() }

    private val request: HttpServletRequest? = request

    private val inputStreamProvider: (() -> InputStream)

    init {
        contentType = request.getHeader("Content-Type")
        contentLength = request.getHeader("Content-Length")?.toIntOrNull()
        inputStreamProvider = { request.inputStream }
    }
}
