package org.geezer.db.migrations

import kotlin.test.Test
import kotlin.test.Ignore
import kotlin.test.assertFailsWith
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DirectoryFilesLocationTest {

    @Ignore
    fun testDirectoryFilesLocation() {
        assertFailsWith<IllegalArgumentException> {
            DirectoryFilesLocation(File("notADir"))
        }

        val dfl = DirectoryFilesLocation(File("test/resources/migration"))
        val sql1 = MigrationFile.fromFile(File("test/resources/migration/v1_init.sql")).first
        val sql2 = MigrationFile.fromFile(File("test/resources/migration/v2_upgrade.sql")).first
        assertTrue(dfl.latestVersion > 0)
        assertContains(dfl.migrationFiles, sql1)
        assertContains(dfl.migrationFiles, sql2)
        assertContains(dfl.migrationFileErrors, "non_versioned_sql_file.sql is not in the proper version format.")
        assertEquals(3, dfl.migrationFileErrors.size)
        assertEquals(5, dfl.migrationFilesOrErrors.size)
        assertEquals(2, dfl.latestVersion)

        val badDfl = DirectoryFilesLocation(File("test/resources/spider"))
        assertEquals(0, badDfl.latestVersion)
    }
}