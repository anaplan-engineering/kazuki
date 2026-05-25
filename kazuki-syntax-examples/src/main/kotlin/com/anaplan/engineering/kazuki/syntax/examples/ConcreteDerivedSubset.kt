package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.*

/**
 * Exemplifies a record whose derived property is inherited through diamond inheritance
 */

@Module(makeable = false)
interface BaseSubset {
    val id: String
    val members: Set<String>
    val excludedLayers: Set<String>
}

@Module(makeable = false)
interface AbstractSubset : BaseSubset {
    override val excludedLayers: Set<String>
    val excludedTypes: Set<String>
}

@Module
interface DerivedSubset : AbstractSubset {
    override val excludedLayers: Set<String>
        get() = excludedTypes.transform { "layer:$it" }
}

@Module
interface ConcreteSubset : AbstractSubset

@Module
interface ConcreteDerivedSubset : DerivedSubset, ConcreteSubset
