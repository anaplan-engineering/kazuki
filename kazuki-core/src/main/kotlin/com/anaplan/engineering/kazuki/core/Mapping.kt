package com.anaplan.engineering.kazuki.core

interface Mapping<D, R> : Relation<D, R> {

    operator fun get(d: D): R

}

interface Mapping1<D, R> : Mapping<D, R> {
    val card: nat1

    override val dom: Set1<D>

    override val rng: Set1<R>

    @Invariant
    fun atLeastOneElement() = card > 0
}

infix operator fun <D, R, M : Mapping<D, R>> M.times(t: Tuple2<D, R>) = transform {
    LinkedHashMap<D, R>().apply {
        put(t._1, t._2)
        it.forEach {
            if (!containsKey(it._1)) {
                put(it._1, it._2)
            }
        }
    }
}

infix operator fun <D, R, M : Mapping<D, R>> M.times(r: Relation<D, R>) = transform {
    if (!is_Mapping(r)) {
        throw PreconditionFailure("Argument to mapping override must be mapping")
    }
    LinkedHashMap<D, R>().apply {
        r.forEach {
            put(it._1, it._2)
        }
        it.forEach {
            if (!containsKey(it._1)) {
                put(it._1, it._2)
            }
        }
    }
}

interface _KMapping<D, R, M : Mapping<D, R>> : Mapping<D, R>, _KRelation<D, R, M> {
    fun construct(base: Map<D, R>): M

    override fun construct(base: Set<Tuple2<D, R>>): M =
        construct(LinkedHashMap<D, R>().apply {
            base.forEach {
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

private fun <D, R, T : Mapping<D, R>> T.transform(fn: (_KMapping<D, R, T>) -> Map<D, R>): T {
    val kMap = this as? _KMapping<D, R, T> ?: throw PreconditionFailure("Mapping was implemented outside Kazuki")
    return kMap.construct(fn(kMap))
}

fun <D, R> mk_Mapping(base: Map<D, R>): Mapping<D, R> = __KMapping(base)

fun <D, R> mk_Mapping(maplets: Iterable<Tuple2<D, R>>): Mapping<D, R> =
    __KMapping(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> mk_Mapping(vararg maplets: Tuple2<D, R>): Mapping<D, R> =
    __KMapping(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> is_Mapping(maplets: Iterable<Tuple2<D, R>>) = mk_Relation(maplets).let { it.dom.card == it.card }

fun <D, R> is_Mapping(vararg maplets: Tuple2<D, R>) = mk_Relation(*maplets).let { it.dom.card == it.card }

// TODO - generate for consistency
private class __KMapping<D, R>(override val baseMap: Map<D, R>) : _KMapping<D, R, Mapping<D, R>> {

    override fun construct(base: Map<D, R>) = __KMapping(base)

    override fun get(d: D) = baseMap.get(d) ?: throw PreconditionFailure("$d not in mapping domain")

    override fun contains(element: Tuple2<D, R>) = baseMap[element._1] == element._2

    override fun containsAll(elements: Collection<Tuple2<D, R>>) = forall(elements) { contains(it) }

    override val dom by lazy { mk_Set(baseMap.keys) }

    override val rng by lazy { mk_Set(baseMap.values) }

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

fun <D, R> mk_Mapping1(base: Map<D, R>): Mapping1<D, R> = __KMapping1(base)

fun <D, R> mk_Mapping1(maplets: Iterable<Tuple2<D, R>>): Mapping1<D, R> =
    __KMapping1(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> mk_Mapping1(vararg maplets: Tuple2<D, R>): Mapping1<D, R> =
    __KMapping1(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

// TODO - generate for consistency
private class __KMapping1<D, R>(override val baseMap: Map<D, R>) : Mapping1<D, R>,
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

    override val dom by lazy { mk_Set1(baseMap.keys) }

    override val rng by lazy { mk_Set1(baseMap.values) }

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

fun <I, OD, OR> mapping(
    provider: Iterable<I>,
    filter: (I) -> Boolean,
    selector: (I) -> Tuple2<OD, OR>
) = mk_Mapping(provider.filter(filter).map(selector))

fun <I, OD, OR> mapping(
    provider: Iterable<I>,
    selector: (I) -> Tuple2<OD, OR>
) = mapping(provider, { true }, selector)
