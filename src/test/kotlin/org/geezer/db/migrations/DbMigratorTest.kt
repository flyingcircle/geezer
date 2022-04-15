package org.geezer.db.migrations

import org.geezer.db.schema.DBIO
import org.geezer.mock.NetworkMockDatasource
import org.geezer.mock.TestDatabase
import kotlin.test.Ignore
import kotlin.concurrent.thread
import kotlin.test.Test
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class DbMigratorTest {
    @Ignore
    fun testPrintMigrations() {
        assertTrue { DbMigrator.printMigration(migrationConfiguration = MigrationConfiguration(migrationFilesLocation = ClassPathMigrationFilesLocation("oracle"))).isNotBlank() }
        assertTrue { DbMigrator.printMigration(2, migrationConfiguration = MigrationConfiguration(migrationFilesLocation = ClassPathMigrationFilesLocation("oracle"))).isBlank() }
    }

    @Ignore
    fun testMigrationConcurrency() {
        var completed = 0
        DBIO.initializeDataSource(NetworkMockDatasource)
        NetworkMockDatasource.getConnection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate("DROP ALL OBJECTS")
            }
            connection.commit()
        }

        val jobs = mutableListOf<Thread>()
        for (i in 0..3) {
            val job = thread {
                println("Thread $i Started")
                Thread.sleep((500 - (i * 50)).toLong())
                println("Thread $i Running")
                DBIO.transaction { connection ->
                    DbMigrator.migrate(connection, printMigrations = false)
                    println("autocommit: ${connection.autoCommit}") //FIXME: H2 not respecting disabling autoCommit to test locks
                }
//                        NetworkMockDatasource.getConnection().use { connection ->
//                            connection.autoCommit = false
//                            DbMigrator.migrate(connection, printMigrations = false)
//                            connection.commit()
//                        }
                println("Thread $i Finished")
                completed++
            }
            jobs.add(job)
        }
        for (job in jobs) {
            job.join()
        }
        assertEquals(4, completed, "All threads should complete without error")

        // Check the last script for completion:
        DBIO.transaction { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM users100").use { statement ->
                statement.executeQuery().use { set ->
                    set.next()
                    assertEquals(300, set.getInt(1))
                }
            }
        }
    }

    @Ignore
    fun testMigrationError() {
        assertFailsWith<SQLException>("no SQL Exception thrown", block = {
            TestDatabase.setupDatabaseTest()
            DBIO.transaction { connection ->
                DbMigrator.migrate(connection)
            }
        })
    }
}