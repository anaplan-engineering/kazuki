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

// TODO - is there any value mapping to pairs?
fun <D, R> Map<D, R>.maplets() = entries // .map { (k, v) -> k to v }

infix fun <D, R> Map<D, R>.munion(other: Map<D, R>): Map<D, R> {
    val keyIntersection = keys intersect other.keys
    if (keyIntersection.isNotEmpty()) {
        throw InvalidMapMergeException("Attempting to merge maps with intersecting domain: $keyIntersection")
    }
    return HashMap(this).apply {
        putAll(other)
    }
}

class InvalidMapMergeException(msg: String) : SpecificationError(msg)

infix fun <D, R> Map<D, R>.domRestrictTo(s: Set<R>) = mk_Map(this - s)

infix fun <D, R> Map<D, R>.drt(s: Set<R>) = domRestrictTo(s)

infix fun <D, R> Map<D, R>.rngRestrictTo(s: Set<R>) = asMap(entries.filter { (_, v) -> v in s })

infix fun <D, R> Map<D, R>.rrt(s: Set<R>) = rngRestrictTo(s)

fun <D, R> mk_Map(base: Map<D, R>): Map<D, R> = __KMap(base)

private fun <D, R> asMap(entries: Collection<Map.Entry<D, R>>): Map<D, R> = mk_Map(entries.map { it.key to it.value })

fun <D, R> mk_Map(entries: Collection<Pair<D, R>>): Map<D, R> = mk_Map(entries.toMap())

fun <D, R> mk_Map(vararg entries: Pair<D, R>): Map<D, R> = mk_Map(entries.toMap())
private class __KMap<D, R>(private val base: Map<D, R>) : Map<D, R> by base {

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

private fun <D, R> asMap1(entries: Collection<Map.Entry<D, R>>): Map<D, R> = mk_Map1(entries.map { it.key to it.value })

fun <D, R> mk_Map1(entries: Collection<Pair<D, R>>): Map1<D, R> = mk_Map1(entries.toMap())

fun <D, R> mk_Map1(vararg entries: Pair<D, R>): Map1<D, R> = mk_Map1(entries.toMap())


private class __KMap1<D, R>(private val base: Map<D, R>) : Map1<D, R>, Map<D, R> by base {

    override val card: nat1 by base::size

    init {
        if (!isValid()) {
            throw InvariantFailure()
        }
    }

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