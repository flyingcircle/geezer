package org.geezer.routes.urls

/**
 * An object that can be accessed from a URL.
 */
interface UrlAddressable {

    val urlPath: String

    val redirectUrlPath: String
        get() = "redirect:$urlPath"

    val editUrlPath: String
        get() = "$urlPath/edit"

    val redirectEditUrlPath: String
        get() = "redirect:$editUrlPath"

    val deleteUrlPath: String
        get() = "$urlPath/delete"

    fun toUrl(): String = UrlGen.url(this)

    fun toEditUrl(): String = UrlGen.editUrl(this)

    fun toDeleteUrl(): String = UrlGen.deleteUrl(this)
}
