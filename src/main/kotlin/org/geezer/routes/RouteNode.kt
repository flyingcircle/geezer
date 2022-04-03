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
            for (beforeFunction in beforeFunctions) {
                beforeFunction.call(getParameters(beforeFunction, context))
            }

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
                for (afterFunction in afterFunctions) {
                    try {
                        afterFunction.call(getParameters(afterFunction, context))
                    } catch (e: Exception) {
                        configuration.logger.error("After function $afterFunction for route function $function threw error.", e)
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
                            when (parameterType.clazz) {
                                FunctionParameterClass.BooleanClass -> {
                                    val value = context.path.getBoolean(criterion.pathIndex)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce path ${context.path} at index ${criterion.pathIndex} into Boolean for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.ByteClass -> {
                                    val value = context.path.getByte(criterion.pathIndex)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce path ${context.path} at index ${criterion.pathIndex} into Byte for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.ShortClass -> {
                                    val value = context.path.getShort(criterion.pathIndex)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce path ${context.path} at index ${criterion.pathIndex} into Short for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.IntClass -> {
                                    val value = context.path.getInt(criterion.pathIndex)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce path ${context.path} at index ${criterion.pathIndex} into Int for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.LongClass -> {
                                    val value = context.path.getLong(criterion.pathIndex)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce path ${context.path} at index ${criterion.pathIndex} into Long for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.FloatClass -> {
                                    val value = context.path.getFloat(criterion.pathIndex)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce path ${context.path} at index ${criterion.pathIndex} into Float for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.DoubleClass -> {
                                    val value = context.path.getDouble(criterion.pathIndex)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce path ${context.path} at index ${criterion.pathIndex} into Double for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.StringClass -> {
                                    if (criterion.type == PathCriterionType.Gobble) {
                                        parameters.add(context.path.substring(criterion.pathIndex).toString())
                                    } else {
                                        val value = context.path.getOrNull(criterion.pathIndex)
                                        if (value == null && !parameterType.optional) {
                                            throw IllegalArgumentException("Unable to access path ${context.path} at index ${criterion.pathIndex} for route node function $function parameter at index $index.")
                                        }
                                        parameters.add(value)
                                    }
                                }

                                else -> throw IllegalStateException("Route node function $function has unsupported parameter type ${parameterType.clazz}.")
                            }
                        }
                        parameterCriterionQueue.isNotEmpty() -> {
                            val criterion = parameterCriterionQueue.removeAt(0)
                            when (parameterType.clazz) {
                                FunctionParameterClass.BooleanClass -> {
                                    val value = context.parameters.getBoolean(criterion.name)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce parameter ${criterion.name} into Boolean for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.ByteClass -> {
                                    val value = context.parameters.getByte(criterion.name)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce parameter ${criterion.name} into Byte for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.ShortClass -> {
                                    val value = context.parameters.getShort(criterion.name)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce parameter ${criterion.name} into Short for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.IntClass -> {
                                    val value = context.parameters.getInt(criterion.name)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce parameter ${criterion.name} into Int for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.LongClass -> {
                                    val value = context.parameters.getLong(criterion.name)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce parameter ${criterion.name} into Long for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.FloatClass -> {
                                    val value = context.parameters.getFloat(criterion.name)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce parameter ${criterion.name} into Float for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.DoubleClass -> {
                                    val value = context.parameters.getDouble(criterion.name)
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to coerce parameter ${criterion.name} into Double for route node function $function parameter at index $index.")
                                    }
                                    parameters.add(value)
                                }

                                FunctionParameterClass.StringClass -> {
                                    val value = context.parameters[criterion.name]
                                    if (value == null && !parameterType.optional) {
                                        throw IllegalArgumentException("Unable to access parameter ${criterion.name} for route node function $function.")
                                    }
                                    parameters.add(value)
                                }

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
        val path = path
        if (path.startsWith("redirect:")) {
            servletResponse.sendRedirect(configuration.redirectHandler.getRedirectUrl(path.substring(9, path.length), servletRequest, servletResponse))
        } else {
            var fullPath = forwardPath
            if (path.isNotBlank()) {
                if (!fullPath.endsWith("/")) {
                    fullPath += "/"
                }

                fullPath += if (path.startsWith("/")) path.substring(1) else path
            }
            servletRequest.getRequestDispatcher(fullPath).forward(servletRequest, servletResponse)
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
