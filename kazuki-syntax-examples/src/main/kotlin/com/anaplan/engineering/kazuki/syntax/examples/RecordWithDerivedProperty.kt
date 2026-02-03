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

    @FunctionProvider(DerivedProperties::class)
    val properties: DerivedProperties<T>

    class DerivedProperties<T>(record: RecordWithDerivedProperty<T>) {
        val firstGoodMember by property { record.members.firstOrNull { it in record.goodMembers } }
    }
}