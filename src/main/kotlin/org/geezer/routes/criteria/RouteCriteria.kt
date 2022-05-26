package org.geezer.routes.criteria

import org.geezer.routes.HttpMethod
import org.geezer.routes.RequestContext

internal data class RouteCriteria(
    val methods: List<HttpMethod>,
    val pathCriteria: List<PathCriterion>,
    val parameterCriteria: List<ParameterCriterion>,
    val acceptTypePatterns: List<Regex>,
    val ignoreCase: Boolean) {

    private val hasPattern: Boolean = pathCriteria.any { it.type == PathCriterionType.Regex } || parameterCriteria.any { it.type == ParameterCriterionType.Regex }

    private val hasGobblePathCriterion: Boolean = pathCriteria.any { it.type == PathCriterionType.Gobble }

    val allPathCriteriaFixed = pathCriteria.all { it.type == PathCriterionType.Exact }

    val allCriteriaFixed: Boolean = !hasPattern && !hasGobblePathCriterion

    fun matches(context: RequestContext): Boolean {
        if (!methods.contains(context.method) ||
            !hasGobblePathCriterion &&
            context.path.size != pathCriteria.size) return false

        val segments = context.path.segments.withIndex()
        if (segments.any { (i,_) -> i > pathCriteria.size }) return false

        val valueMismatch = segments
            .map { (i,s) -> pathCriteria[i] to s }
            .firstOrNull() { when(it.first.type) {
                PathCriterionType.Exact -> !it.second.equals(it.first.value, ignoreCase)
                PathCriterionType.Regex -> !it.first.regex.matches(it.second)
                PathCriterionType.Gobble -> true
            } }
            ?.first
        if (valueMismatch != null &&
            valueMismatch.type != PathCriterionType.Gobble) return false

        if (acceptTypePatterns.isNotEmpty() &&
            acceptTypePatterns.none { it.containsMatchIn(context.requestedContentType.type) }) return false

        val paramValues = parameterCriteria
            .map { it to context.parameters.getValues(it.name) }
            .filterNot { it.second.isEmpty() && it.first.optional }
        // remaining empties are non-optional
        if (paramValues.any { it.second.isEmpty() }) return false

        val paramValueNoMatches = paramValues.any {
            when(it.first.type) {
                ParameterCriterionType.Exact ->  it.second.none { value -> it.first.value.equals(value, ignoreCase) }
                ParameterCriterionType.Regex ->  it.second.none { value -> it.first.regex.matches(value) }
            }
        }
        if (paramValueNoMatches) return false

        return true
    }

    companion object {
        val NeverMatchRegex = Regex.fromLiteral("\$a")
    }
}
