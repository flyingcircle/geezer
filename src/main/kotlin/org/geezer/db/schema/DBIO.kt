package org.geezer.db.schema

import org.geezer.db.toGeezer
import java.io.Writer
import java.sql.Blob
import java.sql.CallableStatement
import java.sql.Clob
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.NClob
import java.sql.PreparedStatement
import java.sql.SQLWarning
import java.sql.SQLXML
import java.sql.Savepoint
import java.sql.Statement
import java.sql.Struct
import java.util.Properties
import java.util.concurrent.Executor
import javax.sql.DataSource

fun String.dbFullName(): String = if (this.contains(".")) this else DBIO.dataSource.tableNamespace?.let { if (it.isNotBlank()) "$it.$this" else this } ?: this

fun <T> db(block: ((connection: Connection) -> T)) = DBIO.transaction(block)

/**
 * Provides common database functions and database transaction management based upon thread locality.
 */
object DBIO {
    val dataSource: GeezerDataSource
        get() = _dataSource

    private lateinit var _dataSource: GeezerDataSource

    /**
     * The timestamp that the current transaction started at or [System.currentTimeMillis] if the
     * current thread is not running inside a transaction.
     */
    val transactionStartedAtOrNow: Long
        get() = transactionStartedAt ?: System.currentTimeMillis()

    /**
     * The timestamp that the current transaction started at or null if the current thread is not
     * running inside a transaction.
     */
    val transactionStartedAt: Long?
        get() = transactionStartedAtLocal.get()

    val tableNamespace: String?
        get() = if (DBIO::_dataSource.isInitialized) _dataSource.tableNamespace else null

    private val transactionStartedAtLocal = ThreadLocal<Long>()

    private val transactionConnectionsLocal = ThreadLocal<Connection>()

    private val preparedStatementLocal = ThreadLocal<MutableMap<String, PreparedStatement>>()

    private val uniqueValueTransactionCache = ThreadLocal<MutableMap<Column, MutableMap<Any, Any>>>()

    private val uniqueValueCacheEnabledLocal = ThreadLocal<Boolean?>()

    private var uniqueValueCacheEnabled = true

    private val defaultUseCache: Boolean
        get() = uniqueValueCacheEnabledLocal.get() ?: uniqueValueCacheEnabled

    /**
     * Initialize the datasource used by the system. This function should be called before any database
     * functions of this object are called otherwise an [IllegalStateException] will be thrown.
     *
     * @see Lifecycle
     */
    fun initializeDataSource(dataSource: GeezerDataSource) {
        this._dataSource = dataSource
    }

    fun disableUniqueValueCache() {
        uniqueValueCacheEnabled = false
    }

    fun <T : Any, C> getByUniqueValue(column: C, value: Number?, useCache: Boolean = defaultUseCache, creationCallback: ((reader: ColumnSet) -> T)): T where C : NumberColumn, C : UniqueColumn =
        find(column, value, useCache, creationCallback) ?: throw IllegalArgumentException("No value for found in ${column.table} for unique column $column")

    fun <T : Any, C> getByUniqueValue(column: C, value: String?, useCache: Boolean = defaultUseCache, creationCallback: ((reader: ColumnSet) -> T)): T where C : StringColumn, C : UniqueColumn =
        find(column, value, useCache, creationCallback) ?: throw IllegalArgumentException("No value for found in ${column.table} for unique column $column")

    /**
     * Get the table object by the given primary key. Or throw an IllegalArgumentException
     */
    fun <T : Any?> getById(table: NumberIdTable, id: Number?, useCache: Boolean = defaultUseCache, creationCallback: ((reader: ColumnSet) -> T)): T =
        find(table.idColumn, id, useCache, creationCallback) ?: throw IllegalArgumentException("No value for found in $table with id $id")

    /**
     * Get the table object by the given primary key. Or return null
     */
    fun <T : Any?> findById(table: NumberIdTable, id: Number?, useCache: Boolean = defaultUseCache, creationCallback: ((reader: ColumnSet) -> T)): T? =
        find(table.idColumn, id, useCache, creationCallback)

