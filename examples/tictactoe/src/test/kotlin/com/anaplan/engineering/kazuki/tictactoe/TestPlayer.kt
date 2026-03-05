package com.anaplan.engineering.kazuki.tictactoe

import com.anaplan.engineering.kazuki.core.mk_Set
import com.anaplan.engineering.kazuki.core.mk_Set1
import com.anaplan.engineering.kazuki.core.asSet
import kotlin.test.Test
import kotlin.test.assertEquals

class TestPlayer {

    // TODO -- also have unit tests in core
    @Test
    fun checkSetCreation_vsSet1() {
        val expected = mk_Set1(XO.Player.Cross, XO.Player.Nought)
        assertEquals(expected, XO.Players)
        assertEquals(expected, asSet())
    }

    @Test
    fun checkSetCreation_vsSet() {
        val expected = mk_Set(XO.Player.Cross, XO.Player.Nought)
        assertEquals(expected, XO.Players)
        assertEquals(expected, asSet())
    }
}