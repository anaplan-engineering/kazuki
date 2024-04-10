package com.anaplan.engineering.kazuki.toolkit.sequence

import com.anaplan.engineering.kazuki.core.mk_Seq
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSequenceFunctions {

    @Test
    fun countOf() {
        assertEquals(0, SequenceFunctions<Int>().countOf(1, mk_Seq()))
        assertEquals(1, SequenceFunctions<Int>().countOf(1, mk_Seq(1)))
        assertEquals(2, SequenceFunctions<Int>().countOf(1, mk_Seq(1, 2, 1)))
        assertEquals(2, SequenceFunctions<Int>().countOf(1, mk_Seq(1, 1)))
        assertEquals(0, SequenceFunctions<Int>().countOf(1, mk_Seq(2, 2)))
    }

    @Test
    fun permutation() {
        assertEquals(true, SequenceFunctions<Int>().permutation(mk_Seq(), mk_Seq()))
        assertEquals(false, SequenceFunctions<Int>().permutation(mk_Seq(), mk_Seq(1)))
        assertEquals(true, SequenceFunctions<Int>().permutation(mk_Seq(1), mk_Seq(1)))
        assertEquals(false, SequenceFunctions<Int>().permutation(mk_Seq(1), mk_Seq()))
        assertEquals(false, SequenceFunctions<Int>().permutation(mk_Seq(1, 2), mk_Seq(2, 3)))
        assertEquals(true, SequenceFunctions<Int>().permutation(mk_Seq(1, 2), mk_Seq(2, 1)))
        assertEquals(false, SequenceFunctions<Int>().permutation(mk_Seq(1, 1), mk_Seq(1, 1, 1)))
        assertEquals(true, SequenceFunctions<Int>().permutation(mk_Seq(1, 1), mk_Seq(1, 1)))
    }
}