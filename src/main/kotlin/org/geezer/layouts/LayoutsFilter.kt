package org.geezer.layouts

import org.geezer.layouts.SharedMethods.trueValue
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList
import java.util.Locale
import java.util.regex.Pattern
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.collections.HashMap

/**
 * The `LayoutsFilter` must be in the processing chain for all requests you want rendered with a layout.
 * It should be placed before any other filters that might generate content for the HTTP response.
 *
 * You can use the filter parameters *ONLY* and *EXCEPT* for finer grained control (than *url-pattern*)
 * over which HTTP requests the `LayoutsFilter` is engaged for.
 *
 * <init-param>
 * <param-name>ONLY</param-name>
 * <param-value>/home/.*,/users/.*</param-value>
 * </init-param>
 *
 * The *ONLY* parameter must be a list (comma delimited) of valid Java regular expression. If specified,
 * only request URIs that match one of these patterns will be candidates for layouts. The URI from the
 * HTTP request matched against these patterns will not include the context path of your application.
 *
 * The other supported parameter is *EXCEPT*:
 *
 * <init-param>
 * <param-name>EXCEPT</param-name>
 * <param-value>.*\.html$,.*\.htm$</param-value>
 * </init-param>
 *
 * The *EXCEPT* parameter must be a list (comma delimited) of valid Java regular expression. If specified,
 * only request URIs that don't match any of these patterns will be candidates for layouts. The URI from
 * the HTTP request matched against these patterns will not include the context path of your application.
 *
 * If both *ONLY* and *EXCEPT* are specified then a request will only be a candidate for a layout if a match
 * is made one of the *ONLY* patterns and no match is made on any of the *EXCEPT* patterns.
 *
 */
