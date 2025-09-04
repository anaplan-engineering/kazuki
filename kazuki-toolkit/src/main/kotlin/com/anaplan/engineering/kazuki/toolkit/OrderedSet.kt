package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.Sequence
import com.anaplan.engineering.kazuki.core.as_Seq
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.toolkit.OrderedSet_Module.as_OrderedSet

@Module
interface OrderedSet<T> : Sequence<T> {

    @Invariant
    fun noDuplicates() = len == elems.card

}

fun <I, O> orderedSet(
    provider: Iterable<I>,
    selector: (I) -> O
) = orderedSet(provider, { true }, selector)

fun <I, O> orderedSet(
    provider: Iterable<I>,
    filter: (I) -> Boolean,
    selector: (I) -> O
) = as_OrderedSet(provider.filter(filter).map(selector))