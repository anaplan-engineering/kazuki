package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.syntax.examples.CombinedHierarchy_Module.mk_CombinedHierarchy
import com.anaplan.engineering.kazuki.syntax.examples.NarrowGraph_Module.mk_NarrowGraph
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCombinedHierarchy {

    @Test
    fun inheritsTheFinestAvailableGraphType() {
        val graph = mk_NarrowGraph(explicitLayers = mk_Seq("A", "B"))
        val hierarchy = mk_CombinedHierarchy(graph = graph)

        assertEquals(2u, hierarchy.layerCount())
        assertEquals(graph, hierarchy.graph)
    }
}
