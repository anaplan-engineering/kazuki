package com.anaplan.engineering.kazuki.core


fun <T> forall(i1: Collection<T>, condition: (T) -> Boolean) =
    i1.all(condition)

fun <T1, T2> forall(i1: Collection<T1>, i2: Collection<T2>, condition: (T1, T2) -> Boolean) =
    i1.product(i2).all { condition(it.first, it.second) }

fun <T> `∀`(i1: Collection<T>, condition: (T) -> Boolean) = forall(i1, condition)

fun <T> iota(i1: Collection<T>, condition: (T) -> Boolean) =
    i1.singleOrNull(condition) ?: throw IotaDoesNotSelectResult()

class IotaDoesNotSelectResult : RuntimeException()


fun <I1> exists(i1: Collection<I1>, condition: (I1) -> Boolean) =
    i1.any(condition)

fun <I1> `∃`(i1: Collection<I1>, condition: (I1) -> Boolean) = exists(i1, condition)

fun <I1> exists1(i1: Collection<I1>, condition: (I1) -> Boolean) =
    i1.count(condition) == 1

fun <I1> `∃!`(i1: Collection<I1>, condition: (I1) -> Boolean) = exists1(i1, condition)

infix fun Boolean.`∧`(other: Boolean) = this && other

