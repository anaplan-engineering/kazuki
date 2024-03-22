package com.anaplan.engineering.kazuki.core

import kotlin.reflect.KClass


annotation class RecordType
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Module

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Invariant

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrimitiveInvariant(
    val name: String,
    val base: KClass<*>
)

// TODO - create stdlib
typealias nat1 = Int

typealias nat = Int

typealias int = Int

typealias bool = Boolean

object InbuiltPrimitiveInvariant {

    val invariants = mapOf(
        nat1::class to ::isNat1Valid,
        nat::class to ::isNatValid,
    )

    fun isNat1Valid(value: nat1) = value > 0

    fun isNatValid(value: nat1) = value >= 0

}
