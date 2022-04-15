package org.geezer

object PathUtil {

    fun normalizePath(path: String): String {
        val startSlash = if (!path.startsWith("/")) '/' else ""
        val endSlash = if (!path.endsWith("/")) '/' else ""
        return "$startSlash$path$endSlash"
    }
}