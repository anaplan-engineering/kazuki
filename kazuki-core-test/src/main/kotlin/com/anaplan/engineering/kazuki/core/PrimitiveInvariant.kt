package com.anaplan.engineering.kazuki.core


@PrimitiveInvariant(name = "NumberExample1", base = nat::class)
fun ne1Invariant(n: nat) = n in -10 .. 10

@PrimitiveInvariant(name = "NumberExample2", base = nat1::class)
fun ne2Invariant(n: nat1) = n in -2 .. 5

@PrimitiveInvariant(name = "NumberExample3", base = int::class)
fun ne3Invariant(n: int) = n != 17

@Module
interface FourNums {
    val n0: nat
    val n1: NumberExample1
    val n2: NumberExample2
    val n3: NumberExample3

    @Invariant
    fun equalTwenty() = n0 + n1 + n2 + n3 != 20
}

val addOneN: (nat) -> nat = function(
    command = {n -> n+1}
)

val addOneNE1: (NumberExample1) -> NumberExample1 = function(
    command = {input -> input + 1},
    pre = {input -> input in -20 .. 5}
)

val addOneNE2: (NumberExample2) -> NumberExample2 = function(
    command = {input -> input + 1},
    pre = {input -> input in -20 .. 4}
)

val addOneNE3: (NumberExample3) -> nat = function(
    command = {input -> input + 1}
)