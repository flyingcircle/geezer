package org.geezer.routes

import org.geezer.Path
import org.geezer.Path.Companion.PathMonoid.combine
import org.geezer.routes.criteria.RouteCriteriaBuilder
import org.geezer.routes.tokens.RouteTokensParser
import java.util.Collections
import kotlin.reflect.KFunction

/**
 * Contains all routing functions.
 */
class RoutingTable(val configuration: RoutesConfiguration, handler: ((table: RoutingTable) -> Unit)? = null) {

    private val staticPathNodes = mutableMapOf<String, MutableList<RouteNode>>()

    private val dynamicPathNodes = mutableListOf<RouteNode>()

    private val functionsToPaths = Collections.synchronizedMap(mutableMapOf<KFunction<*>, String>())

    private val localContext = ThreadLocal<RouteContext>()

    init {
        handler?.let { it(this) }
    }

    internal fun find(context: RequestContext): RouteNode? {
        staticPathNodes[context.path.toString()]?.let { nodes ->
            return nodes.firstOrNull { it.criteria.matches(context) }
        }

        return dynamicPathNodes.firstOrNull { it.criteria.matches(context) }
    }

    operator fun get(function: KFunction<*>): String? = functionsToPaths[function]

    fun context(route: String = "", method: HttpMethod? = null, methods: List<HttpMethod>? = null, forwardPath: String = "", acceptTypePatterns: List<String>? = null, contentType: String? = null, returnedStringContent: Boolean? = null, beforeRoute: KFunction<*>? = null, beforeRoutes: List<KFunction<*>>? = null, afterRoute: KFunction<*>? = null, afterRoutes: List<KFunction<*>>? = null, exceptionHandler: ((e: Exception, requestContext: RequestContext) -> Unit)? = null, handler: ((table: RoutingTable) -> Unit)? = null): RoutingTable {
        val beforeContext = localContext.get()
        localContext.set(RouteContext(configuration, beforeContext, route = Path(route), method = method, methods = methods, forwardPath = Path(forwardPath), acceptTypePatterns = acceptTypePatterns, contentType = contentType, returnedStringContent = returnedStringContent, beforeFunction = beforeRoute, beforeFunctions = beforeRoutes, afterFunction = afterRoute, afterFunctions = afterRoutes, exceptionHandler = exceptionHandler))
        handler?.let { it(this) }
        localContext.set(beforeContext)
        return this
    }

    fun add(function: KFunction<*>, method: HttpMethod? = null, methods: List<HttpMethod>? = null, forwardPath: String = "", acceptTypePatterns: List<String>? = null, contentType: String? = null, returnedStringContent: Boolean? = null, beforeFunction: KFunction<*>? = null, beforeFunctions: List<KFunction<*>>? = null, afterFunction: KFunction<*>? = null, afterFunctions: List<KFunction<*>>? = null, exceptionHandler: ((e: Exception, requestContext: RequestContext) -> Unit)? = null): RoutingTable {
        return add("", function, method, methods, forwardPath, acceptTypePatterns, contentType, returnedStringContent, beforeFunction, beforeFunctions, afterFunction, afterFunctions, exceptionHandler)
    }

    fun add(route: String = "", function: KFunction<*>, method: HttpMethod? = null, methods: List<HttpMethod>? = null, forwardPath: String = "", acceptTypePatterns: List<String>? = null, contentType: String? = null, returnedStringContent: Boolean? = null, beforeRoute: KFunction<*>? = null, beforeRoutes: List<KFunction<*>>? = null, afterRoute: KFunction<*>? = null, afterRoutes: List<KFunction<*>>? = null, exceptionHandler: ((e: Exception, requestContext: RequestContext) -> Unit)? = null): RoutingTable {
        val context = RouteContext(configuration, localContext.get(), Path(route), method, methods, Path(forwardPath), acceptTypePatterns, contentType, returnedStringContent, beforeFunction = beforeRoute, beforeFunctions = beforeRoutes, afterFunction = afterRoute, afterFunctions = afterRoutes, exceptionHandler)
        val forwardPath = configuration.rootForwardPath.let { Path(it).combine(context.forwardPath) }

        val returnedStringContent = context.returnedStringContent ?: configuration.defaultReturnedStringIsContent
        val routeFunction = RouteFunction(function, configuration.routeInstancePool, returnedStringContent)
        val tokens = RouteTokensParser.parse(context.route.value)
        val criteria = RouteCriteriaBuilder.build(tokens, routeFunction, context, configuration)

        val routeNode = RouteNode(context.route.value, criteria, context.beforeRoutes, routeFunction, context.afterRoutes, forwardPath.value, context.contentType, configuration, context.exceptionHandler)

        if (criteria.allPathCriteriaFixed) {
            val routePathOnly = if (routeNode.criteria.pathCriteria.isEmpty()) "" else "/${routeNode.criteria.pathCriteria.joinToString("/")}"
            staticPathNodes.getOrPut(routePathOnly) { mutableListOf() }.add(routeNode)
        } else {
            dynamicPathNodes.add(routeNode)
        }

        functionsToPaths[function] = context.route.value

        return this
    }

    fun addDirect(route: String, path: String, method: HttpMethod? = null, methods: List<HttpMethod>? = null, forwardPath: String = "", acceptTypePatterns: List<String>? = null, contentType: String? = null, returnedStringContent: Boolean? = null, beforeFunction: KFunction<*>? = null, beforeFunctions: List<KFunction<*>>? = null, afterFunction: KFunction<*>? = null, afterFunctions: List<KFunction<*>>? = null): RoutingTable {
        return add(route, DirectPathRoute(path)::route, method, methods, forwardPath, acceptTypePatterns, contentType, returnedStringContent, beforeFunction, beforeFunctions, afterFunction, afterFunctions)
    }
}
