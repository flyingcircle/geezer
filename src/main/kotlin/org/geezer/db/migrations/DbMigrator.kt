package org.geezer.db.migrations

import org.geezer.db.GeezerResultSet
import java.sql.Connection
import java.sql.Statement

/**
 * Performs database migrations based upon sql files in resources/db/migration.
 *
 * Why not just use Flyway?
 * That's a valid question. Flyway was initially used by this system. However, we are going to be working in an
 * environment where database schema updates will be performed by an external group. The platform account won't
 * have schema modification privileges. So we'll need to provide an SQL script for this group to execute on our behalf.
 * The free version of Flyway does not provide a way to print an SQL script out based upon a migration script version
 * range. We could just give the external group the migration scripts directly but then the [TBL_ANALYTICS_DB_VERSION_HISTORY]
 * table would not be updated, and we display this table within the web application (for administrators) so we can tell the
 * state of the database schema. The logic is pretty straightforward when you can assume a particular database type
 * (in our case Oracle) so this was implemented to fill this gap.
 */
object DbMigrator {

    val DefaultClasspathLocation = ClassPathMigrationFilesLocation("db/migration")

    const val DefaultMigrationsTable = "geezer_migrations"

    /**
     * The current list of installed versions in the database.
     */
    fun getInstalledVersions(connection: Connection, migrationConfiguration: MigrationConfiguration = MigrationConfiguration()): List<InstalledVersion> {
        return connection.createStatement().use { statement ->
            try {
                val versions = mutableListOf<InstalledVersion>()
                statement.executeQuery("SELECT * FROM ${migrationConfiguration.migrationsTableName} FOR UPDATE").use { set ->
                    val reader = GeezerResultSet(set)
                    while (reader.next()) {
                        versions.add(InstalledVersion(reader))
                    }
                }
                if (versions.size > 0) {
                    println("${migrationConfiguration.migrationsTableName} LOCKED FOR UPDATE")
                }
                // Ignore lock placeholder from initial run:
                versions.filter { it.version != 0 }
            } catch (e: Exception) {
                listOf()
            }
        }
    }

    /**
     * Create the migrations table.
     */
    fun initMigrationsTable(connection: Connection, migrationConfiguration: MigrationConfiguration = MigrationConfiguration()) {
        connection.createStatement().use { statement ->
            try {
                statement.execute(migrationConfiguration.getCreateMigrationsTableStatement())
                println("Created table ${migrationConfiguration.migrationsTableName}")
            } catch (e: java.sql.SQLException) { }
            try {
                statement.execute(
                    """
                    INSERT INTO ${migrationConfiguration.migrationsTableName} (version, file_name, installed_at, checksum)
                    VALUES(0, 'placeholder.sql', CURRENT_TIMESTAMP, 'placeholder')
                    """.trimIndent()
                )
                println("Added placeholder row for initial lock of ${migrationConfiguration.migrationsTableName}")
            } catch (e: java.sql.SQLException) { }
        }
    }

    /**
     * Find any errors between the state of the local migration scripts and the database. Potential errors include:
     *
     * If there are gaps in the migration script version numbers.
     * If the current database migration version is not found any local migration scripts.
     * If the checksum of an installed migration script does not match its local migration script checksum.
     */
    fun getErrors(connection: Connection, migrationConfiguration: MigrationConfiguration = MigrationConfiguration(), installedVersions: List<InstalledVersion> = getInstalledVersions(connection, migrationConfiguration)): List<String> {
        val errors = mutableListOf<String>()

        val migrationFiles = migrationConfiguration.migrationFilesLocation.migrationFiles
        if (migrationFiles.isEmpty()) {
            errors.add("No migration files.")
        } else {
            addMigrationVersionErrors(migrationFiles, errors)
            addInstalledVersionErrors(installedVersions, errors)
        }

        if (errors.isEmpty()) {
            if (installedVersions.size > migrationFiles.size) {
                val latestMigrationFileVersion = installedVersions.lastOrNull()?.version ?: 0
                errors.add("Installed versions ${installedVersions.last().version} is ahead of available file migration at version $latestMigrationFileVersion.")
            }
            addChecksumErrors(installedVersions, migrationFiles, errors)
        }

        return errors
    }

    private fun addChecksumErrors(installedVersions: List<InstalledVersion>, migrationFiles: List<MigrationFile>, errors: MutableList<String>) {
        for (i in installedVersions.indices) {
            val installedVersion = installedVersions[i]
            val migrationFile = migrationFiles[i]

            if (installedVersion.checksum != migrationFile.checksum) {
                errors.add("Checksum for version ${installedVersion.version} for file ${migrationFile.fileName} of value ${migrationFile.checksum} does not match installed version checksum of ${installedVersion.checksum}.")
            }
        }
    }

    private fun addInstalledVersionErrors(installedVersions: List<InstalledVersion>, errors: MutableList<String>) {
        for (i in 1..installedVersions.size) {
            if (installedVersions.none { it.version == i }) {
                errors.add("Missing installed version $i.")
            }
        }
    }

