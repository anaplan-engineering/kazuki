package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.*

/**
 * Exemplifies a combined hierarchy that inherits `graph` from two supertypes with
 * different static types (diamond inheritance)
 */

@Module
interface BaseGraph {
    val explicitLayers: Sequence<String>
}

@Module
interface NarrowGraph : BaseGraph {
    override val explicitLayers: Sequence<String>
}

@Module
interface SpecializedHierarchy {
    val graph: NarrowGraph
}

@Module
interface AlternativeHierarchy {
    val graph: BaseGraph
}

@Module
interface CombinedHierarchy : SpecializedHierarchy, AlternativeHierarchy {
    fun layerCount(): nat = graph.explicitLayers.len
}
