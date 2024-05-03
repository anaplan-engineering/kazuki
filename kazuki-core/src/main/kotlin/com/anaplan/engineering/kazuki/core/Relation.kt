package com.anaplan.engineering.kazuki.core

import com.anaplan.engineering.kazuki.core.internal.__KRelation
import com.anaplan.engineering.kazuki.core.internal.transformRelation

interface Relation<D, R> : Set<Tuple2<D, R>> {

    val dom: Set<D>

    val rng: Set<R>

}

fun <D, R> mk_Relation(vararg elems: Tuple2<D, R>): Relation<D, R> = __KRelation(elems.toSet())

fun <D, R> mk_Relation(elems: Iterable<Tuple2<D, R>>): Relation<D, R> = __KRelation(elems.toSet())

infix operator fun <D, R, T : Relation<D, R>> T.plus(t: Tuple2<D, R>) = transformRelation { it.baseSet + mk_(t._1, t._2) }

infix operator fun <D, R, T : Relation<D, R>> T.plus(m: Relation<D, R>) = transformRelation { it.baseSet + m }

infix fun <D, R, T : Relation<D, R>> T.domRestrictTo(s: Set<D>) = transformRelation { it.baseSet.filter { (k, _) -> k in s } }

infix fun <D, R, T : Relation<D, R>> T.drt(s: Set<D>) = domRestrictTo(s)

infix fun <D, R, T : Relation<D, R>> T.rngRestrictTo(s: Set<R>) = transformRelation { it.baseSet.filter { (_, v) -> v in s } }

infix fun <D, R, T : Relation<D, R>> T.rrt(s: Set<R>) = rngRestrictTo(s)

infix fun <D, R, T : Relation<D, R>> T.domSubtract(s: Set<D>) = transformRelation { it.baseSet.filter { (k, _) -> k !in s } }

infix fun <D, R, T : Relation<D, R>> T.dsub(s: Set<D>) = domSubtract(s)

infix fun <D, R, T : Relation<D, R>> T.rngSubtract(s: Set<R>) = transformRelation { it.baseSet.filter { (_, v) -> v !in s } }

infix fun <D, R, T : Relation<D, R>> T.rsub(s: Set<R>) = rngSubtract(s)


