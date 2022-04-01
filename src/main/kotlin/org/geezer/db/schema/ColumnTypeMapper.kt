package org.geezer.db.schema

@FunctionalInterface
interface ColumnTypeMapper {
    fun mapColumnType(table: String, column: String, sqlType: Int, precision: Int?, scale: Int?): GeezerColumnType?
}
