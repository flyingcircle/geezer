package org.geezer.routes

import kotlin.reflect.KClass

/**
 * When routes are added to {@link RoutingTable} as class objects, a route instance must be used
 * each time to process an HTTP request. This interface controls the lifecycle of those route instances.
 *
 * @see RoutesConfiguration#routeInstancePool
 */
interface RouteInstancePool {
    /**
     * Called when a route match has been made to a method in the given class and an instance is needed to process the
     * request. If the route class is not thread safe, then once an instance is returned from this method it should not
     * be used again until it's put back in the pool (`returnRouteInstance`).
     *
     * @param routeClass A route class that was added to the [org.geezer.routes.RoutingTable].
     * @return An instance of the given route class this is ready to process an HTTP request.
     * @throws RouteInstanceBorrowException If a route instance cannot be borrowed for any reason.
     */
    fun borrowRouteInstance(routeClass: KClass<*>): Any

    /**
     * The Routes engine is done using this route instance.
     *
     * @param routeInstance A route object previous retrieved from `borrowRouteInstance`.
     */
    fun returnRouteInstance(routeInstance: Any)
}
