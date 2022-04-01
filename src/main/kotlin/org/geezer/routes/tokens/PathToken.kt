package org.geezer.routes.tokens

internal abstract class PathToken(val pathIndex: Int)

internal class GobblePathToken(pathIndex: Int) : PathToken(pathIndex)
internal class ExactValuePathToken(pathIndex: Int, val value: String) : PathToken(pathIndex)
internal class FunctionParameterPathToken(pathIndex: Int) : PathToken(pathIndex)
internal class PatternPathToken(pathIndex: Int, val pattern: String) : PathToken(pathIndex)
internal class SymbolPathToken(pathIndex: Int, val symbol: String) : PathToken(pathIndex)
internal class WildcardPathToken(pathIndex: Int) : PathToken(pathIndex)
