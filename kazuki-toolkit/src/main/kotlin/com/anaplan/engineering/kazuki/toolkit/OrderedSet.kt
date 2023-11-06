package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.card

@Module
interface OrderedSet<T> : Sequence<T> {

    @Invariant
    fun noDuplicates() = len == elems.card

}