    fun <T : Any?> findWhere(table: Table, query: String, creationCallback: ((reader: ColumnSet) -> T?)): List<T> =
        findWhere(table, query, listOf(), creationCallback)

    /**
     * Looks for a row where the given column == the given value. This function assumes this query is unique.
     * If more than one row is found, the first one in the result set will be returned. If no rows are found,
     * then null will be returned.
     *
     * @param table
     * @param query The 'where' clause of the SQL statement.
     * @param parameters list of parameters to fill in the ?'s.
     * @param orderBy optional 'order by' clause of the SQL statement.
     * @param creationCallback The callback used to create the table object from a [ResultSetReader]. The reader
     * will be positioned to the correct position when called (i.e. the callback does not call reader.next()).
     * @return The table object that matches the unique query or null if not found.
     */
    fun <T : Any?> findWhere(table: Table, query: String, parameters: List<Any>, orderBy: String?, creationCallback: ((reader: ColumnSet) -> T?)): List<T> {
        return transaction { connection ->
            var sql = "SELECT * FROM $table WHERE $query"
            if (!orderBy.isNullOrBlank()) {
                sql += " ORDER BY $orderBy"
            }

            connection.prepareStatement(sql).use { statement ->
                val writer = statement.toGeezer()
                for (parameter in parameters) {
                    writer.setAny(parameter)
                }

                val values = mutableListOf<T>()
                statement.executeQuery().use { set ->
                    val geezerSet = set.toColumnSet()
                    while (geezerSet.next()) {
                        creationCallback(geezerSet)?.let { values.add(it) }
                    }
                }
                values
            }
        }
    }

    fun <T : Any?> findWhere(table: Table, query: String, parameters: List<Any>, creationCallback: ((reader: ColumnSet) -> T?)): List<T> = findWhere(table, query, parameters, null, creationCallback)

    fun exists(table: Table, query: String, parameters: List<Any> = listOf()): Boolean = count(table, query, parameters) > 0

    fun count(table: Table, query: String, parameters: List<Any> = listOf()): Int {
        return transaction { connection ->
            val sql = "SELECT COUNT(*) FROM $table WHERE $query"
            connection.prepareStatement(sql).use { statement ->
                val writer = statement.toGeezer()
                for (parameter in parameters) {
                    writer.setAny(parameter)
                }

                statement.executeQuery().use { set ->
                    set.next()
                    set.getInt(1)
                }
            }
        }
    }

    /**
     * Gets a row where the given column == value. Or throw an IllegalArgumentException
     *
     * @param column
     * @param columnValue - The column value to perform the unique query with.
     * @param creationCallback The callback used to create the table object from a [ResultSetReader].
     *        The reader will be positioned to the correct position when called (i.e. the callback does not call reader.next()).
     * @return The table object that matches the unique query, exception otherwise.
     * @throws IllegalArgumentException
     */
    fun <T : Any?> getByUniqueColumnValue(column: Column, columnValue: Any, creationCallback: ((set: ColumnSet) -> T)): T =
        findByUniqueQuery(column.table, "LOWER($column) = ?", listOf(columnValue), creationCallback) ?: throw IllegalArgumentException("No value for found in ${column.table} for unique column $column")

    /**
     * Gets for a row where the given column == value. Or null will be returned.
     *
     * @param column
     * @param columnValue - The column value to perform the unique query with.
     * @param creationCallback The callback used to create the table object from a [ResultSetReader].
     *        The reader will be positioned to the correct position when called (i.e. the callback does not call reader.next()).
     * @return table object or null
     */
    fun <T : Any?> findByUniqueColumnValue(column: Column, columnValue: Any, creationCallback: ((set: ColumnSet) -> T)): T? = findByUniqueQuery(column.table, "$column = ?", listOf(columnValue), creationCallback)

    fun <T : Any?> findByUniqueQuery(table: Table, query: String, creationCallback: ((reader: ColumnSet) -> T?)): T? = findByUniqueQuery(table, query, listOf(), creationCallback)

