package org.geezer.routes

/**
 * The default Routes logger. Logs everything to {@link java.lang.System#out} and {@link java.lang.System#err}.
 *
 * @see RoutesConfiguration#logger
 */
class SystemLogger : RoutesLogger {
    override fun info(message: String) {
        println(message)
    }

    override fun warn(message: String, e: Exception?) {
        println(message)
        e?.printStackTrace(System.out)
    }

    override fun error(message: String, e: Exception?) {
        System.err.println(message)
        e?.printStackTrace()
    }
}
