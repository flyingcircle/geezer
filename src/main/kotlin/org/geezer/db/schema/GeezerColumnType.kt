package org.geezer.db.schema

import kotlin.reflect.KClass

enum class GeezerColumnType(val geezerClass: KClass<*>) {
    BOOLEAN(BooleanColumn::class),
    BYTE(ByteColumn::class),
    SHORT(ShortColumn::class),
    INT(IntColumn::class),
    LONG(LongColumn::class),
    FLOAT(FloatColumn::class),
    DOUBLE(DoubleColumn::class),
    BIG_DECIMAL(BigDecimalColumn::class),
    STRING(StringColumn::class),
    BINARY(BinaryColumn::class),
    DATE(DateColumn::class),
    TIME(TimeColumn::class),
    TIMESTAMP(TimestampColumn::class)
}
