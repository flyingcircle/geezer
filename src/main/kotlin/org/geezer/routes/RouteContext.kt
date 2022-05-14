package org.geezer.routes

import org.geezer.Path
import org.geezer.Path.Companion.PathMonoid.combine
import org.geezer.Path.Companion.PathMonoid.empty
import java.util.regex.PatternSyntaxException
import kotlin.reflect.KFunction

internal class RouteContext(
    configuration: RoutesConfiguration,
    context: RouteContext? = null,
    route: Path = empty(),
    method: HttpMethod? = null,
    methods: List<HttpMethod>? = null,
    forwardPath: Path = empty(),
    acceptTypePatterns: List<String>? = null,
    contentType: String?,
    returnedStringContent: Boolean?,
    beforeFunction: KFunction<*>? = null,
    beforeFunctions: List<KFunction<*>>? = null,
    afterFunction: KFunction<*>? = null,
    afterFunctions: List<KFunction<*>>? = null,
    exceptionHandler: ((e: Exception, requestContext: RequestContext) -> Unit)? = null
) {
    val route: Path

    val methods: List<HttpMethod>

    val acceptTypeRegexs: List<Regex>

    val forwardPath: Path

    val contentType: String?

    val returnedStringContent: Boolean?

    val beforeRoutes: List<WrapperFunction>

    val afterRoutes: List<WrapperFunction>

    val exceptionHandler: ((e: Exception, requestContext: RequestContext) -> Unit)?

    init {
        this.route = context?.route.let { (it ?: empty()).combine(route) }
        this.methods = run {
            val newMethods = context?.methods?.toMutableSet() ?: mutableSetOf()
            method?.let { newMethods.add(it) }
            methods?.let { newMethods.addAll(it.toSet()) }
            newMethods.toList()
        }
        this.forwardPath = context?.forwardPath?.combine(forwardPath) ?: empty()
        this.acceptTypeRegexs = run {
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
            newAcceptTypeRegexs
        }
        this.contentType = contentType ?: context?.contentType
        this.returnedStringContent = returnedStringContent ?: context?.returnedStringContent
        this.beforeRoutes = run {
            val beforeRoutes = context?.beforeRoutes?.toMutableList() ?: mutableListOf()
            beforeFunction?.let {
                beforeRoutes.add(WrapperFunction(it, configuration.routeInstancePool))
            }
            beforeFunctions?.let { fs ->
                beforeRoutes.addAll(
                    fs.map { f -> WrapperFunction(f, configuration.routeInstancePool) }
                )
            }
            beforeRoutes
        }
        this.afterRoutes = run {
            val afterRoutes = context?.afterRoutes?.toMutableList() ?: mutableListOf()
            afterFunction?.let {
                afterRoutes.add(WrapperFunction(it, configuration.routeInstancePool))
            }
            afterFunctions?.let { fs ->
                afterRoutes.addAll(
                    fs.map { f-> WrapperFunction(f, configuration.routeInstancePool) }
                )
            }
            afterRoutes
        }
        this.exceptionHandler = exceptionHandler ?: context?.exceptionHandler
    }
}
