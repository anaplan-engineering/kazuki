package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.syntax.examples.GenericSequence_Module.as_GenericSequence
import com.anaplan.engineering.kazuki.syntax.examples.GenericSequence_Module.is_GenericSequence
import com.anaplan.engineering.kazuki.syntax.examples.GenericSequence_Module.mk_GenericSequence
import kotlin.test.Test
import kotlin.test.assertEquals

class TestGenericSequence {

    @Test
    fun emptyAxisValid() {
        assertEquals(true, is_GenericSequence<Int>(mk_Seq()))
    }

    @Test
    fun noDuplicatesValid() {
        assertEquals(true, is_GenericSequence(mk_Seq(1, 2)))
    }

    @Test
    fun duplicateInvalid() {
        assertEquals(false, is_GenericSequence(mk_Seq(1, 1)))
    }

    @Test
    fun get() {
        assertEquals(2, mk_GenericSequence(3, 2, 1)[2])
    }

    @Test
    fun equals_sameObject() {
        val seq = mk_GenericSequence(3, 2, 1)
        assertEquals(true, seq == seq)
    }

    @Test
    fun equals_explicitlyTyped() {
        assertEquals(true, mk_GenericSequence(3, 2, 1) == mk_GenericSequence(3, 2, 1))
    }

    @Test
    fun equals_implicitlyTyped() {
        assertEquals(true, mk_GenericSequence(3, 2, 1) == mk_Seq(3, 2, 1))
    }

    @Test
    fun cast() {
        assertEquals(true, as_GenericSequence(mk_Seq(3, 2, 1)) is GenericSequence<Int>)
    }
}