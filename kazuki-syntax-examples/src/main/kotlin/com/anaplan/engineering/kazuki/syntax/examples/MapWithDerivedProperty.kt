package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.dom
import com.anaplan.engineering.kazuki.core.set

/**
 * Exemplifies a map with:
 *  - derived properties
 */

@Module
interface Name {
    val first: String
    val second: String
}

@Module
interface MapWithDerivedProperty: Map<Name, Int> {
    val firstNames get() = set(dom()) { it.first }
}