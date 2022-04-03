package org.geezer.routes.urls

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class CachedAssetsFilter : Filter {

    override fun init(filterConfig: FilterConfig) {}

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        var uri = httpRequest.requestURI
        val hashStartIndex = uri.indexOf("--")
        if (hashStartIndex > 0) {
            val hashEndIndex = uri.lastIndexOf(".")
            if (hashEndIndex > hashStartIndex) {
                uri = "${uri.substring(0, hashStartIndex)}${uri.substring(hashEndIndex)}"
            }

            request.contextPath?.let { contextPath ->
                if (contextPath.isNotBlank()) {
                    uri = uri.substring(contextPath.length)
                }
            }

            (response as HttpServletResponse).addHeader("cache-control", "max-age=${UrlGen.cacheSeconds}")
            request.getRequestDispatcher(uri).forward(request, response)
        } else {
            chain.doFilter(request, response)
        }
    }

    override fun destroy() {}
}
