package com.anaplan.engineering.kazuki.tictactoe

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.tictactoe.XO_Module.as_Moves
import com.anaplan.engineering.kazuki.tictactoe.XO_Module.mk_Position
import com.anaplan.engineering.kazuki.tictactoe.XO_Module.mk_Moves
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMoves {

    private val completeMoves = listOf(
        mk_Position(1u, 1u),
        mk_Position(2u, 1u),
        mk_Position(1u, 2u),
        mk_Position(2u, 2u),
        mk_Position(1u, 3u),
    )

    // TODO -- also have unit tests in core
    @Test
    fun checkEquality_vsMoves() {
        val moves1 = as_Moves(completeMoves)
        val moves2 = as_Moves(completeMoves)
        assertEquals(moves1, moves2)
    }

    @Test
    fun checkEquality_vsSeq() {
        val moves = as_Moves(completeMoves)
        val sequence = as_Seq1(completeMoves)
        assertEquals(moves, sequence)
    }
}