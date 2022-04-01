package org.geezer.routes

/**
 * When thrown the {@code status} of this exception and no further processing will continue.
 * @property status The HTTP status to return.
 */
class ReturnStatus(val status: Int, val statusMessage: String? = null) : Exception() {

    fun isError(): Boolean {
        return status >= 400
    }

    companion object {
        val BadRequest400: ReturnStatus = ReturnStatus(400, "Bad Request")

        val Unauthorized401: ReturnStatus = ReturnStatus(401, "Unauthorized")

        val Forbidden403: ReturnStatus = ReturnStatus(403, "Forbidden")

        var NotFound404: ReturnStatus = ReturnStatus(404, "Not Found")

        var InternalServerError500: ReturnStatus = ReturnStatus(500, "Internal Server Error")

        var ServiceUnavailableError503: ReturnStatus = ReturnStatus(503, "Service Unavailable")
    }
}
