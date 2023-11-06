package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.MeasureFailure
import com.anaplan.engineering.kazuki.core.mk_Set
import kotlin.test.Test
import kotlin.test.assertEquals

class TestRecursiveFunctions {

    @Test
    fun noMeasure() {
        assertEquals(4, RecursiveFunctions.noRecursion(mk_Set(1, 2, 3, 4)))
    }

    // TODO -- test measure cleared with pre/post failures
    @Test
    fun validRecursion() {
        assertEquals(4, RecursiveFunctions.validRecursion(mk_Set(1, 2, 3, 4)))

        // run twice to confirm that measure is reset
        assertEquals(4, RecursiveFunctions.validRecursion(mk_Set(1, 2, 3, 4)))
    }

    @Test(expected = MeasureFailure::class)
    fun invalidRecursion() {
        RecursiveFunctions.invalidRecursion(mk_Set(1, 2, 3, 4))
    }
}