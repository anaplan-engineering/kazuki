package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.AnimationConditionFailure
import com.anaplan.engineering.kazuki.core.ConditionFailure
import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.UnreachableFailure
import kotlin.reflect.KClass
import kotlin.test.fail

fun causesInvariantFailure(fn: () -> Unit) = causesConditionFailure(fn, InvariantFailure::class)
fun causesPreconditionFailure(fn: () -> Unit) = causesConditionFailure(fn, PreconditionFailure::class)
fun causesUnreachableFailure(fn: () -> Unit) = causesConditionFailure(fn, UnreachableFailure::class)
fun causesAnimationConditionFailure(fn: () -> Unit) = causesConditionFailure(fn, AnimationConditionFailure::class)

fun <T : ConditionFailure> causesConditionFailure(fn: () -> Unit, condition: KClass<T>) {
    try {
        fn()
        fail("Expected ${condition.simpleName}, but no exception was thrown")
    } catch (e: ConditionFailure) {
        if (e::class != condition) {
            fail("Expected ${condition.simpleName}, but got ${e::class.simpleName}")
        }
    }
}