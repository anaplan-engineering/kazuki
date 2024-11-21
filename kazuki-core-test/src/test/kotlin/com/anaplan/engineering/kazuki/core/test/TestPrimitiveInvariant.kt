package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.FourNums_Module.mk_FourNums
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PrimitiveInvariantTest {

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
    fun invariantOrdering() {
        val exception = assertFailsWith<InvariantFailure> { mk_FourNums(100,-100,3,17) } // n1, n3 and the FourNums' invariants are triggered
        assertEquals("FourNums invariant failed in: equalTwenty and n1 and n3", exception.message) // it tests the FourNums invariant, then n1 and n3's invariants
    }

    @Ignore
    @Test
    fun testN() {
        assertFailsWith<InvariantFailure> {addOneN(-1)}          // -1 is not a natural number, so this should fail
    }

    @Ignore
    @Test
    fun testNE1_Invariants() {
        assertFailsWith<InvariantFailure> { addOneNE1(-11) }     // This is out of "NumberExample1"'s Invariant range, so should fail
        assertFailsWith<InvariantFailure> { addOneNE1(-1) }      // "NumberExample1" inherits from "nat", so should be >= 0, so should fail, even though it is within range of the invariance
    }

    @Test
    fun testNE1_Other() {
        assertEquals(1, addOneNE1(0))                   // This one should pass as everything is within range
        assertEquals(5, addOneNE1(4))                   // This one should pass as everything is within range
        assertFailsWith<PreconditionFailure> { addOneNE1(6) }    // This one should fail due to the precondition on the function, not the primitive invariance
    }

    @Ignore
    @Test
    fun testNE2_Invariants() {
        assertFailsWith<InvariantFailure> { addOneNE2(-11) }     // This is out of "NumberExample2"'s Invariant range, so should fail
        assertFailsWith<InvariantFailure> { addOneNE2(-1) }      // "NumberExample2" inherits from "nat1", so should be > 0, so should fail, even though it is within range of the invariance
        assertFailsWith<InvariantFailure> { addOneNE2(0) }       // "NumberExample2" inherits from "nat1", so should be > 0, so should fail, even though it is within range of the invariance
    }

    @Test
    fun testNE2_Other() {
        assertEquals(5, addOneNE2(4))                   // This one should pass as everything is within range
        assertFailsWith<PreconditionFailure> { addOneNE2(5) }    // This one should fail due to the precondition on the function, not the primitive invariance
    }

    @Ignore
    @Test
    fun testNE3_Invariants() {
        assertFailsWith<InvariantFailure> { addOneNE3(17) }      // This is the one invariant failure for "NumberExample3"
    }

    @Test
    fun testNE3_Other() {
        assertEquals(0, addOneNE3(-1) )                 // This should run as it inherits from "int"
        assertEquals(1, addOneNE3(0) )                  // This should run as it inherits from "int"
    }

    @Ignore
    @Test
    fun testInterface_InheritedInvariants() {
        assertFailsWith<InvariantFailure> { mk_FourNums(-1,1,1,1) }          // nat's should be >=0, which this is not

        assertFailsWith<InvariantFailure> { mk_FourNums(1,-1,1,1) }          // Within NE1's invariance but outside of nat's, which it should inherit
        assertFailsWith<InvariantFailure> { mk_FourNums(1,1,-1,1) }          // Within NE2's invariance but outside of nat's, which it should inherit
    }

    @Test
    fun testInterface_SpecifiedInvariants() {
        assertFailsWith<InvariantFailure> { mk_FourNums(5,5,5,5) }

        assertEquals(mk_FourNums(1,1,2,3),mk_FourNums(1,1,2,3))   // All are within scope, so should run

        assertFailsWith<InvariantFailure> { mk_FourNums(1,-11,1,1) }         // Outside of NE1's invariance
        assertFailsWith<InvariantFailure> { mk_FourNums(1,111,1,1) }         // Outside of NE1's invariance

        assertFailsWith<InvariantFailure> { mk_FourNums(1,1,-11,1) }         // Outside of NE2's invariance
        assertFailsWith<InvariantFailure> { mk_FourNums(1,1,11,1) }          // Outside of NE2's invariance

        assertFailsWith<InvariantFailure> { mk_FourNums(1,1,1,17) }          // Outside of NE3's invariance

        assertFailsWith<InvariantFailure> { mk_FourNums(-1,-11,-11,17) }     // Outside of all invariance
    }

    @Ignore
    @Test
    fun testTypes_Not() {       // Each of these do not safisfy the necessary invariance, so should not be identified as those types
        assertEquals(ne1Invariant(50), 50 is NumberExample1)
        assertEquals(ne2Invariant(50), 50 is NumberExample2)
        assertEquals(ne3Invariant(17), 17 is NumberExample3)
    }
    @Test
    fun testTypes_Are() {       // Each of these do satisfy the necessary invariance, so should be identified as those types
        assertEquals(ne1Invariant(5), 5 is NumberExample1)
        assertEquals(ne2Invariant(3), 3 is NumberExample2)
        assertEquals(ne3Invariant(10), 10 is NumberExample3)
    }


}