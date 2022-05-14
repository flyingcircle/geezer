package org.geezer.routes

import java.util.concurrent.ConcurrentHashMap

class RoutesConfiguration {
    /**
     * The global path prepended to all route paths. For example if you want your route objects to process request that
     * start with "/api", set this variable to "/api" and then all route class paths will get prepended with this value.
     */
    var rootPath: String = ""

    /**
     * The root directory prepended to all file resource forward paths.
     */
    var rootForwardPath: String = "/WEB-INF/jsps"

    /**
     * Are route path and parameter matching case-insensitive?
     */
    var ignoreCase = false

    /**
     * Default content type returned by all routes if not overridden. This can be overridden in [Routes.defaultContentType],
     * [RouteNode.contentType], or by explicitly calling [jakarta.servlet.http.HttpServletResponse.setContentType] in the
     * route method.
     */
    var defaultContentType: String = ""

    /**
     * If true, strings returned from route methods are sent back as the content. If false, returned strings are interpreted as
     * file paths that the request is forwarded to. This can be overridden in [Routes.defaultReturnedStringIsContent] or [RouteNode.returnedStringIsContent].
     */
    var defaultReturnedStringIsContent = false

    /**
     * The buffer size for streaming back content.
     */
    var streamBufferSize = 16 * 1024

    /**
     * The factory for creating new route instances to process HTTP requests.
     */
    var routeInstancePool: RouteInstancePool = DefaultRouteInstancePool()

    /**
     * Logger Routes uses for error messages. Set to null to suppress all logging.
     */
    var logger: RoutesLogger = SystemLogger()

    var redirectHandler: RoutesRedirectHandler = AddContextPathRedirectHandler()

    /**
     * The maximum number of milliseconds a generated CSRF token is valid.
     */
    var maxCsrfTokenValidMSecs: Long = 120_00L

    /**
     * The name of the CSRF token parameter.
     */
    var csrfTokenParameterName: String = "csrf-token"

    val symbolsToRegexs: MutableMap<String, Regex> = ConcurrentHashMap()
}
