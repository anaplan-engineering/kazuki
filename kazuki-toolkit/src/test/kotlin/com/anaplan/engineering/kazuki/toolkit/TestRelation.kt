package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.Tuple2
import com.anaplan.engineering.kazuki.core.as_Relation
import com.anaplan.engineering.kazuki.core.as_Set
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.core.dunion
import com.anaplan.engineering.kazuki.core.exists
import com.anaplan.engineering.kazuki.core.forall
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Mapping
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.core.set
import com.anaplan.engineering.kazuki.core.subset
import com.anaplan.engineering.kazuki.core.union
import com.anaplan.engineering.kazuki.toolkit.RelationZ_Module.as_RelationZ
import com.anaplan.engineering.kazuki.toolkit.RelationZ_Module.mk_RelationZ
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Tests ported from VDM_Toolkit Relations.vdmsl traces (lines 477-939).
// Uses core Relation via the RelationZ extension module for toolkit operations.
class TestRelation {

    companion object {
        const val MAX_TEST = 4

        val TYPE_RANGE1 = as_Set((1..MAX_TEST / 2 + 1).toSet())
        val TYPE_RANGE2 = as_Set((MAX_TEST / 2..MAX_TEST).toSet())
        val TYPE_RANGE = TYPE_RANGE1 union TYPE_RANGE2

        fun fR1() = RelationZOps.makeRelFromSet<Int, Int>()(TYPE_RANGE1, TYPE_RANGE1)
        fun fR2() = RelationZOps.makeRelFromSet<Int, Int>()(TYPE_RANGE2, TYPE_RANGE2)
        fun fR10() = RelationZOps.mapAsRel<Int, Int>()(
            mk_Mapping(*TYPE_RANGE1.map { mk_(it, 0) }.toTypedArray())
        )
        fun fR2i() = RelationZOps.mapAsRel<Int, Int>()(
            mk_Mapping(*TYPE_RANGE2.map { mk_(it, it + 1) }.toTypedArray())
        )

        val X1 = TYPE_RANGE1 - mk_Set(1)
        val X1Prime = X1 - mk_Set(2)
        val Y1 = TYPE_RANGE2 - mk_Set(MAX_TEST / 2)
        val R1 = fR1()
        val S1 = fR2()
        val R1Prime = R1.functions.ndres(mk_Set(1))
        val S1Prime = S1.functions.nrres(mk_Set(1))
        val F1 = fR10()
        val F2 = fR2i()

        fun relUnion(vararg rels: RelationZ<Int, Int>): RelationZ<Int, Int> =
            as_RelationZ(as_Relation(rels.fold(mk_Set<Tuple2<Int, Int>>()) { acc, r ->
                kUnion(acc, as_Set(r))
            }))
    }

    @Test
    fun tIdCup() {
        for (max in 2..10) {
            val tr1 = as_Set((1..max / 2 + 1).toSet())
            val tr2 = as_Set((max / 2..max).toSet())
            assertEquals(
                RelationZOps.id<Int>()(tr1 union tr2),
                as_RelationZ(
                    as_Relation(
                        as_Set(RelationZOps.id<Int>()(tr1)) union as_Set(RelationZOps.id<Int>()(tr2))
                    )
                )
            )
        }
    }

    @Test
    fun tIdSubset() {
        for (m in 2..MAX_TEST) {
            val tr1 = as_Set((1..m / 2 + 1).toSet())
            val tr2 = as_Set((m / 2..m).toSet())
            val id1 = as_Set(RelationZOps.id<Int>()(tr1))
            val id2 = as_Set(RelationZOps.id<Int>()(tr2))
            assertEquals(id1 subset id2, tr1 subset tr2)
        }
    }

    @Test
    fun tDomRngSingleton() {
        for (x in 1..MAX_TEST) {
            for (y in 1..MAX_TEST) {
                val rel = mk_RelationZ(mk_(x, y))
                assertEquals(mk_Set(x), rel.functions.dom())
                assertEquals(mk_Set(y), rel.functions.rng())
            }
        }
    }

