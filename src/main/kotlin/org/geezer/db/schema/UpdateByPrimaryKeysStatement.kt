package org.geezer.db.schema

import org.geezer.db.GeezerDbUtils

class UpdateByPrimaryKeysStatement(table: Table) : TableValuesStatement(table) {

    val updateColumnValues: List<Pair<Column, Any?>>
        get() = columnsValues.filter { !it.first.primaryKey }

    override val sql: String
        get() = "UPDATE $table SET ${updateColumnValues.joinToString(", ") { "${it.first} = ?" }} WHERE ${columnsValues.filter { it.first.primaryKey }.joinToString(" AND ") { "${it.first} = ?" }}"

    override fun execute() {
        if (updateColumnValues.isEmpty()) {
            return
        }

        val missingPrimaryKeys = table.primaryKeyColumns.filter { pkey -> columnsValues.none { it.first === pkey } }
        if (missingPrimaryKeys.isNotEmpty()) {
            if (missingPrimaryKeys.size == 1) {
                throw IllegalStateException("Unable to update table $table without a value for primary key column ${missingPrimaryKeys[0]}")
            } else {
                throw IllegalStateException("Unable to update table $table without a value for the primary key columns ${missingPrimaryKeys.joinToString()}")
            }
        }

        DBIO.transaction {
            DBIO.prepareCachedStatement(sql).use { statement ->
                var parameterIndex = 1
                for ((column, value) in columnsValues.filter { !it.first.primaryKey }) {
                    if (value == null) {
                        setNull(column, parameterIndex++, statement)
                    } else {
                        GeezerDbUtils.setValue(value, column.sqlType, parameterIndex++, statement)
                    }
                }

                for ((column, value) in columnsValues.filter { it.first.primaryKey }) {
                    if (value == null) {
                        setNull(column, parameterIndex++, statement)
                    } else {
                        GeezerDbUtils.setValue(value, column.sqlType, parameterIndex++, statement)
                    }
                }

                val updated = statement.executeUpdate()
                if (updated != 1) {
                    throw IllegalStateException("Update table $table by primary keys completed with $updated rows updated. Was expecting only one.")
                }
            }
        }
    }
}
