package org.geezer.layouts

import org.geezer.layouts.SharedMethods.isHtmlContent
import javax.servlet.http.HttpServletRequest

/**
 * By default, a request will be a candidate for a layout unless one the following conditions is met:
 * 1. The HTTP header _Accept_ is set with a non-HTML mime type.
 * 2. The URL ends in a known, non-HTML file extension such as _css_, _js_, or _png_.
 * 3. The HTTP header _X-Requested-With_ is set with the value _XMLHttpRequest_ (Ajax request).
 */
class DefaultHtmlPageDecider : UseLayoutDecider {
    override fun isCandidateForLayout(request: HttpServletRequest?): Boolean {
        return isHtmlRequest(request) && !isAjaxRequest(request)
    }

    private fun isHtmlRequest(request: HttpServletRequest?): Boolean {
        return isHtmlContent(request!!.getHeader("Accept")) && !knownNonHtmlFile(request.requestURI)
    }

    private fun isAjaxRequest(request: HttpServletRequest?): Boolean {
        return "XMLHttpRequest".equals(request!!.getHeader("X-Requested-With"), ignoreCase = true)
    }
}

fun knownNonHtmlFile(uri: String): Boolean {
    val matchResult = fileTypeRegex.find(uri)
    return when (val fileType = matchResult?.groups?.get(1)) {
        is MatchGroup -> knownNonHtmlFileExtensions.contains(fileType.value)
        else -> false
    }
}

private val fileTypeRegex = "\\.(\\w+)$".toRegex()

private val knownNonHtmlFileExtensions = listOf(
    "ace", "aif", "ani", "api", "art", "asc", "asm",
    "asp", "avi", "bak", "bas", "bat", "bfc", "bin", "bin", "bmp", "bud", "bz2", "c", "cat",
    "cbl", "cbt", "cda", "cdt", "cgi", "class", "clp", "cmd", "cmf", "com", "cpl", "cpp",
    "css", "csv", "cur", "dao", "dat", "dd", "deb", "dev", "dic", "dir", "dll", "doc", "docx",
    "dot", "drv", "ds", "dun", "dwg", "dxf", "emf", "eml", "eps", "eps2", "exe", "ffl", "ffo",
    "fla", "fnt", "gid", "gif", "grp", "gz", "hex", "hlp", "hqx", "ht", "icl", "icm", "ico",
    "inf", "ini", "jar", "jpeg", "jpg", "js", "lab", "lgo", "lit", "lnk", "log", "lsp", "maq",
    "mar", "mdb", "mdl", "mid", "mod", "mov", "mp3", "mpeg", "mpp", "msg", "msg", "ncf", "nlm",
    "o", "ocx", "ogg", "ost", "pak", "pcl", "pct", "pdf", "pdf", "pdr", "pif", "pif", "pif",
    "pl", "pm", "pm3", "pm4", "pm5", "pm6", "png", "pol", "pot", "ppd", "pps", "ppt", "prn",
    "ps", "psd", "psp", "pst", "pub", "qif", "ram", "rar", "raw", "rdo", "reg", "rm", "rpm",
    "rsc", "rtf", "s pwl", "scr", "sea", "sh", "sit", "smd", "svg", "swf", "swp", "sys", "tar",
    "tga", "tiff", "tmp", "ttf", "txt", "udf", "uue", "vbx", "vm", "vxd", "wav", "wmf", "wri",
    "wsz", "xcf", "xif", "xif", "xif", "xls", "xlsx", "xlt", "xml", "xsl", "zip"
)
