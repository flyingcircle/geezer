package org.geezer.mock

import org.geezer.db.schema.GeezerDataSource
import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.util.logging.Logger

abstract class MockDataSource : GeezerDataSource {
    abstract val jdbcUrl: String

    abstract val password: String

    @Volatile
    var driverInitialized: Boolean = false

    var printWriter: PrintWriter? = null

    var loginTimeoutSeconds = 30

    override fun getLogWriter(): PrintWriter? {
        return printWriter
    }

    override fun setLogWriter(out: PrintWriter?) {
        printWriter = out
    }

    override fun setLoginTimeout(seconds: Int) {
        loginTimeoutSeconds = seconds
    }

    override fun getLoginTimeout(): Int {
        return loginTimeoutSeconds
    }

    override fun getParentLogger(): Logger = Logger.getGlobal()

    override fun <T : Any?> unwrap(iface: Class<T>?): T? = null

    override fun isWrapperFor(iface: Class<*>?): Boolean = false

    override fun getConnection(): Connection {
        if (!driverInitialized) {
            Class.forName("org.h2.Driver")
            driverInitialized = true
        }
        val connection = DriverManager.getConnection(jdbcUrl, JdbcUsername, password)
        connection.autoCommit = true
        return connection
    }

    override fun getConnection(username: String, password: String): Connection = connection

    override fun close() {}

    companion object {
        const val JdbcUsername = "SA"
    }
}