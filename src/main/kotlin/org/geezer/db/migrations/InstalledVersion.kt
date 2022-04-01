package org.geezer.db.migrations

import org.geezer.db.GeezerResultSet

/**
 * The record of an installed migration script.
 *
 * @property version The migration script version.
 * @property fileName The file name of the migration script.
 * @property installedAt The timestamp when this migration script was installed into the database.
 * @property checksum The checksum of the migration script when installed.
 */
class InstalledVersion(val version: Int, val fileName: String, val installedAt: Long, val checksum: String) : Comparable<InstalledVersion> {
    constructor(reader: GeezerResultSet) : this(reader.getInt("version"), reader.getString("file_name"), reader.getTimestamp("installed_at"), reader.getString("checksum"))

    override fun compareTo(other: InstalledVersion): Int = version.compareTo(other.version)
}
