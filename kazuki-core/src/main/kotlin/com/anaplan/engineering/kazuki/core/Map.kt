package com.anaplan.engineering.kazuki.core


fun <D, R> Map<D, R>.dom() = mk_Set(keys)

fun <D, R> Map<D, R>.rng() = mk_Set(values)

infix fun <D, R> Map<D, R>.munion(other: Map<D, R>): Map<D, R> {
    val keyIntersection = keys intersect other.keys
    if (keyIntersection.isNotEmpty()) {
        throw InvalidMapMergeException("Attempting to merge maps with intersecting domain: $keyIntersection")
    }
    return HashMap(this).apply {
        putAll(other)
    }
}

class InvalidMapMergeException(msg: String) : RuntimeException(msg)

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