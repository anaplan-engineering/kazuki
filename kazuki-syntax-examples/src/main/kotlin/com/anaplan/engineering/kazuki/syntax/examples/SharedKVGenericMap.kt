package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.Module

/**
 * Exemplifies a map with:
 *  - key & value consuming shared generic
 *  - no invariant
 */
@Module
interface SharedKVGenericMap<C>: Map<C, Set<C>> {

}