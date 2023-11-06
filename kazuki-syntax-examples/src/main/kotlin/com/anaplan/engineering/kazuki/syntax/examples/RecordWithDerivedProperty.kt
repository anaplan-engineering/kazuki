package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.*

/**
 * Exemplifies a record with:
 *  - derived properties
 */
@Module
interface RecordWithDerivedProperty<T> {

    val members: Sequence<T>
    val goodMembers: Set<T>

    @Invariant
    fun goodMembersAllMembers() = goodMembers subset members.elems

    val firstGoodMember get() = members.firstOrNull { it in goodMembers }
}