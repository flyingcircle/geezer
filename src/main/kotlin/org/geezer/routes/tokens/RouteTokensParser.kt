package org.geezer.routes.tokens

internal object RouteTokensParser {
    fun parse(route: String): RouteTokens {
        val pathTokens = mutableListOf<PathToken>()
        val parameterTokens = mutableListOf<ParameterToken>()

        val parameterIndex: Int = findParameterIndex(route)

        var pathPart = if (parameterIndex == -1) route else route.substring(0, parameterIndex)
        val parameterPart = if (parameterIndex == -1 || parameterIndex == route.length - 1) null else route.substring(parameterIndex + 1, route.length)

        if (pathPart.startsWith("/")) pathPart = route.substring(1, pathPart.length)
        if (pathPart.endsWith("/")) pathPart = route.substring(0, pathPart.length - 1)

        val pathSegments = pathPart.split("/").toTypedArray()
        for (i in pathSegments.indices) {
            val pathSegment = pathSegments[i]
            if (pathSegment == "{}") {
                pathTokens.add(FunctionParameterPathToken(i))
            } else if (pathSegment.startsWith("{") && pathSegment.endsWith("}")) {
                pathTokens.add(PatternPathToken(i, pathSegment.substring(1, pathSegment.length - 1)))
            } else if (pathSegment == "*") {
                pathTokens.add(WildcardPathToken(i))
            } else if (pathSegment == "**") {
                pathTokens.add(GobblePathToken(i))
            } else if (isSymbol(pathSegment)) {
                pathTokens.add(SymbolPathToken(i, pathSegment.substring(1, pathSegment.length)))
            } else if (pathSegment.isNotEmpty()) {
                pathTokens.add(ExactValuePathToken(i, pathSegment))
            }
        }

        if (parameterPart != null) {
            val pairs = parameterPart.split("&").toTypedArray()
            for (pair in pairs) {
                var pair = pair
                var optional: Boolean

                if (pair.startsWith("(") && pair.endsWith(")?")) {
                    optional = true
                    pair = pair.substring(1, pair.length - 2)
                } else {
                    optional = false
                }
                val index = pair.indexOf('=')
                if (index != -1) {
                    val name = pair.substring(0, index)
                    val value = pair.substring(index + 1, pair.length)
                    if (value == "{}") {
                        parameterTokens.add(FunctionParameterParameterToken(name, optional))
                    } else if (value.startsWith("{") && value.endsWith("}")) {
                        parameterTokens.add(PatternParameterToken(name, optional, value.substring(1, value.length - 1)))
                    } else if (value == "*") {
                        parameterTokens.add(WildcardParameterToken(name, optional))
                    } else if (isSymbol(value)) {
                        parameterTokens.add(SymbolParameterToken(name, optional, value.substring(1, value.length)))
                    } else {
                        parameterTokens.add(ExactValueParameterToken(name, optional, value))
                    }
                }
            }
        }

        return RouteTokens(pathTokens, parameterTokens)
    }

    fun findParameterIndex(route: String): Int {
        var openPatternBrackets = 0
        val chars = route.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            if (c == '?' && openPatternBrackets == 0) {
                return i
            } else if (c == '{') {
                ++openPatternBrackets
            } else if (c == '}') {
                openPatternBrackets = 0.coerceAtLeast(openPatternBrackets - 1)
            }
        }
        return -1
    }

    fun isSymbol(string: String?): Boolean {
        return string != null && string.startsWith(":") && string.length > 1 && string.indexOf(' ') == -1
    }
}
