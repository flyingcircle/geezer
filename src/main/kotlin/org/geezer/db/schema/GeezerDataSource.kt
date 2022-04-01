package org.geezer.db.schema

import java.io.Closeable
import java.sql.Connection
import javax.sql.DataSource

/**
 * A database connection provider.
 */
interface GeezerDataSource : DataSource, Closeable {
    val tableNamespace: String?

    fun nextSequenceValue(sequenceName: String, connection: Connection): Long
}
