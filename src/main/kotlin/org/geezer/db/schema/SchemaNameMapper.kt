package org.geezer.db.schema

@FunctionalInterface
interface SchemaNameMapper {
    fun mapName(dbName: String): String
}
