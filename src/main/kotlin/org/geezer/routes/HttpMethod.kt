package org.geezer.routes

enum class HttpMethod {

    GET, POST, PUT, DELETE, HEAD;

    companion object {
        /**
         * @return The matched HTTP method or `null` if no match is found.
         * @see javax.servlet.http.HttpServletRequest.getMethod
         */
        fun fromServletMethod(servletMethod: String): HttpMethod = values().firstOrNull { it.toString().equals(servletMethod, true) } ?: GET

        fun fromMethod(method: String?): HttpMethod? = method?.let { method -> values().firstOrNull { it.toString().equals(method, true) } }
    }
}
