package org.geezer.routes.tokens

internal object RouteTokensParser {
    fun parse(route: String): RouteTokens = RouteTokens(getPathTokens(route), getParameterTokens(route))

    private fun getPathTokens(route: String): List<PathToken> {
        val parameterIndex: Int = findParameterIndex(route)
        val pathPart = (if (parameterIndex == -1) route else route.substring(0, parameterIndex)).removeSurrounding("/")

        return pathPart.split("/").mapIndexedNotNull { i, s ->
            when {
                s == "{}" -> FunctionParameterPathToken(i)
                s.startsWith("{") && s.endsWith("}") -> PatternPathToken(i, s.substring(1, s.length - 1))
                s == "*" -> WildcardPathToken(i)
                s == "**" -> GobblePathToken(i)
                isSymbol(s) -> SymbolPathToken(i, s.substring(1, s.length))
                s.isNotEmpty() -> ExactValuePathToken(i, s)
                else -> null
            }
        }
    }

    private fun getParameterTokens(route: String): List<ParameterToken> {
        val parameterIndex: Int = findParameterIndex(route)
        if (parameterIndex == -1 || parameterIndex == route.length - 1) return listOf()
        val parameterPart = route.substring(parameterIndex + 1, route.length)

        return parameterPart.split("&")
            .map { if(it.startsWith("(") && it.endsWith(")?"))
                it.substring(1, it.length - 2) to true
                else it to false}
            .filter { it.first.contains('=') }
            .map { it.first.split('=', limit=2) to it.second }
            .map {
                val (name, value) = it.first[0] to it.first[1]
                when {
                    value == "{}" -> FunctionParameterParameterToken(name, it.second)
                    value.startsWith("{") && value.endsWith("}") ->
                        PatternParameterToken(name, it.second, value.substring(1, value.length - 1))
                    value == "*" -> WildcardParameterToken(name, it.second)
                    isSymbol(value) -> SymbolParameterToken(name, it.second, value.substring(1, value.length))
                    else -> ExactValueParameterToken(name, it.second, value)
                }
            }
    }

    private tailrec fun findParameterIndex(route: String, i: Int = 0, openBracketCount: Int = 0): Int {
        return if (i >= route.length) {
            -1
        } else if(route[i] == '?' && openBracketCount == 0) {
            i
        } else if(route[i] == '{') {
            findParameterIndex(route, i+1, openBracketCount+1)
        } else if(route[i] == '}') {
            findParameterIndex(route, i+1, (openBracketCount - 1).coerceAtLeast(0))
        } else {
            findParameterIndex(route, i+1, openBracketCount)
        }
    }

    private fun isSymbol(string: String?): Boolean {
        return string != null &&
                string.startsWith(":") &&
                string.length > 1 &&
                !string.contains(' ')
    }
}
