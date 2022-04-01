package org.geezer.db.migrations

import java.io.File

class DirectoryFilesLocation(val migrationDirectory: File) : MigrationFilesLocation {
    init {
        if (!migrationDirectory.isDirectory) {
            throw IllegalArgumentException("Migration directory ${migrationDirectory.absolutePath} does not exists.")
        }
    }

    override val migrationFilesOrErrors: List<Pair<MigrationFile?, String>> by lazy {
        val migrationFilesOrErrors = mutableListOf<Pair<MigrationFile?, String>>()
        migrationDirectory.listFiles()?.forEach { migrationFilesOrErrors.add(MigrationFile.fromFile(it)) }
        migrationFilesOrErrors
    }
}
