package org.geezer.routes

/**
 * Routes will only log error statements using this interface.
 *
 * @see RoutesConfiguration#logger
 */
interface RoutesLogger {
    fun info(message: String)

    fun warn(message: String, e: Exception? = null)

    fun error(message: String, e: Exception? = null)
}
