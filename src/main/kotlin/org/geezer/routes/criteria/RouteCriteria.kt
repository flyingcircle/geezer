package org.geezer.routes.criteria

import org.geezer.routes.HttpMethod
import org.geezer.routes.RequestContext

internal class RouteCriteria(val methods: List<HttpMethod>, val pathCriteria: List<PathCriterion>, val parameterCriteria: List<ParameterCriterion>, val acceptTypePatterns: List<Regex>, val ignoreCase: Boolean) {

    val hasPattern: Boolean = pathCriteria.any { it.type == PathCriterionType.Regex } || parameterCriteria.any { it.type == ParameterCriterionType.Regex }

    val hasGobblePathCriterion: Boolean = pathCriteria.any { it.type == PathCriterionType.Gobble }

    val allPathCriteriaFixed = pathCriteria.all { it.type == PathCriterionType.Exact }

    val allCriteriaFixed: Boolean = !hasPattern && !hasGobblePathCriterion

    fun matches(context: RequestContext): Boolean {
        if (!methods.contains(context.method)) {
            return false
        }

        if (!hasGobblePathCriterion && context.path.size != pathCriteria.size) {
            return false
        }

        PathCheck@
        for ((index, segment) in context.path.segments.withIndex()) {
            if (index > pathCriteria.size) {
                return false
            }

            val pathCriteria = pathCriteria[index]
            when (pathCriteria.type) {
                PathCriterionType.Gobble -> break@PathCheck
                PathCriterionType.Exact -> {
                    if (!segment.equals(pathCriteria.value, ignoreCase)) {
                        return false
                    }
                }
                PathCriterionType.Regex -> {
                    if (!pathCriteria.regex.matches(segment)) {
                        return false
                    }
                }
            }
        }

        if (acceptTypePatterns.isNotEmpty() && acceptTypePatterns.none { it.containsMatchIn(context.requestedContentType.type) }) {
            return false
        }

        for (parameterCriterion in parameterCriteria) {
            val parameterValues = context.parameters.getValues(parameterCriterion.name)
            if (parameterValues.isEmpty()) {
                if (!parameterCriterion.optional) {
                    return false
                } else {
                    continue
                }
            }

            when (parameterCriterion.type) {
                ParameterCriterionType.Exact -> {
                    if (parameterValues.none { parameterCriterion.value.equals(it, ignoreCase) }) {
                        return false
                    }
                }

                ParameterCriterionType.Regex -> {
                    if (parameterValues.none { parameterCriterion.regex.matches(it) }) {
                        return false
                    }
                }
            }
        }

        return true
    }

    companion object {
        val NeverMatchRegex = Regex.fromLiteral("\$a")
    }
}
