package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.OtherRecord_Module.mk_OtherRecord
import com.anaplan.engineering.kazuki.core.RecordExtension_Module.mk_RecordExtension
import com.anaplan.engineering.kazuki.core.RecordInvOnlyExtension_Module.mk_RecordInvOnlyExtension
import com.anaplan.engineering.kazuki.core.Record_Module.mk_Record
import com.anaplan.engineering.kazuki.core.SequenceExtension_Module.mk_SequenceExtension
import com.anaplan.engineering.kazuki.core.SetExtension_Module.mk_SetExtension
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Some very basic tests, but semantics v.likely to change
 */
class TestTypeUtilities {

    @Test
    fun is_null() {
        assertEquals(false, is_<Int>(null))
        assertEquals(false, is_<Mapping<Int, Int>>(null))
    }

    @Test
    fun is_set() {
        assertEquals(false, is_<Set<Int>>(null))
        assertEquals(true, is_<Set<Int>>(mk_Set(1, 2, 3)))
        assertEquals(false, is_<Set<Int>>(mk_Seq(1, 2, 3))) // Provisional
        assertEquals(false, is_<Set1<Int>>(mk_Set<Int>()))
        assertEquals(true, is_<Set<Int>>(mk_Set1(1)))
    }

    @Test
    fun is_seq() {
        assertEquals(false, is_<Sequence<Int>>(null))
        assertEquals(true, is_<Sequence<Int>>(mk_Seq(1, 2, 3)))
        assertEquals(false, is_<Sequence<Int>>(mk_Set(1, 2, 3)))
        assertEquals(false, is_<Sequence1<Int>>(mk_Seq<Int>()))
        assertEquals(true, is_<Sequence<Int>>(mk_Seq1(1)))
    }

    @Test
    fun is_record() {
        assertEquals(true, is_<Record>(mk_Record(2)))
        assertEquals(true, is_<Record>(mk_RecordInvOnlyExtension(2)))
        assertEquals(true, is_<Record>(mk_RecordExtension(2, "3")))
        assertEquals(false, is_<Record>(mk_OtherRecord(2)))
    }

    @Test
    fun is_setOfSets_bug() {
        assertEquals(
            true,
            is_<SetExtension<Set<Int>>>(
                mk_SetExtension(mk_Set(1, 2, 3))
            )
        )
        assertEquals(
            true,
            is_<SetExtension<Set<Int>>>(
                mk_SetExtension(mk_Set(1, 2, 3), mk_Set(4, 2, 3)),
            )
        )
        assertEquals(
            false,
            is_<SetExtension<Set<Int>>>(null),
        )
        assertEquals(
            false,
            is_<SetExtension<Set<Int>>>("abc"),
        )
    }

    @Test
    fun is_seqOfSets_bug() {
        assertEquals(
            true,
            is_<SequenceExtension<Set<Int>>>(
                mk_SequenceExtension(mk_Set(1, 2, 3))
            )
        )
        assertEquals(
            true,
            is_<SequenceExtension<Set<Int>>>(
                mk_SequenceExtension(mk_Set(1, 2, 3), mk_Set(4, 2, 3)),
            )
        )
        assertEquals(
            false,
            is_<SequenceExtension<Set<Int>>>(null),
        )
        assertEquals(
            false,
            is_<SequenceExtension<Set<Int>>>("abc"),
        )

    }

    /**
     * We hit the same problems with Java generics here that Kotlin does..
     * The problem being that whilst the outer generic can be inlined we are still stuck with an
     * erased nested type that we cannot reason about!
     */
    @Ignore
    @Test
    fun problemsWithGenerics() {
        // Both of the below give size 2 as the filter only checks that they are sets
        assertEquals(1, setOf(setOf(1, 2, 3), setOf("a")).filterIsInstance<Set<Int>>().size)
        assertEquals(1uL, mk_Set(mk_Set(1, 2, 3), mk_Set("a")).filterIs_<Set<Int>>().card)

        // The remaining scenarios all fail for the same reason
        assertEquals(false, is_<Set<Int>>(mk_Set("a", "b")))
        assertEquals(false, is_<Sequence<Int>>(mk_Seq("a", "b")))
        assertEquals(
            false,
            is_<SetExtension<Set<Int>>>(
                mk_SetExtension(mk_Set(1, 2, 3), null),
            )
        )
        assertEquals(
            false,
            is_<SetExtension<Set<Int>>>(
                mk_SetExtension(mk_Set(1, 2, 3), mk_Set("a")),
            )
        )
        assertEquals(
            false,
            is_<SetExtension<Set<Int>>>(
                mk_SetExtension(mk_Set(1, 2, 3), "abc"),
            )
        )
        assertEquals(
            false,
            is_<SequenceExtension<Set<Int>>>(
                mk_SequenceExtension(mk_Set(1, 2, 3), null),
            )
        )
        assertEquals(
            false,
            is_<SequenceExtension<Set<Int>>>(
                mk_SequenceExtension(mk_Set(1, 2, 3), mk_Set("a")),
            )
        )
        assertEquals(
            false,
            is_<SequenceExtension<Set<Int>>>(
                mk_SequenceExtension(mk_Set(1, 2, 3), "abc"),
            )
        )
    }

}