package org.geezer.routes

import java.util.regex.PatternSyntaxException
import kotlin.reflect.KFunction

internal class RouteContext(
    configuration: RoutesConfiguration,
    context: RouteContext? = null,
    route: String? = null,
    method: HttpMethod? = null,
    methods: List<HttpMethod>? = null,
    forwardPath: String? = null,
    acceptTypePatterns: List<String>? = null,
    contentType: String?,
    returnedStringContent: Boolean?,
    beforeFunction: KFunction<*>? = null,
    beforeFunctions: List<KFunction<*>>? = null,
    afterFunction: KFunction<*>? = null,
    afterFunctions: List<KFunction<*>>? = null,
    exceptionHandler: ((e: Exception, requestContext: RequestContext) -> Unit)? = null
) {
    val route: String?

    val methods: List<HttpMethod>

    val acceptTypeRegexs: List<Regex>

    val forwardPath: String?

    val contentType: String?

    val returnedStringContent: Boolean?

    val beforeRoutes: List<WrapperFunction>

    val afterRoutes: List<WrapperFunction>

    val exceptionHandler: ((e: Exception, requestContext: RequestContext) -> Unit)?

    init {
        var newRoute = context?.route
        if (route != null && route.isNotBlank()) {
            val route = route
            if (newRoute == null) {
                newRoute = "/"
            } else if (!newRoute.endsWith("/")) {
                newRoute += "/"
            }

            newRoute += if (route.startsWith("/")) {
                route.substring(1)
            } else {
                route
            }
        }
        this.route = newRoute
        val newMethods = context?.methods?.toMutableList() ?: mutableListOf()
        if (method != null && !newMethods.contains(method)) {
            newMethods.add(method)
        }
        if (methods != null) {
            for (method in methods) {
                if (!newMethods.contains(method)) {
                    newMethods.add(method)
                }
            }
        }
        this.methods = newMethods
        var newForwardPath = context?.forwardPath
        if (forwardPath != null && forwardPath.isNotBlank()) {
            var forwardPath = forwardPath
            if (newForwardPath == null) {
                newForwardPath = forwardPath
            } else {
                if (!newForwardPath.endsWith("/")) {
                    newForwardPath += "/"
                }

                if (forwardPath.startsWith("/")) {
                    forwardPath = forwardPath.substring(1)
                }
                newForwardPath += forwardPath
            }
        }
        this.forwardPath = newForwardPath
        val newAcceptTypeRegexs = context?.acceptTypeRegexs?.toMutableList() ?: mutableListOf()
        if (acceptTypePatterns != null) {
            for (acceptTypePattern in acceptTypePatterns) {
                try {
                    newAcceptTypeRegexs.add(Regex(acceptTypePattern))
                } catch (e: PatternSyntaxException) {
                    throw IllegalArgumentException("Invalid accept type pattern $acceptTypePattern.", e)
                }
            }
        }
        this.acceptTypeRegexs = newAcceptTypeRegexs
        this.contentType = contentType ?: context?.contentType
        this.returnedStringContent = returnedStringContent ?: context?.returnedStringContent
        val beforeRoutes = context?.beforeRoutes?.toMutableList() ?: mutableListOf()
        if (beforeFunction != null) {
            beforeRoutes.add(WrapperFunction(beforeFunction, configuration.routeInstancePool))
        }
        if (beforeFunctions != null) {
            for (beforeFunction in beforeFunctions) {
                beforeRoutes.add(WrapperFunction(beforeFunction, configuration.routeInstancePool))
            }
        }
        this.beforeRoutes = beforeRoutes
        val afterRoutes = context?.afterRoutes?.toMutableList() ?: mutableListOf()
        if (afterFunction != null) {
            afterRoutes.add(WrapperFunction(afterFunction, configuration.routeInstancePool))
        }
        if (afterFunctions != null) {
            for (afterFunction in afterFunctions) {
                afterRoutes.add(WrapperFunction(afterFunction, configuration.routeInstancePool))
            }
        }
        this.afterRoutes = afterRoutes
        this.exceptionHandler = exceptionHandler ?: context?.exceptionHandler
    }
}
