package org.geezer.db.schema

abstract class Table(private val _name: String) {
    abstract val columns: List<Column>

    val name: String
        get() = DBIO.tableNamespace?.let { "$it.$_name" } ?: _name

    val primaryKeyColumns: List<Column>
        get() = columns.filter { it.primaryKey }

    val primaryKeyColumn: Column?
        get() = if (primaryKeyColumns.size == 1) primaryKeyColumns[0] else null

    val nonPrimaryKeyColumns: List<Column>
        get() = columns.filter { !it.primaryKey }

    val updatable: Boolean
        get() = primaryKeyColumns.isNotEmpty()

    val deletable: Boolean
        get() = primaryKeyColumns.isNotEmpty()

    override fun toString(): String = name

    fun getInsertSql(columnsToInclude: List<Column>): String {
        columnsToInclude.filter { !columns.contains(it) }.let { columnsNotInTable ->
            if (columnsNotInTable.isNotEmpty()) {
                throw IllegalArgumentException("Provided insert columns ${columnsNotInTable.joinToString(", ")} are not in table $name")
            }
        }

        if (columnsToInclude.isEmpty()) {
            throw IllegalArgumentException("No insert columns available for table $name")
        }

        columns.filter { it !is OptionalColumn }.filter { !columnsToInclude.contains(it) }.let { missingRequiredColumns ->
            if (missingRequiredColumns.isNotEmpty()) {
                throw IllegalArgumentException("The required columns ${missingRequiredColumns.joinToString(", ")} were not included in insert for table $name")
            }
        }

        return "INSERT INTO $name(${columnsToInclude.joinToString(",")}) VALUES(${columnsToInclude.joinToString(",") { "?" }})"
    }

    fun getUpdateSql(columnsToFilter: List<Column> = listOf()): String {
        if (!updatable) {
            throw IllegalArgumentException("No primary key columns available on update sql for table $name")
        }

        columnsToFilter.filter { !columns.contains(it) }.let { columnsNotInTable ->
            if (columnsNotInTable.isNotEmpty()) {
                throw IllegalArgumentException("Provided insert columns ${columnsNotInTable.joinToString(", ")} are not in table $name")
            }
        }

        columnsToFilter.filter { it.primaryKey }.let { primaryKeyColumns ->
            if (primaryKeyColumns.isNotEmpty()) {
                throw IllegalArgumentException("Provided insert columns ${primaryKeyColumns.joinToString(", ")} are primary keys in table $name")
            }
        }

        val columnsToInclude = columnsToFilter.filter { columns.contains(it) && !it.primaryKey }
        if (columnsToInclude.isEmpty()) {
            throw IllegalArgumentException("No update columns available for table $name")
        }

        return "UPDATE $name SET ${columnsToInclude.joinToString(", ") { "$it = ?" }} WHERE ${primaryKeyColumns.joinToString("AND") { "($it = ?)" }}"
    }
}

abstract class NumberIdTable(name: String) : Table(name) {
    open val idColumn: NumberColumn
        get() = primaryKeyColumn as NumberColumn
}

abstract class StringIdTable(name: String) : Table(name) {
    val idColumn: StringColumn
        get() = primaryKeyColumn as StringColumn
}
