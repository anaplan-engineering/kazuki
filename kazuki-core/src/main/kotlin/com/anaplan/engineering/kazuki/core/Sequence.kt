package com.anaplan.engineering.kazuki.core

interface Sequence<T> : List<T> {

    val len: nat

    val elems: Set<T>

    val inds: Set<nat1>

    override fun indexOf(element: T): nat1

    override fun lastIndexOf(element: T): nat1

}

interface KSequence<T, S : Sequence<T>>: Sequence<T> {
    fun construct(elements: List<T>): S

    val elements: List<T>
}

private fun <T, S : Sequence<T>> S.transform(fn: (KSequence<T, S>) -> List<T>): S {
    val kSequence = this as? KSequence<T, S> ?: throw PreconditionFailure("Sequence was implemented outside Kazuki")
    return kSequence.construct(fn(kSequence))
}

// TODO -- should we use different name?
fun <T, S : Sequence<T>> S.drop(n: Int) = transform { it.elements.drop(n) }

fun <T, S : Sequence<T>> S.reverse() = transform { it.elements.reversed() }

infix fun <T, S : Sequence<T>> S.domRestrictTo(s: Set<nat1>) = transform {
    it.elements.filterIndexed { i, _ -> i in s }
}

infix fun <T, S : Sequence<T>> S.drt(s: Set<nat1>) = domRestrictTo(s)

infix fun <T, S : Sequence<T>> S.rngRestrictTo(s: Set<T>) = transform {
    it.elements.filter { e -> e in s }
}

infix fun <T, S : Sequence<T>> S.rrt(s: Set<T>) = rngRestrictTo(s)

// TODO -- override plus for Seq?
infix fun <T, S : Sequence<T>> S.cat(s: Sequence<T>) = transform { it.elements + s }

fun <T> Sequence<T>.first(): T {
    if (isEmpty()) {
        throw PreconditionFailure("Sequence is empty")
    }
    return this[1]
}


fun <T> mk_Seq(vararg elems: T): Sequence<T> = mk_Seq(elems.toList())

fun <T> mk_Seq(elems: List<T>): Sequence<T> = __KSequence(elems)


// TODO generate impls to ensure consistenct
private class __KSequence<T>(override val elements: List<T>) : Sequence<T>, List<T> by elements,
    KSequence<T, Sequence<T>> {
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
        mk_Set(this)
    }

    override val inds by lazy {
        mk_Set(1..len)
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

interface Sequence1<T> : Sequence<T> {

    override val len: nat

    override val elems: Set1<T>

    override val inds: Set1<nat1>

    override operator fun get(index: nat1): T

    @Invariant
    fun atLeastOneElement() = len > 0

}

fun <T> mk_Seq1(vararg elems: T): Sequence1<T> = mk_Seq1(elems.toList())

fun <T> mk_Seq1(elems: List<T>): Sequence1<T> = __KSequence1(elems)


private class __KSequence1<T>(private val elements: List<T>) : Sequence1<T>, List<T> by elements {

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
        mk_Set1(this)
    }

    override val inds by lazy {
        mk_Set1(1..len)
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

inline fun <reified T : Enum<T>> asSequence(): Sequence1<T> {
    return mk_Seq1(enumValues<T>().toList())
}

