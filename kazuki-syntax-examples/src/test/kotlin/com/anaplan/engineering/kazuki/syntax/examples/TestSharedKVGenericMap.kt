package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.mk_Map
import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.syntax.examples.SharedKVGenericMap_Module.is_SharedKVGenericMap
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSharedKVGenericMap {

    @Test
    fun is_implicitType() {
        assertEquals(true, is_SharedKVGenericMap(mk_Map(1 to mk_Set(1, 2))))
    }


}