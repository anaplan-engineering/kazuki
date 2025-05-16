package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.core.nat1

/**
 * Exemplifies a record with:
 *  - generic members
 *  - explicit member
 *  - multiple invariants
 */
@Module
interface GroupRecord<T> {

    val leader: T
    val members: Set<T>
    val maxCount: nat1

    @Invariant
    fun membersContainsLeader() = leader in members

    @Invariant
    fun membersUnderMaxCount() = members.card < maxCount

}