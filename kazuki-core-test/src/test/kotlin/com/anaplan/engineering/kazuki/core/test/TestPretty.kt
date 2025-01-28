package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.GenericRecord_Module.mk_GenericRecord
import com.anaplan.engineering.kazuki.core.GenericRecord_Module.pretty
import com.anaplan.engineering.kazuki.core.InjectiveMappingExtension_Module.mk_InjectiveMappingExtension
import com.anaplan.engineering.kazuki.core.MappingExtension_Module.mk_MappingExtension
import com.anaplan.engineering.kazuki.core.PrettyRecord_Module.mk_PrettyRecord
import com.anaplan.engineering.kazuki.core.PrettySequenceExtension_Module.mk_PrettySequenceExtension
import com.anaplan.engineering.kazuki.core.RecordExtension_Module.mk_RecordExtension
import com.anaplan.engineering.kazuki.core.RecordNullableField_Module.mk_RecordNullableField
import com.anaplan.engineering.kazuki.core.RecordNullableField_Module.pretty
import com.anaplan.engineering.kazuki.core.Record_Module.mk_Record
import com.anaplan.engineering.kazuki.core.Record_Module.pretty
import com.anaplan.engineering.kazuki.core.SequenceExtension_Module.mk_SequenceExtension
import com.anaplan.engineering.kazuki.core.SetExtension_Module.mk_SetExtension
import kotlin.test.Test
import kotlin.test.assertEquals

class TestPretty {

    @Test
    fun basicSet() {
        assertEquals("{}", mk_Set<Int>().pretty())
        assertEquals("{1, 2, 3}", mk_Set(1, 2, 3).pretty())
    }

    @Test
    fun extendedSet() {
        assertEquals("{}", mk_SetExtension<Int>().pretty())
        assertEquals("{1, 2, 3}", mk_SetExtension(1, 2, 3).pretty())
    }

    @Test
    fun basicSequence() {
        assertEquals("<>", mk_Seq<Int>().pretty())
        assertEquals("<1, 2, 3>", mk_Seq(1, 2, 3).pretty())
    }

    @Test
    fun extendedSequence() {
        assertEquals("<>", mk_SequenceExtension<Int>().pretty())
        assertEquals("<1, 2, 3>", mk_SequenceExtension(1, 2, 3).pretty())
    }

    @Test
    fun extendedPrettySequence() {
        assertEquals("[[]]", mk_PrettySequenceExtension<Int>().pretty())
        assertEquals("[[1, 2, 3]]", mk_PrettySequenceExtension(1, 2, 3).pretty())
    }

    @Test
    fun basicRelation() {
        assertEquals("{}", mk_Relation<Int, Int>().pretty())
        assertEquals("{(1, 2), (3, 4)}", mk_Relation(mk_(1, 2), mk_(3, 4)).pretty())
    }

    // tbd
    @Test
    fun extendedRelation() {
//        assertEquals("{}", mk_RelationExtension<Int, Int>().pretty())
//        assertEquals("{(1, 2), (3, 4)}", mk_RelationExtension(mk_(1, 2), mk_(3, 4)).pretty())
    }

    @Test
    fun basicMapping() {
        assertEquals("{}", mk_Mapping<Int, Int>().pretty())
        assertEquals("{1 ↦ 2, 3 ↦ 4}", mk_Mapping(mk_(1, 2), mk_(3, 4)).pretty())
    }

    @Test
    fun extendedMapping() {
        assertEquals("{}", mk_MappingExtension<Int, Int>().pretty())
        assertEquals("{1 ↦ 2, 3 ↦ 4}", mk_MappingExtension(mk_(1, 2), mk_(3, 4)).pretty())
    }

    @Test
    fun basicInjectiveMapping() {
        assertEquals("{}", mk_InjectiveMapping<Int, Int>().pretty())
        assertEquals("{1 ↔ 2, 3 ↔ 4}", mk_InjectiveMapping(mk_(1, 2), mk_(3, 4)).pretty())
    }

    @Test
    fun extendedInjectiveMapping() {
        assertEquals("{}", mk_InjectiveMappingExtension<Int, Int>().pretty())
        assertEquals("{1 ↔ 2, 3 ↔ 4}", mk_InjectiveMappingExtension(mk_(1, 2), mk_(3, 4)).pretty())
    }

    @Test
    fun basicRecord() {
        assertEquals("(a=1)", mk_Record(1).pretty())
        assertEquals("(a=1, b=a)", mk_RecordExtension(1, "a").pretty())
        assertEquals("(a=1)", mk_GenericRecord(1).pretty())
    }

    @Test
    fun recordWithCustom() {
        assertEquals("28/10", mk_PrettyRecord(28, 10).pretty())
    }

    @Test
    fun recordWithNested() {
        assertEquals("8/16/10", mk_PrettyRecord(mk_PrettyRecord(8, 16), 10).pretty())
        assertEquals("(a=8/16)", mk_GenericRecord<PrettyRecord<Int>>(mk_PrettyRecord(8, 16)).pretty())
    }

    @Test
    fun recordWithNullableModule() {
        assertEquals("(a=9, b=8/16)", mk_RecordNullableField(9, mk_PrettyRecord(8, 16)).pretty())
        assertEquals("(a=9, b=null)", mk_RecordNullableField(9, null).pretty())
    }

}