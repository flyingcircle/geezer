package org.geezer.layouts

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper

/*
 * A HttpServletResponse that backs the PrintWriter with the ServletOutputStream.
 * This allows the binary content of the view to be written to the output stream
 * and the JSP content of the layout to be written to the PrintWriter.
 */
internal class HttpMixedOutputResponse(response: HttpServletResponse) : HttpServletResponseWrapper(response) {
    private val outputStream: OutputStream
    private val printWriter: PrintWriter

    override fun getWriter(): PrintWriter = printWriter

    init {
        outputStream = response.outputStream
        printWriter = PrintWriter(OutputStreamWriter(outputStream))
    }
}
