package com.anaplan.engineering.kazuki.core

fun <T> mk_Set(vararg elems: T): Set<T> = mk_Set(elems.toSet())

fun <T> mk_Set(elems: Collection<T>): Set<T> = __KSet(elems.toSet())

fun mk_Set(elems: IntRange): Set<Int> = mk_Set(elems.toSet())

infix fun <T> Set<T>.subset(other: Set<T>) = other.containsAll(this)

val <T> Set<T>.card
    get() = size

fun <T> dunion(sets: Set<Set<T>>) = sets.flatten().toSet()


private class __KSet<T>(private val elements: Set<T>) : Set<T> by elements {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Set<*>) return false
        return elements == other
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "set$elements"
}

interface Set1<T>: Set<T> {

    @Invariant
    fun atLeastOneElement() = card > 0

}



fun <T> mk_Set1(vararg elems: T): Set1<T> = mk_Set1(elems.toSet())

fun <T> mk_Set1(elems: Collection<T>): Set1<T> = __KSet1(elems.toSet())

fun mk_Set1(elems: IntRange): Set1<Int> = mk_Set1(elems.toSet())

private class __KSet1<T>(private val elements: Set<T>) : Set1<T>, Set<T> by elements {

    init {
        if (!isValid()) {
            throw InvariantFailure()
        }
    }

    protected fun isValid(): Boolean = atLeastOneElement()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Set<*>) return false
        return elements == other
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }

    override fun toString() = "set1$elements"
}

inline fun <reified T: Enum<T>> asSet(): Set1<T> {
    return mk_Set1(enumValues<T>().toList())
}
