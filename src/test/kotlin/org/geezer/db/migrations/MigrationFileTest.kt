package org.geezer.db.migrations

import kotlin.test.Ignore
import kotlin.test.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MigrationFileTest {

    @Test
    fun testFromFileErrors() {
        assertNull(MigrationFile.fromFile(File("test/resources/oracle/notafile")).first, "Non-existent file fails to return null")
        assertNull(MigrationFile.fromFile(File("test/resources/grype.xml")).first, "Non-sql file should return null.")
        assertNull(MigrationFile.fromFile(File("test/resources/migration/non_versioned_sql_file.sql")).first, "Sql migration file needs to start with 'v'.")
        assertNull(MigrationFile.fromFile(File("test/resources/migration/v3sqlNoUnderscore.sql")).first)
        assertNull(MigrationFile.fromFile(File("test/resources/migration/va_versionNotANumber.sql")).first)
    }

    @Ignore
    fun testMigrationFileCompare() {
        val file1 = MigrationFile.fromFile(File("test/resources/migration/v1_init.sql")).first!!
        val file2 = MigrationFile.fromFile(File("test/resources/migration/v2_upgrade.sql")).first!!
        val file3 = MigrationFile.fromFile(File("test/resources/migration/v1_init.sql")).first!!
        assertTrue(file2 > file1)
        assertEquals(file1, file3)
    }
}