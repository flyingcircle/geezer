package org.geezer.layouts

internal object SharedMethods {
    @JvmStatic
    fun isHtmlContent(mimeType: String?): Boolean {
        return mimeType == null || mimeType.contains("text/html") || mimeType.contains("application/xhtml") || mimeType.contains("*/*")
    }

    @JvmStatic
    fun trueValue(attribute: Any?): Boolean {
        return when (attribute) {
            null -> false
            is Boolean -> attribute
            is String -> java.lang.Boolean.parseBoolean(attribute as String?)
            else -> false
        }
    }
}