    @Test
    fun tDomRngUnion() {
        for (m in 1..MAX_TEST) {
            val tr1 = as_Set((1..m / 2 + 1).toSet())
            val tr2 = as_Set((m / 2..m).toSet())
            val r1 = RelationZOps.makeRelFromSet<Int, Int>()(tr1, tr2)
            val s1 = RelationZOps.makeRelFromSet<Int, Int>()(tr2, tr1)
            val u = relUnion(r1, s1)
            assertEquals(u.functions.dom(), r1.functions.dom() union s1.functions.dom())
            assertEquals(u.functions.rng(), r1.functions.rng() union s1.functions.rng())
        }
    }

    @Test
    fun tDomRngIn() {
        assertTrue(forall(R1.functions.dom()) { x ->
            exists(TYPE_RANGE) { y -> mk_(x, y) in R1 }
        })
        assertTrue(forall(R1) { it._1 in R1.functions.dom() })
        assertTrue(forall(R1.functions.rng()) { y ->
            exists(TYPE_RANGE) { x -> mk_(x, y) in R1 }
        })
        assertTrue(forall(R1) { it._2 in R1.functions.rng() })
    }

    @Test
    fun tDomRngSubset() {
        assertTrue(forall(power(as_Set(R1))) { t ->
            val sub = as_RelationZ(as_Relation(t))
            sub.functions.dom() subset R1.functions.dom() &&
                    sub.functions.rng() subset R1.functions.rng()
        })
    }

    @Test
    fun tDomRngId() {
        val id = RelationZOps.id<Int>()(TYPE_RANGE)
        assertEquals(TYPE_RANGE, id.functions.dom())
        assertEquals(TYPE_RANGE, id.functions.rng())
    }

    @Test
    fun tDomRngCompPair() {
        assertTrue(forall(R1.functions.dom(), S1.functions.rng()) { x, z ->
            val inComp = mk_(x, z) in R1.functions.comp<Int>()(S1)
            val existsBridge = exists(kInter(R1.functions.rng(), S1.functions.dom())) { y ->
                mk_(x, y) in R1 && mk_(y, z) in S1
            }
            inComp == existsBridge
        })
    }

    @Test
    fun tCompAssoc() {
        assertEquals(
            R1.functions.comp<Int>()(S1.functions.comp<Int>()(F1)),
            R1.functions.comp<Int>()(S1).functions.comp<Int>()(F1)
        )
    }

    @Test
    fun tCompDomRngEasy() {
        if (R1.functions.rng() subset S1.functions.dom()) {
            assertEquals(R1.functions.dom(), R1.functions.comp<Int>()(S1).functions.dom())
        }
        if (S1.functions.dom() subset R1.functions.rng()) {
            assertEquals(S1.functions.rng(), R1.functions.comp<Int>()(S1).functions.rng())
        }
    }

    @Test
    fun tCompMap() {
        if (F1.functions.isMap() && F2.functions.isMap()) {
            assertTrue(F1.functions.comp<Int>()(F2).functions.isMap())
        }
    }

    @Test
    fun tCompApply() {
        if (F1.functions.isMap() && F2.functions.isMap()) {
            assertTrue(forall(F1.functions.dom()) { x ->
                val f1x = F1.functions.apply(x)
                if (f1x in F2.functions.dom()) {
                    val f1CompF2 = F1.functions.comp<Int>()(F2)
                    val f2CircF1 = F2.functions.circ<Int>()(F1)
                    f1CompF2.functions.isMap() &&
                            f2CircF1.functions.isMap() &&
                            f1CompF2.functions.apply(x) == f2CircF1.functions.apply(x) &&
                            f1CompF2.functions.apply(x) == F2.functions.apply(f1x)
                } else {
                    true
                }
            })
        }
    }

    @Test
    fun tCompMonotone() {
        assertTrue(
            (as_Set(R1Prime) subset as_Set(R1) &&
                    as_Set(S1Prime) subset as_Set(S1)) implies
                    (as_Set(R1Prime.functions.comp<Int>()(S1Prime)) subset
                            as_Set(R1.functions.comp<Int>()(S1)))
        )
    }

    @Test
    fun tResDomRng() {
        assertEquals(kInter(X1, R1.functions.dom()), R1.functions.dres(X1).functions.dom())
        assertEquals(kInter(R1.functions.rng(), Y1), R1.functions.rres(Y1).functions.rng())
    }

