package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal.__KSet
import com.anaplan.engineering.kazuki.core.internal.__KSet1
import com.anaplan.engineering.kazuki.core.internal.transformSet


interface Set1<T> : Set<T> {

    @Invariant
    fun atLeastOneElement() = card > 0

}


fun <T> mk_Set(vararg elems: T): Set<T> = __KSet(elems.toSet())

fun <T> as_Set(elems: Iterable<T>): Set<T> = __KSet(elems.toSet())

fun <T> as_Set(elems: Array<T>): Set<T> = __KSet(elems.toSet())

fun <T> mk_Set1(vararg elems: T): Set1<T> = __KSet1(elems.toSet())

fun <T> as_Set1(elems: Iterable<T>): Set1<T> = __KSet1(elems.toSet())

fun <T> as_Set1(elems: Array<T>): Set1<T> = __KSet1(elems.toSet())


infix fun <T> Set<T>.subset(other: Set<T>) = other.containsAll(this)

infix fun <T, U> Iterable<T>.x(other: Iterable<U>) = as_Set(flatMap { t -> other.map { u -> mk_(t, u) } })

infix fun <T, S : Set<T>> S.inter(other: Set<T>) = transformSet { it.elements.filter { it in other } }

val <T> Set<T>.card get() = size

fun <T> Set<T>.arbitrary() =
    if (isEmpty()) throw PreconditionFailure("Cannot get arbitrary member of emptyset") else first()

fun <T> dunion(sets: Set<Set<T>>) = sets.flatten().toSet()

fun <T> dunion(vararg sets: Set<T>) = dunion(sets.toSet())

