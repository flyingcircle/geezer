package org.geezer.db

import java.io.Closeable
import java.io.InputStream
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.util.Date

fun PreparedStatement.toGeezer(): GeezerPreparedStatement = GeezerPreparedStatement(this)

/**
 * Supports optional types and supports an internal parameter index counter so the parameter index doesn't
 * have to be supplied each time as long as the parameter set functions are called in the correct order.
 */
class GeezerPreparedStatement(val realStatement: PreparedStatement) : Closeable {

    private var parameterIndexCounter = 1

    fun execute(): Boolean = realStatement.execute()

    fun executeQuery(): GeezerResultSet = realStatement.executeQuery().toGeezer()

    fun executeUpdate(): Int = realStatement.executeUpdate()

    fun set(value: Boolean?, sqlType: Int = Types.BOOLEAN): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: Byte?, sqlType: Int = Types.TINYINT): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: Short?, sqlType: Int = Types.SMALLINT): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: Int?, sqlType: Int = Types.INTEGER): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: Long?, sqlType: Int = Types.BIGINT): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: Float?, sqlType: Int = Types.FLOAT): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: Double?, sqlType: Int = Types.DOUBLE): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: String?, sqlType: Int = Types.VARCHAR): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: ByteArray?, sqlType: Int = Types.BINARY): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: InputStream?, sqlType: Int = Types.BINARY): GeezerPreparedStatement = setAny(value, sqlType)

    fun setDate(value: Long?): GeezerPreparedStatement = setAny(value, Types.DATE)

    fun setTime(value: Long?): GeezerPreparedStatement = setAny(value, Types.TIME)

    fun setTimestamp(value: Long?): GeezerPreparedStatement = setAny(value, Types.TIMESTAMP)

    fun set(value: Date?, sqlType: Int = Types.DATE): GeezerPreparedStatement = setAny(value, sqlType)

    fun set(value: Timestamp?): GeezerPreparedStatement = setAny(value, Types.TIMESTAMP)

    fun set(values: List<*>, separator: String = GeezerDbUtils.DefaultListSeparator, sqlType: Int = Types.VARCHAR): GeezerPreparedStatement = setAny(if (values.isEmpty()) null else values.joinToString(separator) { it.toString() }, sqlType)

    fun setAny(value: Any?, sqlType: Int): GeezerPreparedStatement {
        if (value == null) {
            realStatement.setNull(parameterIndexCounter++, sqlType)
        } else {
            GeezerDbUtils.setValue(value, sqlType, parameterIndexCounter++, realStatement)
        }
        return this
    }

    fun setAny(value: Any): GeezerPreparedStatement {
        GeezerDbUtils.setValue(value, null, parameterIndexCounter++, realStatement)
        return this
    }

    override fun close() {
        realStatement.close()
    }
}
