package org.geezer.layouts

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedMethodsTest {

    @Test
    fun `boolean value`() {
        assertTrue { SharedMethods.trueValue(true) }
        assertFalse { SharedMethods.trueValue(false) }
    }
    @Test
    fun `string value`() {
        assertTrue { SharedMethods.trueValue("true") }
        assertTrue { SharedMethods.trueValue("True") }
        assertTrue { SharedMethods.trueValue("TRUE") }
        assertFalse { SharedMethods.trueValue("") }
        assertFalse { SharedMethods.trueValue("false") }
        assertFalse { SharedMethods.trueValue("goobildy") }
    }
    @Test
    fun `null value`() {
        assertFalse { SharedMethods.trueValue(null) }
    }
    @Test
    fun `other value`() {
        assertFalse { SharedMethods.trueValue(40) }
    }
}