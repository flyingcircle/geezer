package org.geezer.routes

import arrow.core.foldLeft
import jakarta.servlet.http.HttpServletRequest

class RequestParameters {
    val queryString: String

    private val request: HttpServletRequest?

    private var parsedParameters: Map<String, List<String>> = mapOf()

    /**
     * @return A parameter map where the name is the parameter name and the value is the parameter values.
     */
    val parameters: Map<String, List<String>>
        get() {
            if (parsedParameters.isNotEmpty()) {
                return parsedParameters
            }
            this.parsedParameters = (request?.parameterMap?.map { (name, values) ->
                name!! to listOf(*values)
            } ?: listOf()).toMap()
            return parsedParameters
        }

    val names: List<String> get() = parameters.keys.toList()

    val size: Int
        get() = parameters.size

    /**
     * Parameters with multiple values will only contain one value in this map.
     *
     * @return A parameter map where the name is the parameter name and the value is the parameter value.
     */
    val singleParameters: Map<String, String>
        get() = parameters.entries.mapNotNull { entry ->
                    entry.value.firstOrNull { it.isNotBlank() }?.let { value ->
                        entry.key to value
                    }
                }.toMap()

    constructor(request: HttpServletRequest) {
        this.request = request
        queryString = request.queryString ?: ""
    }

    constructor(parameterMap: Map<String, Array<String>>) {
        this.parsedParameters = parameterMap.map { (name, values) ->
            name to values.toList()
        }.toMap()
        queryString = parsedParameters.foldLeft("") { b, it -> "$b${it.key}=${it.value}&" }.dropLast(1)
        request = null
    }

    constructor(queryString: String) {
        this.queryString = queryString
        request = null
        parsedParameters = queryString.split("&")
            .filter { it.isNotBlank() && it.contains('=') && !it.contains(".*=.*=.*".toRegex()) }
            .fold(mutableMapOf()) { m, pair ->
                val (name, value) = pair.split("=").map { it.trim() }
                val l = m.getOrPut(name) { listOf() }
                m[name] = l + value
                m
            }
    }

    fun clone(): RequestParameters {
        val clonedParameters = parameters.entries.associate {
            it.key to it.value.toTypedArray()
        }
        return RequestParameters(clonedParameters)
    }

    val hasParameters: Boolean
        get() = parsedParameters.isNotEmpty() || queryString.isNotBlank() ||
                (request != null && "post" == request.method.lowercase() &&
                ("application/x-www-form-urlencoded" == request.getHeader("content-type").lowercase() ||
                "multipart/form-data"== request.getHeader("content-type").lowercase()))

    /**
     *
     * @param name The name of the parameter
     * @return The number parameter values for the given parameter name.
     */
    fun size(name: String): Int = getValues(name).size

    operator fun contains(name: String): Boolean {
        return parameters.containsKey(name)
    }

    /**
     *
     * @param name The parameter name.
     * @return True if a value for the given parameter name exists and has content (not an empty string).
     */
    fun containsContent(name: String): Boolean = parameters[name]?.let { values -> values.any { it.isNotBlank() } } ?: false

    /**
     *
     * @param name The parameter name.
     * @param defaultValue The default value to return if the given parameter is not present.
     * @return The parameter value or defaultValue if not present.
     */
    operator fun get(name: String, defaultValue: String? = null): String? = parameters[name]?.let { values -> values.firstOrNull { it.isNotBlank() } } ?: defaultValue

    /**
     *
     * @param name The parameter name.
     * @return The parameter values for the given name. If no values for the given parameter exists an empty list is returned.
     */
    fun getValues(name: String): List<String> = parameters[name] ?: listOf()

    /**
     *
     * @param name The parameter name
     * @param defaultValue The value to return if the parameter doesn't exists.
     * @return The first character of the parameter value or the given defaultValue if the given parameter doesn't exist.
     */
    fun getCharacter(name: String, defaultValue: Char? = null): Char? = parameters[name]?.let { values -> values[0].let { if (it.isNotEmpty()) it[0] else null } } ?: defaultValue

    /**
     *
     * @param name The parameter name.
     * @return Each parameter value as a Character for the given name. If no values for the given parameter exists an empty list is returned.
     */
    fun getCharacters(name: String): List<Char> = parameters[name]?.let { values -> values.mapNotNull { if (it.isNotEmpty()) it[0] else null } } ?: listOf()

    /**
     *
     * @param name The parameter name
     * @return The parameter value parsed as a Boolean or <status>null</status> if the given parameter doesn't exist.
     */
    fun getBoolean(name: String): Boolean? = parameters[name]?.let { values -> values.firstOrNull { it.isNotBlank() }?.let { it.toBoolean() } }

    fun getBoolean(name: String, defaultValue: Boolean): Boolean = getBoolean(name) ?: defaultValue

