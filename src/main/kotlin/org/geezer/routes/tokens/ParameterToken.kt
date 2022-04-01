package org.geezer.routes.tokens

internal abstract class ParameterToken(val name: String, val optional: Boolean)

internal class ExactValueParameterToken(name: String, optional: Boolean, val value: String) : ParameterToken(name, optional)
internal class FunctionParameterParameterToken(name: String, optional: Boolean) : ParameterToken(name, optional)
internal class PatternParameterToken(name: String, optional: Boolean, val pattern: String) : ParameterToken(name, optional)
internal class SymbolParameterToken(name: String, optional: Boolean, val symbol: String) : ParameterToken(name, optional)
internal class WildcardParameterToken(name: String, optional: Boolean) : ParameterToken(name, optional)
