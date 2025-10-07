package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.OtherRecord_Module.mk_OtherRecord
import com.anaplan.engineering.kazuki.core.RecordExtension_Module.mk_RecordExtension
import com.anaplan.engineering.kazuki.core.RecordInvOnlyExtension_Module.mk_RecordInvOnlyExtension
import com.anaplan.engineering.kazuki.core.Record_Module.mk_Record
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalIsAs
/**
 * Some very basic tests, but semantics v.likely to change
 */
class TestTypeUtilities {

    @Test
    fun is_null() {
        assertEquals(false, is_<Int>(null))
        assertEquals(false, is_<Mapping<Int, Int>>(null))
    }

    @Test
    fun is_set() {
        assertEquals(false, is_<Set<Int>>(null))
        assertEquals(true, is_<Set<Int>>(mk_Set(1, 2, 3)))
        // TODO
//        assertEquals(false, is_<Set<Int>>(mk_Set("a", "b")))
        assertEquals(false, is_<Set1<Int>>(mk_Set<Int>()))
        assertEquals(true, is_<Set<Int>>(mk_Set1(1)))
    }

    @Test
    fun is_record() {
        assertEquals(true, is_<Record>(mk_Record(2)))
        assertEquals(true, is_<Record>(mk_RecordInvOnlyExtension(2)))
        // TODO -- is this correct?
        assertEquals(false, is_<Record>(mk_RecordExtension(2, "3")))
        assertEquals(false, is_<Record>(mk_OtherRecord(2)))
    }
}