package org.geezer.db.schema

import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.Types
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.reflect.full.isSubclassOf

object SchemaWriter {
    fun writeSchema(configuration: SchemaWriteConfiguration, connection: Connection): String {
        var schema = """//This file was auto-generated at ${SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(Date())}
package ${configuration.schemaPackage}

import BigDecimalColumn
import BinaryColumn
import BooleanColumn
import Column
import DoubleColumn
import IntColumn
import LongColumn
import NumberIdTable
import OptionalBooleanColumn
import OptionalIntColumn
import OptionalLongColumn
import OptionalStringColumn
import OptionalTimestampColumn
import PrimaryKeyColumn
import Sequence
import StringColumn
import Table
import TimestampColumn
import UniqueColumn
import ValueColumn
import java.sql.Types""".trimMargin()

        for (sequenceName in configuration.sequenceNames) {
            schema += "\n\nobject ${configuration.nameMapper.mapName(sequenceName)} : Sequence(\"$sequenceName\")"
        }

        val dbMeta = connection.metaData
        val tableSchemaNames = mutableListOf<Triple<String, String, String>>()
        dbMeta.getTables(null, null, null, null).use { set ->
            while (set.next()) {
                val tableSchema = set.getString("TABLE_SCHEM")
                if (configuration.tableSchema == null || tableSchema.equals(tableSchema, true)) {
                    val tableType = set.getString("TABLE_TYPE")
                    if (configuration.tableTypes.isEmpty() || configuration.tableTypes.any { tableType.equals(it, true) }) {
                        val tableName = set.getString("TABLE_NAME")
                        tableSchemaNames.add(Triple(tableSchema, tableName, tableType))
                    }
                }
            }
        }
        for ((tableSchema, tableName, tableType) in tableSchemaNames) {
            schema += "\n\n${getTableDefinition(tableSchema, tableName, tableType, dbMeta, configuration)}"
        }

        return schema
    }

    fun getTableDefinition(tableSchema: String, tableName: String, tableType: String, dbMetaData: DatabaseMetaData, configuration: SchemaWriteConfiguration): String {
        val columnDefs = mutableListOf<ColumnDef>()
        dbMetaData.getColumns(null, tableSchema, tableName, null).use { set ->
            while (set.next()) {
                val columnName = set.getString("COLUMN_NAME")
                val sqlType = set.getInt("DATA_TYPE")
                val size = set.getInt("COLUMN_SIZE")
                var decimalDigits: Int? = set.getInt("DECIMAL_DIGITS")
                if (set.wasNull()) {
                    decimalDigits = null
                }

                val optional = configuration.optionalMapper?.isOptional(tableType, tableName, columnName) ?: (set.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls)
                val autoIncrement = "yes".equals(set.getString("IS_AUTOINCREMENT"), true)

                val type = getColumnColumnType(tableName, columnName, sqlType, size, decimalDigits, configuration.columnTypeMapper)
                columnDefs.add(ColumnDef(type, columnName, sqlType, size, decimalDigits, optional, autoIncrement))
            }
        }

        dbMetaData.getPrimaryKeys(null, tableSchema, tableName).use { set ->
            while (set.next()) {
                val name = set.getString("COLUMN_NAME")
                columnDefs.firstOrNull { it.name == name }?.let { it.primaryKey = true }
            }
        }

        val primaryKeys = columnDefs.filter { it.primaryKey }
        if (primaryKeys.size == 1) {
            primaryKeys[0].unique = true
        }

        val uniqueIndexes = mutableMapOf<String, MutableList<ColumnDef>>()
        dbMetaData.getIndexInfo(null, tableSchema, tableName, true, false).use { set ->
            while (set.next()) {
                val columnName = set.getString("COLUMN_NAME")
                columnDefs.firstOrNull { it.name == columnName }?.let { columnDef ->
                    val indexName = set.getString("INDEX_NAME")
                    val indexColumns = uniqueIndexes[indexName] ?: run {
                        val indexColumns = mutableListOf<ColumnDef>()
                        uniqueIndexes[indexName] = indexColumns
                        indexColumns
                    }
                    indexColumns.add(columnDef)
                }
            }
        }

        for (uniqueIndexColumns in uniqueIndexes.values) {
            if (uniqueIndexColumns.size == 1) {
                uniqueIndexColumns[0].unique = true
            }
        }

        val suffix = if (tableType.lowercase().contains("view")) "View" else "Table"
        val tableObjectName = "${configuration.nameMapper.mapName(tableName)}$suffix"

        val tableType = if (primaryKeys.size == 1) {
            if (primaryKeys[0].columnType == GeezerColumnType.STRING) {
                StringIdTable::class
            } else if (primaryKeys[0].columnType.geezerClass.isSubclassOf(NumberColumn::class)) {
                NumberIdTable::class
            } else {
                Table::class
            }
        } else {
            Table::class
        }

        var table = """object $tableObjectName : ${tableType.simpleName}("$tableName") {"""

        for (columnDefinition in columnDefs.sorted()) {
            table += "\n    ${columnDefinition.toString(tableObjectName, configuration.nameMapper)}"
        }
        table += "\n\n    override val columns: List<${Column::class.simpleName}>" +
            "\n        get() = listOf(${columnDefs.joinToString(", ") { configuration.nameMapper.mapName(it.name) }})"
        table += "\n}"
        return table
    }

