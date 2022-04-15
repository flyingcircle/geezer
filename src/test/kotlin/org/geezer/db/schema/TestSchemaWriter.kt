package org.geezer.db.schema

import kotlin.test.Ignore
import org.geezer.db.DbCompatibilityMode
import org.geezer.db.SchemaFromMigrations
import org.geezer.db.migrations.ClassPathMigrationFilesLocation
import org.geezer.db.migrations.MigrationConfiguration
import kotlin.test.Test
import java.io.StringWriter

class TestSchemaWriter {
    @Ignore
    fun testWriterOracle() {
        val writer = StringWriter()
        SchemaFromMigrations.writeSchema(SchemaWriteConfiguration("org.geezer.db.schema.oracle", listOf("analytics_sequence")), writer,
            DbCompatibilityMode.ORACLE, MigrationConfiguration(migrationFilesLocation = ClassPathMigrationFilesLocation("oracle")))
    }

    @Ignore
    fun testWriterPostgres() {
        val writer = StringWriter()
        SchemaFromMigrations.writeSchema(SchemaWriteConfiguration("org.geezer.db.schema.postgres", listOf("analytics_sequence")), writer,
            DbCompatibilityMode.POSTGRE_SQL, MigrationConfiguration(migrationFilesLocation = ClassPathMigrationFilesLocation("postgres"), dbType = "postgres"))
    }

}