package org.geezer.db.schema

import org.geezer.db.BaseSet
import org.geezer.db.GeezerDbUtils
import java.io.Writer
import java.math.BigDecimal
import java.sql.ResultSet

fun ResultSet.toColumnSet(): ColumnSet = ColumnSet(this)

open class ColumnSet(set: ResultSet) : BaseSet(set) {

    operator fun get(column: BooleanColumn): Boolean = getBoolean(column.name)

    operator fun get(column: OptionalBooleanColumn): Boolean? = getOptionalBoolean(column.name)

    operator fun get(column: IntColumn): Int = getInt(column.name)

    operator fun get(column: OptionalIntColumn): Int? = getOptionalInt(column.name)

    operator fun get(column: LongColumn): Long = getLong(column.name)

    operator fun get(column: OptionalLongColumn): Long? = getOptionalLong(column.name)

    operator fun get(column: FloatColumn): Float = getFloat(column.name)

    operator fun get(column: OptionalFloatColumn): Float? = getOptionalFloat(column.name)

    operator fun get(column: DoubleColumn): Double = getDouble(column.name)

    operator fun get(column: OptionalDoubleColumn): Double? = getOptionalDouble(column.name)

    operator fun get(column: BigDecimalColumn): BigDecimal = getBigDecimal(column.name)

    operator fun get(column: OptionalBigDecimalColumn): BigDecimal? = getOptionalBigDecimal(column.name)

    operator fun get(column: StringColumn): String = getString(column.name)

    operator fun get(column: OptionalStringColumn): String? = getOptionalString(column.name)

    operator fun get(column: TimestampColumn): Long = getTimestamp(column.name)

    operator fun get(column: OptionalTimestampColumn): Long? = getOptionalTimestamp(column.name)

    operator fun get(column: BinaryColumn): ByteArray = getBytes(column.name)

    operator fun get(column: OptionalBinaryColumn): ByteArray? = getOptionalBytes(column.name)

    fun getIntList(column: StringColumn, separator: String = GeezerDbUtils.DefaultListSeparator): List<Int> = getOptionalString(column.name)?.split(separator)?.mapNotNull { it.toIntOrNull() } ?: listOf()

    fun getStringList(column: StringColumn, separator: String = GeezerDbUtils.DefaultListSeparator): List<String> = getOptionalString(column.name)?.split(separator) ?: listOf()

    fun hasColumn(column: Column): Boolean = hasColumn(column.name)

    fun <T : Any> collect(creationCallback: ((reader: ColumnSet) -> T?)): List<T> {
        val collection = mutableListOf<T>()
        while (next()) {
            val t = creationCallback(this)
            if (t != null) {
                collection.add(t)
            }
        }
        return collection
    }

    /**
     * Streams the results of the given reader to the writer.
     */
    fun stream(writer: Writer, encoderCallback: ((reader: ColumnSet) -> String)) {
        while (next()) {
            writer.write(encoderCallback(this))
        }
    }
}
