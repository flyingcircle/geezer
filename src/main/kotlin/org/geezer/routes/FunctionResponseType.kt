package org.geezer.routes

internal enum class FunctionResponseType {
    ForwardDispatch,
    BytesContent,
    StreamContent,
    StringContent,
    Unit
}
