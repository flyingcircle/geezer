package org.geezer.routes

import org.geezer.routes.utils.TestHttpServletRequest
import kotlin.test.Test
import kotlin.test.*


class RequestParametersTest {

    @Test
    fun testNullValues() {
        val parameters = RequestParameters(mapOf())

        assertNull(parameters.getInt("one"))
        assertNull(parameters.getOptionalInt("one", null))
        assertNull(parameters.getLong("one"))
        assertNull(parameters.getOptionalLong("one", null))
        assertNull(parameters.getFloat("one"))
        assertNull(parameters.getOptionalFloat("one", null))
        assertNull(parameters.getDouble("one"))
        assertNull(parameters.getOptionalDouble("one", null))

        assertNotNull(parameters.toString())
    }

    @Test
    fun testConversions() {
        val parameters = RequestParameters(mapOf(

            "zero" to arrayOf(""),
            "one" to arrayOf("1"),
            "two" to arrayOf("1", "2"),
            "three" to arrayOf("1.234"),
            "four" to arrayOf("1.88, 4.35"),
            "six" to arrayOf("TRUE", "fALse", "TrUe", "1", "Yes", "0", "No"),
            ))

        assertFalse(parameters.containsContent("xxx"))
        assertFalse(parameters.containsContent("xx"))
        assertTrue(parameters.contains("zero"))
        assertFalse(parameters.containsContent("zero"))

        assertTrue(parameters.contains("one"))
        assertTrue(parameters.containsContent("one"))
        assertEquals("1", parameters["one"])
        assertEquals(listOf("1"), parameters.getValues("one"))
        assertEquals(1.toByte(), parameters.getByte("one"))
        assertEquals(1.toShort(), parameters.getShort("one"))
        assertEquals(1, parameters.getInt("one"))
        assertEquals(1, parameters.getInt("one", 2))
        assertEquals(1, parameters.getOptionalInt("one", null))
        assertEquals(1L, parameters.getLong("one"))
        assertEquals(1.0F, parameters.getFloat("one"))
        assertEquals(1.0, parameters.getDouble("one"))
        assertEquals(listOf(1.toByte()), parameters.getBytes("one"))
        assertEquals(listOf(1.toShort()), parameters.getShorts("one"))
        assertEquals(listOf(1), parameters.getInts("one"))
        assertEquals(listOf(1L), parameters.getLongs("one"))
        assertEquals(listOf(1.0F), parameters.getFloats("one"))
        assertEquals(listOf(1.0), parameters.getDoubles("one"))

        assertTrue(parameters.contains("two"))
        assertTrue(parameters.containsContent("two"))
        assertEquals("1", parameters["two"])
        assertEquals(listOf("1", "2"), parameters.getValues("two"))
        assertEquals(1.toByte(), parameters.getByte("two"))
        assertEquals(1.toByte(), parameters.getByte("two", 2.toByte()))
        assertEquals(1.toShort(), parameters.getShort("two"))
        assertEquals(1.toShort(), parameters.getShort("two", 2.toShort()))
        assertEquals(1.toShort(), parameters.getOptionalShort("two", null))
        assertEquals(1, parameters.getInt("two"))
        assertEquals(listOf(1, 2), parameters.getInts("two"))
        assertEquals(1L, parameters.getLong("two"))
        assertEquals(1L, parameters.getLong("two", 2L))
        assertEquals(1L, parameters.getOptionalLong("two", null))
        assertEquals(1.0F, parameters.getFloat("two"))
        assertEquals(1.0F, parameters.getFloat("two", 2.0F))
        assertEquals(1.0F, parameters.getOptionalFloat("two", null))
        assertEquals(1.0, parameters.getDouble("two"))
        assertEquals(1.0, parameters.getDouble("two", 2.0), 0.0)
        assertEquals(1.0, parameters.getOptionalDouble("two", null))
        assertEquals(listOf(1.toByte(), 2.toByte()), parameters.getBytes("two"))
        assertEquals(listOf(1.toShort(), 2.toShort()), parameters.getShorts("two"))
        assertEquals(listOf(1, 2), parameters.getInts("two"))
        assertEquals(listOf(1L, 2L), parameters.getLongs("two"))
        assertEquals(listOf(1.0F, 2.0F), parameters.getFloats("two"))
        assertEquals(listOf(1.0, 2.0), parameters.getDoubles("two"))

        assertTrue(parameters.contains("three"))
        assertTrue(parameters.containsContent("three"))
        assertEquals("1.234", parameters["three"])
        assertEquals(listOf("1.234"), parameters.getValues("three"))
        assertNull(parameters.getByte("three"))
        assertNull(parameters.getShort("three"))
        assertNull(parameters.getInt("three"))
        assertEquals(1.234F, parameters.getFloat("three"))
        assertEquals(1.234, parameters.getDouble("three"))
        assertTrue(parameters.getBytes("three").isEmpty())
        assertTrue(parameters.getShorts("three").isEmpty())
        assertTrue(parameters.getInts("three").isEmpty())
        assertTrue(parameters.getLongs("three").isEmpty())
        assertEquals(listOf(1.234F), parameters.getFloats("three"))
        assertEquals(listOf(1.234), parameters.getDoubles("three"))

        assertTrue(parameters.hasParameters)

        val cloned = parameters.clone()
        assertEquals(parameters.singleParameters, cloned.singleParameters)
    }

    @Test
    fun testFromQueryString() {
        val queryString = "one=1&two=2&three=3"
        val parameters = RequestParameters(queryString)
        assertTrue(parameters.hasParameters)
        assertEquals(1, parameters.getInt("one"))
        assertEquals(2, parameters.getInt("two"))
        assertEquals(3, parameters.getInt("three"))
        assertEquals(queryString, parameters.toString())
    }

    @Test
    fun testFromRequest() {
        val parameters = RequestParameters(TestHttpServletRequest("POST", "", "/test", "one", "1", "two", "2", "three", "3"))
        assertTrue(parameters.hasParameters)
        assertEquals(1, parameters.getInt("one"))
        assertEquals(2, parameters.getInt("two"))
        assertEquals(3, parameters.getInt("three"))
        assertEquals(3, parameters.size)
    }
}