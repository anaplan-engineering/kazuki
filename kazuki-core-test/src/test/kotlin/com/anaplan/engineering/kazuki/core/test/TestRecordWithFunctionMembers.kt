package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.syntax.examples.RecordWithFunctionMembers_Module.is_RecordWithFunctionMembers
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRecordWithFunctionMembers {

    @Test
    fun recognisesTupleWithAliasedFunctionMembers() {
        val isApplicable: BinaryDomainFn = { left, right -> left.toDouble() <= right.toDouble() }
        val evaluate: BinaryEvaluationFn = { left, right -> left.toDouble() + right.toDouble() }

        assertEquals(true, is_RecordWithFunctionMembers(mk_(isApplicable, evaluate)))
        assertEquals(false, is_RecordWithFunctionMembers(mk_("not a function", evaluate)))
    }
}