    @Test
    fun tResSubset() {
        assertTrue(as_Set(R1.functions.dres(X1)) subset as_Set(R1))
        assertTrue(as_Set(R1.functions.rres(Y1)) subset as_Set(R1))
    }

    @Test
    fun tIdCompRes() {
        assertEquals(
            RelationZOps.id<Int>()(X1).functions.comp<Int>()(R1),
            R1.functions.dres(X1)
        )
        assertEquals(
            R1.functions.comp<Int>()(RelationZOps.id<Int>()(X1)),
            R1.functions.rres(X1)
        )
    }

    @Test
    fun tIdResId() {
        assertEquals(
            RelationZOps.id<Int>()(Y1).functions.dres(X1),
            RelationZOps.id<Int>()(kInter(X1, Y1))
        )
        assertEquals(
            RelationZOps.id<Int>()(X1).functions.rres(Y1),
            RelationZOps.id<Int>()(kInter(X1, Y1))
        )
    }

    @Test
    fun tIdNresId() {
        assertEquals(
            RelationZOps.id<Int>()(Y1).functions.ndres(X1),
            RelationZOps.id<Int>()(Y1 - X1)
        )
        assertEquals(
            RelationZOps.id<Int>()(X1).functions.nrres(Y1),
            RelationZOps.id<Int>()(X1 - Y1)
        )
    }

    @Test
    fun tResAccumulates() {
        assertEquals(
            R1.functions.dres(Y1).functions.dres(X1),
            R1.functions.dres(kInter(X1, Y1))
        )
        assertEquals(
            R1.functions.rres(X1).functions.rres(Y1),
            R1.functions.rres(kInter(X1, Y1))
        )
    }

    @Test
    fun tResElimination() {
        if (R1.functions.dom() subset X1) {
            assertEquals(R1, R1.functions.dres(X1))
        }
        if (R1.functions.rng() subset Y1) {
            assertEquals(R1, R1.functions.rres(Y1))
        }
    }

    @Test
    fun tResUnion() {
        val u = relUnion(R1, S1)
        assertEquals(
            u.functions.dres(X1),
            relUnion(R1.functions.dres(X1), S1.functions.dres(X1))
        )
        assertEquals(
            u.functions.rres(Y1),
            relUnion(R1.functions.rres(Y1), S1.functions.rres(Y1))
        )
    }

    @Test
    fun tResApply() {
        if (F1.functions.isMap()) {
            assertTrue(forall(kInter(X1, F1.functions.dom())) { x ->
                F1.functions.dres(X1).functions.apply(x) == F1.functions.apply(x)
            })
            assertTrue(forall(F1.functions.dom()) { x ->
                (F1.functions.apply(x) in Y1) implies {
                    F1.functions.rres(Y1).functions.apply(x) == F1.functions.apply(x)
                }
            })
        }
    }

    @Test
    fun tNresDomRng() {
        assertEquals(R1.functions.dom() - X1, R1.functions.ndres(X1).functions.dom())
        assertEquals(R1.functions.rng() - Y1, R1.functions.nrres(Y1).functions.rng())
    }

    @Test
    fun tNresSubset() {
        assertTrue(as_Set(R1.functions.ndres(X1)) subset as_Set(R1))
        assertTrue(as_Set(R1.functions.nrres(Y1)) subset as_Set(R1))
    }

    @Test
    fun tNresAccumulates() {
        assertEquals(
            R1.functions.ndres(Y1).functions.ndres(X1),
            R1.functions.ndres(kUnion(X1, Y1))
        )
        assertEquals(
            R1.functions.nrres(X1).functions.nrres(Y1),
            R1.functions.nrres(kUnion(X1, Y1))
        )
    }

    @Test
    fun tNresElimination() {
        if (R1.functions.dom() subset X1) {
            assertEquals(mk_RelationZ<Int, Int>(), R1.functions.ndres(X1))
        }
        if (R1.functions.rng() subset Y1) {
            assertEquals(mk_RelationZ<Int, Int>(), R1.functions.nrres(Y1))
        }
    }

    @Test
    fun tNresUnion() {
        val u = relUnion(R1, S1)
        assertEquals(
            u.functions.ndres(X1),
            relUnion(R1.functions.ndres(X1), S1.functions.ndres(X1))
        )
        assertEquals(
            u.functions.nrres(Y1),
            relUnion(R1.functions.nrres(Y1), S1.functions.nrres(Y1))
        )
    }

