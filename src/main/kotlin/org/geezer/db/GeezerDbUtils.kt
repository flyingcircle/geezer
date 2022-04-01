package org.geezer.db

import java.io.InputStream
import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types
import java.util.Date
import java.util.GregorianCalendar

internal object GeezerDbUtils {
    val StringTypes = listOf(Types.VARCHAR, Types.LONGNVARCHAR, Types.CLOB, Types.NCLOB, Types.SQLXML, Types.NVARCHAR, Types.LONGVARCHAR, Types.CHAR)

    val NumberTypes = listOf(Types.BIT, Types.SMALLINT, Types.INTEGER, Types.DECIMAL, Types.FLOAT, Types.DOUBLE, Types.TINYINT, Types.BIGINT, Types.NUMERIC)

    const val DefaultListSeparator = "\t"

    fun setValue(value: Any, sqlType: Int?, index: Int, statement: PreparedStatement, timeZoneCalendar: GregorianCalendar? = null) {
        when (value) {
            is Long -> {
                when (sqlType) {
                    Types.DATE -> statement.setDate(index, java.sql.Date(value))
                    Types.TIME -> statement.setTime(index, Time(value))
                    Types.TIME_WITH_TIMEZONE -> if (timeZoneCalendar == null) statement.setTime(index, Time(value)) else statement.setTime(index, Time(value), timeZoneCalendar)
                    Types.TIMESTAMP -> statement.setTimestamp(index, Timestamp(value))
                    Types.TIMESTAMP_WITH_TIMEZONE -> if (timeZoneCalendar == null) statement.setTimestamp(index, Timestamp(value)) else statement.setTimestamp(index, Timestamp(value), timeZoneCalendar)
                    else -> statement.setLong(index, value)
                }
            }

            is Boolean -> {
                if (NumberTypes.contains(sqlType)) {
                    statement.setByte(index, if (value) 1 else 0)
                } else {
                    statement.setBoolean(index, value)
                }
            }
            is Timestamp -> statement.setTimestamp(index, value) // Timestamp extends Date so we need to make sure it comes here first
            is Date -> setValue(value.time, sqlType, index, statement, timeZoneCalendar)
            is Int -> statement.setInt(index, value)
            is Short -> statement.setShort(index, value)
            is Byte -> statement.setByte(index, value)
            is String -> statement.setString(index, value)
            is Float -> statement.setFloat(index, value)
            is Double -> statement.setDouble(index, value)
            is ByteArray -> statement.setBytes(index, value)
            is InputStream -> statement.setBinaryStream(index, value)
            is BigDecimal -> statement.setBigDecimal(index, value)

            else -> {
                if (StringTypes.contains(sqlType)) {
                    statement.setString(index, value.toString())
                } else {
                    throw IllegalArgumentException("Invalid prepared statement parameter $value of type type ${value.javaClass}")
                }
            }
        }
    }
}
