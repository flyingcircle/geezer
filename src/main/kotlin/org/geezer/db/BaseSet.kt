package org.geezer.db

import java.io.InputStream
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.SQLException

abstract class BaseSet(val set: ResultSet) : AutoCloseable {

    inline fun <reified T : Any> collect(): List<T> {
        val constructor = T::class.constructors.firstOrNull { it.parameters.size == 1 && it.parameters[0].type.classifier == GeezerResultSet::class } ?: throw IllegalArgumentException("Type ${T::class.qualifiedName} has no constructor that takes a single ${GeezerResultSet::class.qualifiedName}.")
        val collection = mutableListOf<T>()
        while (next()) {
            collection.add(constructor.call(this))
        }
        return collection
    }

    fun next(): Boolean = set.next()

    fun wasNull(): Boolean = set.wasNull()

    private fun <T> getOptional(getVal: () -> T): T? {
        val value = getVal()
        if (set.wasNull()) {
            return null
        }
        return value
    }

    @Throws(SQLException::class)
    fun getBoolean(column: String): Boolean = set.getBoolean(column)

    @Throws(SQLException::class)
    fun getOptionalBoolean(column: String): Boolean? = getOptional { set.getBoolean(column) }

    @Throws(SQLException::class)
    fun getInt(column: String): Int = set.getInt(column)

    @Throws(SQLException::class)
    fun getOptionalInt(column: String): Int? = getOptional { set.getInt(column) }

    @Throws(SQLException::class)
    fun getLong(column: String): Long = set.getLong(column)

    @Throws(SQLException::class)
    fun getOptionalLong(column: String): Long? = getOptional { set.getLong(column) }

    @Throws(SQLException::class)
    fun getFloat(column: String): Float = set.getFloat(column)

    @Throws(SQLException::class)
    fun getOptionalFloat(column: String): Float? = getOptional { set.getFloat(column) }

    @Throws(SQLException::class)
    fun getDouble(column: String): Double = set.getDouble(column)

    @Throws(SQLException::class)
    fun getOptionalDouble(column: String): Double? = getOptional { set.getDouble(column) }

    @Throws(SQLException::class)
    fun getBigDecimal(column: String): BigDecimal = set.getBigDecimal(column)

    @Throws(SQLException::class)
    fun getOptionalBigDecimal(column: String): BigDecimal? = getOptional { set.getBigDecimal(column) }

    @Throws(SQLException::class)
    fun getString(column: String): String = set.getString(column)

    @Throws(SQLException::class)
    fun getOptionalString(column: String): String? = getOptional { set.getString(column) }

    @Throws(SQLException::class)
    fun getTimestamp(column: String): Long = set.getTimestamp(column).time

    @Throws(SQLException::class)
    fun getOptionalTimestamp(column: String): Long? = getOptional { set.getTimestamp(column)?.time }

    @Throws(SQLException::class)
    fun getBytes(column: String): ByteArray = set.getBytes(column)

    @Throws(SQLException::class)
    fun getOptionalBytes(column: String): ByteArray? = getOptional { set.getBytes(column) }

    fun readBytes(column: String, bytesReader: (inputStream: InputStream?) -> Unit) {
        set.getBinaryStream(column).use { bytesReader(it) }
    }

    fun readOptionalBytes(column: String, bytesReader: (inputStream: InputStream?) -> Unit) {
        val stream = set.getBinaryStream(column)
        if (stream == null) {
            bytesReader(null)
        } else {
            stream.use { bytesReader(it) }
        }
    }

    fun getIntList(column: String, separator: String = GeezerDbUtils.DefaultListSeparator): List<Int> = getOptionalString(column)?.split(separator)?.mapNotNull { it.toIntOrNull() } ?: listOf()

    fun getStringList(column: String, separator: String = GeezerDbUtils.DefaultListSeparator): List<String> = getOptionalString(column)?.split(separator) ?: listOf()

    fun hasColumn(name: String): Boolean {
        for (i in 1..set.metaData.columnCount) {
            if (set.metaData.getColumnName(i).equals(name, true)) {
                return true
            }
        }
        return false
    }

    override fun close() {
        set.close()
    }
}
