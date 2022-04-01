package org.geezer.layouts

import org.geezer.layouts.DefaultHtmlPageDecider.Companion.knownNonHtmlFile
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.Test

class DefaultHtmlPageDeciderTest {
    @Test
    fun testKnownNonHtmlFile() {
        assertTrue(knownNonHtmlFile("/one/two/hello.txt"))
        assertTrue(knownNonHtmlFile("/one/two/hello.doc"))
        assertTrue(knownNonHtmlFile("/one.xlsx"))
        assertTrue(knownNonHtmlFile("hello.pdf"))
        assertFalse(knownNonHtmlFile("/hellopdf"))
        assertFalse(knownNonHtmlFile("/one/two.html"))
        assertFalse(knownNonHtmlFile("/one/two.htm"))
        assertFalse(knownNonHtmlFile("/one/two.jsp"))
        assertFalse(knownNonHtmlFile("/one/two.pdf/test"))
    }
}