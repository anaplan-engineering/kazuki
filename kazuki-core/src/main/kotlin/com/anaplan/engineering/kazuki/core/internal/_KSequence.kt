package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.*
import kotlin.reflect.KClass

interface _KSequence<T, S : Sequence<T>> : Sequence<T>, _KazukiObject {
    fun construct(elements: List<T>): S

    val elements: List<T>

    val comparableWith: KClass<*>

    override val tuples: Sequence<Tuple2<nat1, T>>
        get() = as_Seq(elements.mapIndexed { index, t -> mk_((index + 1).toNat1(), t) })

}

internal fun <T, S : Sequence<T>> S.transformSequence(fn: (_KSequence<T, S>) -> List<T>): S {
    val kSequence = this as? _KSequence<T, S> ?: throw PreconditionFailure("Sequence was implemented outside Kazuki")
    val elements = fn(kSequence)
    if (elements.isEmpty() && this is Sequence1<*>) {
        throw PreconditionFailure("Cannot create seq1 without elements")
    }
    return kSequence.construct(elements)
}

// TODO generate impls to ensure consistenct
internal class __KSequence<T>(override val elements: List<T>) : Sequence<T>, _KSequence<T, Sequence<T>>, Collection<T> by elements {
    init {
        assert(elements !is _KazukiObject) {
            "Internal state should not be a Kazuki-generated object"
        }
    }

    override fun pretty() = "<${
        elements.joinToString(", ") {
            if (it is PrettyPrintable) {
                it.pretty()
            } else {
                it.toString()
            }
        }
    }>"

    override val comparableWith = Sequence::class

    override val len: nat by lazy { elements.size.toNat() }

    override operator fun get(index: nat1): T {
        if (index < 1u || index > len) {
            throw PreconditionFailure("Index $index out of range")
        }
        return elements.get((index - 1u).safeToInt())
    }

    override fun indexOf(element: T): nat1 {
        if (element !in elements) {
            throw PreconditionFailure("Element $element not in $this")
        }
        return elements.indexOf(element).toNat() + 1u
    }

    override fun lastIndexOf(element: T): nat1 {
        if (element !in elements) {
            throw PreconditionFailure("Element $element not in $this")
        }
        return elements.lastIndexOf(element).toNat() + 1u
    }

    override val elems by lazy {
        as_Set(elements)
    }

    override val inds by lazy {
        as_Set(1uL..len)
    }

    override fun construct(elements: List<T>) = __KSequence(elements)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is _KSequence<*, *>) return false
        return elements == other.elements
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "seq$elements"
}

internal class __KSequence1<T>(override val elements: List<T>) : Sequence1<T>, _KSequence<T, Sequence1<T>>, Collection<T> by elements {

    override fun construct(elements: List<T>) = __KSequence1(elements)

    override val len: nat1 by lazy { elements.size.toNat1() }

    override operator fun get(index: nat1): T {
        if (index < 1u || index > len) {
            throw PreconditionFailure("Index $index is not valid for sequence of length $len")
        }
        return elements.get((index - 1u).safeToInt())
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

    // TODO -- add to generator
    override fun indexOf(element: T): nat1 {
        if (element !in elements) {
            throw PreconditionFailure()
        }
        return elements.indexOf(element).toNat() + 1u
    }

    override fun lastIndexOf(element: T): nat1 {
        if (element !in elements) {
            throw PreconditionFailure()
        }
        return elements.lastIndexOf(element).toNat() + 1u
    }

    protected fun isValid(): Boolean = atLeastOneElement()

    override val elems by lazy {
        as_Set1(this)
    }

    override val inds by lazy {
        as_Set1(1uL..len)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is _KSequence<*, *>) return false
        return elements == other.elements
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "seq1$elements"

    override fun pretty() = "<${
        elements.joinToString(", ") {
            if (it is PrettyPrintable) {
                it.pretty()
            } else {
                it.toString()
            }
        }
    }>"
}



