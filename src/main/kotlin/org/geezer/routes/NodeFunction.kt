package org.geezer.routes

import java.lang.reflect.InvocationTargetException
import kotlin.jvm.internal.FunctionReference
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

internal abstract class NodeFunction(val function: KFunction<*>, val instancePool: RouteInstancePool) {

    /**
     * Is this function tied to an object singleton so that we don't need to pass in an instance at invoke time?
     */
    val isObjectFunction: Boolean

    val routeClass: KClass<*>

    val parameterTypes: List<FunctionParameterType>

    init {
        val instanceType = function.instanceParameter?.type?.classifier as KClass<*>?
        if (instanceType == null) {
            isObjectFunction = true
            val reference = function as FunctionReference
            routeClass = reference.owner as KClass<*>
        } else {
            isObjectFunction = false
            routeClass = instanceType
        }
        parameterTypes = function.valueParameters.map {
            val parameterClass = it.type.classifier as KClass<*>
            val clazz = FunctionParameterClass.fromClass(parameterClass) ?: throw IllegalArgumentException("Unsupported parameter type in route function $this of type ${parameterClass.qualifiedName}.")
            FunctionParameterType(clazz, it.type.isMarkedNullable)
        }
    }

    fun call(parameters: List<Any?>): Any? {
        return try {
            if(!isObjectFunction) {
                function.call(instancePool.borrowRouteInstance(routeClass), *parameters.toTypedArray())
            } else {
                function.call(*parameters.toTypedArray())
            }
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }

    override fun toString(): String = "${routeClass.qualifiedName}.${function.name}"
}
