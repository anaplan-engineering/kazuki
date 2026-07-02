package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.InjectiveMapping
import com.anaplan.engineering.kazuki.core.Mapping
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Relation
import com.anaplan.engineering.kazuki.core.Tuple2
import com.anaplan.engineering.kazuki.core.as_InjectiveMapping
import com.anaplan.engineering.kazuki.core.as_Mapping
import com.anaplan.engineering.kazuki.core.as_Relation
import com.anaplan.engineering.kazuki.core.as_Set
import com.anaplan.engineering.kazuki.core.arbitrary
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.core.dunion
import com.anaplan.engineering.kazuki.core.exists
import com.anaplan.engineering.kazuki.core.forall
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.inter
import com.anaplan.engineering.kazuki.core.integer
import com.anaplan.engineering.kazuki.core.iota
import com.anaplan.engineering.kazuki.core.implies
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Mapping
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.core.nat
import com.anaplan.engineering.kazuki.core.nat1
import com.anaplan.engineering.kazuki.core.set
import com.anaplan.engineering.kazuki.core.subset
import com.anaplan.engineering.kazuki.core.toNat
import com.anaplan.engineering.kazuki.core.times
import com.anaplan.engineering.kazuki.core.union
import com.anaplan.engineering.kazuki.toolkit.RelationZ_Module.as_RelationZ
import com.anaplan.engineering.kazuki.toolkit.RelationZ_Module.mk_RelationZ

// Suggested extensions to core Relation based on VDM_Toolkit Relations.vdmsl.
// Core Relation keeps basic set-of-pairs operations; RelationZ adds the Z/Eves relation toolkit.
@Module
interface RelationZ<D, R> : Relation<D, R> {

    @FunctionProvider(RelationZFunctions::class)
    val functions: RelationZFunctions<D, R>
}

class RelationZFunctions<D, R>(private val relation: RelationZ<D, R>) {

    val dom = function(
        command = { -> as_Set(relation.dom) },
        post = { result -> forall(relation) { it._1 in result } }
    )

    val rng = function(
        command = { -> as_Set(relation.rng) },
        post = { result -> forall(relation) { it._2 in result } }
    )

    fun <Z> comp() = function(
        command = { other: Relation<R, Z> ->
            val qDom = relation.dom
            val qRng = relation.rng
            val rRng = other.rng
            val qrRd = qRng union other.dom
            as_RelationZ(as_Relation(
                set(qDom, rRng,
                    filter = { x, z ->
                        exists(qrRd) { y ->
                            mk_(x, y) in relation && mk_(y, z) in other
                        }
                    }
                ) { x, z -> mk_(x, z) }
            ))
        },
        post = { other: Relation<R, Z>, result: RelationZ<D, Z> ->
            result.dom subset relation.dom && result.rng subset other.rng
        }
    )

    fun <Z> circ() = function(
        command = { other: Relation<D, R> ->
            @Suppress("UNCHECKED_CAST")
            val rhs = relation as Relation<R, Z>
            as_RelationZ(other).functions.comp<Z>()(rhs)
        }
    )

    val inv = function(
        command = { ->
            //TODO is this right? Sounds redundant but kazuki RelationExtension does the same
            as_RelationZ(as_Relation(set(relation) { mk_(it._2, it._1) }))
        },
        post = { result: RelationZ<R, D> -> relation.card == result.card }
    )

    val dres = function(
        command = { s: Set<D> ->
            as_RelationZ(as_Relation(set(relation, filter = { (k, _) -> k in s }) { mk_(it._1, it._2) }))
        }
    )

    val rres = function(
        command = { s: Set<R> ->
            as_RelationZ(as_Relation(set(relation, filter = { (_, v) -> v in s }) { mk_(it._1, it._2) }))
        }
    )

    val ndres = function(
        command = { s: Set<D> ->
            as_RelationZ(as_Relation(set(relation, filter = { (k, _) -> k !in s }) { mk_(it._1, it._2) }))
        }
    )

