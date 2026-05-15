package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.*

typealias BinaryEvaluationFn = (Number, Number) -> Number
typealias BinaryDomainFn = (Number, Number) -> Boolean

@Module
interface RecordWithFunctionMembers {

    val isApplicable: BinaryDomainFn
    val evaluate: BinaryEvaluationFn
}