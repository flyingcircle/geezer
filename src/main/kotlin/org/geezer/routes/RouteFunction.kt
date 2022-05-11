package org.geezer.routes

import java.io.InputStream
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf

internal class RouteFunction(function: KFunction<*>, instancePool: RouteInstancePool, returnedStringContent: Boolean) : NodeFunction(function, instancePool) {
    val responseType: FunctionResponseType

    init {
        val returnClass = function.returnType.classifier as KClass<*>
        responseType = when {
            returnClass == ByteArray::class -> FunctionResponseType.BytesContent
            returnClass == Unit::class -> FunctionResponseType.Unit
            returnClass.isSubclassOf(InputStream::class) -> FunctionResponseType.StreamContent
            returnClass.isSubclassOf(CharSequence::class) ->
                if (returnedStringContent) FunctionResponseType.StringContent else FunctionResponseType.ForwardDispatch
            else -> FunctionResponseType.StringContent
        }
    }
}
