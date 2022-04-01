package org.geezer.layouts

import javax.servlet.http.HttpServletRequest

/**
 *
 * A way to tell Layouts if an HTTP request is a candidate for a layout. If the request is *not* a candidate, then the
 * request will be processed unaltered by Layouts. If a request is a candidate, then the content of this request will
 * be rendered in a layout unless:
 * 1. The content type of the response is set and is something other than HTML.
 * 2. The [Layouts.NO_LAYOUT] `HttpServletRequest` attribute is set to true.
 * 3. The request returns no content.
 * 4. The layout specified by [Layouts.LAYOUT] is invalid.
 */
interface UseLayoutDecider {
    /**
     *
     * @param httpRequest The HTTP request.
     * @return `true` if this HTTP request is a candidate for a layout.
     */
    fun isCandidateForLayout(httpRequest: HttpServletRequest?): Boolean
}
