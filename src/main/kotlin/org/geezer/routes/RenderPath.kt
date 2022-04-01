package org.geezer.routes

/**
 * When thrown from {@link BeforeRoute} or {@link Route} methods the current request
 * will immediately (no further processing) be directed to the given {@code path}. Any {@link AfterRoute} methods
 * for the current request will still be processed.
 */
class RenderPath(val pathToRender: String) : Exception()