    val nrres = function(
        command = { s: Set<R> ->
            as_RelationZ(as_Relation(set(relation, filter = { (_, v) -> v !in s }) { mk_(it._1, it._2) }))
        }
    )

    val img = function(
        command = { s: Set<D> -> relation.functions.dres(s).functions.rng() },
        post = { s: Set<D>, result: Set<R> ->
            (result.card == 1uL) implies { relation.functions.isMapOn(s) }
        }
    )

    val dagger = function(
        command = { other: Relation<D, R> ->
            as_RelationZ(as_Relation(
                set(relation, filter = { (k, _) -> k !in other.dom }) { mk_(it._1, it._2) }
                    union as_Set(other)
            ))
        }
    )

    val iter = function(
        command = { n: nat ->
            @Suppress("UNCHECKED_CAST")
            homogeneousIter(relation as RelationZ<D, D>, n) as RelationZ<D, R>
        }
    )

    val tclosure = function(
        command = { ->
            @Suppress("UNCHECKED_CAST")
            val hom = relation as RelationZ<D, D>
            val card = hom.card
            if (card == 0uL) {
                mk_RelationZ(emptySet())
            } else {
                as_RelationZ(as_Relation(dunion(set(1uL..card) { n -> as_Set(hom.functions.iter(n)) }))) as RelationZ<D, R>
            }
        },
        post = { result: RelationZ<D, R> -> relation subset result }
    )

    val tclosure2 = function(
        command = { ->
            @Suppress("UNCHECKED_CAST")
            val hom = relation as RelationZ<D, D>
            val rel = as_Set(hom)
            val closed = as_Set(power(rel).filter { q ->
                rel subset q && as_Set(hom.functions.comp<D>()(as_RelationZ(as_Relation(q)))) subset q
            })
            if (closed.isEmpty()) {
                mk_RelationZ(emptySet())
            } else {
                as_RelationZ(as_Relation(dinter(closed))) as RelationZ<D, R>
            }
        }
    )

    val niter = function(
        command = { n: integer ->
            @Suppress("UNCHECKED_CAST")
            val hom = relation as RelationZ<D, D>
            homogeneousIter(hom.functions.inv() as RelationZ<D, D>, (-n).toNat()) as RelationZ<D, R>
        },
        pre = { n -> n < 0 }
    )

    val rtclosure = function(
        command = { ->
            @Suppress("UNCHECKED_CAST")
            val hom = relation as RelationZ<D, D>
            as_RelationZ(as_Relation(
                as_Set(hom.functions.tclosure()) union as_Set(RelationZOps.idRel<D>()(hom))
            )) as RelationZ<D, R>
        },
        post = { result: RelationZ<D, R> -> relation subset result }
    )

    val isMapOn = function(
        command = { s: Set<D> ->
            forall(s) { x ->
                forall(relation.functions.dres(setOf(x)).functions.rng()) { y ->
                    forall(relation.functions.dres(setOf(x)).functions.rng()) { yPrime ->
                        (mk_(x, y) in relation && mk_(x, yPrime) in relation) implies (y == yPrime)
                    }
                }
            }
        }
    )

    val isMap = function(
        command = { -> relation.functions.isMapOn(relation.functions.dom()) }
    )

    val isInmapOn = function(
        command = { s: Set<D> ->
            relation.functions.isMapOn(s) &&
                    relation.functions.inv().functions.isMapOn(relation.functions.inv().functions.dom())
        }
    )

    val isInmap = function(
        command = { -> relation.functions.isInmapOn(relation.functions.dom()) }
    )

    val isTotalOn = function(
        command = { s: Set<D> -> relation.functions.dom() == s }
    )

    val isMapSimple = function(
        command = { -> relation.dom.card == relation.card }
    )

    val isInmapSimple = function(
        command = { -> relation.rng.card == relation.card }
    )

    val isSurjOn = function(
        command = { s: Set<R> -> relation.functions.isMap() && relation.functions.rng() == s }
    )

    val isBijOn = function(
        command = { s: Set<R> -> relation.functions.isInmap() && relation.functions.isSurjOn(s) }
    )

