package org.geezer.db.schema

abstract class Column(val name: String, val table: Table, val sqlType: Int) {
    val fullName: String
        get() = "${table.name}.$name"

    val primaryKey: Boolean by lazy { this is PrimaryKeyColumn }

    val optional: Boolean by lazy { this is OptionalColumn }

    val unique: Boolean by lazy { this is UniqueColumn }

    val autoIncrements: Boolean by lazy { this is AutoIncrementColumn }

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean = (other is Column) && fullName == other.fullName

    override fun hashCode(): Int = fullName.hashCode()
}

interface OptionalColumn

interface PrimaryKeyColumn

interface ValueColumn

interface UniqueColumn

interface AutoIncrementColumn

abstract class NumberColumn(name: String, table: Table, sqlType: Int) : Column(name, table, sqlType)

abstract class BooleanColumn(name: String, table: Table, sqlType: Int) : Column(name, table, sqlType)

abstract class OptionalBooleanColumn(name: String, table: Table, sqlType: Int) : BooleanColumn(name, table, sqlType), OptionalColumn

abstract class ByteColumn(name: String, table: Table, sqlType: Int) : NumberColumn(name, table, sqlType)

abstract class OptionalByteColumn(name: String, table: Table, sqlType: Int) : ByteColumn(name, table, sqlType), OptionalColumn

abstract class ShortColumn(name: String, table: Table, sqlType: Int) : NumberColumn(name, table, sqlType)

abstract class OptionalShortColumn(name: String, table: Table, sqlType: Int) : ShortColumn(name, table, sqlType), OptionalColumn

abstract class IntColumn(name: String, table: Table, sqlType: Int) : NumberColumn(name, table, sqlType)

abstract class OptionalIntColumn(name: String, table: Table, sqlType: Int) : IntColumn(name, table, sqlType), OptionalColumn

abstract class LongColumn(name: String, table: Table, sqlType: Int) : NumberColumn(name, table, sqlType)

abstract class OptionalLongColumn(name: String, table: Table, sqlType: Int) : LongColumn(name, table, sqlType), OptionalColumn

abstract class FloatColumn(name: String, table: Table, sqlType: Int) : NumberColumn(name, table, sqlType)

abstract class OptionalFloatColumn(name: String, table: Table, sqlType: Int) : FloatColumn(name, table, sqlType), OptionalColumn

abstract class DoubleColumn(name: String, table: Table, sqlType: Int) : NumberColumn(name, table, sqlType)

abstract class OptionalDoubleColumn(name: String, table: Table, sqlType: Int) : DoubleColumn(name, table, sqlType), OptionalColumn

abstract class BigDecimalColumn(name: String, table: Table, sqlType: Int) : NumberColumn(name, table, sqlType)

abstract class OptionalBigDecimalColumn(name: String, table: Table, sqlType: Int) : BigDecimalColumn(name, table, sqlType)

abstract class StringColumn(name: String, table: Table, sqlType: Int, val maxLength: Int?) : Column(name, table, sqlType)

abstract class OptionalStringColumn(name: String, table: Table, sqlType: Int, maxLength: Int?) : StringColumn(name, table, sqlType, maxLength), OptionalColumn

abstract class BinaryColumn(name: String, table: Table, sqlType: Int) : Column(name, table, sqlType)

abstract class OptionalBinaryColumn(name: String, table: Table, sqlType: Int) : BinaryColumn(name, table, sqlType), OptionalColumn

abstract class DateColumn(name: String, table: Table, sqlType: Int) : Column(name, table, sqlType)

abstract class OptionalDateColumn(name: String, table: Table, sqlType: Int) : DateColumn(name, table, sqlType), OptionalColumn

abstract class TimeColumn(name: String, table: Table, sqlType: Int) : Column(name, table, sqlType)

abstract class OptionalTimeColumn(name: String, table: Table, sqlType: Int) : TimeColumn(name, table, sqlType), OptionalColumn

abstract class TimestampColumn(name: String, table: Table, sqlType: Int) : Column(name, table, sqlType)

abstract class OptionalTimestampColumn(name: String, table: Table, sqlType: Int) : TimestampColumn(name, table, sqlType), OptionalColumn
