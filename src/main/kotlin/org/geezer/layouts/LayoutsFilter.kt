package org.geezer.layouts

import arrow.core.Either
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.Locale
import java.util.regex.Pattern
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.geezer.PathUtil

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
class LayoutsFilter: Filter {

    private lateinit var layoutPatterns: LayoutPatterns

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        when (val it = layoutPatternFromConfig(filterConfig)) {
            is Either.Right -> layoutPatterns = it.value
            is Either.Left -> throw ServletException(it.value)
        }
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        var httpResponse = response as HttpServletResponse
        if (!requestExcludedFromPatterns(httpRequest) && layoutPatterns.layoutDecider.isCandidateForLayout(httpRequest)) {
            val httpResponseBuffer = HttpBufferedResponse(httpRequest, httpResponse)
            chain.doFilter(httpRequest, httpResponseBuffer)
            if (httpResponseBuffer.hasBufferedContent() && httpResponseBuffer.isHtmlContent && !trueValue(httpRequest.getAttribute(NO_LAYOUT))) {
                val layoutName = httpRequest.getAttribute(LAYOUT) as String?
                val layout = layoutPatterns.layouts.getOrDefault(layoutName, layoutPatterns.defaultLayout)
                httpResponse = HttpMixedOutputResponse(httpResponse)
                httpRequest.setAttribute(VIEW, View(httpResponseBuffer.content!!, httpResponse))
                httpRequest.getRequestDispatcher(layout.jspPath).forward(httpRequest, httpResponse)
            } else {
                httpResponseBuffer.pushContent()
            }
        } else {
            chain.doFilter(httpRequest, httpResponse)
        }
    }

    private fun requestExcludedFromPatterns(httpRequest: HttpServletRequest): Boolean {
        val requestPath = httpRequest.requestURI.substring(httpRequest.contextPath.length)
        return (layoutPatterns.onlyPatterns.isNotEmpty() && !layoutPatterns.onlyPatterns.any { it.matcher(requestPath).matches() }) ||
                (layoutPatterns.exceptPatterns.isNotEmpty() && layoutPatterns.exceptPatterns.any { it.matcher(requestPath).matches() })
    }
}

private data class Layout(val name: String, val jspPath: String)

private data class LayoutPatterns(
    val onlyPatterns: List<Pattern>,
    val exceptPatterns: List<Pattern>,
    val layoutDecider: UseLayoutDecider,
    val defaultLayout: Layout,
    val layouts: Map<String, Layout>
)

private fun layoutPatternFromConfig(filterConfig: FilterConfig): Either<String, LayoutPatterns> {
    val onlyPatterns = getPatterns(filterConfig.getInitParameter("ONLY"))
    val exceptPatterns = getPatterns(filterConfig.getInitParameter("EXCEPT"))
    val layoutDecider = filterConfig.getInitParameter("USE_LAYOUT_DECIDER")?.let {
        try {
            Class.forName(it).getConstructor().newInstance() as UseLayoutDecider
        } catch (e: Exception) {
            return Either.Left(e.message!!)
        }
    } ?: DefaultHtmlPageDecider()
    val layoutsDirPath = PathUtil.normalizePath(
        filterConfig.getInitParameter("LAYOUTS_DIRECTORY") ?: "/WEB-INF/jsps/layouts/")
    val layoutsParameter = filterConfig.getInitParameter("LAYOUTS") ?: ""
    val maybeLayouts = if (layoutsParameter.isBlank()) {
        createLayoutsFromFile(filterConfig, layoutsDirPath)
    } else {
        createLayoutsFromParams(layoutsParameter, layoutsDirPath)
    }
    val layouts = when(maybeLayouts) {
        is Either.Right -> maybeLayouts.value
        is Either.Left -> return maybeLayouts
    }
    val defaultLayout = filterConfig.getInitParameter("DEFAULT_LAYOUT")?.let {
        layouts.getOrElse(it) {
            return Either.Left("Default layout: $it not found.")
        }
    } ?: layouts["application"]
    return Either.Right(LayoutPatterns(onlyPatterns, exceptPatterns, layoutDecider, defaultLayout!!, layouts))
}

private fun getPatterns(param: String?): List<Pattern> {
    return (param ?: "").split(",")
        .filter { it.isNotBlank() }
        .map { Pattern.compile(it) }
}

private fun createLayoutsFromFile(filterConfig: FilterConfig, layoutsDirPath: String): Either<String, Map<String, Layout>> {
    val layoutsDir = File(filterConfig.servletContext.getRealPath(layoutsDirPath))
    if (!layoutsDir.isDirectory) {
        return Either.Left("Layouts directory: $layoutsDirPath does not exists")
    }
    val layoutFiles = layoutsDir.listFiles() ?: arrayOf()
    val layoutMap = layoutFiles
        .filter { it.isFile }
        .filter { "\\.jspx?$".toRegex().containsMatchIn(it.name.lowercase(Locale.ENGLISH)) }
        .associate { it.name.substring(0, it.name.indexOf('.')) to Layout(it.name, layoutsDirPath + it.name) }
    return Either.Right(layoutMap)
}

private fun createLayoutsFromParams(layoutsParameter: String, layoutsDirPath: String): Either<String, Map<String, Layout>> {
    val layoutMap = layoutsParameter.split(",")
        .filter { it.isNotBlank() }
        .map { it.trim() }
        .map { if (!it.startsWith("/")) {layoutsDirPath + it} else {it} }
        .associate {
            val layoutName = it.substringAfterLast("/").substringBeforeLast('.')
            layoutName to Layout(layoutName, it) }
    return Either.Right(layoutMap)
}