    val asMapOn = function(
        command = { s: Set<D> ->
            as_Mapping(set(relation, filter = { (k, _) -> k in s }) { mk_(it._1, it._2) })
        },
        pre = { s -> relation.functions.isMapOn(s) },
        post = { s: Set<D>, result: Mapping<D, R> ->
            kInter(s, relation.functions.dom()) == result.dom &&
                    relation.functions.img(s) == result.rng
        }
    )

    val asMap = function(
        command = { -> relation.functions.asMapOn(relation.functions.dom()) },
        pre = { -> relation.functions.isMap() },
        post = { result: Mapping<D, R> ->
            relation.functions.dom() == result.dom && relation.functions.rng() == result.rng
        }
    )

    val apply = function(
        command = { x: D ->
            iota(relation.functions.rng()) { y ->
                relation.functions.img(setOf(x)) == setOf(y)
            }
        },
        pre = { x -> relation.functions.isMap() && x in relation.functions.dom() },
        post = { x: D, result: R -> relation.functions.img(setOf(x)) == setOf(result) }
    )
}

object RelationZOps {

    fun <D> id() = function(
        command = { s: Set<D> ->
            as_RelationZ(as_Relation(set(s) { x -> mk_(x, x) }))
        },
        post = { s: Set<D>, result: RelationZ<D, D> -> s.size.toULong() == result.card }
    )

    fun <D> idRel() = function(
        command = { r: Relation<D, D> ->
            id<D>()(unzip(r))
        },
        post = { r: Relation<D, D>, result: RelationZ<D, D> -> unzip(r) subset unzip(result) }
    )

    fun <D, R> makeRelFromSet() = function(
        command = { ls: Set<D>, rs: Set<R> ->
            zip<D, R>()(ls, rs)
        },
        post = { ls: Set<D>, rs: Set<R>, result: RelationZ<D, R> ->
            result.dom subset ls && result.rng subset rs
        }
    )

    fun <D, R> zip() = function(
        command = { ls: Set<D>, rs: Set<R> ->
            as_RelationZ(as_Relation(set(ls, rs) { l, r -> mk_(l, r) }))
        },
        post = { ls: Set<D>, rs: Set<R>, result: RelationZ<D, R> ->
            result.dom subset ls && result.rng subset rs
        }
    )

    fun <D> makeRelTrclFromSet() = function(
        command = { s: Set<D> ->
            val rel = zip<D, D>()(s, s)
            rel.functions.tclosure()
        }
    )

    fun <D, R> makeRelSubset() = function(
        command = { r: Relation<D, R>, n: nat1 ->
            as_RelationZ(as_Relation(makeRelSubset0(as_Set(r), mk_Set(), n)))
        },
        pre = { r, n -> n < r.card },
        post = { r: Relation<D, R>, n: nat1, result: RelationZ<D, R> ->
            result.card == n && as_Set(result) subset as_Set(r)
        }
    )

    fun <D, R> makeRelMap() = function(
        command = { r: Relation<D, R> ->
            makeRelMap0(as_Set(r), mk_Mapping(), injective = false)
        },
        post = { r: Relation<D, R>, result: Mapping<D, R> ->
            mapAsRel<D, R>()(result).let { as_Set(it) subset as_Set(r) }
        }
    )

    fun <D, R> makeRelInmap() = function(
        command = { r: Relation<D, R> ->
            as_InjectiveMapping(makeRelMap0(as_Set(r), mk_Mapping(), injective = true))
        },
        post = { r: Relation<D, R>, result: InjectiveMapping<D, R> ->
            mapAsRel<D, R>()(result).let { as_Set(it) subset as_Set(r) }
        }
    )

    fun <D, R> forceRelAsMap() = function(
        command = { r: Relation<D, R> ->
            forceRelAsMap0(as_Set(r), mk_Mapping())
        },
        post = { r: Relation<D, R>, result: Mapping<D, Set<R>> ->
            if (as_RelationZ(r).functions.isMap()) {
                result.dom == as_RelationZ(r).functions.dom()
            } else {
                val counts = mk_Mapping(*result.dom.map { d -> mk_(d, result[d].card) }.toTypedArray())
                h(counts) == r.card
            }
        }
    )

