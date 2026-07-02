package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Mapping
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Relation
import com.anaplan.engineering.kazuki.core.as_Relation
import com.anaplan.engineering.kazuki.core.as_Set
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.core.dunion
import com.anaplan.engineering.kazuki.core.exists
import com.anaplan.engineering.kazuki.core.forall
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.iota
import com.anaplan.engineering.kazuki.core.implies
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.nat
import com.anaplan.engineering.kazuki.core.set
import com.anaplan.engineering.kazuki.core.subset
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

    fun <Z> dagger() = function(
        command = { other: Relation<D, R> ->
            as_RelationZ(as_Relation(
                set(other, filter = { (k, _) -> k !in relation.dom }) { mk_(it._1, it._2) }
                    union as_Set(relation)
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
            as_RelationZ(as_Relation(set(ls, rs) { l, r -> mk_(l, r) }))
        },
        post = { ls: Set<D>, rs: Set<R>, result: RelationZ<D, R> ->
            result.dom subset ls && result.rng subset rs
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

@Suppress("UNCHECKED_CAST")
private fun <D> homogeneousIter(relation: RelationZ<D, D>, n: nat): RelationZ<D, D> =
    when (n) {
        0uL -> RelationZOps.idRel<D>()(relation)
        1uL -> relation
        else -> relation.functions.comp<D>()(homogeneousIter(relation, n - 1uL))
    }
