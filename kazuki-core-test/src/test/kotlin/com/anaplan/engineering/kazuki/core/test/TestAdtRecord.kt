package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.EmptyAdtConcrete
import com.anaplan.engineering.kazuki.core.EmptyAdt_Module.is_EmptyAdt
import com.anaplan.engineering.kazuki.core.EmptyAdt_Module.mk_EmptyAdt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TestAdtRecord {

    @Test
    fun mkEmptyAdtDelegatesToInternalConcrete() {
        val adt = mk_EmptyAdt(42)
        assertTrue(is_EmptyAdt(adt))
        assertEquals(42, (adt as EmptyAdtConcrete).value)
    }
}
