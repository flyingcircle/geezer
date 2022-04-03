package org.geezer.routes

import org.geezer.routes.RoutesPatterns.BooleanPattern
import org.geezer.routes.RoutesPatterns.BytePattern
import org.geezer.routes.RoutesPatterns.CharPattern
import org.geezer.routes.RoutesPatterns.DoublePattern
import org.geezer.routes.RoutesPatterns.FloatPattern
import org.geezer.routes.RoutesPatterns.IntPattern
import org.geezer.routes.RoutesPatterns.LongPattern
import org.geezer.routes.RoutesPatterns.ShortPattern
import org.geezer.routes.RoutesPatterns.StringPattern
import java.net.URL
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import kotlin.reflect.KClass

/**
 * The type of function parameters for before routes, routes, or after routes.
 */
internal enum class FunctionParameterClass(val clazz: KClass<*>, val routeRegex: Regex? = null) {
    RequestContextClass(RequestContext::class),
    RequestPathClass(RequestPath::class),
    RequestParametersClass(RequestParameters::class),
    ServletRequestClass(HttpServletRequest::class),
    ServletResponseClass(HttpServletResponse::class),
    SessionClass(HttpSession::class),
    RequestedContentTypeClass(RequestedContentType::class),
    RequestContentClass(RequestContent::class),
    ParameterMapClass(Map::class),
    UrlClass(URL::class),
    StringClass(String::class, Regex(StringPattern)),
    CharClass(Char::class, Regex(CharPattern)),
    BooleanClass(Boolean::class, Regex(BooleanPattern)),
    ByteClass(Byte::class, Regex(BytePattern)),
    ShortClass(Short::class, Regex(ShortPattern)),
    IntClass(Int::class, Regex(IntPattern)),
    LongClass(Long::class, Regex(LongPattern)),
    FloatClass(Float::class, Regex(FloatPattern)),
    DoubleClass(Double::class, Regex(DoublePattern));

    val providedFromRoute: Boolean
        get() = routeRegex != null

    companion object {
        val types = values().map { it.clazz }.toSet()

        fun fromClass(clazz: KClass<*>): FunctionParameterClass? = values().firstOrNull { it.clazz == clazz }
    }
}
