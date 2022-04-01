package org.geezer.mock

import org.geezer.db.migrations.DbMigrator
import org.geezer.db.schema.DBIO

object TestDatabase {
    fun setupDatabaseTest(createMockData: Boolean = false) {
        DBIO.initializeDataSource(MemoryMockDatasource)
        MemoryMockDatasource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("DROP ALL OBJECTS")
            }
            connection.commit()
        }

        DBIO.transaction { connection ->
            DbMigrator.migrate(connection, printMigrations = false)
        }

        if (createMockData) {
            MockDataLoader.load()
        }
    }
}