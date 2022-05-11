package org.geezer.routes

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class RoutingFilter : Filter {
    private lateinit var onlyRegexs: List<Regex>
    private lateinit var exceptRegexs: List<Regex>

    override fun init(filterConfig: FilterConfig) {
        val onlyInitParam = filterConfig.getInitParameter("ONLY")
        onlyInitParam?.split(",")?.toTypedArray()?.let { onlyInitParams ->
            onlyRegexs = onlyInitParams.filter { it.isNotBlank() }.map { Regex(it.trim()) }
        }
        val exceptInitParam = filterConfig.getInitParameter("EXCEPT")
        exceptInitParam?.split(",")?.toTypedArray()?.let { exceptInitParams ->
            exceptRegexs = exceptInitParams.filter { it.isNotBlank() }.map { Regex(it.trim()) }
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
