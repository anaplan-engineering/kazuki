package com.anaplan.engineering.kazuki.toolkit.sequence

import com.anaplan.engineering.kazuki.core.Tuple2
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.toolkit.Ord
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSequenceOrd {

    @Test
    fun ascending_natual() {
        assertEquals(true, SequenceOrd.natural<Int>().ascending(mk_Seq()))
        assertEquals(true, SequenceOrd.natural<Int>().ascending(mk_Seq(1, 12, 18)))
        assertEquals(false, SequenceOrd.natural<Int>().ascending(mk_Seq(1, 22, 12, 18)))

        assertEquals(true, SequenceOrd.natural<String>().ascending(mk_Seq()))
        assertEquals(true, SequenceOrd.natural<String>().ascending(mk_Seq("a", "b", "z")))
        assertEquals(false, SequenceOrd.natural<String>().ascending(mk_Seq("a", "A")))
    }

    @Test
    fun descending_natual() {
        assertEquals(true, SequenceOrd.natural<Int>().descending(mk_Seq()))
        assertEquals(false, SequenceOrd.natural<Int>().descending(mk_Seq(1, 12, 18)))
        assertEquals(true, SequenceOrd.natural<Int>().descending(mk_Seq(122, 21, 18)))

        assertEquals(true, SequenceOrd.natural<String>().descending(mk_Seq()))
        assertEquals(false, SequenceOrd.natural<String>().descending(mk_Seq("a", "b", "z")))
        assertEquals(true, SequenceOrd.natural<String>().descending(mk_Seq("a", "A")))
    }

    @Test
    fun insert_natual() {
        assertEquals(mk_Seq(1), SequenceOrd.natural<Int>().insert(1, mk_Seq()))
        assertEquals(mk_Seq(1, 2, 3), SequenceOrd.natural<Int>().insert(1, mk_Seq(2, 3)))
        assertEquals(mk_Seq(1, 2, 3), SequenceOrd.natural<Int>().insert(2, mk_Seq(1, 3)))
        assertEquals(mk_Seq(1, 2, 3), SequenceOrd.natural<Int>().insert(3, mk_Seq(1, 2)))
    }

    @Test
    fun ascending_byFunction() {
        assertEquals(true, SequenceOrd.byFunction(TupleComparator).ascending(mk_Seq()))
        assertEquals(true, SequenceOrd.byFunction(TupleComparator).ascending(
            mk_Seq(mk_("a", 1), mk_("c", 12), mk_("a", 18)))
        )
        assertEquals(false, SequenceOrd.byFunction(TupleComparator).ascending(
            mk_Seq(mk_("a", 1), mk_("a", 22), mk_("c", 12), mk_("a", 18)))
        )
    }

    @Test
    fun descending_byFunction() {
        assertEquals(true, SequenceOrd.byFunction(TupleComparator).descending(mk_Seq()))
        assertEquals(false, SequenceOrd.byFunction(TupleComparator).descending(
            mk_Seq(mk_("a", 1), mk_("c", 12), mk_("a", 18)))
        )
        assertEquals(true, SequenceOrd.byFunction(TupleComparator).descending(
            mk_Seq(mk_("a", 122), mk_("a", 22), mk_("c", 12), mk_("a", 8)))
        )
    }

    companion object {
        private val TupleComparator = function(
            command = { t1: Tuple2<String, Int>, t2: Tuple2<String, Int> ->
                Ord.natural<Int>().compare(t1._2, t2._2)
            }
        )
    }
}