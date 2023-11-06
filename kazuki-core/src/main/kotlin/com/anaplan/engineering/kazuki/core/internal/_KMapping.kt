package com.anaplan.engineering.kazuki.core.internal

import com.anaplan.engineering.kazuki.core.*

interface _KMapping<D, R, M : Mapping<D, R>> : Mapping<D, R>, _KRelation<D, R, M> {
    fun construct(base: Map<D, R>): M

    override fun construct(baseSet: Set<Tuple2<D, R>>): M =
        construct(LinkedHashMap<D, R>().apply {
            baseSet.forEach {
                if (containsKey(it._1)) {
                    throw PreconditionFailure("${it._1} is already present in mapping domain")
                }
                put(it._1, it._2)
            }
        })

    val baseMap: Map<D, R>

    override val baseSet: Set<Tuple2<D, R>>
        get() = set(baseMap.entries) { (k, v) -> mk_(k, v) }

}

// TODO - generate impls for consistency
internal class __KMapping<D, R>(override val baseMap: Map<D, R>) : _KMapping<D, R, Mapping<D, R>> {

    override fun construct(base: Map<D, R>) = __KMapping(base)

    override fun get(d: D) = baseMap.get(d) ?: throw PreconditionFailure("$d not in mapping domain")

    override fun contains(element: Tuple2<D, R>) = baseMap[element._1] == element._2

    override fun containsAll(elements: Collection<Tuple2<D, R>>) = forall(elements) { contains(it) }

    override val dom by lazy { as_Set(baseMap.keys) }

    override val rng by lazy { as_Set(baseMap.values) }

    override val size by baseMap::size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Relation<*, *>) return false
        return baseSet == other
    }

    override fun hashCode() = baseMap.hashCode()

    override fun isEmpty() = baseMap.isEmpty()

    override fun iterator() = baseSet.iterator()

    override fun toString() = "map$baseMap"
}

internal class __KMapping1<D, R>(override val baseMap: Map<D, R>) : Mapping1<D, R>,
    _KMapping<D, R, Mapping1<D, R>> {

    init {
        if (!isValid()) {
            throw InvariantFailure()
        }
    }

    protected fun isValid(): Boolean = atLeastOneElement()

    override fun get(d: D) = baseMap.get(d) ?: throw PreconditionFailure("$d not in mapping domain")

    override fun construct(base: Map<D, R>) = __KMapping1(base)

    override val card: nat1 by lazy { baseMap.size }

    override val dom by lazy { as_Set1(baseMap.keys) }

    override val rng by lazy { as_Set1(baseMap.values) }

    override val size by baseMap::size

    override fun contains(element: Tuple2<D, R>) = baseMap[element._1] == element._2

    override fun containsAll(elements: Collection<Tuple2<D, R>>) = forall(elements) { contains(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Relation<*, *>) return false
        return baseSet == other
    }

    override fun hashCode() = baseMap.hashCode()

    override fun isEmpty() = baseMap.isEmpty()

    override fun iterator() = baseSet.iterator()

    override fun toString() = "map1$baseMap"
}

internal fun <D, R, T : Mapping<D, R>> T.transformMapping(fn: (_KMapping<D, R, T>) -> Map<D, R>): T {
    val kMap = this as? _KMapping<D, R, T> ?: throw PreconditionFailure("Mapping was implemented outside Kazuki")
    return kMap.construct(fn(kMap))
}