class LayoutsFilter : Filter {
    private var onlyPatterns: List<Pattern> = listOf()
    private var exceptPatterns: List<Pattern> = listOf()
    private var layoutDecider: UseLayoutDecider? = null
    private var defaultLayout: Layout? = null
    private var layouts: MutableMap<String, Layout> = HashMap()

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        val onlyInitParam = filterConfig.getInitParameter("ONLY")
        this.onlyPatterns = getPatterns(onlyInitParam)
        val exceptInitParam = filterConfig.getInitParameter("EXCEPT")
        this.exceptPatterns = getPatterns(exceptInitParam)
        layoutDecider = filterConfig.getInitParameter("USE_LAYOUT_DECIDER")?.let {
            try {
                Class.forName(it).getConstructor().newInstance() as UseLayoutDecider
            } catch (e: Exception) {
                throw ServletException(e)
            }
        } ?: DefaultHtmlPageDecider()
        var layoutsDirPath = filterConfig.getInitParameter("LAYOUTS_DIRECTORY") ?: "/WEB-INF/jsps/layouts"
        if (!layoutsDirPath.startsWith("/")) {
            layoutsDirPath = "/$layoutsDirPath"
        }
        if (!layoutsDirPath.endsWith("/")) {
            layoutsDirPath += "/"
        }
        val layoutsParameter = filterConfig.getInitParameter("LAYOUTS") ?: ""
        layouts = if (layoutsParameter.isBlank()) {
            createLayoutsFromFile(filterConfig, layoutsDirPath)
        } else {
            createLayoutsFromParams(layoutsParameter, layoutsDirPath)
        }
        defaultLayout = filterConfig.getInitParameter("DEFAULT_LAYOUT")?.let {
            if (layouts.containsKey(it)) {
                layouts[it]
            } else {
                throw ServletException("Default layout: $it not found.")
            }
        } ?: layouts["application"]
    }

    private fun createLayoutsFromParams(layoutsParameter: String, layoutsDirPath: String): MutableMap<String, Layout> {
        val layoutMap: MutableMap<String, Layout> = HashMap()
        val layoutParameterValues = layoutsParameter.split(",").toTypedArray()
        for (layoutParameterValue in layoutParameterValues) {
            if (layoutParameterValue.isNotBlank()) {
                var layoutPath = layoutParameterValue.trim()
                if (!layoutPath.startsWith("/")) {
                    layoutPath = layoutsDirPath + layoutPath
                }
                var layoutName = layoutPath
                var index = layoutPath.lastIndexOf('/')
                layoutName = layoutName.substring(index + 1, layoutName.length)
                index = layoutName.indexOf('.')
                if (index > -1) {
                    layoutName = layoutName.substring(0, index)
                }
                layoutMap[layoutName] = Layout(layoutName, layoutPath)
            }
        }
        return layoutMap
    }

    private fun createLayoutsFromFile(filterConfig: FilterConfig, layoutsDirPath: String): MutableMap<String, Layout> {
        val layoutsDir = File(filterConfig.servletContext.getRealPath(layoutsDirPath))
        if (!layoutsDir.isDirectory) {
            throw ServletException("Layouts directory: $layoutsDirPath does not exists")
        }
        val layoutMap: MutableMap<String, Layout> = HashMap()
        val layoutFiles = layoutsDir.listFiles() ?: arrayOf()
        for (layoutFile in layoutFiles) {
            if (layoutFile.isFile) {
                val layoutFileName = layoutFile.name
                if (layoutFileName.lowercase(Locale.ENGLISH).endsWith("jsp") || layoutFileName.lowercase(Locale.ENGLISH).endsWith("jspx")) {
                    var layoutName = layoutFileName
                    val index = layoutName.indexOf('.')
                    if (index > -1) {
                        layoutName = layoutName.substring(0, index)
                    }
                    layoutMap[layoutName] = Layout(layoutName, layoutsDirPath + layoutFileName)
                }
            }
        }
        return layoutMap
    }

    private fun getPatterns(param: String?): List<Pattern> {
        val patterns: MutableList<Pattern> = ArrayList()
        if (param != null) {
            val params = param.split(",").toTypedArray()
            for (pattern in params) {
                if (pattern.isNotBlank()) {
                    patterns.add(Pattern.compile(pattern))
                }
            }
        }
        return patterns
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val httpRequest = servletRequest as HttpServletRequest
        var httpResponse = servletResponse as HttpServletResponse
        if (!requestExcludedFromPatterns(httpRequest) && layoutDecider!!.isCandidateForLayout(httpRequest)) {
            val httpResponseBuffer = HttpBufferedResponse(httpRequest, httpResponse)
            chain.doFilter(httpRequest, httpResponseBuffer)
            if (httpResponseBuffer.hasBufferedContent() && httpResponseBuffer.isHtmlContent && !trueValue(httpRequest.getAttribute(Layouts.NO_LAYOUT))) {
                val layoutName = httpRequest.getAttribute(Layouts.LAYOUT) as String?
                val layout = layouts.getOrDefault(layoutName, defaultLayout)
                if (layout != null) {
                    httpResponse = HttpMixedOutputResponse(httpResponse)
                    httpRequest.setAttribute(Layouts.VIEW, View(httpResponseBuffer.content!!, httpResponse))
                    httpRequest.getRequestDispatcher(layout.jspPath).forward(httpRequest, httpResponse)
                } else {
                    httpResponse.status = 500
                    httpResponse.writer.write("<html><body>No layout defined with named: <i>$layoutName</i></body></html>")
                }
            } else {
                httpResponseBuffer.pushContent()
            }
        } else {
            chain.doFilter(httpRequest, httpResponse)
        }
    }

    override fun destroy() {}
    private fun requestExcludedFromPatterns(httpRequest: HttpServletRequest): Boolean {
        val requestPath = httpRequest.requestURI.substring(httpRequest.contextPath.length)
        return (onlyPatterns.isNotEmpty() && !onlyPatterns.any { it.matcher(requestPath).matches() }) ||
            (exceptPatterns.isNotEmpty() && exceptPatterns.any { it.matcher(requestPath).matches() })
    }
}
