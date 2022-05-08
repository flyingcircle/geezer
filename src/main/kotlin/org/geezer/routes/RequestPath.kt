package org.geezer.routes

import java.net.URLDecoder
import jakarta.servlet.http.HttpServletRequest

class RequestPath {
    val segments: List<String>

    private val path: String

    /**
     * @return The number of segments in this path.
     */
    val size: Int
        get() = segments.size

    /**
     * @return The file name of the path or empty string.
     */
    val fileName: String
        get() {
            return if (segments.isEmpty()) {
                ""
            } else {
                val fileNameCandidate = segments.last()
                if (fileNameCandidate.contains('.')) fileNameCandidate else ""
            }
        }

    /**
     * @return The file name extension or empty string.
     */
    val fileExtension: String
        get() = fileName.substring(fileName.lastIndexOf('.') + 1)

    constructor(servletRequest: HttpServletRequest) : this(parseUrlSegments(servletRequest.requestURI, servletRequest.contextPath))

    constructor(path: String) : this(parseUrlSegments(path, ""))

    constructor(segments: List<String>) {
        this.segments = segments
        path = "/${segments.joinToString("/")}"
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
            segments.withIndex().all { (i, seg) -> seg == this.segments[i] }
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
            return URLDecoder.decode(url.removePrefix(contextPath), "UTF-8")
                .trim()
                .removePrefix("/")
                .split('/')
                .filter { it.isNotEmpty() }
        }
    }
}
