package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.OtherRecord_Module.mk_OtherRecord
import com.anaplan.engineering.kazuki.core.RecordExtension_Module.mk_RecordExtension
import com.anaplan.engineering.kazuki.core.RecordInvOnlyExtension_Module.mk_RecordInvOnlyExtension
import com.anaplan.engineering.kazuki.core.Record_Module.mk_Record
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestRecordExtension {

    @Test
    fun creation() {
        val record = mk_RecordExtension(3, "a")
        assertEquals(record.a, 3)
        assertEquals(record.b, "a")
    }

    @Test
    fun equalityOfInvOnlyExtension() {
        assertEquals(mk_RecordInvOnlyExtension(2), mk_Record(2))
        assertEquals(mk_Record(2), mk_RecordInvOnlyExtension(2))

        assertNotEquals(mk_RecordInvOnlyExtension(2), mk_Record(3),)
        assertNotEquals(mk_Record(3), mk_RecordInvOnlyExtension(2))

        assertEquals(mk_RecordInvOnlyExtension(2), mk_RecordInvOnlyExtension(2))
        assertEquals(mk_RecordInvOnlyExtension(2), mk_RecordInvOnlyExtension(2))

        assertNotEquals(mk_RecordInvOnlyExtension(2), mk_RecordInvOnlyExtension(3))
        assertNotEquals(mk_RecordInvOnlyExtension(3), mk_RecordInvOnlyExtension(2))

        // Making choice that type declaration is part of equality not just tuple
        assertNotEquals<Any>(mk_OtherRecord(2), mk_Record(2))
        assertNotEquals<Any>(mk_Record(2), mk_OtherRecord(2))
    }

    @Test
    fun hashCodeEqualityOfInvOnlyExtension() {
        assertEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_Record(2).hashCode())
        assertEquals(mk_Record(2).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())

        assertNotEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_Record(3).hashCode(),)
        assertNotEquals(mk_Record(3).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())

        assertEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())
        assertEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())

        assertNotEquals(mk_RecordInvOnlyExtension(2).hashCode(), mk_RecordInvOnlyExtension(3).hashCode())
        assertNotEquals(mk_RecordInvOnlyExtension(3).hashCode(), mk_RecordInvOnlyExtension(2).hashCode())
    }

}