    @Test
    fun tNresApply() {
        if (F1.functions.isMap()) {
            assertTrue(forall(F1.functions.dom() - X1) { x ->
                F1.functions.ndres(X1).functions.apply(x) == F1.functions.apply(x)
            })
            assertTrue(forall(F1.functions.dom()) { x ->
                (F1.functions.apply(x) !in Y1) implies {
                    F1.functions.nrres(Y1).functions.apply(x) == F1.functions.apply(x)
                }
            })
        }
    }

    @Test
    fun tRelInvSubset() {
        assertTrue(
            (as_Set(R1Prime.functions.inv()) subset as_Set(R1.functions.inv())) implies
                    (as_Set(R1Prime) subset as_Set(R1))
        )
    }

    @Test
    fun tRelInvSetops() {
        val u = relUnion(R1, S1)
        val i = as_RelationZ(as_Relation(kInter(as_Set(R1), as_Set(S1))))
        val d = as_RelationZ(as_Relation(as_Set(R1) - as_Set(S1)))
        assertEquals(
            u.functions.inv(),
            relUnion(R1.functions.inv(), S1.functions.inv())
        )
        assertEquals(
            i.functions.inv(),
            as_RelationZ(as_Relation(kInter(as_Set(R1.functions.inv()), as_Set(S1.functions.inv()))))
        )
        assertEquals(
            d.functions.inv(),
            as_RelationZ(as_Relation(as_Set(R1.functions.inv()) - as_Set(S1.functions.inv())))
        )
    }

    @Test
    fun tRelDoubleInv() {
        assertEquals(R1, R1.functions.inv().functions.inv())
    }

    @Test
    fun tRelInvComp() {
        assertEquals(
            R1.functions.comp<Int>()(S1).functions.inv(),
            S1.functions.inv().functions.comp<Int>()(R1.functions.inv())
        )
    }

    @Test
    fun tRelInvDomRng() {
        assertEquals(R1.functions.rng(), R1.functions.inv().functions.dom())
        assertEquals(R1.functions.dom(), R1.functions.inv().functions.rng())
    }

    @Test
    fun tRelInvRes() {
        assertEquals(R1.functions.dres(X1).functions.inv(), R1.functions.inv().functions.rres(X1))
        assertEquals(R1.functions.ndres(X1).functions.inv(), R1.functions.inv().functions.nrres(X1))
        assertEquals(R1.functions.rres(Y1).functions.inv(), R1.functions.inv().functions.dres(Y1))
        assertEquals(R1.functions.nrres(Y1).functions.inv(), R1.functions.inv().functions.ndres(Y1))
    }

    @Test
    fun tRelInvApply() {
        if (F2.functions.isInmap()) {
            assertTrue(forall(F2.functions.dom()) { x ->
                F2.functions.inv().functions.apply(F2.functions.apply(x)) == x
            })
        }
    }

    @Test
    fun tRelImgMapSingleton() {
        assertEquals(
            F1.functions.isMap(),
            forall(F1.functions.dom()) { x -> F1.functions.img(mk_Set(x)).card == 1uL }
        )
    }

    @Test
    fun tRelImgSubset() {
        assertTrue(R1.functions.img(X1) subset R1.functions.rng())
    }

    @Test
    fun tRelImgMonotonic() {
        assertTrue(
            (X1Prime subset X1 && as_Set(R1Prime) subset as_Set(R1)) implies
                    (as_Set(R1Prime.functions.img(X1Prime)) subset as_Set(R1.functions.img(X1)))
        )
        assertTrue(
            (X1Prime subset X1) implies
                    (as_Set(R1.functions.img(X1Prime)) subset as_Set(R1.functions.img(X1)))
        )
        assertTrue(
            (as_Set(R1Prime) subset as_Set(R1)) implies
                    (as_Set(R1Prime.functions.img(X1)) subset as_Set(R1.functions.img(X1)))
        )
    }

    @Test
    fun tRelImgUnion() {
        assertEquals(
            R1.functions.img(kUnion(X1, Y1)),
            kUnion(R1.functions.img(X1), R1.functions.img(Y1))
        )
    }

