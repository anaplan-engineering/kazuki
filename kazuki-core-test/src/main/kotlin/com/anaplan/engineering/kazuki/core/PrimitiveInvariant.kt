package com.anaplan.engineering.kazuki.core


@PrimitiveInvariant(name = "NumberExample1", base = nat::class)
fun ne1Invariant(n: nat) = n in 0u..10u

@PrimitiveInvariant(name = "NumberExample2", base = nat1::class)
fun ne2Invariant(n: nat1) = n in 2u..5u

@PrimitiveInvariant(name = "NumberExample3", base = integer::class)
fun ne3Invariant(n: integer) = n != 17L

@Module
interface FourNums {
    val n0: nat
    val n1: NumberExample1
    val n2: NumberExample2
    val n3: NumberExample3

    @Invariant
    fun equalTwenty() = (n0 + n1 + n2).toInteger() + n3 != 20L
}

val addOneN: (nat) -> nat = function(
    command = { n -> n + 1u }
)

val addOneNE1: (NumberExample1) -> NumberExample1 = function(
    command = { input -> input + 1u },
    pre = { input -> input in 0u..5u }
)

val addOneNE2: (NumberExample2) -> NumberExample2 = function(
    command = { input -> input + 1u },
    pre = { input -> input in 0u..4u }
)

val addOneNE3: (NumberExample3) -> nat = function(
    command = { input -> (input + 1).toNat() }
)