package org.geezer.routes.urls

import org.apache.commons.codec.binary.Base64
import org.geezer.routes.RoutingTable
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Collections
import jakarta.servlet.http.HttpServletRequest
import kotlin.reflect.KFunction

/**
 * The URL Generator. This classes in used various JSPs and classes to generate URLS back to the system.
 * It's used for two reasons:
 *
 * 1. For assets within the web application the [Constants.VERSION] number is used as a parameter to reload
 *    browser cache on version update.
 * 2. There are two dynamic session attributes within the web application that affects many/most of the analytics viewed:
 *    selected applications and date range. If possible we'd like for a user to be able to
 *    copy any URL within the web application (from the browser) send it to another user and,
 *    assuming this other user has the same level access, view the exact same data.
 *    To accomplish this we need to make sure that these dynamic session attributes show up in all (GET) urls.
 */
object UrlGen {

    private lateinit var rootUrlOrPath: String

    private lateinit var routingTables: List<RoutingTable>

    private val assetHashPath = Collections.synchronizedMap(mutableMapOf<String, String>())

    var cacheAssets = true

    var cacheSeconds = 100 * 24 * 60 * 60

    var cssPath = "/assets/css"

    var jsPath = "/assets/js"

    var imagePath = "/assets/img"

    fun initialize(rootUrlOrPath: String, routingTable: RoutingTable, vararg routingTables: RoutingTable) {
        this.rootUrlOrPath = rootUrlOrPath.removeSuffix("/")
        this.routingTables = listOf(routingTable, *routingTables)
    }

    @JvmStatic
    fun refererUrl(request: HttpServletRequest, defaultRouteFunction: KFunction<*>): String {
        val referer = request.getHeader("Referer")
        return if (!referer.isNullOrBlank()) {
            referer
        } else {
            url(defaultRouteFunction)
        }
    }

    @JvmStatic
    fun url(routeFunction: KFunction<*>): String {
        val urlPath: String = routingTables.firstOrNull {
            it[routeFunction] != null }?.get(routeFunction) ?:
            throw IllegalArgumentException("Route function $routeFunction has not been added.")
        return url(urlPath)
    }

    @JvmStatic
    fun url(routeFunction: KFunction<*>, parameters: List<Any>): String {
        val routeSegments: List<String> = routingTables.firstOrNull {
            it[routeFunction] != null }?.get(routeFunction)
                ?.split("/")
                ?.filter { it.isNotBlank() } ?:
                throw IllegalArgumentException("Route function $routeFunction has not been added.")

        var parameterIndex = 0

        val urlSegments = routeSegments.map {
            when (it) {
                "{}", "*", "**" -> {
                    if (parameterIndex < parameters.size) {
                        parameters[parameterIndex++].toString()
                    } else {
                        it
                    }
                }
                else -> it
            }
        }

        return url("/${urlSegments.joinToString("/")}")
    }

    @JvmStatic
    fun url(urlAddressable: UrlAddressable) = url(urlAddressable.urlPath)

    @JvmStatic
    fun editUrl(urlAddressable: UrlAddressable) = url(urlAddressable.editUrlPath)

    @JvmStatic
    fun deleteUrl(urlAddressable: UrlAddressable) = url(urlAddressable.deleteUrlPath)

    @JvmStatic
    fun cssUrl(cssRelativePath: String, request: HttpServletRequest): String =
        assetUrl("$cssPath/$cssRelativePath", request)

    @JvmStatic
    fun jsUrl(jsRelativePath: String, request: HttpServletRequest): String =
        assetUrl("$jsPath/$jsRelativePath", request)

    @JvmStatic
    fun imgUrl(imageRelativePath: String, request: HttpServletRequest): String =
        assetUrl("$imagePath/$imageRelativePath", request)

    @JvmStatic
    fun assetUrl(assetPath: String, request: HttpServletRequest): String =
        url(if (cacheAssets) {
            assetHashPath.getOrPut(assetPath) {
                val assetFile = File(request.session.servletContext.getRealPath(assetPath))
                if (assetFile.isFile) {
                    var hashedPath = assetPath
                    val index = hashedPath.lastIndexOf('.')
                    if (index > 0) {
                        hashedPath = "${hashedPath.substring(0, index)}--${fileChecksum(assetFile)}${hashedPath.substring(index)}"
                    }
                    hashedPath
                } else {
                    assetPath
                }
            }
        } else {
            assetPath
        })

    @JvmStatic
    fun url(urlOrPath: String): String {
        return if (urlOrPath.lowercase().startsWith("http")) {
            urlOrPath
        } else {
            val leadingSlash = if (!urlOrPath.startsWith("/")) "/" else ""
            "$rootUrlOrPath$leadingSlash$urlOrPath"
        }
    }

    @Throws(NoSuchAlgorithmException::class, IOException::class)
    private fun fileChecksum(file: File): String = Base64.encodeBase64URLSafeString(
        MessageDigest.getInstance("MD5")
        .digest(Files.readAllBytes(file.toPath())))
}
