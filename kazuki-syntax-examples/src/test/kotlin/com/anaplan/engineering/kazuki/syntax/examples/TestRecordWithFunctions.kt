package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.syntax.examples.RecordWithFunctions_Module.mk_RecordWithFunctions
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRecordWithFunctions {

    @Test
    fun canInvokeFunctions() {
        val record = mk_RecordWithFunctions(mk_Seq(1, 4, 2, 3), mk_Set(3, 4))
        val result = record.functions.remove(4)
        assertEquals(mk_Seq(1, 2, 3), result.members)
        assertEquals(mk_Set(3), result.goodMembers)
    }
}