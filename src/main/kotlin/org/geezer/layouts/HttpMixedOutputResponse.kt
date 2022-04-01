package org.geezer.layouts

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

/*
 * A HttpServletResponse that backs the PrintWriter with the ServletOutputStream.
 * This allows the binary content of the view to be written to the output stream
 * and the JSP content of the layout to be written to the PrintWriter.
 */
internal class HttpMixedOutputResponse(response: HttpServletResponse) : HttpServletResponseWrapper(response) {
    private val outputStream: OutputStream
    private var printWriter: PrintWriter? = null
    override fun getWriter(): PrintWriter? {
        if (printWriter == null) {
            printWriter = PrintWriter(OutputStreamWriter(outputStream))
        }
        return printWriter
    }

    init {
        outputStream = response.outputStream
    }
}
