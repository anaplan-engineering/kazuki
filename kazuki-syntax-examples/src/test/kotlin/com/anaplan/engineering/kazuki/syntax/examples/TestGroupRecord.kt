package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.syntax.examples.GroupRecord_Module.mk_GroupRecord
import kotlin.test.Test
import kotlin.test.assertEquals

class TestGroupRecord {

    @Test
    fun make() {
        val record = mk_GroupRecord("leader", mk_Set("leader", "other"), 3u)
        assertEquals(mk_Set("leader", "other"), record.members)
        assertEquals(3u, record.maxCount)
    }

    @Test(expected = InvariantFailure::class)
    fun membersContainsLeader_invalid() {
        mk_GroupRecord("leader", mk_Set("other"), 3u)
    }

    @Test(expected = InvariantFailure::class)
    fun membersUnderMaxCount_invalid() {
        mk_GroupRecord("leader", mk_Set("leader", "other"), 1u)
    }

}