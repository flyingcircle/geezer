package org.geezer.routes

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Create a new route object each time using the default constructor and discards the object after use.
 */
class DefaultRouteInstancePool : RouteInstancePool {
    override fun borrowRouteInstance(routeClass: KClass<*>): Any =
        routeClass.primaryConstructor?.call() ?:
        throw IllegalArgumentException("Route class ${routeClass.qualifiedName} has no default constructor.")

    override fun returnRouteInstance(routeInstance: Any) {}
}
