package org.geezer.db.migrations

import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * The details of a migration script found in resources/db/migrations.
 *
 * @property version The migration script numerical version.
 * @property fileName The file name of the migration script.
 * @property fileContent The SQL script content.
 * @property checksum The MD5 checksum of the SQL script content.
 */
class MigrationFile(val version: Int, val fileName: String, val fileContent: String, val checksum: String) : Comparable<MigrationFile> {

    override fun compareTo(other: MigrationFile): Int = version.compareTo(other.version)

    override fun equals(other: Any?): Boolean = other is MigrationFile && version == other.version

    companion object {
        fun fromFile(file: File): Pair<MigrationFile?, String> {
            if (!file.isFile) {
                return Pair(null, "${file.name} is not a file.")
            }
            val filenameRegex = Regex("v\\d+_.*\\.sql")
            if (!filenameRegex.matches(file.name)) {
                return Pair(null, "${file.name} is not in the proper version format.")
            }
            val fileName = file.name
            val index = fileName.indexOf("_")
            val version = fileName.substring(1, index).toInt()
            val fileContent = IOUtils.toString(FileInputStream(file), "UTF-8")
            val checksum = hashContent(fileContent)
            return Pair(MigrationFile(version, fileName, fileContent, checksum), "")
        }

        fun hashContent(content: String): String {
            val md = MessageDigest.getInstance("MD5")
            md.update(content.toByteArray())
            return bytesToHex(md.digest())
        }
    }
}

private val HEX_ARRAY: ByteArray = "0123456789ABCDEF".toByteArray()
fun bytesToHex(bytes: ByteArray): String {
    val hexChars = ByteArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v: Int = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = HEX_ARRAY[v ushr 4]
        hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars, StandardCharsets.UTF_8)
}
