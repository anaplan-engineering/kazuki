package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.card

/**
 * Exemplifies a sequence with an additional invariant
 */
@Module
interface GenericSequence<T>: Sequence<T> {

    @Invariant
    fun noDuplicates() = len == elems.card

}