package org.geezer.layouts

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
        val pattern = "122345"
        assertEquals(-1, indexOf("ABC123456789DEF".toByteArray(), pattern))
        assertEquals(3, indexOf("ABC1223456789DEF".toByteArray(), pattern))
    }

    @Test
    fun testLastIndexOf() {
        val pattern = "122345"
        assertEquals(3, lastIndexOf("ABC1223456789DEF".toByteArray(), pattern))
        assertEquals(-1, lastIndexOf("ABC123456789DEF".toByteArray(), pattern))
        assertEquals(16, lastIndexOf("ABC1223456789DEF122345".toByteArray(), pattern))
        assertEquals(16, lastIndexOf("ABC1223456789DEF122345XXFFGG".toByteArray(), pattern))
    }
}