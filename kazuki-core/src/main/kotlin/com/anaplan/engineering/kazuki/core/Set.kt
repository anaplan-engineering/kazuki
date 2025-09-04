package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal.__KSet
import com.anaplan.engineering.kazuki.core.internal.__KSet1
import com.anaplan.engineering.kazuki.core.internal.transformSet
import kotlin.collections.filter as kotlinFilter

interface Set1<T> : Set<T> {

    @Invariant
    fun atLeastOneElement() = card > 0uL

}

fun <T> mk_Set(vararg elems: T): Set<T> = __KSet(elems.toSet())

fun <T> as_Set(elems: Iterable<T>): Set<T> = __KSet(LinkedHashSet<T>(elems.count()).apply { addAll(elems) })

fun <T> as_Set(elems: Array<T>): Set<T> = __KSet(elems.toSet())

fun <T> mk_Set1(vararg elems: T): Set1<T> =
    if (elems.isEmpty()) {
        throw PreconditionFailure("Cannot create set1 without elements")
    } else {
        __KSet1(elems.toSet())
    }

fun <T> as_Set1(elems: Iterable<T>): Set1<T> =
    if (elems.count() == 0) {
        throw PreconditionFailure("Cannot convert to set1 without elements")
    } else {
        __KSet1(LinkedHashSet<T>(elems.count()).apply { addAll(elems) })
    }

fun <T> as_Set1(elems: Array<T>): Set1<T> =
    if (elems.isEmpty()) {
        throw PreconditionFailure("Cannot convert to set1 without elements")
    } else {
        __KSet1(elems.toSet())
    }

fun Set<*>.pretty() = this.prettyOrDefault()

fun <T, S : Set<T>> S.filter(fn: (T) -> Boolean): S = transformSet { it.elements.kotlinFilter(fn) }

fun <T> Set<T>.filter(retainType: Boolean, fn: (T) -> Boolean): Set<T> =
    if (retainType) {
        this.filter(fn)
    } else {
        as_Set(this.kotlinFilter(fn))
    }

fun <T> Set1<T>.fold1(fn: (T, T) -> T): T = toList().let { list ->
    list.drop(1).fold(list.first(), fn)
}

infix fun <T> Set<T>.subset(other: Set<T>) = other.containsAll(this)

infix fun <T, U> Iterable<T>.x(other: Iterable<U>) = as_Set(flatMap { t -> other.map { u -> mk_(t, u) } })

infix fun <T, S : Set<T>> S.inter(other: Set<T>) = transformSet { it.elements.kotlinFilter { it in other } }

infix fun <T, S : Set<T>> S.union(other: Set<T>) = transformSet { it.elements.toMutableSet().apply { addAll(other) } }

val <T> Set<T>.card: nat get() = size.toNat()

fun <T> Set<T>.arbitrary() =
    if (isEmpty()) throw PreconditionFailure("Cannot get arbitrary member of emptyset") else first()

fun <T> Set<T>.single(): T {
    if (card != 1uL) {
        throw PreconditionFailure("Cannot get single item for set with cardinality $card")
    }
    return first()
}

fun <T> dunion(sets: Set<Set<T>>) = as_Set(sets.flatten())

infix operator fun <T, S : Set<T>> S.plus(s: Set<T>) = transformSet { it.elements.toMutableSet().apply { addAll(s) } }
infix operator fun <T, S : Set<T>> S.plus(t: T) = transformSet { it.elements.toMutableSet().apply { add(t) } }

infix operator fun <T, S : Set<T>> S.minus(s: Set<T>): S = diff(s)
infix operator fun <T, S : Set<T>> S.minus(t: T): S = diff(t)

infix operator fun <T> Set<T>.div(s: Set<T>): Set<T> = as_Set(this).diff(s)
infix operator fun <T> Set<T>.div(t: T): Set<T> = as_Set(this).diff(t)

fun <T, S : Set<T>> S.diff(s: Set<T>): S =
    transformSet { it.elements.toMutableSet().apply { removeAll(s) } }

fun <T, S : Set<T>> S.diff(t: T): S =
    transformSet { it.elements.toMutableSet().apply { remove(t) } }