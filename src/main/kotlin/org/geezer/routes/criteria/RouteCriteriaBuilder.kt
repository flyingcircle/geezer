package org.geezer.routes.criteria

import org.geezer.routes.FunctionParameterClass
import org.geezer.routes.FunctionParameterType
import org.geezer.routes.HttpMethod
import org.geezer.routes.RouteContext
import org.geezer.routes.RouteFunction
import org.geezer.routes.RoutesConfiguration
import org.geezer.routes.tokens.ExactValueParameterToken
import org.geezer.routes.tokens.ExactValuePathToken
import org.geezer.routes.tokens.FunctionParameterParameterToken
import org.geezer.routes.tokens.FunctionParameterPathToken
import org.geezer.routes.tokens.GobblePathToken
import org.geezer.routes.tokens.PatternParameterToken
import org.geezer.routes.tokens.PatternPathToken
import org.geezer.routes.tokens.RouteTokens
import org.geezer.routes.tokens.SymbolParameterToken
import org.geezer.routes.tokens.SymbolPathToken
import org.geezer.routes.tokens.WildcardParameterToken
import org.geezer.routes.tokens.WildcardPathToken
import java.util.regex.PatternSyntaxException

internal object RouteCriteriaBuilder {
    fun build(tokens: RouteTokens, function: RouteFunction, context: RouteContext, configuration: RoutesConfiguration): RouteCriteria {
        val pathCriteria = mutableListOf<PathCriterion>()
        val parameterCriteria = mutableListOf<ParameterCriterion>()

        var numberPotentialRouteParameters = 0
        val parameterTypesFromRoutes = function.parameterTypes.filter { it.clazz.providedFromRoute }

        numberPotentialRouteParameters = handlePathTokens(
            tokens,
            pathCriteria,
            numberPotentialRouteParameters,
            parameterTypesFromRoutes,
            function,
            configuration
        )

        numberPotentialRouteParameters = handleParameterTokens(
            tokens,
            parameterCriteria,
            numberPotentialRouteParameters,
            parameterTypesFromRoutes,
            function,
            configuration
        )

        if (parameterTypesFromRoutes.size > numberPotentialRouteParameters) {
            throw IllegalArgumentException("Route function $function has ${parameterTypesFromRoutes.size} parameters dynamically provided from the route but the route only has $numberPotentialRouteParameters dynamic values.")
        }

        val methods = context.methods.ifEmpty { listOf(getMethodFromFunction(function)) }

        return RouteCriteria(methods, pathCriteria, parameterCriteria, context.acceptTypeRegexs, configuration.ignoreCase)
    }

    private fun handlePathTokens(
        tokens: RouteTokens,
        pathCriteria: MutableList<PathCriterion>,
        numberPotentialRouteParameters: Int,
        parameterTypesFromRoutes: List<FunctionParameterType>,
        function: RouteFunction,
        configuration: RoutesConfiguration
    ): Int {
        var numberPotentialRouteParameters1 = numberPotentialRouteParameters
        for ((index, token) in tokens.pathTokens.withIndex()) {
            when (token) {
                is ExactValuePathToken -> {
                    pathCriteria.add(PathCriterion(index, PathCriterionType.Exact, token.value))
                }

                else -> {
                    val parameterTypeFromRoute =
                        if (numberPotentialRouteParameters1 < parameterTypesFromRoutes.size) parameterTypesFromRoutes[numberPotentialRouteParameters1] else null
                    if (parameterTypeFromRoute != null && parameterTypeFromRoute.optional) {
                        throw IllegalArgumentException("Route function $function has unsupported optional path parameter ${parameterTypeFromRoute.clazz.clazz.qualifiedName} at index $numberPotentialRouteParameters1.")
                    }

                    when (token) {
                        is WildcardPathToken -> {
                            pathCriteria.add(PathCriterion(index, PathCriterionType.Regex, "*", WildCardRegex, 0))
                            if (parameterTypeFromRoute != null && parameterTypeFromRoute.clazz != FunctionParameterClass.StringClass) {
                                throw IllegalArgumentException("Route function $function has wildcard path token mapped to ${parameterTypeFromRoute.clazz.clazz.simpleName} but wildcards must be mapped to a String parameter.")
                            }
                        }

                        is PatternPathToken -> {
                            try {
                                val criterion =
                                    PathCriterion(index, PathCriterionType.Regex, token.pattern, Regex(token.pattern))
                                pathCriteria.add(criterion)
                                numberPotentialRouteParameters1 += criterion.numberPatternGroups.coerceAtLeast(1)
                            } catch (e: PatternSyntaxException) {
                                throw IllegalArgumentException(
                                    "Route function $function has invalid path regular expression pattern ${token.pattern}.",
                                    e
                                )
                            }
                        }

                        is FunctionParameterPathToken -> {
                            val regex =
                                parameterTypesFromRoutes.getOrNull(numberPotentialRouteParameters1)?.clazz?.routeRegex
                                    ?: throw IllegalArgumentException("Route function $function has path pattern {} with no matching parameter.")
                            pathCriteria.add(PathCriterion(index, PathCriterionType.Regex, regex.pattern, regex))
                        }

                        is SymbolPathToken -> {
                            val regex = configuration.symbolsToRegexs[token.symbol]
                                ?: throw IllegalArgumentException("Route function $function references undefined symbol ${token.symbol} in path token.")
                            pathCriteria.add(PathCriterion(index, PathCriterionType.Regex, regex.pattern, regex))
                        }

                        is GobblePathToken -> {
                            if (index < tokens.pathTokens.size - 1) {
                                throw IllegalArgumentException("Gobble path token in $function must be last.")
                            }
                            pathCriteria.add(PathCriterion(index, PathCriterionType.Gobble, "**"))
                        }

                        else -> throw IllegalArgumentException("Invalid path token type ${token.javaClass.kotlin.qualifiedName}.")
                    }
                    ++numberPotentialRouteParameters1
                }
            }
        }
        return numberPotentialRouteParameters1
    }

