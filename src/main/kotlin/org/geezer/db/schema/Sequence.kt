package org.geezer.db.schema

abstract class Sequence(private val _name: String) {
    val name: String
        get() = DBIO.tableNamespace?.let { "$it.$_name" } ?: _name
}
