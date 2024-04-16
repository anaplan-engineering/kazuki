package com.anaplan.engineering.kazuki.core

interface Relation<D, R> : Set<Tuple2<D, R>> {

    val dom: Set<D>

    val rng: Set<R>

}

interface _KRelation<D, R, T : Relation<D, R>> : Relation<D, R> {
    fun construct(baseSet: Set<Tuple2<D, R>>): T

    val baseSet: Set<Tuple2<D, R>>
}

fun <D, R> mk_Relation(vararg elems: Tuple2<D, R>): Relation<D, R> = __KRelation(elems.toSet())

fun <D, R> mk_Relation(elems: Iterable<Tuple2<D, R>>): Relation<D, R> = __KRelation(elems.toSet())

infix operator fun <D, R, T : Relation<D, R>> T.plus(t: Tuple2<D, R>) = transform { it.baseSet + mk_(t._1, t._2) }

infix operator fun <D, R, T : Relation<D, R>> T.plus(m: Relation<D, R>) = transform { it.baseSet + m }

infix fun <D, R, T : Relation<D, R>> T.domRestrictTo(s: Set<D>) = transform { it.baseSet.filter { (k, _) -> k in s } }

infix fun <D, R, T : Relation<D, R>> T.drt(s: Set<D>) = domRestrictTo(s)

infix fun <D, R, T : Relation<D, R>> T.rngRestrictTo(s: Set<R>) = transform { it.baseSet.filter { (_, v) -> v in s } }

infix fun <D, R, T : Relation<D, R>> T.rrt(s: Set<R>) = rngRestrictTo(s)

infix fun <D, R, T : Relation<D, R>> T.domSubtract(s: Set<D>) = transform { it.baseSet.filter { (k, _) -> k !in s } }

infix fun <D, R, T : Relation<D, R>> T.dsub(s: Set<D>) = domSubtract(s)

infix fun <D, R, T : Relation<D, R>> T.rngSubtract(s: Set<R>) = transform { it.baseSet.filter { (_, v) -> v !in s } }

infix fun <D, R, T : Relation<D, R>> T.rsub(s: Set<R>) = rngSubtract(s)

private fun <D, R, T : Relation<D, R>> T.transform(fn: (_KRelation<D, R, T>) -> Collection<Tuple2<D, R>>): T {
    val kRelation = this as? _KRelation<D, R, T> ?: throw PreconditionFailure("Relation was implemented outside Kazuki")
    return kRelation.construct(fn(kRelation).toSet())
}

private class __KRelation<D, R>(override val baseSet: Set<Tuple2<D, R>>) : _KRelation<D, R, __KRelation<D, R>>,
    Set<Tuple2<D, R>> by baseSet {

    override fun construct(baseSet: Set<Tuple2<D, R>>) = __KRelation(baseSet)

    override val dom by lazy { set(baseSet) { it._1 } }

    override val rng by lazy { set(baseSet) { it._2 } }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Relation<*, *>) return false
        return baseSet == other
    }

    override fun hashCode(): Int {
        return baseSet.hashCode()
    }

    override fun toString() = "relation$baseSet"
}