    /**
     * Join column == the given value.
     * If more than one row is found, the first one in the result set will be returned.
     * If no rows are found, then null will be returned.
     *
     * @param table The table name of the object.
     * @param query
     * @param parameters
     * @param creationCallback The callback used to create the table object from a [ResultSetReader].
     *        The reader will be positioned to the correct position when called (i.e. the callback does not call reader.next()).
     * @return table object or null
     */
    fun <T : Any?> findByUniqueQuery(table: Table, query: String, parameters: List<Any>, creationCallback: ((reader: ColumnSet) -> T?)): T? {
        return transaction { connection ->
            val sql = "SELECT * FROM $table WHERE $query"
            connection.prepareStatement(sql).use { statement ->
                val writer = statement.toGeezer()
                for (parameter in parameters) {
                    writer.setAny(parameter)
                }

                statement.executeQuery().use { set ->
                    val geezerSet = set.toColumnSet()
                    if (geezerSet.next()) {
                        creationCallback(geezerSet)
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun <T : Any?> findByJoinTable(sourceId: Number, sourceJoinColumn: Column, targetJoinColumn: Column, targetTable: NumberIdTable, creationCallback: (reader: ColumnSet) -> T?): List<T> {
        if (sourceJoinColumn.table != targetJoinColumn.table) {
            throw IllegalArgumentException("Source join column table ${sourceJoinColumn.table} is not the same as target join column table ${targetJoinColumn.table}.")
        }
        return transaction { connection ->
            val ids = connection.createStatement().use { statement ->
                statement.executeQuery("SELECT $targetJoinColumn FROM ${sourceJoinColumn.table} WHERE $sourceJoinColumn = $sourceId").use { it.toColumnSet().collect { it.getLong(targetJoinColumn.name) } }
            }
            findByIds(ids, targetTable, creationCallback)
        }
    }

    fun <T : Any?> findByJoinTable(sourceUuid: String, sourceJoinColumn: StringColumn, targetJoinColumn: StringColumn, targetTable: StringIdTable, creationCallback: (reader: ColumnSet) -> T?): List<T> {
        if (sourceJoinColumn.table != targetJoinColumn.table) {
            throw IllegalArgumentException("Source join column table ${sourceJoinColumn.table} is not the same as target join column table ${targetJoinColumn.table}.")
        }
        return transaction { connection ->
            val ids = connection.prepareStatement("SELECT $targetJoinColumn FROM ${sourceJoinColumn.table} WHERE $sourceJoinColumn = ?").use { statement ->
                statement.setString(1, sourceUuid)
                statement.executeQuery().use { it.toColumnSet().collect { it[targetJoinColumn] } }
            }
            findByUuids(ids, targetTable, creationCallback)
        }
    }

    /**
     * Inserts the given source and target ids into a join table.
     * @return true if the join was added to the table or false if the join already existed.
     */
    fun insertIntoJoinTable(sourceId: String, sourceJoinColumn: StringColumn, targetId: String, targetJoinColumn: StringColumn): Boolean {
        if (sourceJoinColumn.table != targetJoinColumn.table) {
            throw IllegalArgumentException("Source join column table ${sourceJoinColumn.table} is not the same as target join column table ${targetJoinColumn.table}.")
        }

        val table = sourceJoinColumn.table
        return transaction { connection ->
            connection.prepareStatement("INSERT INTO $table($sourceJoinColumn, $targetJoinColumn) VALUES(?, ?) WHERE NOT EXISTS (SELECT * FROM $table WHERE $sourceJoinColumn = ? AND $targetJoinColumn = ?)").use { statement ->
                statement.setString(1, sourceId)
                statement.setString(2, targetId)
                statement.setString(3, sourceId)
                statement.setString(4, targetId)
                statement.executeUpdate() > 0
            }
        }
    }

    /**
     * Delete the given source and target ids into a join table.
     * @return true if the join was deleted to the table or false if the join was not found.
     */
    fun deleteFromJoinTable(sourceId: String, sourceJoinColumn: StringColumn, targetId: String, targetJoinColumn: StringColumn): Boolean {
        if (sourceJoinColumn.table != targetJoinColumn.table) {
            throw IllegalArgumentException("Source join column table ${sourceJoinColumn.table} is not the same as target join column table ${targetJoinColumn.table}.")
        }

        val table = sourceJoinColumn.table
        return transaction { connection ->
            connection.prepareStatement("DELETE $table WHERE $sourceJoinColumn = ? AND $targetJoinColumn = ?").use { statement ->
                statement.setString(1, sourceId)
                statement.setString(2, targetId)
                statement.executeUpdate() > 0
            }
        }
    }

    fun <T : Any?> findByIds(ids: List<Long>, table: NumberIdTable, creationCallback: ((reader: ColumnSet) -> T?)): List<T> {
        if (ids.isEmpty()) {
            return listOf()
        }

        return transaction { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT * FROM $table WHERE ${table.idColumn} in (${ids.joinToString(",")})").use { set ->
                    val values = mutableListOf<T>()
                    val geezerSet = set.toColumnSet()
                    while (geezerSet.next()) {
                        creationCallback(geezerSet)?.let { values.add(it) }
                    }
                    values
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun <T : Any?> findByUuids(ids: List<String>, table: Table, creationCallback: ((reader: ColumnSet) -> T?)): List<T> {
        TODO()
    }

    fun <T : Any?> findAll(table: Table, creationCallback: ((reader: ColumnSet) -> T?)): List<T> {
        return transaction { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT * FROM $table").use { set ->
                    val values = mutableListOf<T>()
                    val geezerSet = set.toColumnSet()
                    while (geezerSet.next()) {
                        creationCallback(geezerSet)?.let { values.add(it) }
                    }
                    values
                }
            }
        }
    }

    fun <T : Comparable<T>> findAllSorted(table: Table, creationCallback: ((reader: ColumnSet) -> T?)): List<T> {
        val values = findAll(table, creationCallback)
        return values.sorted()
    }

    fun <T : Any, C : NumberColumn> findByForeignKey(id: Number, foreignKeyColumn: C, createCallback: (set: ColumnSet) -> T): List<T> {
        return transaction { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT * FROM ${foreignKeyColumn.table} where $foreignKeyColumn = $id").use { set ->
                    val gs = set.toColumnSet()
                    val values = mutableListOf<T>()
                    while (gs.next()) {
                        values.add(createCallback(gs))
                    }
                    values
                }
            }
        }
    }

    fun nextSequenceValue(sequence: Sequence): Long = transaction { connection -> _dataSource.nextSequenceValue(sequence.name, connection) }

    private fun <T : Any?> find(column: Column, columnValue: Any?, useCache: Boolean, creationCallback: ((reader: ColumnSet) -> T?)): T? {
        if (columnValue == null) {
            return null
        }

        if (useCache) {
            uniqueValueTransactionCache.get()?.let { cache ->
                cache[column]?.let { columnCache ->
                    columnCache[columnValue]?.let { value ->
                        return value as T?
                    }
                }
            }
        }

        return transaction {
            prepareCachedStatement("SELECT * FROM ${column.table} where $column = ?").let { statement ->
                val geezerStatement = statement.toGeezer()
                geezerStatement.setAny(columnValue, column.sqlType)
                geezerStatement.executeQuery().use { set ->
                    if (!set.next()) {
                        null
                    } else {
                        val value = creationCallback(set.set.toColumnSet())

                        if (value != null) {
                            if (set.next()) {
                                throw IllegalStateException("More than one value retrieved for unique column ${column.fullName}")
                            }

                            if (useCache) {
                                var cache = uniqueValueTransactionCache.get()
                                if (cache == null) {
                                    cache = mutableMapOf()
                                    uniqueValueTransactionCache.set(cache)
                                }

                                var columnCache = cache[column]
                                if (columnCache == null) {
                                    columnCache = mutableMapOf()
                                    cache[column] = columnCache
                                }

                                columnCache[columnValue] = value
                            }
                        }

                        value
                    }
                }
            }
        }
    }

    fun transactionQuietly(callback: (connection: Connection) -> Unit) {
        try {
            transaction(callback)
        } catch (_: Exception) {
        }
    }

    fun transactionCacheEnabled(cacheEnabled: Boolean, callback: () -> Unit) {
        val before = uniqueValueCacheEnabledLocal.get()
        try {
            uniqueValueCacheEnabledLocal.set(cacheEnabled)
            callback()
        } finally {
            uniqueValueCacheEnabledLocal.set(before)
        }
    }

    fun commit() = transactionConnectionsLocal.get()?.commit()

    /**
     * Executes the given function using the connection from the current, thread local transaction.
     * If no transaction is assigned to the current thread,
     * the function will be executed in its own transaction and closed when callback returns.
     *
     * @param callback The callback to execute within a database transaction.
     */
    fun <T : Any?> transaction(callback: (connection: Connection) -> T): T {
        val currentTransactionConnection = transactionConnectionsLocal.get()
        return if (currentTransactionConnection != null) {
            callback(currentTransactionConnection)
        } else {
            // Don't create a database connection until (if) it's actually needed.
            TransactionConnection(_dataSource).use { connection ->
                transactionStartedAtLocal.set(System.currentTimeMillis())
                transactionConnectionsLocal.set(connection)
                preparedStatementLocal.set(mutableMapOf())
                try {
                    val returnValue = callback(connection)
                    connection.commit()
                    return returnValue
                } catch (e: Exception) {
                    try {
                        connection.rollback()
                    } catch (_: Exception) {
                    }
                    throw e
                } finally {
                    for (preparedStatement in preparedStatementLocal.get().values) {
                        preparedStatement.close()
                    }

                    transactionStartedAtLocal.set(null)
                    transactionConnectionsLocal.set(null)
                    preparedStatementLocal.set(null)
                    uniqueValueTransactionCache.set(null)
                }
            }
        }
    }

    /**
     * Shortcut function to use a statement within a database transaction.
     * @see callback The callback to execute within a database transaction.
     * @see transaction
     */
    fun <T : Any?> transactionStatement(callback: (statement: Statement) -> T): T {
        return transaction { connection ->
            connection.createStatement().use { statement ->
                callback(statement)
            }
        }
    }

    /**
     * Perform a collection objects using the given SQL within a database transaction.
     */
    fun <T : Any?> transactionCollect(sql: String, callback: (reader: ColumnSet) -> T?): List<T> {
        return transactionStatement { statement ->
            statement.executeQuery(sql).use { set ->
                set.toColumnSet().collect(callback)
            }
        }
    }

    /**
     * Perform a collection objects using the given SQL within a database transaction.
     */
    fun transactionStream(sql: String, writer: Writer, encoderCallback: ((reader: ColumnSet) -> String)) {
        return transactionStatement { statement ->
            statement.executeQuery(sql).use { set ->
                set.toColumnSet().stream(writer, encoderCallback)
            }
        }
    }

    /**
     * Creates a prepared statement that can be shared within a database transaction (by this function).
     * This function should only be called within a transaction context otherwise [IllegalStateException] will be thrown.
     *
     * @param sql The SQL used to create the prepared statement.
     * @throws IllegalStateException If the current thread is not running within a transaction.
     */
    fun prepareCachedStatement(sql: String): PreparedStatement {
        val cache = preparedStatementLocal.get() ?: throw IllegalStateException("Prepared statement cache for $sql called outside of database transaction.")
        var statement = cache[sql]
        if (statement == null || statement.isClosed) {
            statement = transactionConnectionsLocal.get().prepareStatement(sql)
            cache[sql] = statement
        }
        return statement!!
    }
}

private class TransactionConnection(val dataSource: DataSource) : Connection {

    var _realConnection: Connection? = null

    val realConnection: Connection
        get() = _realConnection ?: dataSource.connection.apply { this.autoCommit = false; _realConnection = this }

    override fun setAutoCommit(autoCommit: Boolean) {
        realConnection.autoCommit = autoCommit
    }

    override fun getAutoCommit(): Boolean = realConnection.autoCommit

    override fun commit() {
        _realConnection?.commit()
    }

    override fun close() {
        _realConnection?.close()
    }

    override fun rollback() {
        _realConnection?.rollback()
    }

    override fun rollback(savepoint: Savepoint?) {
        _realConnection?.rollback(savepoint)
    }

    override fun <T : Any?> unwrap(iface: Class<T>?): T = realConnection.unwrap(iface)

    override fun isWrapperFor(iface: Class<*>?): Boolean = realConnection.isWrapperFor(iface)

    override fun createStatement(): Statement = realConnection.createStatement()

    override fun createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement = realConnection.createStatement(resultSetType, resultSetConcurrency)

    override fun createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): Statement = realConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)

    override fun prepareStatement(sql: String?): PreparedStatement = realConnection.prepareStatement(sql)

    override fun prepareStatement(sql: String?, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement = realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency)

    override fun prepareStatement(sql: String?, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): PreparedStatement = realConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)

