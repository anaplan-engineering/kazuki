package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.syntax.examples.MapWithDerivedProperty_Module.mk_MapWithDerivedProperty
import com.anaplan.engineering.kazuki.syntax.examples.Name_Module.mk_Name
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMapWithDervivedProperty {

    @Test
    fun firstNames() {
        val map = mk_MapWithDerivedProperty(
            mk_(mk_Name("joe", "bloggs"), 3),
            mk_(mk_Name("bill", "smith"), 3),
            mk_(mk_Name("joe", "stevens"), 3),
        )
        assertEquals(mk_Set("joe", "bill"), map.firstNames)
    }

}