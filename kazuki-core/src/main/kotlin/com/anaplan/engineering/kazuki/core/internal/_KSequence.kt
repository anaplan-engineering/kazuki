package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.*

interface _KSequence<T, S : Sequence<T>>: Sequence<T> {
    fun construct(elements: List<T>): S

    val elements: List<T>
}

internal fun <T, S : Sequence<T>> S.transformSequence(fn: (_KSequence<T, S>) -> List<T>): S {
    val kSequence = this as? _KSequence<T, S> ?: throw PreconditionFailure("Sequence was implemented outside Kazuki")
    return kSequence.construct(fn(kSequence))
}

// TODO generate impls to ensure consistenct
internal class __KSequence<T>(override val elements: List<T>) : Sequence<T>, List<T> by elements,
    _KSequence<T, Sequence<T>> {
    override val len: nat by elements::size

    override operator fun get(index: nat1): T {
        if (index < 1 || index > len) {
            throw PreconditionFailure()
        }
        return elements.get(index - 1)
    }

    override fun indexOf(element: T): nat1 {
        if (element !in elements) {
            throw PreconditionFailure()
        }
        return elements.indexOf(element) + 1
    }

    override fun lastIndexOf(element: T): nat1 {
        if (element !in elements) {
            throw PreconditionFailure()
        }
        return elements.lastIndexOf(element) + 1
    }

    override val elems by lazy {
        as_Set(elements)
    }

    override val inds by lazy {
        as_Set(1..len)
    }

    override fun construct(elements: List<T>) = __KSequence(elements)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sequence<*>) return false
        return elements == other
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "seq$elements"
}

internal class __KSequence1<T>(private val elements: List<T>) : Sequence1<T>, List<T> by elements {

    override val len: nat1 by elements::size

    override operator fun get(index: nat1): T {
        if (index < 1 || index > len) {
            throw PreconditionFailure()
        }
        return elements.get(index - 1)
    }

    init {
        if (!isValid()) {
            throw InvariantFailure()
        }
    }

    // TODO -- add to generator
    override fun indexOf(element: T): nat1 {
        if (element !in elements) {
            throw PreconditionFailure()
        }
        return elements.indexOf(element) + 1
    }

    override fun lastIndexOf(element: T): nat1 {
        if (element !in elements) {
            throw PreconditionFailure()
        }
        return elements.lastIndexOf(element) + 1
    }

    protected fun isValid(): Boolean = atLeastOneElement()

    override val elems by lazy {
        as_Set1(this)
    }

    override val inds by lazy {
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



