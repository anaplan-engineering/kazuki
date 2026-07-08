package com.anaplan.engineering.kazuki.core

sealed class ConditionFailure(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}

// TODO -- default failures should include message
class PreconditionFailure(message: String? = null) : ConditionFailure(message)
class PostconditionFailure(message: String? = null) : ConditionFailure(message)
class MeasureFailure(message: String? = null) : ConditionFailure(message)
class InvariantFailure(message: String? = null) : ConditionFailure(message)
class UnreachableFailure(message: String? = null) : ConditionFailure(message)
class AnimationConditionFailure(message: String? = null) : ConditionFailure(message)

sealed class SpecificationError(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}

fun pre(msg: String? = null, condition: () -> Boolean) {
    if (!condition()) throw PreconditionFailure(msg)
}

fun post(msg: String? = null, condition: () -> Boolean) {
    if (!condition()) throw PostconditionFailure(msg)
}

/**
 * When writing an animation command it is frequently useful to add arbitrary conditions
 * (that will be checked during animation) to validate our assumptions about the local state.
 */
fun condition(msg: String? = null, condition: () -> Boolean) {
    if (!condition()) throw AnimationConditionFailure(msg)
}

/**
 * A precondition enables us to assume that certain things are true in our command.
 * However, the kotlin compiler cannot alwyas make the same inference. In such instances
 * we can use `unreachable()` to assert that a certain animation path can never be
 * reached. If said path is reached an `UnreachableFailure` will be thrown.
 */
inline fun unreachable(): Nothing = throw UnreachableFailure()


