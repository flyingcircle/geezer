package org.geezer.routes

import kotlin.test.Test
import kotlin.test.assertEquals

class MIMETypesTest {
    @Test
    fun testParse() {
        assertEquals(MIMETypes.EXCEL, MIMETypes.getMimeTypeFromFileExtension("xlsx"))
        assertEquals(MIMETypes.EXCEL, MIMETypes.getMimeTypeFromFileExtension("xls"))
        assertEquals(MIMETypes.ATOM, MIMETypes.getMimeTypeFromFileExtension("atom"))
        assertEquals(MIMETypes.CSS, MIMETypes.getMimeTypeFromFileExtension("css"))
        assertEquals(MIMETypes.HTML, MIMETypes.getMimeTypeFromFileExtension("html"))
        assertEquals(MIMETypes.HTML, MIMETypes.getMimeTypeFromFileExtension("htm"))
        assertEquals(MIMETypes.ICS, MIMETypes.getMimeTypeFromFileExtension("ics"))
        assertEquals(MIMETypes.JAVASCRIPT, MIMETypes.getMimeTypeFromFileExtension("js"))
        assertEquals(MIMETypes.JSON, MIMETypes.getMimeTypeFromFileExtension("json"))
        assertEquals(MIMETypes.PDF, MIMETypes.getMimeTypeFromFileExtension("pdf"))
        assertEquals(MIMETypes.RSS, MIMETypes.getMimeTypeFromFileExtension("rss"))
        assertEquals(MIMETypes.TEXT, MIMETypes.getMimeTypeFromFileExtension("txt"))
        assertEquals(MIMETypes.TEXT, MIMETypes.getMimeTypeFromFileExtension("text"))
        assertEquals(MIMETypes.WORD, MIMETypes.getMimeTypeFromFileExtension("doc"))
        assertEquals(MIMETypes.XML, MIMETypes.getMimeTypeFromFileExtension("xml"))
    }
}