package org.geezer.db.migrations


class MigrationConfiguration(val migrationsTableName: String = "geezer_migrations", val migrationFilesLocation: MigrationFilesLocation = ClassPathMigrationFilesLocation("db/migrations"), val dbType: String = "oracle") {
    fun getCreateMigrationsTableStatement(): String {
        when (dbType) {
            "postgres" ->
                return """
                    CREATE TABLE $migrationsTableName
                    (
                        version      bigint primary key,
                        file_name    varchar(2048) not null,
                        installed_at timestamp,
                        checksum     varchar(2048) not null
                    )
                """.trimIndent()
            else ->
                // Oracle:
                return """
                    CREATE TABLE $migrationsTableName
                    (
                        version      NUMBER PRIMARY KEY,
                        file_name    VARCHAR(2048) NOT NULL,
                        installed_at TIMESTAMP,
                        checksum     VARCHAR(2048) NOT NULL
                    )
                """.trimIndent()
        }
    }
}
