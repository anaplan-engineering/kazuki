package com.anaplan.engineering.kazuki.core

interface Sequence<T> : List<T> {

    val len: nat

    val elems: Set<T>

    val inds: Set<nat1>
}

fun <T> mk_Seq(vararg elems: T): Sequence<T> = mk_Seq(elems.toList())

fun <T> mk_Seq(elems: List<T>): Sequence<T> = __KSequence(elems)


// TODO generate impls to ensure consistenct
private class __KSequence<T>(val elements: List<T>) : Sequence<T>, List<T> by elements {
    override val len: nat by elements::size

    // TODO -- need to enforce nat
    override operator fun get(index: nat): T = elements.get(index - 1)

    override val elems by lazy {
        mk_Set(this)
    }

    override val inds by lazy {
        mk_Set(1..len)
    }

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

    // TODO -- need to enfore nat1
    override operator fun get(index: nat1): T = elements.get(index - 1)

    init {
        if (!isValid()) {
            throw InvariantFailure()
        }
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

inline fun <reified T: Enum<T>> asSequence(): Sequence1<T> {
    return mk_Seq1(enumValues<T>().toList())
}