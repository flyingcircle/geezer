package org.geezer.routes.tokens

import kotlin.test.Test

class RoutesTokensParserTest {

    @Test
    fun test() {
        RouteTokensParser.parse("{}/*/**")
    }
}