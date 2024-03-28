package com.anaplan.engineering.kazuki.core

fun <T> mk_Set(vararg elems: T): Set<T> = mk_Set(elems.toSet())

fun <T> mk_Set(elems: Collection<T>): Set<T> = __KSet(elems.toSet())

fun mk_Set(elems: IntRange): Set<Int> = mk_Set(elems.toSet())

infix fun <T> Set<T>.subset(other: Set<T>) = other.containsAll(this)

val <T> Set<T>.card
    get() = size

fun <T> dunion(sets: Set<Set<T>>) = sets.flatten().toSet()

fun <T> dunion(vararg sets: Set<T>) = dunion(sets.toSet())

interface KSet<T, S : Set<T>>: Set<T> {
    fun construct(elements: Set<T>): S

    val elements: Set<T>
}

private fun <T, S : Set<T>> S.transform(fn: (KSet<T, S>) -> Set<T>): S {
    val kSet = this as? KSet<T, S> ?: throw PreconditionFailure("Set was implemented outside Kazuki")
    return kSet.construct(fn(kSet))
}

fun <T, S : Set<T>> S.drop(n: Int) = transform { (it.elements as Iterable<T>).drop(n).toSet() }

private class __KSet<T>(override val elements: Set<T>) : Set<T> by elements, KSet<T, Set<T>> {

    override fun construct(elements: Set<T>) = __KSet(elements)

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

interface Set1<T> : Set<T> {

    @Invariant
    fun atLeastOneElement() = card > 0

}


fun <T> mk_Set1(vararg elems: T): Set1<T> = mk_Set1(elems.toSet())

fun <T> mk_Set1(elems: Collection<T>): Set1<T> = __KSet1(elems.toSet())

fun mk_Set1(elems: IntRange): Set1<Int> = mk_Set1(elems.toSet())

private class __KSet1<T>(override val elements: Set<T>) : KSet<T, Set<T>>, Set1<T>, Set<T> by elements {

    init {
        if (!isValid()) {
            throw InvariantFailure()
        }
    }

    override fun construct(elements: Set<T>) = __KSet1(elements)

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

inline fun <reified T : Enum<T>> asSet(): Set1<T> {
    return mk_Set1(enumValues<T>().toList())
}

fun <I, O> set(
    selector: (I) -> O,
    provider: Collection<I>,
    filter: (I) -> Boolean = { true }
) = mk_Set(provider.filter(filter).map(selector))

