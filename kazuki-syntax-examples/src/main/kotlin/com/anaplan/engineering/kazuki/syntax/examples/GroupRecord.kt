package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.Invariant
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.card

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
    val maxCount: Int

    @Invariant
    fun membersContainsLeader() = leader in members

    @Invariant
    fun membersUnderMaxCount() = members.card < maxCount

}