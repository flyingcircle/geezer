package org.geezer.db

import java.io.Writer
import java.sql.ResultSet

fun ResultSet.toGeezer(): GeezerResultSet = GeezerResultSet(this)

/**
 * A utility class that makes working working with a [ResultSet] more Kotlin like.
 */
open class GeezerResultSet(set: ResultSet) : BaseSet(set) {

    /**
     * Collect the results as a List<T> using the [creationCallback] for each result set row.
     */
    fun <T : Any> collect(creationCallback: ((reader: GeezerResultSet) -> T?)): List<T> {
        val collection = mutableListOf<T>()
        while (next()) {
            creationCallback(this)?.let {
                collection.add(it)
            }
        }
        return collection
    }

    /**
     * Streams the results of the given reader to the writer.
     */
    fun stream(writer: Writer, encoderCallback: ((reader: GeezerResultSet) -> String)) {
        while (next()) {
            writer.write(encoderCallback(this))
        }
    }
}
