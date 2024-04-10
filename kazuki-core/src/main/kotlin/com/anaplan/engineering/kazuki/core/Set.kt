package com.anaplan.engineering.kazuki.core

fun <T> mk_Set(vararg elems: T): Set<T> = mk_Set(elems.toSet())

fun <T> mk_Set(elems: Collection<T>): Set<T> = __KSet(elems.toSet())

fun mk_Set(elems: IntRange): Set<Int> = mk_Set(elems.toSet())

infix fun <T> Set<T>.subset(other: Set<T>) = other.containsAll(this)

val <T> Set<T>.card
    get() = size

fun <T> dunion(sets: Set<Set<T>>) = sets.flatten().toSet()

fun <T> dunion(vararg sets: Set<T>) = dunion(sets.toSet())

infix fun <T, U> Iterable<T>.x(other: Iterable<U>) = mk_Set(flatMap { t -> other.map { u -> mk_(t, u) } })

interface KSet<T, S : Set<T>> : Set<T> {
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

// TODO -- generate the below to suitable size!

fun <I, O> set(
    provider: Collection<I>,
    selector: (I) -> O
) = set(provider, { true }, selector)

fun <I, O> set(
    provider: Collection<I>,
    filter: (I) -> Boolean,
    selector: (I) -> O
) = mk_Set(provider.filter(filter).map(selector))

fun <I1, I2, O> set(
    p1: Iterable<I1>,
    p2: Iterable<I2>,
    selector: (I1, I2) -> O
) = set(p1, p2, { _, _ -> true }, selector)

fun <I1, I2, O> set(
    p1: Iterable<I1>,
    p2: Iterable<I2>,
    filter: (I1, I2) -> Boolean,
    selector: (I1, I2) -> O
) = mk_Set((cross(p1, p2)).filter(tupleAdapter(filter)).map(tupleAdapter(selector)))

fun <I1, I2, I3, O> set(
    p1: Iterable<I1>,
    p2: Iterable<I2>,
    p3: Iterable<I3>,
    filter: (I1, I2, I3) -> Boolean,
    selector: (I1, I2, I3) -> O
) = mk_Set((cross(p1, p2, p3)).filter(tupleAdapter(filter)).map(tupleAdapter(selector)))

fun <I1, I2, O> tupleAdapter(fn: (I1, I2) -> O): (Tuple2<I1, I2>) -> O = { t -> fn(t._1, t._2) }
fun <I1, I2, I3, O> tupleAdapter(fn: (I1, I2, I3) -> O): (Tuple3<I1, I2, I3>) -> O = { t -> fn(t._1, t._2, t._3) }

private fun <T1, T2> cross(l: Iterable<T1>, r: Iterable<T2>) = l.flatMap { t -> r.map { u -> mk_(t, u) } }
private fun <T1, T2, T3> cross(i1: Iterable<T1>, i2: Iterable<T2>, i3: Iterable<T3>) =
    i1.flatMap { t1 ->
        i2.flatMap { t2 ->
            i3.map { t3 -> mk_(t1, t2, t3) }
        }

    }


internal operator fun <T1, T2, U> Iterable<Tuple2<T1, T2>>.times(other: Iterable<U>) =
    mk_Set(flatMap { t -> other.map { u -> mk_(t._1, t._2, u) } })

val x = set(mk_Set(1, 2, 3), { it != 2 }) { it * 2 }

val y1 = set(mk_Set(1, 2, 3), mk_Set("a", "b")) { i1, i2 -> i1 }

val y2 = set(mk_Set(1, 2, 3), mk_Set("a", "b"), { i1, i2 -> i1 > 1 }) { i1, i2 -> i1 }

val z = (mk_Set(1, 2, 3) x mk_Set(1, 2, 3)) * mk_Set(1, 2, 3)
