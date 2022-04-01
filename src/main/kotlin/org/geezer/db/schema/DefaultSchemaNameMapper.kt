package org.geezer.db.schema

object DefaultSchemaNameMapper : SchemaNameMapper {
    override fun mapName(dbName: String): String {
        val segments = dbName.split("_").filter { it.isNotBlank() }.toMutableList()
        for (i in segments.indices) {
            val segment = segments[i]
            segments[i] = "${segment[0].uppercaseChar()}${segment.substring(1).lowercase()}"
        }

        return segments.joinToString("")
    }
}
