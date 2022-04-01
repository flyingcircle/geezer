package org.geezer.routes

/**
 * The requested content type (MIME type) of an HTTP request. The request content is determined in the following order.
 *
 * 1. Taken from the acceptType parameter if present.
 * 2. Determined from the file extension of the request path if present and known.
 * 3. Taken from the Accept header.
 * 4. Defaults to "text/html".
 */
class RequestedContentType {
    /**
     * The content type (MIME type) requested.
     */
    val type: String

    /**
     * The value of the Accept header from the request.
     */
    val acceptTypeHeader: String?

    /**
     * The file extension from the request path.
     */
    val pathFileExtension: String?

    constructor(context: RequestContext) : this(context.parameters["acceptType"], context.path.fileExtension, context.request.getHeader("Accept"))

    constructor(acceptTypeParameter: String? = null, pathFileExtension: String? = null, acceptTypeHeader: String? = null) {
        this.acceptTypeHeader = acceptTypeHeader
        this.pathFileExtension = pathFileExtension

        if (acceptTypeParameter?.isNotBlank() == true) {
            type = acceptTypeParameter
        } else {
            var type = pathFileExtension?.let { MIMETypes.getMimeTypeFromFileExtension(it) }
            if (type == null) {
                type = acceptTypeHeader ?: MIMETypes.HTML
            }
            this.type = type
        }
    }

    operator fun contains(mimeTypePartial: String): Boolean {
        return type.lowercase().contains(mimeTypePartial.lowercase())
    }

    override fun toString() = type
}
