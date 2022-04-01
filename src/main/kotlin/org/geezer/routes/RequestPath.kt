package org.geezer.routes

import java.net.URLDecoder
import javax.servlet.http.HttpServletRequest

class RequestPath {
    val segments: List<String>

    private val path: String

    /**
     * @return The number of segments in this path.
     */
    val size: Int
        get() {
            return segments.size
        }

    /**
     * @return The file name of the path or null if this path does not contain a file name at the end.
     */
    val fileName: String?
        get() {
            return if (segments.isEmpty()) {
                null
            } else {
                val fileNameCandidate = segments[segments.size - 1]
                val lastIndex = fileNameCandidate.lastIndexOf('.')
                if (lastIndex > 0 && lastIndex < fileNameCandidate.length) fileNameCandidate else null
            }
        }

    /**
     * @return The file name extension or null if the path does contain a file name.
     */
    val fileExtension: String?
        get() {
            val fileName = fileName
            return fileName?.substring(fileName.lastIndexOf('.') + 1, fileName.length)
        }

    constructor(servletRequest: HttpServletRequest) : this(parseUrlSegments(servletRequest.requestURI, servletRequest.contextPath))

    constructor(path: String) : this(parseUrlSegments(path, ""))

    constructor(segments: List<String>) {
        this.segments = segments
        val pathBuilder = StringBuilder()
        for (i in segments.indices) {
            pathBuilder.append('/').append(segments[i])
        }
        path = pathBuilder.toString()
    }

    @Throws(IndexOutOfBoundsException::class)
    operator fun get(index: Int): String {
        return segments[index]
    }

    fun getOrNull(index: Int): String? = segments.getOrNull(index)

    fun getChar(index: Int): Char? = segments.getOrNull(index)?.let { if (it.isNotEmpty()) it[0] else null }

    fun getBoolean(index: Int): Boolean? = segments.getOrNull(index)?.toBoolean()

    fun getByte(index: Int): Byte? = segments.getOrNull(index)?.toByteOrNull()

    fun getShort(index: Int): Short? = segments.getOrNull(index)?.toShortOrNull()

    fun getInt(index: Int): Int? = segments.getOrNull(index)?.toIntOrNull()

    fun getLong(index: Int): Long? = segments.getOrNull(index)?.toLongOrNull()

    fun getFloat(index: Int): Float? = segments.getOrNull(index)?.toFloatOrNull()

    fun getDouble(index: Int): Double? = segments.getOrNull(index)?.toDoubleOrNull()

    fun startsWith(path: String): Boolean = startsWith(parseUrlSegments(path))

    fun startsWith(segments: List<String>): Boolean {
        return if (segments.size > this.segments.size) {
            false
        } else {
            for (i in segments.indices) {
                if (segments[i] != this.segments[i]) {
                    return false
                }
            }
            true
        }
    }

    /**
     * @param numberSegments The number of segments to pop from this path.
     * @return A new RequestPath with the given number of segments removed from the head of the path.
     * @throws IndexOutOfBoundsException If numberSegments < 0 or numberSegments >= [.size].
     */
    @Throws(IndexOutOfBoundsException::class)
    fun pop(numberSegments: Int = 1): RequestPath {
        return RequestPath(segments.subList(numberSegments, segments.size))
    }

    @Throws(IndexOutOfBoundsException::class)
    fun substring(index: Int): RequestPath {
        return RequestPath(path.substring(index))
    }

    override fun equals(other: Any?): Boolean = other is RequestPath && other.segments == segments

    override fun toString(): String = path

    override fun hashCode(): Int = segments.hashCode()

    companion object {
        fun parseUrlSegments(url: String, contextPath: String = ""): List<String> {
            var url = url
            if (url.startsWith(contextPath)) {
                url = url.substring(contextPath.length, url.length)
            }
            url = URLDecoder.decode(url, "UTF-8").trim()
            val urlSegments: MutableList<String> = ArrayList()
            if (url.startsWith("/")) {
                url = url.substring(1, url.length)
            }
            var index = url.indexOf('/')
            while (index != -1) {
                urlSegments.add(url.substring(0, index))
                url = url.substring(index + 1, url.length)
                index = url.indexOf('/')
            }
            if (url.isNotEmpty()) {
                urlSegments.add(url)
            }
            return urlSegments
        }
    }
}