    @Test
    fun tRelImgFull() {
        if (R1.functions.dom() subset X1) {
            assertEquals(R1.functions.rng(), R1.functions.img(X1))
        }
    }

    @Test
    fun tRelImgIdMap() {
        val id = RelationZOps.id<Int>()(TYPE_RANGE)
        assertTrue(forall(TYPE_RANGE) { x ->
            id.functions.img(mk_Set(x)) == mk_Set(x)
        })
    }

    @Test
    fun tRelIgmUnionFcn() {
        val u = relUnion(F1, F2)
        assertEquals(
            u.functions.isMap(),
            F1.functions.isMap() && F2.functions.isMap() &&
                    forall(kInter(F1.functions.dom(), F2.functions.dom())) { x ->
                        F1.functions.img(mk_Set(x)) == F2.functions.img(mk_Set(x))
                    }
        )
    }

    @Test
    fun tRelImgRes() {
        assertEquals(
            R1.functions.dres(X1).functions.img(Y1),
            R1.functions.img(kInter(X1, Y1))
        )
        assertEquals(
            R1.functions.rres(X1).functions.img(Y1),
            kInter(R1.functions.img(Y1), X1)
        )
    }

    @Test
    fun tRelImgApply() {
        if (F1.functions.isMap()) {
            assertTrue(forall(X1) { x ->
                (x in F1.functions.dom()) implies (F1.functions.apply(x) in F1.functions.img(X1))
            })
        }
    }

    @Test
    fun tRtrclIteration() {
        val rt = R1.functions.rtclosure()
        val expected = as_RelationZ(
            as_Relation(dunion(set(0uL..R1.card) { n -> as_Set(R1.functions.iter(n)) }))
        )
        assertEquals(expected, rt)
    }

    @Test
    fun tTrclSubset() {
        assertTrue(
            (as_Set(R1Prime) subset as_Set(R1) &&
                    as_Set(R1.functions.comp<Int>()(R1)) subset as_Set(R1)) implies
                    (as_Set(R1Prime.functions.tclosure()) subset as_Set(R1))
        )
        assertTrue(
            (as_Set(R1Prime) subset as_Set(R1) &&
                    as_Set(R1Prime.functions.comp<Int>()(R1)) subset as_Set(R1)) implies
                    (as_Set(R1Prime.functions.tclosure()) subset as_Set(R1))
        )
        assertTrue(
            (as_Set(R1Prime) subset as_Set(R1) &&
                    as_Set(R1.functions.comp<Int>()(R1Prime)) subset as_Set(R1)) implies
                    (as_Set(R1Prime.functions.tclosure()) subset as_Set(R1))
        )
        assertTrue(as_Set(R1) subset as_Set(R1.functions.tclosure()))
    }

    @Test
    fun tTrclTransitive() {
        assertTrue(
            as_Set(R1.functions.tclosure().functions.comp<Int>()(R1.functions.tclosure())) subset
                    as_Set(R1.functions.tclosure())
        )
        if (as_Set(R1.functions.comp<Int>()(R1)) subset as_Set(R1)) {
            assertEquals(R1, R1.functions.tclosure())
        }
    }

    @Test
    fun tRelIsInmap() {
        assertTrue(F2.functions.isInmap())
    }

    @Test
    fun tRelDagger() {
        assertEquals(
            R1.functions.dagger(S1),
            as_RelationZ(as_Relation(
                kUnion(
                    as_Set(R1.functions.ndres(S1.functions.dom())),
                    as_Set(S1)
                )
            ))
        )
    }

    @Test
    fun tRelNiter() {
        assertEquals(R1.functions.niter(-1), R1.functions.inv())
        assertEquals(R1.functions.niter(-2), R1.functions.iter(2uL).functions.inv())
    }

    @Test
    fun tRelTclosure2() {
        val tc2 = R1.functions.tclosure2()
        assertTrue(as_Set(R1) subset as_Set(tc2))
        assertTrue(
            as_Set(tc2.functions.comp<Int>()(tc2)) subset as_Set(tc2)
        )
        assertTrue(as_Set(tc2) subset as_Set(R1.functions.tclosure()))
    }

