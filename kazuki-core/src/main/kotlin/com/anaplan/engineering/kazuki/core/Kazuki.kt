package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal._EvaluationProfiler
import org.slf4j.LoggerFactory
import kotlin.jvm.java
import kotlin.reflect.KClass

object Kazuki {
    internal val Log = LoggerFactory.getLogger(Kazuki::class.java)

    internal val Profile by lazy {
        System.getProperty("com.anaplan.engineering.kazuki.profile").toBoolean()
    }

    fun logEvaluationProfile() {
        if (Profile) {
            _EvaluationProfiler.log()
        } else {
            Log.warn("Cannot log evaluation profile as not enabled")
        }
    }
}


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Module(
    /** If true, concrete implementation is generated with necessary constructors and utilities. If false, no concrete
     *  implementation is generated, but fields, invariants etc. are incorporated into descendant classes.
     */
    val makeable: Boolean = true
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Invariant


/**
 * Annotates a derived property that should be used for all available comparisons (including equality)
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComparableProperty(
    val useForOutput: Boolean = false
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComparableTypeLimit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PrimitiveInvariant(
    val name: String,
    val base: KClass<*>
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Pretty

/**
 * Note that, the provider must:
 *  - have inferable generic types (only tested where same as the module thus far)
 *  - have constructor that takes a single param, which is an instance of the module
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
// TODO - better name!
annotation class FunctionProvider(
    val provider: KClass<*>
)


// TODO - create stdlib
typealias nat1 = ULong

private val MAX_LONG = Long.MAX_VALUE.toULong()

fun ULong.toInteger() : integer {
    pre { this <= MAX_LONG }
    return toLong()
}

fun integer.toNat(): nat {
    pre { this >= 0 }
    return toULong()
}

fun integer.toNat1() : nat1 {
    pre { this >= 1 }
    return toULong()
}

fun Int.toNat(): nat {
    pre { this >= 0 }
    return toULong()
}

fun Int.toNat1() : nat1 {
    pre { this >= 1 }
    return toULong()
}

private val MAX_INT = Int.MAX_VALUE.toUInt()

fun ULong.safeToInt(): Int {
    if (this > MAX_INT) {
        throw IllegalStateException("nat/nat1/int greater than $MAX_INT not currently supported in sequence addressing")
    }
    return this.toInt()
}

typealias nat = ULong

typealias integer = Long

typealias bool = Boolean

object InbuiltPrimitiveInvariant {

    val invariants = mapOf(
        nat1::class to ::isNat1Valid,
        nat::class to ::isNatValid,
    )

    fun isNat1Valid(value: nat1) = value > 0uL

    fun isNatValid(value: nat1) = value >= 0uL

}