    fun getOptionalBoolean(name: String, defaultValue: Boolean?): Boolean? = getBoolean(name) ?: defaultValue

    /**
     *
     * @param name The parameter name.
     * @return Each parameter value parsed into a Boolean for the given name. If no values for the given parameter exists an empty list is returned.
     */
    fun getBooleans(name: String): List<Boolean> = parameters[name]?.let { values -> values.filter { it.isNotBlank() }.map { it.trim().toBoolean() } } ?: listOf()

    /**
     *
     * @param name The parameter name
     * @return The parameter value parsed as a Byte or <status>null</status> if the given parameter doesn't exist.
     */
    fun getByte(name: String): Byte? = parameters[name]?.let { values -> values.firstOrNull { it.isNotBlank() }?.trim()?.toByteOrNull() }

    fun getByte(name: String, defaultValue: Byte): Byte = getByte(name) ?: defaultValue

    /**
     *
     * @param name The parameter name.
     * @return Each parameter value parsed into a Byte for the given name. If no values for the given parameter exists an empty list is returned.
     */
    fun getBytes(name: String): List<Byte> = parameters[name]?.let { values -> values.mapNotNull { it.trim().toByteOrNull() } } ?: listOf()

    /**
     *
     * @param name The parameter name
     * @return The parameter value parsed as a Short or <status>null</status> if the given parameter doesn't exist.
     */
    fun getShort(name: String): Short? = parameters[name]?.let { values -> values.firstOrNull { it.isNotBlank() }?.trim()?.toShortOrNull() }

    fun getShort(name: String, defaultValue: Short): Short = getShort(name) ?: defaultValue

    fun getOptionalShort(name: String, defaultValue: Short?): Short? = getShort(name) ?: defaultValue

    /**
     *
     * @param name The parameter name.
     * @return Each parameter value parsed into a Short for the given name. If no values for the given parameter exists an empty list is returned.
     */
    fun getShorts(name: String): List<Short> = parameters[name]?.let { values -> values.mapNotNull { it.trim().toShortOrNull() } } ?: listOf()

    /**
     *
     * @param name The parameter name
     * @return The parameter value parsed as a Integer or <status>null</status> if the given parameter doesn't exist.
     */
    fun getInt(name: String): Int? = parameters[name]?.let { values -> values.firstOrNull { it.isNotBlank() }?.trim()?.toIntOrNull() }

    fun getInt(name: String, defaultValue: Int): Int = getInt(name) ?: defaultValue

    fun getOptionalInt(name: String, defaultValue: Int?): Int? = getInt(name) ?: defaultValue

    fun getInts(name: String): List<Int> = parameters[name]?.let { values -> values.mapNotNull { it.trim().toIntOrNull() } } ?: listOf()

    fun getLong(name: String): Long? = parameters[name]?.let { values -> values.firstOrNull { it.isNotBlank() }?.trim()?.toLongOrNull() }

    fun getLong(name: String, defaultValue: Long): Long = getLong(name) ?: defaultValue

    fun getOptionalLong(name: String, defaultValue: Long?): Long? = getLong(name) ?: defaultValue

    fun getLongs(name: String): List<Long> = parameters[name]?.let { values -> values.mapNotNull { it.trim().toLongOrNull() } } ?: listOf()

    /**
     *
     * @param name The parameter name
     * @return The parameter value parsed as a Float or <status>null</status> if the given parameter doesn't exist.
     */
    fun getFloat(name: String): Float? = parameters[name]?.let { values -> values.firstOrNull { it.isNotBlank() }?.trim()?.toFloatOrNull() }

    fun getFloat(name: String, defaultValue: Float): Float = getFloat(name) ?: defaultValue

    fun getOptionalFloat(name: String, defaultValue: Float?): Float? = getFloat(name) ?: defaultValue

    fun getFloats(name: String): List<Float> = parameters[name]?.let { values -> values.mapNotNull { it.trim().toFloatOrNull() } } ?: listOf()

    /**
     *
     * @param name The parameter name
     * @return The parameter value parsed as a Double or <status>null</status> if the given parameter doesn't exist.
     */
    fun getDouble(name: String): Double? = parameters[name]?.let { values -> values.firstOrNull { it.isNotBlank() }?.trim()?.toDoubleOrNull() }

    fun getDouble(name: String, defaultValue: Double): Double = getDouble(name) ?: defaultValue

    fun getOptionalDouble(name: String, defaultValue: Double?): Double? = getDouble(name) ?: defaultValue

    fun getDoubles(name: String): List<Double> = parameters[name]?.let { values -> values.mapNotNull { it.trim().toDoubleOrNull() } } ?: listOf()

    override fun toString(): String = queryString ?: "No Parameters"
}
