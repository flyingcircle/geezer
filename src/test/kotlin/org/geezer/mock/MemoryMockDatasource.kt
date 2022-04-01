package org.geezer.mock

import java.sql.Connection

/**
 * A database stored in-memory used for local development and testing.
 */
object MemoryMockDatasource : MockDataSource() {

    override val jdbcUrl: String = "jdbc:h2:mem:flexline;MODE=Oracle;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;"

    override val password: String
        get() = ""

    override val tableNamespace: String?
        get() = TODO("Not yet implemented")

    override fun nextSequenceValue(sequenceName: String, connection: Connection): Long {
        TODO("Not yet implemented")
    }

}