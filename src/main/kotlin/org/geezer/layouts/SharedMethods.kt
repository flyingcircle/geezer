package org.geezer.layouts

fun isHtmlContent(mimeType: String?): Boolean {
    return mimeType == null ||
            mimeType.contains("text/html") ||
            mimeType.contains("application/xhtml") ||
            mimeType.contains("*/*")
}

fun trueValue(attribute: Any?): Boolean {
    return when (attribute) {
        null -> false
        is Boolean -> attribute
        is String -> java.lang.Boolean.parseBoolean(attribute as String?)
        else -> false
    }
}
