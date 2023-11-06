package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.syntax.examples.ExplicitSequence_Module.as_ExplicitSequence
import com.anaplan.engineering.kazuki.syntax.examples.ExplicitSequence_Module.is_ExplicitSequence
import com.anaplan.engineering.kazuki.syntax.examples.ExplicitSequence_Module.mk_ExplicitSequence
import kotlin.test.Test
import kotlin.test.assertEquals

class TestExplicitSequence {

    @Test
    fun emptyAxisValid() {
        assertEquals(true, is_ExplicitSequence(mk_Seq()))
    }

    @Test
    fun noDuplicatesValid() {
        assertEquals(true, is_ExplicitSequence(mk_Seq(1, 2)))
    }

    @Test
    fun duplicateInvalid() {
        assertEquals(false, is_ExplicitSequence(mk_Seq(1, 1)))
    }

    @Test
    fun get() {
        assertEquals(2, mk_ExplicitSequence(3, 2, 1)[2])
    }

    @Test
    fun equals_sameObject() {
        val seq = mk_ExplicitSequence(3, 2, 1)
        assertEquals(true, seq == seq)
    }

    @Test
    fun equals_explicitlyTyped() {
        assertEquals(true, mk_ExplicitSequence(3, 2, 1) == mk_ExplicitSequence(3, 2, 1))
    }

    @Test
    fun equals_implicitlyTyped() {
        assertEquals(true, mk_ExplicitSequence(3, 2, 1) == mk_Seq(3, 2, 1))
    }

    @Test
    fun cast() {
        assertEquals(true, as_ExplicitSequence(mk_Seq(3, 2, 1)) is ExplicitSequence)
    }
}