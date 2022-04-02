package org.geezer.layouts

import java.io.IOException
import java.io.OutputStream
import javax.servlet.ServletResponse
import javax.servlet.jsp.PageContext

/**
 * The content of the view. This object can be accessed in layouts using the `HttpServletRequest`
 * attribute [VIEW]. The Expression Language syntax for this is:
 * ${view.yield(pageContext)}
 */
class View(private val content: ByteArray, private val response: ServletResponse) {

    /**
     * Yield the content of the <head> tag. Same as:
     * yield("head", pageContext)
     *
     * @param pageContext The PageContext of the layout JSP file.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun yieldHead(pageContext: PageContext) {
        yield("head", pageContext)
    }

    /**
     * Yield the content of the <body> tag. Same as:
     * yield("body", pageContext)
     *
     * @param pageContext The PageContext of the layout JSP file.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun yieldBody(pageContext: PageContext) {
        yield("body", pageContext)
    }

    /**
     * Yield the content of the <footer> tag. Same as:
     * yield("footer", pageContext)
     *
     * @param pageContext The PageContext of the layout JSP file.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun yieldFooter(pageContext: PageContext) {
        yield("footer", pageContext)
    }

    /**
     * Yield the entire content of the view.
     *
     * @param pageContext The PageContext of the layout JSP file.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun yield(pageContext: PageContext) {
        pageContext.out.flush()
        response.outputStream.write(content)
    }

    /**
     * Yield the content of the outermost tag <tagName>.
     *
     * @param tagName The name of the outermost tag to yield the content of.
     * @param pageContext The PageContext of the layout JSP file.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun yield(tagName: String, pageContext: PageContext) {
        /*
     * TODO JSPs using a different encoding then the system default will break here if the values for the characters in the opening
     * or closing tags are different. Not sure how to grab the encoding from the JSP that created the current content automatically.
     * Maybe add a filter parameter to specify the encoding if it's different then default.
     */
        val openTag = makeOpenTag(tagName)
        val openTagIndex = indexOf(content, openTag)
        if (openTagIndex >= 0) {
            val closedTag = makeCloseTag(tagName)
            val closeTagIndex = lastIndexOf(content, closedTag)
            if (closeTagIndex > openTagIndex) {
                val startIndex = openTagIndex + openTag.length
                val length = closeTagIndex - startIndex
                /*
                 * We're mixing the JSPWriter and the ServletOutStream here because we don't want to take the hit to turn
                 * content back into a String. Need to make sure everything written to JSPWriter to this point is flushed so
                 * the content doesn't get out of order.
                 */
                pageContext.out.flush()
                val out: OutputStream = response.outputStream
                out.write(content, startIndex, length)
                out.flush()
            }
        }
    }

    private fun makeCloseTag(tagName: String): String = "</$tagName>"

    private fun makeOpenTag(tagName: String): String = "<$tagName>"

    /**
     * Is the tag name defined in the current content?.
     *
     * @param tagName The name of the outermost tag to yield the content of.
     * @return `true` if the given tag name is found in the current page content.
     * @throws IOException
     */
    operator fun contains(tagName: String): Boolean {
        /*
     * TODO JSPs using a different encoding then the system default will break here if the values for the characters in the opening
     * or closing tags are different. Not sure how to grab the encoding from the JSP that created the current content automatically.
     * Maybe add a filter parameter to specify the encoding if it's different then default.
     */
        val openTag = makeOpenTag(tagName)
        val openTagIndex = indexOf(content, openTag)
        return if (openTagIndex >= 0) {
            val closedTag = makeCloseTag(tagName)
            val closeTagIndex = lastIndexOf(content, closedTag)
            closeTagIndex > openTagIndex
        } else {
            false
        }
    }
}

fun indexOf(data: ByteArray, pattern: String): Int {
    var matchIndex = 0
    val finalIndex = pattern.length - 1
    for (i in data.indices) {
        if (data[i].toInt().toChar() == pattern[matchIndex]) {
            if (matchIndex == finalIndex) {
                return i - finalIndex
            } else {
                ++matchIndex
            }
        } else {
            matchIndex = 0
        }
    }
    return -1
}

fun lastIndexOf(data: ByteArray, pattern: String): Int {
    val startMatchIndex = pattern.length - 1
    var matchIndex = startMatchIndex
    for (i in data.indices.reversed()) {
        if (data[i].toInt().toChar() == pattern[matchIndex]) {
            if (matchIndex == 0) {
                return i
            } else {
                --matchIndex
            }
        } else {
            matchIndex = startMatchIndex
        }
    }
    return -1
}
