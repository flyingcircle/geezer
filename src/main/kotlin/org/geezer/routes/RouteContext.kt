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
            val contextMethods = context?.methods?.toSet() ?: setOf()
            val newMethod = method?.let { setOf(it) } ?: setOf()
            val newMethods = methods?.toSet() ?: setOf()
            (contextMethods union newMethod union newMethods).toList()
        }
        this.forwardPath = context?.forwardPath?.combine(forwardPath) ?: empty()
        this.acceptTypeRegexs = run {
            val contextRegexs = context?.acceptTypeRegexs ?: listOf()
            val newAcceptTypeRegexs = acceptTypePatterns?.map {
                try {
                    Regex(it)
                } catch (e: PatternSyntaxException) {
                    throw IllegalArgumentException("Invalid accept type pattern $it.", e)
                }
            } ?: listOf()
            contextRegexs + newAcceptTypeRegexs
        }
        this.contentType = contentType ?: context?.contentType
        this.returnedStringContent = returnedStringContent ?: context?.returnedStringContent
        this.beforeRoutes = run {
            val contextBeforeRoutes = context?.beforeRoutes ?: listOf()
            val beforeFunc = beforeFunction?.let {
                listOf(WrapperFunction(it, configuration.routeInstancePool))
            } ?: listOf()
            val beforeFuncs = beforeFunctions?.let { fs ->
                fs.map { f -> WrapperFunction(f, configuration.routeInstancePool) }
            } ?: listOf()
            contextBeforeRoutes + beforeFunc + beforeFuncs
        }
        this.afterRoutes = run {
            val contextAfterRoutes = context?.afterRoutes ?: listOf()
            val afterFunc = afterFunction?.let {
                listOf(WrapperFunction(it, configuration.routeInstancePool))
            } ?: listOf()
            val afterFuncs = afterFunctions?.let { fs ->
                fs.map { f-> WrapperFunction(f, configuration.routeInstancePool) }
            } ?: listOf()
            contextAfterRoutes + afterFunc + afterFuncs
        }
        this.exceptionHandler = exceptionHandler ?: context?.exceptionHandler
    }
}
