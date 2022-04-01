package org.geezer.routes

import java.net.URL

/**
 * When thrown from {@link org.geezer.routes.BeforeRoute} or {@link org.geezer.routes.Route} methods the current request
 * will immediately (no further processing) be redirected to the given {@code redirectUrl}. Any {@link AfterRoute} methods
 * for the current request will still be processed.
 */
class RedirectTo : Exception {
    val redirectUrl: String

    /**
     * If `redirectUrl` is not a fully qualified URL, then it will be considered relative from the current HTTP request.
     */
    constructor(redirectUrl: String) {
        assert(redirectUrl.isNotEmpty())
        this.redirectUrl = redirectUrl
    }

    constructor(redirectUrl: URL) {
        this.redirectUrl = redirectUrl.toString()
    }
}
