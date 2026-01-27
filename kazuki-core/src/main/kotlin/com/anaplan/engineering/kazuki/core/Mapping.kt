package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal.*

interface Mapping<D, out R> : Relation<D, R> {

    operator fun get(d: D): R

}

interface InjectiveMapping<D, R> : Mapping<D, R> {
    val inverse: Mapping<R, D>

    @Invariant
    fun noDuplicatesInRange() = dom.card == rng.card
}

interface InjectiveMapping1<D, R> : InjectiveMapping<D, R>, Mapping1<D, R>

interface Mapping1<D, out R> : Mapping<D, R>, Set1<Tuple2<D, R>> {
    val card: nat1

    override val dom: Set1<D>

    override val rng: Set1<R>

}

fun <D, R> as_Mapping(maplets: Iterable<Tuple2<D, R>>): Mapping<D, R> =
    __KMapping(createBaseMap(maplets))

fun <D, R> as_Mapping(maplets: Array<Tuple2<D, R>>): Mapping<D, R> = as_Mapping(maplets.toList())

fun <D, R> mk_Mapping(vararg maplets: Tuple2<D, R>): Mapping<D, R> = as_Mapping(maplets.toList())

fun <D, R> as_InjectiveMapping(maplets: Iterable<Tuple2<D, R>>): InjectiveMapping<D, R> {
    val baseMap = createBaseMap(maplets)
    pre("Injective map may not have duplicates in range") { baseMap.keys.size == baseMap.values.toSet().size }
    return __KInjectiveMapping(baseMap)
}

fun <D, R> as_InjectiveMapping(maplets: Array<Tuple2<D, R>>): InjectiveMapping<D, R> =
    as_InjectiveMapping(maplets.toList())

fun <D, R> mk_InjectiveMapping(vararg maplets: Tuple2<D, R>): InjectiveMapping<D, R> =
    as_InjectiveMapping(maplets.toList())

fun <D, R> as_InjectiveMapping1(maplets: Iterable<Tuple2<D, R>>): InjectiveMapping1<D, R> {
    val baseMap = createBaseMap(maplets)
    pre("Cannot create empty InjectiveMapping1") { baseMap.isNotEmpty() }
    pre("Injective map may not have duplicates in range") { baseMap.keys.size == baseMap.values.toSet().size }
    return __KInjectiveMapping1(baseMap)
}

fun <D, R> as_InjectiveMapping1(maplets: Array<Tuple2<D, R>>): InjectiveMapping1<D, R> =
    as_InjectiveMapping1(maplets.toList())

fun <D, R> mk_InjectiveMapping1(vararg maplets: Tuple2<D, R>): InjectiveMapping1<D, R> =
    as_InjectiveMapping1(maplets.toList())

fun <D, R> is_Mapping(maplets: Iterable<Tuple2<D, R>>) = as_Relation(maplets).let { it.dom.card == it.card }

fun <D, R> is_Mapping(vararg maplets: Tuple2<D, R>) = mk_Relation(*maplets).let { it.dom.card == it.card }

fun <D, R> as_Mapping1(maplets: Iterable<Tuple2<D, R>>): Mapping1<D, R> {
    val baseMap = createBaseMap(maplets)
    pre("Cannot create empty Mapping1") { baseMap.isNotEmpty() }
    return __KMapping1(baseMap)
}

fun <D, R> mk_Mapping1(vararg maplets: Tuple2<D, R>) = as_Mapping1(maplets.toList())

private fun <D, R> createBaseMap(maplets: Iterable<Tuple2<D, R>>): LinkedHashMap<D, R> =
    LinkedHashMap<D, R>().apply {
        maplets.forEach { (d, r) ->
            pre("${d.prettyOrDefault()} cannot map to ${get(d).prettyOrDefault()} and ${r.prettyOrDefault()}") {
                containsKey(d) implies { get(d) == r }
            }
            put(d, r)
        }
    }

infix fun <D, R, M : Mapping<D, R>> M.munion(r: Relation<D, R>) = this + r

infix operator fun <D, R, M : Mapping<D, R>> M.times(t: Tuple2<D, R>) = transformMapping {
    LinkedHashMap<D, R>().apply {
        put(t._1, t._2)
        it.forEach {
            if (!containsKey(it._1)) {
                put(it._1, it._2)
            }
        }
    }
}

infix operator fun <D, R, M : Mapping<D, R>> M.times(r: Relation<D, R>) = transformMapping {
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

fun <D, R> dmerge(mappings: Set<Mapping<D, R>>) = as_Mapping(mappings.flatten())

// TODO - generate for consistency

