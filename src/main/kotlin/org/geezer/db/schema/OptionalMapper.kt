package org.geezer.db.schema

interface OptionalMapper {
    fun isOptional(tableType: String, table: String, column: String): Boolean?
}
