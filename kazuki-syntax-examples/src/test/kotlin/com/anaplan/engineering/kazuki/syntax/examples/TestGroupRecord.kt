package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.syntax.examples.GroupRecord_Module.mk_GroupRecord
import kotlin.test.Test
import kotlin.test.assertEquals

class TestGroupRecord {

    @Test
    fun make() {
        val record = mk_GroupRecord("leader", mk_Set("leader", "other"), 3)
        assertEquals(mk_Set("leader", "other"), record.members)
        assertEquals(3, record.maxCount)
    }

    @Test(expected = InvariantFailure::class)
    fun membersContainsLeader_invalid() {
        mk_GroupRecord("leader", mk_Set("other"), 3)
    }

    @Test(expected = InvariantFailure::class)
    fun membersUnderMaxCount_invalid() {
        mk_GroupRecord("leader", mk_Set("leader", "other"), 1)
    }

}