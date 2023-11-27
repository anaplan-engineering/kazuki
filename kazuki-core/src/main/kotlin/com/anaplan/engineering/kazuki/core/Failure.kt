package com.anaplan.engineering.kazuki.core

sealed class ConditionFailure(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}

class PreconditionFailure : ConditionFailure()
class PostconditionFailure : ConditionFailure()
class InvariantFailure : ConditionFailure()

sealed class SpecificationError(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}