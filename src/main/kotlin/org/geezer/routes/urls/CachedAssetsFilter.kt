package org.geezer.routes.urls

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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
