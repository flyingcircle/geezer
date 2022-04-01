package org.geezer.db.schema

import org.geezer.db.GeezerDbUtils

class DeleteByPrimaryKeysStatement(table: Table) : TableValuesStatement(table) {

    override val sql: String
        get() = "DELETE FROM $table WHERE ${columnsValues.filter { it.first.primaryKey }.joinToString(" AND ") { "${it.first} = ?" }}"

    override fun assertColumnInTable(column: Column) {
        super.assertColumnInTable(column)
        if (!column.primaryKey) {
            throw IllegalArgumentException("Column ${column.fullName} is not a primary key of table $table. Only primary key values can be added to a delete by primary keys statement.")
        }
    }

    override fun execute() {
        val missingPrimaryKeys = table.primaryKeyColumns.filter { pkey -> columnsValues.none { it.first == pkey } }
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
                for ((column, value) in columnsValues) {
                    if (value == null) {
                        setNull(column, parameterIndex++, statement)
                    } else {
                        GeezerDbUtils.setValue(value, column.sqlType, parameterIndex++, statement)
                    }
                }

                val deleted = statement.executeUpdate()
                if (deleted != 1) {
                    throw IllegalStateException("Update table $table by primary keys completed with $deleted rows updated. Was expecting only one.")
                }
            }
        }
    }
}
