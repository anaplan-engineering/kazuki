package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.FunctionProvider
import com.anaplan.engineering.kazuki.core.Mapping
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.property
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

    @FunctionProvider(DerivedProperties::class)
    val properties: DerivedProperties

    class DerivedProperties(mapWithDerivedProperty: MapWithDerivedProperty) {
        val firstNames by property { set(mapWithDerivedProperty.dom) { it.first } }
    }
}
