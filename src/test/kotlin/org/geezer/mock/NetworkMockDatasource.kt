package org.geezer.mock

import org.h2.tools.Server
import java.sql.Connection

/**
 * A database stored in-memory used for local development and testing.
 */
object NetworkMockDatasource : MockDataSource() {

    const val DbPath = "/tmp/flexline"

    override val jdbcUrl: String = "jdbc:h2:tcp://localhost$DbPath;MODE=Oracle;AUTO_RECONNECT=TRUE;AUTO_SERVER=TRUE"

    override val password: String
        get() = ""

    var server: Server? = null

    override fun close() {
        super.close()
        server?.stop()
    }

    override val tableNamespace: String
        get() = "mockTablespace"

    override fun nextSequenceValue(sequenceName: String, connection: Connection): Long {
        return connection.createStatement().use { statement ->
            statement.executeQuery("select ${sequenceName}.nextval from dual").use { set ->
                set.next()
                set.getLong(1)
            }
        }
    }
}