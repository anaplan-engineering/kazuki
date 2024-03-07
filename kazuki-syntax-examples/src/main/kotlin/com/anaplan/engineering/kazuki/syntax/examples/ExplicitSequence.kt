package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.card

/**
 * Exemplifies a sequence with an explicit type
 */
@Module
interface ExplicitSequence: Sequence<Int> {

    @Invariant
    fun noDuplicates() = len == elems.card

}