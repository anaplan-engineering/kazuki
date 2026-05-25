package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.syntax.examples.ConcreteDerivedSubset_Module.mk_ConcreteDerivedSubset
import kotlin.test.Test
import kotlin.test.assertEquals

class TestConcreteDerivedSubset {

    @Test
    fun derivesExcludedLayers() {
        val subset = mk_ConcreteDerivedSubset(
            id = "subset",
            members = mk_Set("b1", "b2"),
            excludedTypes = mk_Set("A", "C"),
        )

        assertEquals(mk_Set("layer:A", "layer:C"), subset.excludedLayers)
    }
}
