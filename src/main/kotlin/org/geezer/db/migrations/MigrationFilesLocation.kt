package org.geezer.db.migrations

interface MigrationFilesLocation {
    val migrationFilesOrErrors: List<Pair<MigrationFile?, String>>

    val latestVersion: Int
        get() = migrationFiles.lastOrNull()?.version ?: 0

    /**
     * The local migration files.
     */
    val migrationFiles: List<MigrationFile>
        get() = migrationFilesOrErrors.mapNotNull { it.first }.sorted()

    val migrationFileErrors: List<String>
        get() = migrationFilesOrErrors.mapNotNull { if (it.first == null) it.second else null }
}
