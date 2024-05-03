package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.syntax.examples.RecordWithFunctions_Module.mk_RecordWithFunctions

/**
 * Exemplifies a record with:
 *  - functions property
 */
@Module
interface RecordWithFunctions<T> {

    val members: Sequence<T>
    val goodMembers: Set<T>

    @Invariant
    fun goodMembersAllMembers() = goodMembers subset members.elems

    @FunctionProvider(Functions::class)
    val functions: Functions<T>
}

class Functions<T>(private val record: RecordWithFunctions<T>) {

    val remove = function(
        command = { member: T ->
            mk_RecordWithFunctions(
                members = record.members - member,
                goodMembers = record.goodMembers - member
            )
        },
        pre = { member -> member in record.members },
        post=  { member, result ->
            member !in result.members && member != result.goodMembers
        }
    )

}