    private fun handleParameterTokens(
        tokens: RouteTokens,
        parameterCriteria: MutableList<ParameterCriterion>,
        numberPotentialRouteParameters: Int,
        parameterTypesFromRoutes: List<FunctionParameterType>,
        function: RouteFunction,
        configuration: RoutesConfiguration
    ): Int {
        var numberPotentialRouteParameters1 = numberPotentialRouteParameters
        for (token in tokens.parameterTokens) {
            when (token) {
                is ExactValueParameterToken -> {
                    parameterCriteria.add(
                        ParameterCriterion(
                            token.name,
                            ParameterCriterionType.Exact,
                            token.value,
                            token.optional
                        )
                    )
                }

                else -> {
                    var optional = token.optional
                    val parameterTypeFromRoute =
                        if (numberPotentialRouteParameters1 < parameterTypesFromRoutes.size) parameterTypesFromRoutes[numberPotentialRouteParameters1] else null
                    if (parameterTypeFromRoute != null) {
                        if (token.optional && !parameterTypeFromRoute.optional) {
                            throw IllegalArgumentException("Route function $function has optional parameter route token ${token.name} mapped to non-optional function parameter type ${parameterTypeFromRoute.clazz.clazz.simpleName}.")
                        }
                        optional = optional || parameterTypeFromRoute.optional
                    }

                    when (token) {
                        is WildcardParameterToken -> {
                            parameterCriteria.add(
                                ParameterCriterion(
                                    token.name,
                                    ParameterCriterionType.Regex,
                                    "*",
                                    optional,
                                    WildCardRegex
                                )
                            )
                        }

                        is PatternParameterToken -> {
                            try {
                                val criterion = ParameterCriterion(
                                    token.name,
                                    ParameterCriterionType.Regex,
                                    token.pattern,
                                    optional,
                                    token.pattern.toRegex()
                                )
                                parameterCriteria.add(criterion)
                                numberPotentialRouteParameters1 += criterion.numberPatternGroups.coerceAtLeast(1)
                            } catch (e: PatternSyntaxException) {
                                throw IllegalArgumentException(
                                    "Route function $function has invalid parameter regular expression pattern ${token.pattern}.",
                                    e
                                )
                            }
                        }

                        is FunctionParameterParameterToken -> {
                            val regex =
                                parameterTypesFromRoutes.getOrNull(numberPotentialRouteParameters1)?.clazz?.routeRegex
                                    ?: throw IllegalArgumentException("Route function $function has parameter pattern {} with no matching parameter.")
                            parameterCriteria.add(
                                ParameterCriterion(
                                    token.name,
                                    ParameterCriterionType.Regex,
                                    regex.pattern,
                                    optional,
                                    regex
                                )
                            )
                        }

                        is SymbolParameterToken -> {
                            val regex = configuration.symbolsToRegexs[token.symbol]
                                ?: throw IllegalArgumentException("Route function $function references undefined symbol ${token.symbol} in parameter token.")
                            parameterCriteria.add(
                                ParameterCriterion(
                                    token.name,
                                    ParameterCriterionType.Regex,
                                    regex.pattern,
                                    optional,
                                    regex
                                )
                            )
                        }

                        else -> throw IllegalArgumentException("Invalid parameter token type ${token.javaClass.kotlin.qualifiedName}.")
                    }
                    ++numberPotentialRouteParameters1
                }
            }
        }
        return numberPotentialRouteParameters1
    }

    private fun findNextParameterRegex(parameters: List<FunctionParameterClass>, parameterProvidedFromRouteIndex: Int): Regex? = parameters.filter { it.providedFromRoute }.getOrNull(parameterProvidedFromRouteIndex)?.routeRegex

    private fun getMethodFromFunction(function: RouteFunction): HttpMethod {
        val functionName = function.function.name.lowercase()
        return if (functionName.startsWith("post")) {
            HttpMethod.POST
        } else if (functionName.startsWith("delete")) {
            HttpMethod.DELETE
        } else if (functionName.startsWith("put")) {
            HttpMethod.PUT
        } else {
            HttpMethod.GET
        }
    }

    private val WildCardRegex = Regex(".*")
}