    override fun prepareStatement(sql: String?, autoGeneratedKeys: Int): PreparedStatement = realConnection.prepareStatement(sql, autoGeneratedKeys)

    override fun prepareStatement(sql: String?, columnIndexes: IntArray?): PreparedStatement = realConnection.prepareStatement(sql, columnIndexes)

    override fun prepareStatement(sql: String?, columnNames: Array<out String>?): PreparedStatement = realConnection.prepareStatement(sql, columnNames)

    override fun prepareCall(sql: String?): CallableStatement = realConnection.prepareCall(sql)

    override fun prepareCall(sql: String?, resultSetType: Int, resultSetConcurrency: Int): CallableStatement = realConnection.prepareCall(sql, resultSetType, resultSetConcurrency)

    override fun prepareCall(sql: String?, resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): CallableStatement = realConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability)

    override fun nativeSQL(sql: String?): String = realConnection.nativeSQL(sql)

    override fun isClosed(): Boolean = realConnection.isClosed

    override fun getMetaData(): DatabaseMetaData = realConnection.metaData

    override fun setReadOnly(readOnly: Boolean) {
        realConnection.isReadOnly = readOnly
    }

    override fun isReadOnly(): Boolean = realConnection.isReadOnly

    override fun setCatalog(catalog: String?) {
        realConnection.catalog = catalog
    }

    override fun getCatalog(): String = realConnection.catalog

    override fun setTransactionIsolation(level: Int) {
        realConnection.transactionIsolation = level
    }

    override fun getTransactionIsolation(): Int = realConnection.transactionIsolation

    override fun getWarnings(): SQLWarning = realConnection.warnings

    override fun clearWarnings() {
        realConnection.clearWarnings()
    }

    override fun getTypeMap(): MutableMap<String, Class<*>> = realConnection.typeMap

    override fun setTypeMap(map: MutableMap<String, Class<*>>?) {
        realConnection.typeMap = map
    }

    override fun setHoldability(holdability: Int) {
        realConnection.holdability = holdability
    }

    override fun getHoldability(): Int = realConnection.holdability

    override fun setSavepoint(): Savepoint = realConnection.setSavepoint()

    override fun setSavepoint(name: String?): Savepoint = realConnection.setSavepoint(name)

    override fun releaseSavepoint(savepoint: Savepoint?) {
        realConnection.releaseSavepoint(savepoint)
    }

    override fun createClob(): Clob = realConnection.createClob()

    override fun createBlob(): Blob = realConnection.createBlob()

    override fun createNClob(): NClob = realConnection.createNClob()

    override fun createSQLXML(): SQLXML = realConnection.createSQLXML()

    override fun isValid(timeout: Int): Boolean = realConnection.isValid(timeout)

    override fun setClientInfo(name: String?, value: String?) {
        realConnection.setClientInfo(name, value)
    }

    override fun setClientInfo(properties: Properties?) {
        realConnection.clientInfo = properties
    }

    override fun getClientInfo(name: String?): String = realConnection.getClientInfo(name)

    override fun getClientInfo(): Properties = realConnection.clientInfo

    override fun createArrayOf(typeName: String?, elements: Array<out Any>?): java.sql.Array = realConnection.createArrayOf(typeName, elements)

    override fun createStruct(typeName: String?, attributes: Array<out Any>?): Struct = realConnection.createStruct(typeName, attributes)

    override fun setSchema(schema: String?) {
        realConnection.schema = schema
    }

    override fun getSchema(): String = realConnection.schema

    override fun abort(executor: Executor?) {
        realConnection.abort(executor)
    }

    override fun setNetworkTimeout(executor: Executor?, milliseconds: Int) {
        realConnection.setNetworkTimeout(executor, milliseconds)
    }

    override fun getNetworkTimeout(): Int = realConnection.networkTimeout
}
