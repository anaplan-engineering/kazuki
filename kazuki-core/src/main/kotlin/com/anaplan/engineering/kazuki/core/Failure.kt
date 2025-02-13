package com.anaplan.engineering.kazuki.core

sealed class ConditionFailure(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}

// TODO -- default failures should include message
class PreconditionFailure(message: String? = null) : ConditionFailure(message)
class PostconditionFailure(message: String? = null) : ConditionFailure(message)
class MeasureFailure(message: String? = null) : ConditionFailure(message)
class InvariantFailure(message: String? = null) : ConditionFailure(message)

sealed class SpecificationError(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}

fun pre(msg: String? = null, condition: () -> Boolean) {
    if (!condition()) throw PreconditionFailure(msg)
}

fun post(msg: String? = null, condition: () -> Boolean) {
    if (!condition()) throw PostconditionFailure(msg)
}