    private fun addMigrationVersionErrors(migrationFiles: List<MigrationFile>, errors: MutableList<String>) {
        for (i in 1..migrationFiles.size) {
            if (migrationFiles.none { it.version == i }) {
                errors.add("Missing migration version $i.")
            }
        }
    }

    /**
     * Updates the database with the necessary migration scripts found in resources/db/migrations. The database table
     * geezer_migrations will be queried to determine which migration scripts need to be executed. This table will be
     * updated each time a migration script is executed so the migrator knows not to execute the script again.
     *
     * @see errors
     * @return The number of migration scripts executed.
     * @throws IllegalStateException If any errors are found in the system.
     */
    fun migrate(connection: Connection, printMigrations: Boolean = true, migrationConfiguration: MigrationConfiguration = MigrationConfiguration()): Int {
        val migrationFiles = migrationConfiguration.migrationFilesLocation.migrationFiles
        var installedVersions = getInstalledVersions(connection, migrationConfiguration)
        if (installedVersions.isEmpty()) {
            initMigrationsTable(connection, migrationConfiguration)
            // Acquires the lock again since the table didn't exist before:
            installedVersions = getInstalledVersions(connection, migrationConfiguration)
        }
        val errors = getErrors(connection, migrationConfiguration, installedVersions)
        if (errors.isNotEmpty()) {
            throw IllegalStateException("Unable to perform database migration with the following errors:${errors.joinToString("\n\t")}")
        }
        val latestInstalledVersion = installedVersions.lastOrNull()?.version ?: 0
        val migrationFilesToLoad = migrationFiles.filter { it.version > latestInstalledVersion }

        if (migrationFilesToLoad.isNotEmpty()) {
            connection.createStatement().use { statement ->
                for (migrationFileToLoad in migrationFilesToLoad) {
                    val sqls = migrationFileToLoad.fileContent.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().map { it.trim() }
                    if (printMigrations) {
                        println("Migrating ${migrationFileToLoad.fileName} with ${sqls.size} SQL statements.")
                    }
                    executeSqls(sqls, printMigrations, statement)

                    val insertVersionStatement = connection.prepareStatement("INSERT INTO ${migrationConfiguration.migrationsTableName}(version, file_name, installed_at, checksum) VALUES(?, ?, CURRENT_TIMESTAMP, ?)")
                    insertVersionStatement.setInt(1, migrationFileToLoad.version)
                    insertVersionStatement.setString(2, migrationFileToLoad.fileName)
                    insertVersionStatement.setString(3, migrationFileToLoad.checksum)
                    insertVersionStatement.executeUpdate()

                    println("Migration of ${migrationFileToLoad.fileName} complete.")
                }
            }
        } else {
            println("No new DB migrations needed.")
        }
        if (latestInstalledVersion == 0) {
            println("Removing placeholder row for initial lock of ${migrationConfiguration.migrationsTableName}")
            connection.createStatement().use { statement ->
                statement.executeUpdate("DELETE FROM ${migrationConfiguration.migrationsTableName} WHERE version = 0")
            }
        }
        return migrationFilesToLoad.size
    }

    private fun executeSqls(sqls: List<String>, printMigrations: Boolean, statement: Statement) {
        for (sql in sqls) {
            if (printMigrations) {
                println("Migrating SQL:\n$sql")
            }
            statement.execute(sql)
        }
    }

    /**
     * Prints a single SQL script required to update the database from the given [fromVersion] to
     * the latest migration script version. This allows the database to be updated outside the migrator.
     */
    fun printMigration(fromVersion: Int = 0, migrationConfiguration: MigrationConfiguration = MigrationConfiguration()): String {
        val builder = StringBuilder()
        val migrationFiles = migrationConfiguration.migrationFilesLocation.migrationFiles
        val migrationFilesToLoad = migrationFiles.filter { it.version >= fromVersion }
        if (migrationFilesToLoad.isNotEmpty()) {
            printMigrationFiles(migrationFilesToLoad, builder)
        } else {
            println("No new DB migrations needed.")
        }

        return builder.toString()
    }

    private fun printMigrationFiles(migrationFilesToLoad: List<MigrationFile>, builder: StringBuilder) {
        for (migrationFileToLoad in migrationFilesToLoad) {
            val sqls = migrationFileToLoad.fileContent.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().map { it.trim() }
            builder.append("--${migrationFileToLoad.fileName}\n")
            for (sql in sqls.filter { it.isNotBlank() }) {
                builder.append("$sql;\n\n")
            }

            builder.append("INSERT INTO geezer_migrations(version, file_name, installed_at, checksum) VALUES(${migrationFileToLoad.version}, '${migrationFileToLoad.fileName}', CURRENT_TIMESTAMP, '${migrationFileToLoad.checksum}');\n\n")
        }
    }
}
