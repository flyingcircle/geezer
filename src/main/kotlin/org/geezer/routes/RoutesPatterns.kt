package org.geezer.routes

import kotlin.reflect.KClass

internal object RoutesPatterns {
    const val BytePattern = "(-?)\\d{1,3}"

    const val ShortPattern = "(-?)\\d{1,5}"

    const val IntPattern = "(-?)\\d{1,10}"

    const val LongPattern = "(-?)\\d{1,19}"

    const val FloatPattern = "(-?)\\d{1,8}(\\.\\d{1,23})?"

    const val DoublePattern = "(-?)\\d{1,15}(\\.\\d{1,46})?"

    const val BooleanPattern = "(([tT][rR][uU][eE])|([fF][aA][lL][sS][eE]))"

    const val CharPattern = "."

    const val StringPattern = ".*"

    const val WildcardPattern = StringPattern

    val typePatterns = mapOf<KClass<*>, String>(
        Byte::class to BytePattern,
        Short::class to ShortPattern,
        Int::class to IntPattern,
        Long::class to LongPattern,
        Float::class to FloatPattern,
        Double::class to DoublePattern,
        Boolean::class to BooleanPattern,
        Char::class to CharPattern,
        String::class to StringPattern
    )
}
