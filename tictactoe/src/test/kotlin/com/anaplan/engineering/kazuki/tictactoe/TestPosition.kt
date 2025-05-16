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
        val position = mk_Position(1u, 3u)
        assertEquals(1u, position.row)
        assertEquals(3u, position.col)
    }

    @Test(expected = InvariantFailure::class)
    fun checkRowInvariant_tooBig() {
        mk_Position(4u, 2u)
    }

    @Test(expected = InvariantFailure::class)
    fun checkColInvariant_tooBig() {
        mk_Position(2u, 4u)
    }

    @Test(expected = InvariantFailure::class)
    fun checkRowInvariant_tooSmall() {
        mk_Position(0u, 2u)
    }

    @Test(expected = InvariantFailure::class)
    fun checkColInvariant_tooSmall() {
        mk_Position(2u, 0u)
    }

    @Test
    fun checkEquality() {
        val p1 = mk_Position(1u, 1u)
        val p2 = mk_Position(2u, 1u)
        val p3 = mk_Position(1u, 1u)

        assertTrue(p1 == p1)
        assertFalse(p1 == p2)
        assertTrue(p1 == p3)
    }
}