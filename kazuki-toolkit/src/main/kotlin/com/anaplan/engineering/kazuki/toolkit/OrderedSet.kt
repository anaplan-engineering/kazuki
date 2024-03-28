package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.card

@Module
interface OrderedSet<T> : Sequence<T> {

    // TODO -- should be able to do `len == card` if is this is-a set
    // should we inherit from both sequence and set.. pros and cons?
    @Invariant
    fun noDuplicates() = len == elems.card

    override fun spliterator() = super<Sequence>.spliterator()
}