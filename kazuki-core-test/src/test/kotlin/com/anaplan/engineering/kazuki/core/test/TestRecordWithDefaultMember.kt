package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.syntax.examples.RecordWithDefaultMember_Module.is_RecordWithDefaultMember
import com.anaplan.engineering.kazuki.syntax.examples.RecordWithInheritedDefaultMember_Module.mk_RecordWithInheritedDefaultMember
import kotlin.test.Test
import kotlin.test.assertTrue

class TestRecordWithDefaultMember {

    @Test
    fun test() {
        val record = mk_RecordWithInheritedDefaultMember(1u, 3u)
        assertTrue(is_RecordWithDefaultMember(record))
    }
}