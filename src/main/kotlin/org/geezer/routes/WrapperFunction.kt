package org.geezer.routes

import kotlin.reflect.KFunction

internal class WrapperFunction(function: KFunction<*>, instancePool: RouteInstancePool) : NodeFunction(function, instancePool) {
    init {
        require(parameterTypes.all { !it.clazz.providedFromRoute }) {
            "Route wrapper function $this has invalid parameter type ${parameterTypes.firstOrNull { it.clazz.providedFromRoute }}." }
    }
}
