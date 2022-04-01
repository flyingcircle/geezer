package org.geezer.db.schema

class SchemaWriteConfiguration(
    val schemaPackage: String,
    val sequenceNames: List<String> = listOf(),
    val tableSchema: String? = "PUBLIC",
    val columnTypeMapper: ColumnTypeMapper? = null,
    val nameMapper: SchemaNameMapper = DefaultSchemaNameMapper,
    val tableTypes: List<String> = listOf("TABLE", "VIEW"),
    val optionalMapper: OptionalMapper? = null
)
