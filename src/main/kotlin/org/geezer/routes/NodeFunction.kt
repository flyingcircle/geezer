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
        val parameterTypes = mutableListOf<FunctionParameterType>()
        for (valueParameter in function.valueParameters) {
            val parameterClass = valueParameter.type.classifier as KClass<*>
            val clazz = FunctionParameterClass.fromClass(parameterClass) ?: throw IllegalArgumentException("Unsupported parameter type in route function $this of type ${parameterClass.qualifiedName}.")
            parameterTypes.add(FunctionParameterType(clazz, valueParameter.type.isMarkedNullable))
        }
        this.parameterTypes = parameterTypes
    }

    fun call(parameters: List<Any?>): Any? {
        val parameters = parameters.toMutableList()
        if (!isObjectFunction) {
            parameters.add(0, instancePool.borrowRouteInstance(routeClass))
        }

        try {
            return function.call(*parameters.toTypedArray())
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }

    override fun toString(): String = "${routeClass.qualifiedName}.${function.name}"
}
