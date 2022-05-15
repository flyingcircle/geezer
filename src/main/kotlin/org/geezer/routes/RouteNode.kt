package org.geezer.routes

import org.geezer.routes.criteria.ParameterCriterionType
import org.geezer.routes.criteria.PathCriterionType
import org.geezer.routes.criteria.RouteCriteria
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

internal class RouteNode(
    val route: String,
    val criteria: RouteCriteria,
    val beforeFunctions: List<WrapperFunction>,
    val function: RouteFunction,
    val afterFunctions: List<WrapperFunction>,
    val forwardPath: String,
    val contentType: String?,
    val configuration: RoutesConfiguration,
    val exceptionHandler: ((e: Exception, requestContext: RequestContext) -> Unit)?
) {
    val dynamicPathCriteria = criteria.pathCriteria.filter { it.type != PathCriterionType.Exact }

    val dynamicParameterCriteria = criteria.parameterCriteria.filter { it.type != ParameterCriterionType.Exact }

    fun invoke(context: RequestContext) {
        var routeFunctionCalled = false
        try {
            beforeFunctions.forEach { it.call(getParameters(it, context)) }

            routeFunctionCalled = true
            val result = function.call(getParameters(function, context))
            if (contentType != null && context.response.contentType != null) {
                context.response.contentType = contentType
            }

            if (result != null) {
                when (function.responseType) {
                    FunctionResponseType.BytesContent -> {
                        val byteArray = result as ByteArray
                        context.response.setContentLength(byteArray.size)
                        context.response.outputStream.write(byteArray)
                    }
                    FunctionResponseType.StreamContent -> fastChannelCopy(result as InputStream, context.response.outputStream, configuration.streamBufferSize)
                    FunctionResponseType.StringContent -> context.response.writer.write(result.toString())
                    FunctionResponseType.ForwardDispatch -> forwardDispatch(result.toString(), context.request, context.response)
                    else -> configuration.logger.error("Unsupported Response Type ${function.responseType}")
                }
            }
        } catch (e: ReturnStatus) {
            if (e.isError()) {
                try {
                    context.response.sendError(e.status)
                } catch (e: java.lang.IllegalStateException) {
                    configuration.logger.error("Attempted to send error on response from thrown ReturnStatus exception from route $this but the status had already been set.", e)
                }
            }
        } catch (e: Exception) {
            val exceptionHandler = this.exceptionHandler
            try {
                if (exceptionHandler != null) {
                    exceptionHandler(e, context)
                } else {
                    throw e
                }
            } catch (e: RedirectTo) {
                context.response.sendRedirect(configuration.redirectHandler.getRedirectUrl(e.redirectUrl, context.request, context.response))
            } catch (e: RenderPath) {
                forwardDispatch(e.pathToRender, context.request, context.response)
            }
        } finally {
            if (routeFunctionCalled) {
                afterFunctions.forEach {
                    try {
                        it.call(getParameters(it, context))
                    } catch (e: Exception) {
                        configuration.logger.error("After function $it for route function $function threw error.", e)
                    }
                }
            }
        }
    }

    private fun getParameters(function: NodeFunction, context: RequestContext): List<Any?> {
        val parameters = mutableListOf<Any?>()

        val pathCriterionQueue = dynamicPathCriteria.toMutableList()
        val parameterCriterionQueue = dynamicParameterCriteria.toMutableList()

        for ((index, parameterType) in function.parameterTypes.withIndex()) {
            when (parameterType.clazz) {
                FunctionParameterClass.RequestContextClass -> parameters.add(context)
                FunctionParameterClass.ServletRequestClass -> parameters.add(context.request)
                FunctionParameterClass.ServletResponseClass -> parameters.add(context.response)
                FunctionParameterClass.SessionClass -> parameters.add(context.session)
                FunctionParameterClass.RequestPathClass -> parameters.add(context.path)
                FunctionParameterClass.RequestParametersClass -> parameters.add(context.parameters)
                FunctionParameterClass.ParameterMapClass -> parameters.add(context.parameters.parameters)
                FunctionParameterClass.RequestedContentTypeClass -> parameters.add(context.requestedContentType)
                FunctionParameterClass.RequestContentClass -> parameters.add(context.requestContent)
                FunctionParameterClass.UrlClass -> parameters.add(context.url)
                else -> {
                    when {
                        pathCriterionQueue.isNotEmpty() -> {
                            val criterion = pathCriterionQueue.removeAt(0)
                            fun pathParamInner(typeName: String, typeFun: (Int) -> Any?) {
                                val value = typeFun(criterion.pathIndex)
                                if (value == null && !parameterType.optional) {
                                    throw IllegalArgumentException("Unable to coerce path ${context.path} at index ${criterion.pathIndex} into $typeName for route node function $function parameter at index $index.")
                                }
                                parameters.add(value)
                            }
                            when (parameterType.clazz) {
                                FunctionParameterClass.BooleanClass -> pathParamInner("Boolean") { context.path.getBoolean(it) }
                                FunctionParameterClass.ByteClass -> pathParamInner("Byte") { context.path.getByte(it) }
                                FunctionParameterClass.ShortClass -> pathParamInner("Short") { context.path.getShort(it) }
                                FunctionParameterClass.IntClass -> pathParamInner("Int") { context.path.getInt(it) }
                                FunctionParameterClass.LongClass -> pathParamInner("Long") { context.path.getLong(it) }
                                FunctionParameterClass.FloatClass -> pathParamInner("Float") { context.path.getFloat(it) }
                                FunctionParameterClass.DoubleClass -> pathParamInner("Double") { context.path.getDouble(it) }
                                FunctionParameterClass.StringClass -> {
                                    if (criterion.type == PathCriterionType.Gobble) {
                                        parameters.add(context.path.substring(criterion.pathIndex).toString())
                                    } else {
                                        pathParamInner("String") { context.path.getOrNull(it) }
                                    }
                                }
                                else -> throw IllegalStateException("Route node function $function has unsupported parameter type ${parameterType.clazz}.")
                            }
                        }
                        parameterCriterionQueue.isNotEmpty() -> {
                            val criterion = parameterCriterionQueue.removeAt(0)
                            fun paramParamInner(typeName: String, typeFun: (String) -> Any?) {
                                val value = typeFun(criterion.name)
                                if (value == null && !parameterType.optional) {
                                    throw IllegalArgumentException("Unable to coerce parameter ${criterion.name} into $typeName for route node function $function parameter at index $index.")
                                }
                                parameters.add(value)
                            }
                            when (parameterType.clazz) {
                                FunctionParameterClass.BooleanClass -> paramParamInner("Boolean") { context.parameters.getBoolean(it) }
                                FunctionParameterClass.ByteClass -> paramParamInner("Byte") { context.parameters.getByte(it) }
                                FunctionParameterClass.ShortClass -> paramParamInner("Short") { context.parameters.getShort(it) }
                                FunctionParameterClass.IntClass -> paramParamInner("Int") { context.parameters.getInt(it) }
                                FunctionParameterClass.LongClass -> paramParamInner("Long") { context.parameters.getLong(it) }
                                FunctionParameterClass.FloatClass -> paramParamInner("Float") { context.parameters.getFloat(it) }
                                FunctionParameterClass.DoubleClass -> paramParamInner("Double") { context.parameters.getDouble(it) }
                                FunctionParameterClass.StringClass -> paramParamInner("String") { context.parameters[it] }
                                else -> throw IllegalStateException("Route node function $function has unsupported parameter type ${parameterType.clazz}.")
                            }
                        }
                        else -> throw IllegalArgumentException("Route node function $function has unmapped parameter at index $index of type ${parameterType.clazz}")
                    }
                }
            }
        }

        return parameters
    }

    private fun forwardDispatch(path: String, servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) {
        if (path.startsWith("redirect:")) {
            servletResponse.sendRedirect(configuration.redirectHandler.getRedirectUrl(path.substring(9, path.length), servletRequest, servletResponse))
        } else if (path.isNotBlank()) {
            servletRequest.getRequestDispatcher("${forwardPath.removeSuffix("/")}/${path.removePrefix("/")}").forward(servletRequest, servletResponse)
        } else {
            servletRequest.getRequestDispatcher(forwardPath).forward(servletRequest, servletResponse)
        }
    }

    companion object {
        @Throws(IOException::class)
        fun fastChannelCopy(inStream: InputStream?, outStream: OutputStream?, bufferSize: Int) {
            val src = Channels.newChannel(inStream)
            val dest = Channels.newChannel(outStream)
            val buffer = ByteBuffer.allocateDirect(bufferSize)
            while (src.read(buffer) != -1) {
                // prepare the buffer to be drained
                buffer.flip()
                // write to the channel, may block
                dest.write(buffer)
                // If partial transfer, shift remainder down
                // If buffer is empty, same as doing clear()
                buffer.compact()
            }
            // EOF will leave buffer in fill state
            buffer.flip()
            // make sure the buffer is fully drained.
            while (buffer.hasRemaining()) {
                dest.write(buffer)
            }
        }
    }
}
