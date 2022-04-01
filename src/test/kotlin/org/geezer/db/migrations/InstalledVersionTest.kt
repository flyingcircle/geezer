package org.geezer.db.migrations

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InstalledVersionTest {

    @Test
    fun `Compare 2 different versions`() {
        val v1 = InstalledVersion(1,"",0,"")
        val v2 = InstalledVersion(2,"",0,"")
        val v1_2 = InstalledVersion(1,"file2",0,"")

        assertEquals(0, v1_2.compareTo(v1))
        assertTrue { v1 < v2 }
    }
}