    fun getColumnColumnType(table: String, column: String, sqlType: Int, size: Int, decimalDigits: Int?, columnTypeMapper: ColumnTypeMapper?): GeezerColumnType {
        return columnTypeMapper?.mapColumnType(table, column, sqlType, size, decimalDigits)
            ?: when (sqlType) {
                Types.VARCHAR,
                Types.CHAR,
                Types.NCHAR,
                Types.CLOB,
                Types.NCLOB,
                Types.SQLXML,

                Types.LONGVARCHAR,
                Types.LONGNVARCHAR -> GeezerColumnType.STRING

                Types.LONGVARBINARY,
                Types.BINARY,
                Types.VARBINARY,
                Types.BLOB -> GeezerColumnType.BINARY

                Types.TINYINT -> GeezerColumnType.BYTE
                Types.SMALLINT -> GeezerColumnType.SHORT
                Types.INTEGER -> GeezerColumnType.INT
                Types.BIGINT -> GeezerColumnType.LONG
                Types.FLOAT -> GeezerColumnType.FLOAT
                Types.DOUBLE -> GeezerColumnType.DOUBLE
                Types.ROWID -> GeezerColumnType.INT

                Types.DECIMAL,
                Types.NUMERIC -> {
                    if (size == 1) {
                        return GeezerColumnType.BOOLEAN
                    } else if (decimalDigits == null || decimalDigits == 0) {
                        if (size <= 3) {
                            GeezerColumnType.BYTE
                        } else if (size <= 16) {
                            GeezerColumnType.SHORT
                        } else if (size <= 32) {
                            GeezerColumnType.INT
                        } else {
                            GeezerColumnType.LONG
                        }
                    } else if (size <= 32 && decimalDigits <= 7) {
                        GeezerColumnType.FLOAT
                    } else if (size <= 64 && decimalDigits <= 16) {
                        GeezerColumnType.DOUBLE
                    } else {
                        GeezerColumnType.BIG_DECIMAL
                    }
                }

                Types.BOOLEAN -> GeezerColumnType.BOOLEAN

                Types.DATE -> GeezerColumnType.DATE

                Types.TIME,
                Types.TIME_WITH_TIMEZONE -> GeezerColumnType.TIME

                Types.TIMESTAMP,
                Types.TIMESTAMP_WITH_TIMEZONE -> GeezerColumnType.TIMESTAMP

                else -> throw IllegalArgumentException("Unsupported SQL type $sqlType")
            }
    }

    private class ColumnDef(val columnType: GeezerColumnType, val name: String, val sqlType: Int, val size: Int, val decimalDigits: Int?, val optional: Boolean, val autoIncrement: Boolean, var primaryKey: Boolean = false, var unique: Boolean = false) : Comparable<ColumnDef> {
        override fun compareTo(other: ColumnDef): Int {
            return if (primaryKey && !other.primaryKey) {
                -1
            } else if (other.primaryKey && !primaryKey) {
                1
            } else if (unique && !other.unique) {
                -1
            } else if (other.unique && !unique) {
                1
            } else {
                name.compareTo(other.name)
            }
        }

        fun toString(tableObjectName: String, nameMapper: SchemaNameMapper): String {
            var columnStr = "object ${nameMapper.mapName(name)} : "
            if (optional) {
                columnStr += "Optional"
            }
            columnStr += """${columnType.geezerClass.simpleName}("$name", $tableObjectName, ${toSqlType(sqlType)}"""
            if (columnType == GeezerColumnType.STRING) {
                columnStr += ", $size"
            }
            columnStr += ")"

            if (primaryKey) {
                columnStr += ", ${PrimaryKeyColumn::class.simpleName}"
            }

            if (unique) {
                columnStr += ", ${UniqueColumn::class.simpleName}"
            }

            if (!primaryKey) {
                columnStr += ", ${ValueColumn::class.simpleName}"
            }

            if (autoIncrement) {
                columnStr += ", ${AutoIncrementColumn::class.simpleName}"
            }

            return columnStr
        }
    }

    private fun toSqlType(sqlType: Int): String {
        val type = Types::class.java
        type.declaredFields.firstOrNull { it.get(type) == sqlType }?.let {
            return "Types.${it.name}"
        }
        return sqlType.toString()
    }
}
