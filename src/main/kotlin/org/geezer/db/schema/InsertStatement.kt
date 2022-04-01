package org.geezer.db.schema

import org.geezer.db.GeezerDbUtils

class InsertStatement(table: Table) : TableValuesStatement(table) {

    override val sql: String
        get() = "INSERT INTO $table(${columnsValues.joinToString(",") { it.first.toString() }}) VALUES (${columnsValues.joinToString(",") { "?" }})"

    override fun execute() {
        val missingPrimaryKeys = table.primaryKeyColumns.filter { pkey -> columnsValues.none { it.first === pkey } }
        if (missingPrimaryKeys.isNotEmpty()) {
            if (missingPrimaryKeys.size == 1) {
                throw IllegalStateException("Unable to insert into table $table without a value for primary key column ${missingPrimaryKeys[0]}")
            } else {
                throw IllegalStateException("Unable to insert into table $table without a value for the primary key columns ${missingPrimaryKeys.joinToString()}")
            }
        }

        val missingRequiredColumns = table.columns.filter { column -> column !is OptionalColumn && columnsValues.none { it.first.name == column.name } }
        if (missingRequiredColumns.isNotEmpty()) {
            throw IllegalStateException("Unable to insert into table $table without a value for the non-optional columns ${missingRequiredColumns.joinToString()}")
        }

        DBIO.transaction {
            DBIO.prepareCachedStatement(sql).use { statement ->
                for (i in columnsValues.indices) {
                    val (column, value) = columnsValues[i]
                    if (value == null) {
                        setNull(column, i + 1, statement)
                    } else {
                        GeezerDbUtils.setValue(value, column.sqlType, i + 1, statement)
                    }
                }

                val updated = statement.executeUpdate()
                if (updated != 1) {
                    throw IllegalStateException("Insert into table $table completed with $updated rows updated. Was expecting only one.")
                }
            }
        }
    }
}
