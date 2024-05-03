package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal.__KSequence
import com.anaplan.engineering.kazuki.core.internal.__KSequence1
import com.anaplan.engineering.kazuki.core.internal.transformSequence

// TODO should sequence inherit from relation rather than list?
interface Sequence<T> : List<T> {

    val len: nat

    val elems: Set<T>

    val inds: Set<nat1>

    override fun indexOf(element: T): nat1

    override fun lastIndexOf(element: T): nat1

}

interface Sequence1<T> : Sequence<T> {

    override val len: nat1

    override val elems: Set1<T>

    override val inds: Set1<nat1>

    override operator fun get(index: nat1): T

    @Invariant
    fun atLeastOneElement() = len > 0

}


fun <T> mk_Seq(vararg elems: T): Sequence<T> = __KSequence(elems.toList())

fun <T> as_Seq(elems: Iterable<T>): Sequence<T> = __KSequence(elems.toList())

fun <T> as_Seq(elems: Array<T>): Sequence<T> = __KSequence(elems.toList())

fun <T> mk_Seq1(vararg elems: T): Sequence1<T> = __KSequence1(elems.toList())

fun <T> as_Seq1(elems: Iterable<T>): Sequence1<T> = __KSequence1(elems.toList())

fun <T> as_Seq1(elems: Array<T>): Sequence1<T> = __KSequence1(elems.toList())



// TODO -- should we use different name?
fun <T, S : Sequence<T>> S.drop(n: Int) = transformSequence { it.elements.drop(n) }

fun <T, S : Sequence<T>> S.reverse() = transformSequence { it.elements.reversed() }

infix fun <T, S : Sequence<T>> S.domRestrictTo(s: Set<nat1>) = transformSequence {
    it.elements.filterIndexed { i, _ -> i in s }
}

infix fun <T, S : Sequence<T>> S.drt(s: Set<nat1>) = domRestrictTo(s)

infix fun <T, S : Sequence<T>> S.rngRestrictTo(s: Set<T>) = transformSequence {
    it.elements.filter { e -> e in s }
}

infix fun <T, S : Sequence<T>> S.rrt(s: Set<T>) = rngRestrictTo(s)

infix fun <T, S : Sequence<T>> S.cat(s: Sequence<T>) = transformSequence { it.elements + s }

infix operator fun <T, S : Sequence<T>> S.plus(s: Sequence<T>) = transformSequence { it.elements + s }

infix operator fun <T, S : Sequence<T>> S.plus(t: T) = transformSequence { it.elements + t }

infix operator fun <T, S : Sequence<T>> S.minus(s: Sequence<T>) = transformSequence { it.elements - s }

infix operator fun <T, S : Sequence<T>> S.minus(t: T) = transformSequence { it.elements - t }

fun <T> Sequence<T>.first(): T {
    if (isEmpty()) {
        throw PreconditionFailure("Sequence is empty")
    }
    return this[1]
}

fun <T> Sequence<T>.head() = first()

fun <T> Sequence<T>.tail() = drop(1)




