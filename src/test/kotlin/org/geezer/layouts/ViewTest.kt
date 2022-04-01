package org.geezer.layouts

import org.geezer.layouts.View.Companion.indexOf
import org.geezer.layouts.View.Companion.lastIndexOf
import kotlin.test.assertEquals
import kotlin.test.Test

class ViewTest {
    @Test
    fun testContent() {
        /*
    LayoutContent layoutContent = new LayoutContent("<head>THIS IS THE HEAD</head><body>THIS IS THE BODY</body>");
    assertEquals("THIS IS THE HEAD", layoutContent.getHead());
    assertEquals("THIS IS THE BODY", layoutContent.getBody());
    assertEquals("<head>THIS IS THE HEAD</head><body>THIS IS THE BODY</body>", layoutContent.toString());
    */
    }

    @Test
    fun testIndexOf() {
        val pattern = "122345".toByteArray()
        assertEquals(-1, indexOf("ABC123456789DEF".toByteArray(), pattern))
        assertEquals(3, indexOf("ABC1223456789DEF".toByteArray(), pattern))
    }

    @Test
    fun testLastIndexOf() {
        var pattern = byteArrayOf(6, 6, 6)
        val content = byteArrayOf(1, 2, 3, 4, 5, 6, 6, 6, 7, 8)
        assertEquals(5, lastIndexOf(content, pattern))
        pattern = "122345".toByteArray()
        assertEquals(3, lastIndexOf("ABC1223456789DEF".toByteArray(), pattern))
        assertEquals(-1, lastIndexOf("ABC123456789DEF".toByteArray(), pattern))
        assertEquals(16, lastIndexOf("ABC1223456789DEF122345".toByteArray(), pattern))
        assertEquals(16, lastIndexOf("ABC1223456789DEF122345XXFFGG".toByteArray(), pattern))
    }
}