package org.geezer.routes

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class RoutingFilter : Filter {
    val onlyRegexs = mutableListOf<Regex>()

    val exceptRegexs = mutableListOf<Regex>()

    override fun init(filterConfig: FilterConfig) {
        val onlyInitParam = filterConfig.getInitParameter("ONLY")
        if (onlyInitParam != null) {
            val onlyInitParams = onlyInitParam.split(",").toTypedArray()
            for (pattern in onlyInitParams) {
                if (pattern.isNotBlank()) {
                    onlyRegexs.add(Regex(pattern.trim()))
                }
            }
        }

        val exceptInitParam = filterConfig.getInitParameter("EXCEPT")
        if (exceptInitParam != null) {
            val exceptInitParams = exceptInitParam.split(",").toTypedArray()
            for (pattern in exceptInitParams) {
                if (pattern.isNotBlank()) {
                    exceptRegexs.add(Regex(pattern.trim()))
                }
            }
        }
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse
        val requestPath = httpRequest.requestURI.substring(httpRequest.contextPath.length)
        if (excludedFromParameters(requestPath) || !engine.processRequest(httpRequest, httpResponse)) {
            chain.doFilter(request, response)
        }
    }

    private fun excludedFromParameters(requestPath: String): Boolean {
        return (onlyRegexs.isNotEmpty() && !onlyRegexs.any { it.matches(requestPath) }) ||
            (exceptRegexs.isNotEmpty() && exceptRegexs.any { it.matches(requestPath) })
    }

    override fun destroy() {}

    companion object {
        lateinit var engine: RoutingEngine
    }
}
