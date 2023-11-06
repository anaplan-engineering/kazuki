package com.anaplan.engineering.kazuki.tictactoe

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.tictactoe.XO_Module.mk_Position
import com.anaplan.engineering.kazuki.tictactoe.XO_Module.mk_Moves
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMoves {

    private val completeMoves = listOf(
        mk_Position(1, 1),
        mk_Position(2, 1),
        mk_Position(1, 2),
        mk_Position(2, 2),
        mk_Position(1, 3),
    )

    // TODO -- also have unit tests in core
    @Test
    fun checkEquality_vsMoves() {
        val moves1 = mk_Moves(completeMoves)
        val moves2 = mk_Moves(completeMoves)
        assertEquals(moves1, moves2)
    }

    @Test
    fun checkEquality_vsSeq() {
        val moves = mk_Moves(completeMoves)
        val sequence = as_Seq1(completeMoves)
        assertEquals(moves, sequence)
    }
}