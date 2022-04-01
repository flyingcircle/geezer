package org.geezer.routes.tokens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokensTest {
    @Test
    fun testWildcardPathToken() {
        val token = WildcardPathToken(1)
        assertEquals(1, token.pathIndex)
    }

    @Test
    fun testWildcardParameterToken() {
        val token = WildcardParameterToken("test", true)
        assertEquals("test", token.name)
        assertTrue(token.optional)
    }

    @Test
    fun testExactValueParameterToken() {
        val token = ExactValueParameterToken("name", true, "value")
        assertEquals("name", token.name)
        assertTrue(token.optional)
        assertEquals("value", token.value)
    }

    @Test
    fun testSymbolParameterToken() {
        val token = SymbolParameterToken("name", true, "symbol")
        assertEquals("name", token.name)
        assertTrue(token.optional)
        assertEquals("symbol", token.symbol)
    }

    @Test
    fun testSymbolPathToken() {
        val token = SymbolPathToken(1, "symbol")
        assertEquals(1, token.pathIndex)
        assertEquals("symbol", token.symbol)
    }
}