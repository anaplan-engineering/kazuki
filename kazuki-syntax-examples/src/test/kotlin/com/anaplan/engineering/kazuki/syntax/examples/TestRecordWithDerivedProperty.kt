package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.syntax.examples.RecordWithDerivedProperty_Module.mk_RecordWithDerivedProperty
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRecordWithDerivedProperty {

    @Test
    fun firstGoodMember_hasGood() {
        val record = mk_RecordWithDerivedProperty(mk_Seq(1, 4, 2, 3), mk_Set(3, 4))
        assertEquals(4, record.firstGoodMember)
    }

    @Test
    fun firstGoodMember_noGood() {
        val record = mk_RecordWithDerivedProperty(mk_Seq(1, 4, 2, 3), mk_Set())
        assertEquals(null, record.firstGoodMember)
    }

    @Test
    fun firstGoodMember_noMembers() {
        val record = mk_RecordWithDerivedProperty<Int>(mk_Seq(), mk_Set())
        assertEquals(null, record.firstGoodMember)
    }
}