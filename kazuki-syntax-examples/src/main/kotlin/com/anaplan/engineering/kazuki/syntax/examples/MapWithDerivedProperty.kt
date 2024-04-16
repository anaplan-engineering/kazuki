package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.Mapping
import com.anaplan.engineering.kazuki.core.Module
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
interface MapWithDerivedProperty : Mapping<Name, Int> {
    val firstNames get() = set(dom) { it.first }
}