package org.geezer.db.schema

import java.io.InputStream
import java.sql.PreparedStatement
import java.util.Date

abstract class TableValuesStatement(val table: Table) {

    abstract val sql: String

    abstract fun execute()

    protected val columnsValues = mutableListOf<Pair<Column, Any?>>()

    protected fun setNull(column: Column, index: Int, statement: PreparedStatement) {
        if (column !is OptionalColumn) {
            throw IllegalArgumentException("Cannot set non-optional column ${column.fullName} to null.")
        }

        statement.setNull(index, column.sqlType)
    }

    fun setNumber(column: NumberColumn, value: Number) {
        when (column) {
            is ByteColumn -> set(column, value.toByte())
            is ShortColumn -> set(column, value.toShort())
            is IntColumn -> set(column, value.toInt())
            is LongColumn -> set(column, value.toLong())
            is FloatColumn -> set(column, value.toFloat())
            is DoubleColumn -> set(column, value.toDouble())
            else -> throw IllegalArgumentException("Unsupported number column type $column")
        }
    }

    private fun <T> commonSet(column: Column, value: T): TableValuesStatement {
        assertColumnInTable(column)
        columnsValues.add(Pair(column, value))
        return this
    }

    operator fun set(column: BooleanColumn, value: Boolean): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalBooleanColumn, value: Boolean?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: ByteColumn, value: Byte): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalByteColumn, value: Byte?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: ShortColumn, value: Short): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalShortColumn, value: Short?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: IntColumn, value: Int): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalIntColumn, value: Int?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: LongColumn, value: Long): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalLongColumn, value: Long?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: FloatColumn, value: Float): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalFloatColumn, value: Float?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: DoubleColumn, value: Double): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalDoubleColumn, value: Double?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: StringColumn, value: String): TableValuesStatement = set(column, value, true)

    fun set(column: StringColumn, value: String, trimIfNecessary: Boolean): TableValuesStatement {
        assertColumnInTable(column)
        columnsValues.add(Pair(column, assertValidStringValue(column, value, trimIfNecessary)))
        return this
    }

    operator fun set(column: OptionalStringColumn, value: String?): TableValuesStatement = set(column, value, true)

    fun set(column: OptionalStringColumn, value: String?, trimIfNecessary: Boolean): TableValuesStatement {
        assertColumnInTable(column)
        if (value != null) {
            assertValidStringValue(column, value, trimIfNecessary)
            columnsValues.add(Pair(column, assertValidStringValue(column, value, trimIfNecessary)))
        } else {
            columnsValues.add(Pair(column, null))
        }
        return this
    }

    operator fun set(column: BinaryColumn, value: ByteArray): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalBinaryColumn, value: ByteArray?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: BinaryColumn, value: InputStream): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalBinaryColumn, value: InputStream?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: DateColumn, value: Long): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalDateColumn, value: Long?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: DateColumn, value: Date): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalDateColumn, value: Date?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: TimestampColumn, value: Long): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalTimestampColumn, value: Long?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: TimestampColumn, value: Date): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalTimestampColumn, value: Date?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: TimeColumn, value: Int): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalTimeColumn, value: Int?): TableValuesStatement = commonSet(column, value)

    operator fun set(column: TimeColumn, value: Date): TableValuesStatement = commonSet(column, value)

    operator fun set(column: OptionalTimeColumn, value: Date?): TableValuesStatement = commonSet(column, value)

    protected open fun assertColumnInTable(column: Column) {
        if (table.columns.none { it.name == column.name }) {
            throw IllegalArgumentException("Column ${column.fullName} is not in table $table.")
        }
    }

    protected fun assertValidStringValue(column: StringColumn, value: String, trimIfNecessary: Boolean): String {
        column.maxLength?.let {
            if (value.length > it) {
                if (trimIfNecessary) {
                    return value.substring(0, it)
                } else {
                    throw IllegalArgumentException("Max length for column ${column.fullName} of ${column.maxLength} exceeded by value of length ${value.length}")
                }
            }
        }
        return value
    }
}
