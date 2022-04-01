package org.geezer.routes.criteria

import org.geezer.routes.criteria.RouteCriteria.Companion.NeverMatchRegex

internal class PathCriterion(val pathIndex: Int, val type: PathCriterionType, val value: String, val regex: Regex = NeverMatchRegex, val numberPatternGroups: Int = regex.toPattern().matcher("").groupCount()) {
    override fun toString() = value
}
