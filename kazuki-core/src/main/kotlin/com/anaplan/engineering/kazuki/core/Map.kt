package com.anaplan.engineering.kazuki.core

interface Map1<D, R> : Map<D, R> {
    val card: nat1

    @Invariant
    fun atLeastOneElement() = card > 0
}

val <D, R> Map<D, R>.card: nat
    get() = size

fun <D, R> Map<D, R>.dom() = keys

fun <D, R> Map<D, R>.rng() = values

fun <D, R> Map<D, R>.maplets() = entries.map { (k, v) -> mk_(k, v) }

infix fun <D, R, M : Map<D, R>> M.domRestrictTo(s: Set<D>) = transform { it.base - s }

infix fun <D, R, M : Map<D, R>> M.drt(s: Set<D>) = domRestrictTo(s)

infix fun <D, R, M : Map<D, R>> M.rngRestrictTo(s: Set<R>) = transform { it.base.filter { (_, v) -> v in s } }

infix fun <D, R, M : Map<D, R>> M.rrt(s: Set<R>) = rngRestrictTo(s)

infix operator fun <D, R, M : Map<D, R>> M.plus(t: Tuple2<D, R>) = transform { it.base + (t._1 to t._2) }

infix fun <D, R, M : Map<D, R>> M.munion(other: Map<D, R>) = transform {
    val keyIntersection = keys intersect other.keys
    if (keyIntersection.isNotEmpty()) {
        throw PreconditionFailure("Attempting to merge maps with intersecting domain: $keyIntersection")
    }
    HashMap(it.base).apply {
        putAll(other)
    }
}

interface KMap<D, R, M : Map<D, R>> : Map<D, R> {
    fun construct(base: Map<D, R>): M

    val base: Map<D, R>
}

private fun <D, R, M : Map<D, R>> M.transform(fn: (KMap<D, R, M>) -> Map<D, R>): M {
    val kMap = this as? KMap<D, R, M> ?: throw PreconditionFailure("Map was implemented outside Kazuki")
    return kMap.construct(fn(kMap))
}

// TODO -- update map Type to use below and KMap!
fun <D, R> mk_Map(base: Map<D, R>): Map<D, R> = __KMap(base)

private fun <D, R> asMap(entries: Collection<Map.Entry<D, R>>): Map<D, R> =
    mk_Map(entries.map { mk_(it.key, it.value) })

fun <D, R> mk_Map(maplets: Iterable<Tuple2<D, R>>): Map<D, R> =
    __KMap(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> mk_Map(vararg maplets: Tuple2<D, R>): Map<D, R> =
    __KMap(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

private class __KMap<D, R>(override val base: Map<D, R>) : Map<D, R> by base, KMap<D, R, Map<D, R>> {

    override fun construct(base: Map<D, R>) = __KMap(base)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Map<*, *>) return false
        return base == other
    }

    override fun hashCode(): Int {
        return base.hashCode()
    }

    override fun toString() = "map$base"
}

fun <D, R> mk_Map1(base: Map<D, R>): Map1<D, R> = __KMap1(base)

private fun <D, R> asMap1(entries: Collection<Map.Entry<D, R>>): Map1<D, R> =
    mk_Map1(entries.map { mk_(it.key, it.value) })

fun <D, R> mk_Map1(maplets: Iterable<Tuple2<D, R>>): Map1<D, R> =
    __KMap1(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

fun <D, R> mk_Map1(vararg maplets: Tuple2<D, R>): Map1<D, R> =
    __KMap1(LinkedHashMap<D, R>().apply {
        maplets.forEach { put(it._1, it._2) }
    })

private class __KMap1<D, R>(override val base: Map<D, R>) : Map1<D, R>, Map<D, R> by base, KMap<D, R, Map<D, R>> {

    override val card: nat1 by base::size

    init {
        if (!isValid()) {
            throw InvariantFailure()
        }
    }

    override fun construct(base: Map<D, R>) = __KMap1(base)

    protected fun isValid(): Boolean = atLeastOneElement()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Map<*, *>) return false
        return base == other
    }

    override fun hashCode(): Int {
        return base.hashCode()
    }

    override fun toString() = "map1$base"
}