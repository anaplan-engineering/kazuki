package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal._KSequence
import com.anaplan.engineering.kazuki.core.internal.__KSequence
import com.anaplan.engineering.kazuki.core.internal.__KSequence1
import com.anaplan.engineering.kazuki.core.internal.transformSequence

interface Sequence<out T> : Mapping<Long, T> {

    val len: nat

    val elems: Iterator<T>

    val inds: Iterator<nat1>

    override operator fun get(index: nat1): T

    fun indexOf(element: @UnsafeVariance T): nat1

    fun lastIndexOf(element: @UnsafeVariance T): nat1

    @Invariant
    fun contiguousIndices() = inds == as_Set(1..len)

}

interface Sequence1<out T> : Sequence<T> {

    override val len: nat1

    override val rng: Set1<@UnsafeVariance T>

    override val dom: Set1<nat1>

    @Invariant
    fun atLeastOneElement() = len > 0

}

fun <T> mk_Seq(vararg elems: T): Sequence<T> = __KSequence(arrayOf(elems.toList()))

fun <T> as_Seq(it: Iterable<T>): Sequence<T> = __KSequence(_KSequence.elementsFromIterable(it))

fun <T> as_Seq(elems: Array<T>): Sequence<T> = __KSequence(arrayOf(elems.toList()))

fun <T> mk_Seq1(vararg elems: T): Sequence1<T> =
    if (elems.isEmpty()) {
        throw PreconditionFailure("Cannot construct empty seq1")
    } else {
        __KSequence1(arrayOf(elems.toList()))
    }

fun <T> as_Seq1(it: Iterable<T>): Sequence1<T> {
    val elements = _KSequence.elementsFromIterable(it)
    return if (elements.isEmpty()) {
        throw PreconditionFailure("Cannot convert empty collection to seq1")
    } else {
        __KSequence1(elements)
    }
}

fun <T> as_Seq1(elems: Array<T>): Sequence1<T> =
    if (elems.isEmpty()) {
        throw PreconditionFailure("Cannot convert empty array to seq1")
    } else {
        __KSequence1(arrayOf(elems.toList()))
    }

fun <T, S : Sequence<T>> S.drop(n: nat) =
    if (this is Sequence1<*> && n >= len) {
        throw PreconditionFailure("Cannot drop all elements from seq1")
    } else {
        transformSequence {
            val withChunksRemoved = it.elements.drop((n / _KSequence.ChunkSize).toInt())
            listOf(withChunksRemoved.first().drop<T>((n % _KSequence.ChunkSize).toInt())) + withChunksRemoved.drop(1)
        }
    }

fun <T, S : Sequence<T>> S.take(n: nat) =
    if (this is Sequence1<*> && n < 1) {
        throw PreconditionFailure("Cannot take 0 or fewer elements from seq1")
    } else {
        transformSequence { it.elements.take(n) }
    }

fun <T, S : Sequence<T>> S.reverse() = transformSequence { it.elements.reversed() }

fun <T, S : Sequence<T>> S.insert(t: T, i: nat1) =
    if (i < 1 || i > len + 1) {
        throw PreconditionFailure("Index $i is out of bounds")
    } else {
        transformSequence { it.elements.toMutableList().apply { add(i - 1, t) } }
    }

fun <T, S : Sequence<T>> S.insert(s: S, i: nat1) =
    if (i < 1 || i > len + 1) {
        throw PreconditionFailure("Index $i is out of bounds")
    } else {
        transformSequence { it.elements.toMutableList().apply { addAll(i - 1, s) } }
    }

fun <T, S : Sequence<T>> S.filter(fn: (T) -> Boolean) = transformSequence {
    val filtered = it.elements.map { it.filter(fn) }
    if (filtered.all(List<T>::isEmpty) && this is Sequence1<*>) {
        throw PreconditionFailure("Cannot create empty seq1")
    }
    filtered
}

// can be on interface
fun <T> Sequence<T>.indexOf(s: Sequence<T>) =
    if (!(s subseq this)) {
        throw PreconditionFailure("Sequence $s is not contained in $this")
    } else {
        (1..len).find { i -> s == drop(i - 1).take(s.len) }!!
    }

// can be on interface
infix fun <T> Sequence<T>.subseq(other: Sequence<T>) =
    this == other || (1..other.len).any { i -> this == other.drop(i - 1).take(len) }

infix fun <T, S : Sequence<T>> S.domRestrictTo(s: Set<nat1>) = transformSequence {
    it.elements.filterIndexed { i, _ -> (i + 1) in s }
}

infix fun <T, S : Sequence<T>> S.drt(s: Set<nat1>) = domRestrictTo(s)

infix fun <T, S : Sequence<T>> S.rngRestrictTo(s: Set<T>) = transformSequence {
    it.elements.filter { e -> e in s }
}

infix fun <T, S : Sequence<T>> S.rrt(s: Set<T>) = rngRestrictTo(s)

infix fun <T, S : Sequence<T>> S.cat(s: Sequence<T>) = transformSequence { it.elements + s }

infix fun <T, S : Sequence<T>> S.domSubtract(s: Set<nat1>) = transformSequence {
    it.elements.filterIndexed { i, _ -> (i + 1) !in s }
}

infix fun <T, S : Sequence<T>> S.dsub(s: Set<nat1>) = domSubtract(s)

infix fun <T, S : Sequence<T>> S.rngSubtract(s: Set<T>) = transformSequence {
    it.elements.filter { e -> e !in s }
}

infix fun <T, S : Sequence<T>> S.rsub(s: Set<T>) = rngSubtract(s)


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

fun <T> Sequence<T>.firstOr(onEmpty: T) = if (isEmpty()) onEmpty else this[1]

fun <T> Sequence<T>.single(): T {
    if (len != 1L) {
        throw PreconditionFailure("Cannot get single item for sequence with length $len")
    }
    return this[1]
}

fun <T> Sequence<T>.last(): T {
    if (isEmpty()) {
        throw PreconditionFailure("Sequence is empty")
    }
    return this[len]
}

fun <T> Sequence<T>.head() = first()

fun <T> Sequence<T>.tail() = drop(1)

fun <T> Sequence1<T>.tail(): Sequence<T> = if (size > 1) drop(1) else mk_Seq()

fun <T, S : Sequence<T>> dcat(seqs: Sequence1<S>) =
    if (seqs.size == 1) {
        seqs.first()
    } else {
        seqs.first().transformSequence { init ->
            seqs.drop(1).fold(init.elements) { acc, seq -> acc + seq }
        }
    }




