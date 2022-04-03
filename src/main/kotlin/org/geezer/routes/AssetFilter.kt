package org.geezer.routes

import org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Collections
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class AssetFilter : Filter {

    private var cacheSeconds = 356 * 24 * 60 * 60

    override fun init(filterConfig: FilterConfig) {
        filterConfig.getInitParameter("CACHE_DAYS")?.let {
            it.toIntOrNull()?.let { days ->
                cacheSeconds = days * 24 * 60 * 60
            }
        }
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        var uri = httpRequest.requestURI
        val hashStartIndex = uri.indexOf("--")
        if (hashStartIndex > 0) {
            val hashEndIndex = uri.lastIndexOf(".")
            if (hashEndIndex > hashStartIndex) {
                uri = "${uri.substring(0, hashStartIndex)}${uri.substring(hashEndIndex)}"
            }

            request.contextPath?.let { contextPath ->
                if (contextPath.isNotBlank()) {
                    uri = uri.replace(contextPath, "")
                }
            }

            (response as HttpServletResponse).addHeader("cache-control", "max-age=$cacheSeconds")
            request.getRequestDispatcher(uri).forward(request, response)
            return
        }

        chain.doFilter(request, response)
    }

    override fun destroy() {}

    companion object {
        private val assetHashPath = Collections.synchronizedMap(mutableMapOf<String, String>())

        @Throws(NoSuchAlgorithmException::class, NoSuchFileException::class, IOException::class)
        fun addChecksumToAssetPath(assetRelativePath: String, request: ServletRequest): String {
            return assetHashPath.getOrPut(assetRelativePath) {
                var hashedPath = assetRelativePath
                val index = hashedPath.lastIndexOf('.')
                if (index > 0) {
                    val checksum = assetFileCheckSum(File(request.servletContext.getRealPath(assetRelativePath)))
                    hashedPath = "${hashedPath.substring(0, index)}--$checksum${hashedPath.substring(index)}"
                }
                hashedPath
            }
        }

        @Throws(NoSuchAlgorithmException::class, NoSuchFileException::class, IOException::class)
        fun assetFileCheckSum(assetFile: File): String {
            if (!assetFile.isFile) {
                throw NoSuchFileException(assetFile)
            }
            return encodeBase64URLSafeString(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(assetFile.toPath())))
        }
    }
}