    fun <D, R> subsetIsMapSubset() = function(
        command = { r: Relation<D, R>, f: Relation<D, R> ->
            val rZ = as_RelationZ(as_Relation(r))
            val fZ = as_RelationZ(as_Relation(f))
            (rZ.functions.isMap() && as_Set(f) subset as_Set(r)) implies fZ.functions.isMap()
        }
    )

    fun <D, R> mapAsRel() = function(
        command = { m: Mapping<D, R> ->
            as_RelationZ(as_Relation(set(m.dom) { x -> mk_(x, m[x]) }))
        },
        post = { m: Mapping<D, R>, result: RelationZ<D, R> ->
            result.dom == m.dom &&
                    result.rng == m.rng &&
                    result.functions.isMap()
        }
    )

    fun <D> unzip(r: Relation<D, D>): Set<D> = as_Set(r.dom union r.rng)
}

fun <T> power(s: Set<T>): Set<Set<T>> {
    val elems = s.toList()
    return as_Set((0 until (1 shl elems.size)).map { mask ->
        as_Set(elems.filterIndexed { i, _ -> mask and (1 shl i) != 0 })
    })
}

fun <T> dinter(sets: Set<Set<T>>): Set<T> {
    val iterator = sets.iterator()
    var result = iterator.next()
    while (iterator.hasNext()) {
        result = result inter iterator.next()
    }
    return result
}

private fun <D> kInter(a: Set<D>, b: Set<D>): Set<D> = as_Set(a.filter { it in b })

private fun <D, R> removePair(r: Set<Tuple2<D, R>>, pair: Tuple2<D, R>): Set<Tuple2<D, R>> =
    as_Set(r.filter { it != pair })

private fun <D, R> makeRelSubset0(
    r: Set<Tuple2<D, R>>,
    s: Set<Tuple2<D, R>>,
    n: nat1
): Set<Tuple2<D, R>> =
    if (r.card > n && n > 0uL) {
        val pair = r.arbitrary()
        makeRelSubset0(removePair(r, pair), s + pair, n - 1uL)
    } else {
        s
    }

private fun <D, R> makeRelMap0(
    r: Set<Tuple2<D, R>>,
    m: Mapping<D, R>,
    injective: Boolean
): Mapping<D, R> =
    if (r.isEmpty()) {
        m
    } else {
        val pair = r.arbitrary()
        val rest = removePair(r, pair)
        val extended = if (!injective || pair._2 !in m.rng) {
            m * pair
        } else {
            m
        }
        makeRelMap0(rest, extended, injective)
    }

private fun <D, R> forceRelAsMap0(
    r: Set<Tuple2<D, R>>,
    m: Mapping<D, Set<R>>
): Mapping<D, Set<R>> =
    if (r.isEmpty()) {
        m
    } else {
        val pair = r.arbitrary()
        val rest = removePair(r, pair)
        val nextValue = if (pair._1 in m.dom) {
            m[pair._1] union mk_Set(pair._2)
        } else {
            mk_Set(pair._2)
        }
        forceRelAsMap0(rest, m * mk_(pair._1, nextValue))
    }

private fun <D> h(s: Mapping<D, nat>): nat =
    if (s.dom.isEmpty()) {
        0uL
    } else {
        val x = s.dom.arbitrary()
        val restDom = as_Set(s.dom.filter { it != x })
        val rest = mk_Mapping(*restDom.map { d -> mk_(d, s[d]) }.toTypedArray())
        s[x] + h(rest)
    }

@Suppress("UNCHECKED_CAST")
private fun <D> homogeneousIter(relation: RelationZ<D, D>, n: nat): RelationZ<D, D> =
    when (n) {
        0uL -> RelationZOps.idRel<D>()(relation)
        1uL -> relation
        else -> relation.functions.comp<D>()(homogeneousIter(relation, n - 1uL))
    }
