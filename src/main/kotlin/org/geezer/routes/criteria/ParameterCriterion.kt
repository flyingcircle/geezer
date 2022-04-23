package org.geezer.routes.criteria

internal data class ParameterCriterion(
    val name: String,
    val type: ParameterCriterionType,
    val value: String,
    val optional: Boolean,
    val regex: Regex = RouteCriteria.NeverMatchRegex,
    val numberPatternGroups: Int = regex.toPattern().matcher("").groupCount()) {
    override fun toString() = "$name=$value"
}
