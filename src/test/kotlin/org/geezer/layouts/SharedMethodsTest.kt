package org.geezer.layouts

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedMethodsTest {

    @Test
    fun `boolean value`() {
        assertTrue { trueValue(true) }
        assertFalse { trueValue(false) }
    }
    @Test
    fun `string value`() {
        assertTrue { trueValue("true") }
        assertTrue { trueValue("True") }
        assertTrue { trueValue("TRUE") }
        assertFalse { trueValue("") }
        assertFalse { trueValue("false") }
        assertFalse { trueValue("goobildy") }
    }
    @Test
    fun `null value`() {
        assertFalse { trueValue(null) }
    }
    @Test
    fun `other value`() {
        assertFalse { trueValue(40) }
    }
}