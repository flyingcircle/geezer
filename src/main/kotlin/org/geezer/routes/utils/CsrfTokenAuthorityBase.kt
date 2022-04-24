package org.geezer.routes.utils

import org.geezer.routes.RequestParameters
import org.geezer.routes.ReturnStatus
import org.geezer.routes.RoutesConfiguration
import org.geezer.routes.RoutesLogger
import java.nio.ByteBuffer
import jakarta.servlet.http.HttpServletRequest

/**
 * Manages the generation and confirmation of CSRF Tokens used for administrative forms.
 */
abstract class CsrfTokenAuthorityBase {

    abstract val routesConfiguration: RoutesConfiguration

    abstract fun encryptEncode(value: ByteArray): String

    abstract fun decodeDecrypt(value: String): ByteArray

    open val maxCsrfTokenValidMSecs: Long
        get() = routesConfiguration.maxCsrfTokenValidMSecs

    val csrfTokenParameterName: String
        get() = routesConfiguration.csrfTokenParameterName

    val log: RoutesLogger
        get() = routesConfiguration.logger

    fun generateCsrfTokenInput(userId: Long, timestamp: Long = System.currentTimeMillis()): String =
        """<input name="$csrfTokenParameterName" value="${generateCsrfToken(userId, timestamp)}" type="hidden">"""

    fun assertValidUserAndToken(expectedUserId: Long, request: HttpServletRequest, parameters: RequestParameters) {
        var tokenValidated = false
        val token = parameters[csrfTokenParameterName]

        if (token == null || token.isBlank()) {
            log.warn("No CSRF token provided for user $expectedUserId for request ${request.requestURL}")
        } else {
            try {
                val (userId, timestamp) = parseToken(token)
                if (userId != expectedUserId) {
                    log.warn("Received CSRF token provided for user $expectedUserId for request ${request.requestURL} with invalid user id $userId")
                } else {
                    val now = System.currentTimeMillis()
                    if (timestamp > now) {
                        log.warn("Received CSRF token provided for user $expectedUserId for request ${request.requestURL} with token timestamp $timestamp in the future of now $now")
                    } else if (timestamp < now - maxCsrfTokenValidMSecs) {
                        log.warn("Received CSRF token provided for user $expectedUserId for request ${request.requestURL} with expired token timestamp $timestamp of now $now. Max milliseconds allowed is $maxCsrfTokenValidMSecs.")
                    } else {
                        tokenValidated = true
                    }
                }
            } catch (e: Exception) {
                log.warn("Parse of CSRF token failed for user $expectedUserId for request ${request.requestURL}.", e)
            }
        }

        if (!tokenValidated) {
            throw ReturnStatus.Forbidden403
        }
    }

    fun generateCsrfToken(userId: Long, timestamp: Long = System.currentTimeMillis()): String {
        val buffer = ByteBuffer.allocate(16)

        buffer.putLong(userId)
        buffer.putLong(timestamp)

        return encryptEncode(buffer.array())
    }

    fun parseToken(token: String): Pair<Long, Long> {
        val buffer = ByteBuffer.wrap(decodeDecrypt(token))
        return buffer.long to buffer.long
    }
}
