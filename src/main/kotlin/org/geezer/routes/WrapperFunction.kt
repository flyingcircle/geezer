package org.geezer.routes

import kotlin.reflect.KFunction

internal class WrapperFunction(function: KFunction<*>, instancePool: RouteInstancePool) : NodeFunction(function, instancePool) {
    init {
        parameterTypes.firstOrNull { it.clazz.providedFromRoute }?.let { throw IllegalArgumentException("Route wrapper function $this has invalid parameter type ${it.clazz.clazz.qualifiedName}.") }
    }
}
