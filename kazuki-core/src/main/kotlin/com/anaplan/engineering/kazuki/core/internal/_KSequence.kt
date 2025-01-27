package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.internal._KSequence.Companion.ChunkSize
import kotlin.reflect.KClass

interface _KSequence<T, S : Sequence<T>> : Sequence<T>, _KazukiObject {
    fun construct(elements: Array<List<T>>): S

    val elements: Array<List<T>>

    val comparableWith: KClass<*>

    override fun indexOf(element: T): nat1 {
        val outerArray = elements.find { element in it }
        if (outerArray == null) {
            throw PreconditionFailure("Element $element not in $this")
        }
        return (elements.indexOf(outerArray).toLong() * ChunkSize) + outerArray.indexOf(element) + 1
    }

    private abstract class _KSequenceIterator<T, I>(val elements: Array<List<T>>) : Iterator<I> {
        var outer: Int = 0
        var inner: Int = 0

        override fun hasNext() = outer < elements.size && inner < elements[outer].size

        fun step() {
            if (inner == ChunkSize) {
                inner = 0
                outer++
            } else {
                inner++
            }
        }

        val elem get() = elements[outer][inner]
        val ind get() = (outer.toLong() * ChunkSize) + inner

    }

    private class _KSequenceTupleIterator<T>(elements: Array<List<T>>) :
        _KSequenceIterator<T, Tuple2<nat1, T>>(elements) {
        override fun next(): Tuple2<nat1, T> {
            val tuple = mk_<nat1, T>(ind, elem)
            step()
            return tuple
        }
    }

    private class _KSequenceIndsIterator<T>(elements: Array<List<T>>) :
        _KSequenceIterator<T, nat1>(elements) {
        override fun next(): nat1 {
            val ind = this.ind
            step()
            return ind
        }
    }

    private class _KSequenceElemsIterator<T>(elements: Array<List<T>>) :
        _KSequenceIterator<T, T>(elements) {
        override fun next(): T {
            val elem = this.elem
            step()
            return elem
        }
    }

    override fun iterator(): Iterator<Tuple2<Long, T>> = _KSequenceTupleIterator<T>(elements)

    override val inds: Iterator<nat1> get() = _KSequenceIndsIterator<T>(elements)

    override val elems: Iterator<T> get() = _KSequenceElemsIterator<T>(elements)


    override operator fun get(index: nat1): T {
        if (index < 1 || index > len) {
            throw PreconditionFailure("Index $index out of range")
        }
        return elements[((index - 1) / ChunkSize).toInt()][((index - 1) % ChunkSize).toInt()]
    }

    override fun lastIndexOf(element: T): nat1 {
        val outerArray = elements.findLast { element in it }
        if (outerArray == null) {
            throw PreconditionFailure("Element $element not in $this")
        }
        return (elements.indexOf(outerArray).toLong() * ChunkSize) + outerArray.lastIndexOf(element) + 1
    }

    // TODO -- long sized set!!
    @Deprecated("Use len (size may overflow)")
    override val size: Int get() = len.toInt()

    override fun contains(element: Tuple2<Long, T>) = get(element._1) == element._2

    override fun containsAll(elements: Collection<Tuple2<Long, T>>) = elements.all { contains(it) }

    fun contains(element: T) = elements.any { element in it }

    override fun isEmpty() = elements.isEmpty()

    companion object {
        fun <T> elementsFromIterable(it: Iterable<T>): Array<List<T>> {
            var inner = mutableListOf<T>()
            val outer = mutableListOf<List<T>>(inner)
            it.forEach { e ->
                if (inner.size == ChunkSize) {
                    inner = mutableListOf<T>()
                    outer.add(inner)
                }
                inner.add(e)
            }
            return outer.toTypedArray()
        }

        internal val ChunkSize = Int.MAX_VALUE
    }
}

internal fun <T, S : Sequence<T>> S.transformSequence(fn: (_KSequence<T, S>) -> List<List<T>>): S {
    val kSequence = this as? _KSequence<T, S> ?: throw PreconditionFailure("Sequence was implemented outside Kazuki")
    val outer = fn(kSequence)
    val elements = if (outer.size > 1) {
        TODO()
    } else {
        outer.toTypedArray()
    }
    return kSequence.construct(elements)
}

// TODO generate impls to ensure consistenct
// TODO common sequence abstract class
internal class __KSequence<T>(override val elements: Array<List<T>>) : Sequence<T>,
    _KSequence<T, Sequence<T>> {

    init {
        assert(elements !is _KazukiObject) {
            "Internal state should not be a Kazuki-generated object"
        }
    }

    override val comparableWith = Sequence::class

    override val len: nat by lazy { elements.sumOf { it.size.toLong() } }

    override val rng by lazy {
        // TODO -- long sized set!!
        as_Set(elements.first())
    }

    override val dom by lazy {
        as_Set(1..len)
    }

    override fun construct(elements: Array<List<T>>) = __KSequence(elements)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is _KSequence<*, *>) return false
        return elements == other.elements
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "seq[${elements.joinToString(", ") { it.joinToString(", ") }}]"

}

internal class __KSequence1<T>(override val elements: Array<List<T>>) : Sequence1<T>, _KSequence<T, Sequence1<T>> {

    override fun construct(elements: Array<List<T>>) = __KSequence1(elements)

    override val len: nat1 by lazy { elements.sumOf { it.size.toLong() } }

    override operator fun get(index: nat1): T {
        if (index < 1 || index > len) {
            throw PreconditionFailure("Index $index out of range")
        }
        return elements[((index - 1) / ChunkSize).toInt()][((index - 1) % ChunkSize).toInt()]
    }

    override val comparableWith = Sequence::class

    init {
        assert(elements !is _KazukiObject) {
            "Internal state should not be a Kazuki-generated object"
        }
        if (!isValid()) {
            throw InvariantFailure("Cannot create empty seq1")
        }
    }

    protected fun isValid(): Boolean = atLeastOneElement()

    override val rng by lazy {
        // TODO -- long sized set!!
        as_Set1(elements.first())
    }

    override val dom by lazy {
        as_Set1(1..len)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sequence<*>) return false
        return elements == other
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "seq1$elements"
}



