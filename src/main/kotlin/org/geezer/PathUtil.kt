package org.geezer

import arrow.typeclasses.Monoid

object PathUtil {

    fun normalizePath(path: String): String {
        val startSlash = if (!path.startsWith("/")) '/' else ""
        val endSlash = if (!path.endsWith("/")) '/' else ""
        return "$startSlash$path$endSlash"
    }
}

data class Path(val value: String) {
    companion object {
        object PathMonoid: Monoid<Path> {
            override fun empty() = Path("")

            override fun Path.combine(b: Path): Path {
                return Path("${this.value.removeSuffix("/")}/${b.value.removePrefix("/")}")
            }

        }
    }
}