    @Test
    fun tRelIsTotalOn() {
        assertTrue(R1.functions.isTotalOn(R1.functions.dom()))
        assertTrue(!R1.functions.isTotalOn(TYPE_RANGE))
    }

    @Test
    fun tRelIsMapSimple() {
        assertTrue(F1.functions.isMapSimple())
        assertTrue(!R1.functions.isMapSimple())
    }

    @Test
    fun tRelIsInmapSimple() {
        assertTrue(F2.functions.isInmapSimple())
        assertTrue(!R1.functions.isInmapSimple())
    }

    @Test
    fun tRelIsSurjOn() {
        assertTrue(F1.functions.isSurjOn(F1.functions.rng()))
        assertTrue(!F1.functions.isSurjOn(TYPE_RANGE))
    }

    @Test
    fun tRelIsBijOn() {
        assertTrue(F2.functions.isBijOn(F2.functions.rng()))
    }

    @Test
    fun tRelSubsetIsMapSubset() {
        assertTrue(RelationZOps.subsetIsMapSubset<Int, Int>()(F1, F1))
        assertTrue(RelationZOps.subsetIsMapSubset<Int, Int>()(R1, R1Prime))
        assertTrue(!R1Prime.functions.isMap())
    }

    @Test
    fun tRelAsMap() {
        val m = F1.functions.asMap()
        assertEquals(F1.functions.dom(), m.dom)
        assertEquals(F1.functions.rng(), m.rng)
        assertTrue(forall(F1.functions.dom()) { x ->
            F1.functions.apply(x) == m[x]
        })
    }

    @Test
    fun tRelAsMapOn() {
        val s = F1.functions.dom()
        val m = F1.functions.asMapOn(s)
        assertEquals(kInter(s, F1.functions.dom()), m.dom)
        assertEquals(F1.functions.img(s), m.rng)
    }

    @Test
    fun tZip() {
        assertEquals(
            RelationZOps.makeRelFromSet<Int, Int>()(TYPE_RANGE1, TYPE_RANGE2),
            RelationZOps.zip<Int, Int>()(TYPE_RANGE1, TYPE_RANGE2)
        )
    }

    @Test
    fun tMakeRelTrclFromSet() {
        val trcl = RelationZOps.makeRelTrclFromSet<Int>()(TYPE_RANGE1)
        assertTrue(as_Set(trcl) subset as_Set(trcl.functions.tclosure()))
        assertTrue(forall(TYPE_RANGE1) { x ->
            mk_(x, x) in trcl.functions.rtclosure()
        })
    }

    @Test
    fun tMakeRelSubset() {
        val n = R1.card - 2uL
        val sub = RelationZOps.makeRelSubset<Int, Int>()(R1, n)
        assertEquals(n, sub.card)
        assertTrue(as_Set(sub) subset as_Set(R1))
    }

    @Test
    fun tMakeRelMap() {
        val m = RelationZOps.makeRelMap<Int, Int>()(R1)
        assertTrue(as_Set(RelationZOps.mapAsRel<Int, Int>()(m)) subset as_Set(R1))
    }

    @Test
    fun tMakeRelInmap() {
        val m = RelationZOps.makeRelInmap<Int, Int>()(R1)
        assertTrue(as_Set(RelationZOps.mapAsRel<Int, Int>()(m)) subset as_Set(R1))
        assertTrue(m.dom.card == m.rng.card)
    }

    @Test
    fun tForceRelAsMap() {
        val m = RelationZOps.forceRelAsMap<Int, Int>()(R1)
        assertEquals(R1.functions.dom(), m.dom)
        assertTrue(forall(m.dom) { x ->
            m[x] subset R1.functions.img(mk_Set(x))
        })
        val totalPairs = m.dom.fold(0uL) { acc, x -> acc + m[x].card }
        assertEquals(R1.card, totalPairs)
    }
}

private infix fun Boolean.implies(other: Boolean) = if (this) other else true

private fun <T> kInter(a: Set<T>, b: Set<T>): Set<T> = as_Set(a.filter { it in b })

private fun <T> kUnion(a: Set<T>, b: Set<T>): Set<T> = as_Set(a.toList() + b.toList())

private infix fun Boolean.implies(other: () -> Boolean) = if (this) other() else true
