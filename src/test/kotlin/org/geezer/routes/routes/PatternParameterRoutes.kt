package org.geezer.routes.routes

object PatternParameterRoutes {

    var called = false

    var one: String? = null

    var two: Int? = null

    fun get(one: String, two: Int) {
        called = true
        this.one = one
        this.two = two
    }

    fun reset() {
        called = false
        one = null
        two = null
    }
}