package org.geezer.routes

object MIMETypes {
    const val ATOM = "application/atomcat+xml"
    const val CSV = "text/csv"
    const val EXCEL = "application/vnd.ms-excel"
    const val EXCEL2 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    const val EXCEL3 = "application/msexcel"

    const val EXCEL4 = "application/xls"

    const val EXCEL5 = "application/x-xls"

    const val EXCEL6 = "application/x-ms-excel"

    const val EXCEL7 = "application/x-excel"

    const val HTML = "text/html"

    const val HTML2 = "application/xhtml+xml"

    const val ICS = "text/calendar"

    const val JAVASCRIPT = "application/javascript"

    const val JAVASCRIPT2 = "application/x-javascript"

    const val JAVASCRIPT3 = "text/javascript"

    const val JSON = "application/json"

    const val JSON2 = "text/x-json"

    const val PDF = "application/pdf"

    const val RSS = "application/rss+xml"

    const val TEXT = "text/plain"

    const val WORD = "application/msword"

    const val WORD2 = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

    const val XML = "application/xml"

    const val XML2 = "text/xml"

    const val CSS = "text/css"

    fun getMimeTypeFromFileExtension(extension: String): String? {
        when (extension.lowercase()) {
            "csv" -> return CSV
            "xlsx", "xls" -> return EXCEL
            "html", "htm", "htmls" -> return HTML
            "ics" -> return ICS
            "js" -> return JAVASCRIPT
            "json" -> return JSON
            "pdf" -> return PDF
            "rss" -> return RSS
            "text", "txt" -> return TEXT
            "doc", "docx" -> return WORD
            "xml" -> return XML
            "css" -> return CSS
            "atom" -> return ATOM
        }
        return null
    }
}
