package org.geezer.routes

/**
 * A RoutesLogger that does nothing.
 */
class NoOpLogger : RoutesLogger {
    override fun info(message: String) {}

    override fun warn(message: String, e: Exception?) {}

    override fun error(message: String, e: Exception?) {}
}
