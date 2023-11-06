package com.anaplan.engineering.kazuki.tictactoe

import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.tictactoe.XO_Module.mk_Position
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class TestPosition {

    @Test
    fun checkInvariantPasses() {
        val position = mk_Position(1, 3)
        assertEquals(1, position.row)
        assertEquals(3, position.col)
    }

    @Test(expected = InvariantFailure::class)
    fun checkRowInvariant_tooBig() {
        mk_Position(4, 2)
    }

    @Test(expected = InvariantFailure::class)
    fun checkColInvariant_tooBig() {
        mk_Position(2, 4)
    }

    @Test(expected = InvariantFailure::class)
    fun checkRowInvariant_tooSmall() {
        mk_Position(0, 2)
    }

    @Test(expected = InvariantFailure::class)
    fun checkColInvariant_tooSmall() {
        mk_Position(2, 0)
    }

    @Test
    fun checkEquality() {
        val p1 = mk_Position(1, 1)
        val p2 = mk_Position(2, 1)
        val p3 = mk_Position(1, 1)

        assertTrue(p1 == p1)
        assertFalse(p1 == p2)
        assertTrue(p1 == p3)
    }
}