package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.Box_Module.is_Box
import com.anaplan.engineering.kazuki.core.Box_Module.mk_Box
import com.anaplan.engineering.kazuki.core.Foo_Module.is_Foo
import com.anaplan.engineering.kazuki.core.Foo_Module.mk_Foo
import com.anaplan.engineering.kazuki.core.MixedFoo_Module.is_MixedFoo
import com.anaplan.engineering.kazuki.core.MixedFoo_Module.mk_MixedFoo
import org.junit.Assert.*
import org.junit.Test

class TestAdtChallenge {

    // Case P1
    @Test
    fun p1MkFooDelegatesToInternalBar() {
        val adt = mk_Foo(42uL)
        assertTrue(is_Foo(adt))
        assertEquals(42uL, (adt as Bar).bar)
    }

    // Case P2
    @Test
    fun p2MkBoxDelegatesToInternalBoxImpl() {
        val adt = mk_Box("payload")
        assertTrue(is_Box<String>(adt))
        assertEquals("payload", (adt as BoxImpl<String>).payload)
    }

//    // Case F1
//    @Test
//    fun f1FooNoImplementationRecognisesManualInstance() {
//        val adt = object : FooNoImplementation {}
//        assertTrue(is_FooNoImplementation(adt))
//    }

//    // Case F2
//    @Test
//    fun f2MkFooBarNotInternalUsesPublicConcrete() {
//        val adt = mk_FooBarNotInternal(7uL)
//        assertTrue(is_FooBarNotInternal(adt))
//        assertEquals(7uL, (adt as BarNotInternal).bar)
//    }

//    // Case F3
//    @Test
//    fun f3LeakyFooExposesAbstractFieldInMkAndTuple() {
//        val adt = mk_LeakyFoo(1uL, 2uL)
//        assertTrue(is_LeakyFoo(adt))
//        assertEquals(1uL, adt.foo)
//        assertEquals(2uL, (adt as BarLeakyFoo).bar)
//    }

//    // Case F4
//    @Test
//    fun f4LeakyFooAgainMkOnlyTakesLeakedField() {
//        val adt = mk_LeakyFooAgain(9uL)
//        assertTrue(is_LeakyFooAgain(adt))
//        assertEquals(9uL, adt.foo)
//        assertEquals(9uL, (adt as EmptyBarLeakyFoo).foo)
//    }

//    // Case F5
//    @Test
//    fun f5FooObjIsSingletonObjectModule() {
//        assertSame(FooObj, FooObj)
//    }

//    // Case F11
//    @Test
//    fun f11InternalAbstractAdtGeneratesInternalMkOnly() {
//        val adt = mk_LocalADT(5uL)
//        assertTrue(is_LocalADT(adt))
//        assertEquals(5uL, (adt as LocalImpl).state)
//    }

//    // Case F12
//    @Test
//    fun f12ImplementedByOnSequenceDoesNotWireOpaqueMk() {
//        val impl: OpaqueSeq<String> = mk_SeqImpl("a", "b")
//        assertTrue(is_SeqImpl(listOf("a", "b")))
//        assertEquals(listOf("a", "b"), impl.toList())
//    }

//    // Case F7
//    @Test
//    fun f7MkUnextendedFooDelegatesToWrongBar() {
//        val adt = mk_UnextendedFoo(1uL)
//        assertTrue(is_UnextendedFoo(adt))
//        assertEquals(1uL, (adt as WrongBar).bar)
//    }

//    // Case F8
//    @Test
//    fun f8FixedAdtExposesComparablePropertyField() {
//        val adt = mk_FixedADT(1uL, 2uL)
//        assertTrue(is_FixedADT(adt))
//        assertEquals(1uL, adt.id)
//        assertEquals(2uL, (adt as FixedImpl).secret)
//    }

    // Case M2
    @Test
    fun m2MkMixedFooMergesSuperInterfaceFields() {
        val adt = mk_MixedFoo(3uL, 4uL)
        assertTrue(is_MixedFoo(adt))
        val mixed = adt as MixedBar
        assertEquals(3uL, mixed.x)
        assertEquals(4uL, mixed.bar)
    }
}
