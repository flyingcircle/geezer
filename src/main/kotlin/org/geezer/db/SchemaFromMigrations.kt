package org.geezer.db

import org.geezer.db.migrations.DbMigrator
import org.geezer.db.migrations.MigrationConfiguration
import org.geezer.db.schema.SchemaWriteConfiguration
import org.geezer.db.schema.SchemaWriter
import java.io.Writer
import java.sql.DriverManager

object SchemaFromMigrations {

    fun writeSchema(schemaConfiguration: SchemaWriteConfiguration, schemaWriter: Writer, dbCompatibilityMode: DbCompatibilityMode, migrationConfiguration: MigrationConfiguration = MigrationConfiguration()) {
        schemaWriter.use { writer ->
            val jdbcUrl = "jdbc:h2:mem:geezer;MODE=${dbCompatibilityMode.h2Mode}"

            DriverManager.getConnection(jdbcUrl, "SA", "").use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate("DROP ALL OBJECTS")
                }

                DbMigrator.migrate(connection, false, migrationConfiguration)
                connection.commit()
                writer.write(SchemaWriter.writeSchema(schemaConfiguration, connection))
            }
        }
    }
}
