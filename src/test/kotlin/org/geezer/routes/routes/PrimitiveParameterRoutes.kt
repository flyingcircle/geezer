package org.geezer.routes.routes

object PrimitiveParameterRoutes {

    var called = false
    var one: Boolean = false
    var two: Byte = -1
    var three: Short = -1
    var four: Int = -1
    var five: Long = -1
    var six: Float = -1.0F
    var seven: Double = -1.0
    var eight: String = ""

    fun reset() {
        called = false
        one = false
        two = -1
        three = -1
        four = -1
        five = -1
        six = -1.0F
        seven = -1.0
        eight = ""
    }

    fun get(one: Boolean, two: Byte, three: Short, four: Int, five: Long, six: Float, seven: Double, eight: String) {
        called = true
        this.one = one
        this.two = two
        this.three = three
        this.four = four
        this.five = five
        this.six = six
        this.seven = seven
        this.eight = eight
    }

    fun post(one: Boolean, two: Byte, three: Short, four: Int, five: Long, six: Float, seven: Double, eight: String) {
        called = true
        this.one = one
        this.two = two
        this.three = three
        this.four = four
        this.five = five
        this.six = six
        this.seven = seven
        this.eight = eight
    }
}