package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.FourNums_Module.mk_FourNums
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestPrimitiveInvariant {

    /*
        Whether an invariant failure will be called depends on the context.
        If a function requires an input of a particular type, it will not check to see if the input satisfies any primitive or inherited invariants.
        When building an interface which has data of a particular type, it will check the primitive invariants for that type. It may check inherited invariants but it may not.
        Invariants associated with nat (>=0) and nat1 (>0) are never checked, regardless of where they are called.
        When testing "X is Type", it will not consider the invariants and only checks if they share the same base class(e.g. Int)

        Additionally, when both an interface and its data have invariants, the invariants on the interface will be checked first.
        Then (whether the interface invariant passes or not), the data invariants will be tested.
        This is not shown here but if an interface's invariant depends on the data's invariants being satisfied (e.g. if a mapping is applied), then it can cause exceptions

        A NumberExample1 value has invariant: "in -10 .. 10" and inherits from nat (>= 0)
        A NumberExample2 value has invariant: "in -2 .. 5" and inherits from nat1 (> 0)
        A NumberExample3 value has invariant: "=/= 17" and inherits from int

        addOneN should take a nat value as an input and add one to this input
        addOneNE1 should take a NumberExample1 value as an input and add one to this input
        addOneNE2 should take a NumberExample2 value as an input and add one to this input
        addOneNE3 should take a NumberExample3 value as an input and add one to this input
        mk_FourNumber should take a nat, a NumberExample1, a NumberExample2 and a NumberExample3 value as an input, to build an interface

        All assertions that do not pass have been separated into their own tests, with "@Ignore" annotating them
    */

    @Test
    fun testNE1_Other() {
        assertEquals(1uL, addOneNE1(0u))                   // This one should pass as everything is within range
        assertEquals(5uL, addOneNE1(4u))                   // This one should pass as everything is within range
        assertFailsWith<PreconditionFailure> { addOneNE1(6u) }    // This one should fail due to the precondition on the function, not the primitive invariance
    }

    @Ignore
    @Test
    fun testNE2_Invariants() {
        assertFailsWith<InvariantFailure> { addOneNE2(0uL) }       // "NumberExample2" inherits from "nat1", so should be > 0, so should fail, even though it is within range of the invariance
    }

    @Test
    fun testNE2_Other() {
        assertEquals(5uL, addOneNE2(4uL))                   // This one should pass as everything is within range
        assertFailsWith<PreconditionFailure> { addOneNE2(5uL) }    // This one should fail due to the precondition on the function, not the primitive invariance
    }

    @Ignore
    @Test
    fun testNE3_Invariants() {
        assertFailsWith<InvariantFailure> { addOneNE3(17) }      // This is the one invariant failure for "NumberExample3"
    }

    @Test
    fun testNE3_Other() {
        assertEquals(1uL, addOneNE3(0))                  // This should run as it inherits from "int"
    }

    @Test
    fun testInterface_SpecifiedInvariants() {
        assertFailsWith<InvariantFailure> { mk_FourNums(5u, 5u, 5u, 5) }
        assertEquals(mk_FourNums(1u, 1u, 2u, 3), mk_FourNums(1u, 1u, 2u, 3))   // All are within scope, so should run
        assertFailsWith<InvariantFailure> { mk_FourNums(1u, 111u, 1u, 1) }         // Outside of NE1's invariance
        assertFailsWith<InvariantFailure> { mk_FourNums(1u, 1u, 11u, 1) }          // Outside of NE2's invariance
        assertFailsWith<InvariantFailure> { mk_FourNums(1u, 1u, 1u, 17) }          // Outside of NE3's invariance
    }

    @Ignore
    @Test
    fun testTypes_Not() {       // Each of these do not safisfy the necessary invariance, so should not be identified as those types
        assertEquals(ne1Invariant(50uL), 50uL is NumberExample1)
        assertEquals(ne2Invariant(50uL), 50uL is NumberExample2)
        assertEquals(ne3Invariant(17L), 17L is NumberExample3)
    }

    @Test
    fun testTypes_Are() {       // Each of these do satisfy the necessary invariance, so should be identified as those types
        assertEquals(ne1Invariant(5uL), 5uL is NumberExample1)
        assertEquals(ne2Invariant(3uL), 3uL is NumberExample2)
        assertEquals(ne3Invariant(10L), 10L is NumberExample3)
    }


}