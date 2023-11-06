package com.anaplan.engineering.kazuki.tictactoe

import com.anaplan.engineering.kazuki.core.*

// TODO -- invariants should include message
// TODO -- is_<Moves>() and as_<Moves>


@Module
object XO {

    const val Size = 3

    const val MaxMoves = Size * Size

    enum class Player {
        Nought,
        Cross
    }

    interface Position {
        val row: Coord
        val col: Coord
    }

    @PrimitiveInvariant(name = "Coord", base = Int::class)
    fun coordInvariant(c: Int) = c in 1 .. Size

    // A legal game play sequence
    interface Moves : Sequence1<Position> {
        @Invariant
        fun noDuplicatePositions() = len == elems.card

        @Invariant
        fun hasMinMovesToWin() = len > Players.card * (Size - 1)

        @Invariant
        fun doesntHaveTooManyMoves() = len <= MaxMoves
    }

    val Players = asSet<Player>()

    interface PlayOrder : Sequence1<Player> {

        @Invariant
        fun noDuplicatePlayers() = len == elems.card

        @Invariant
        fun correctNumberOfPlayers() = elems == Players
    }

    val S: Set<nat1> = mk_Set(1..Size)
//
//    val winningLines = dunion(
//        set(
//            set<nat1, VSet<Position>>(selector = { r: nat1 ->
//                set<nat1, Position>(selector = { c: nat1 ->
//                    Position(r,
//                        c)
//                }, S)
//            }, S),
//            set<nat1, VSet<Position>>(selector = { c: nat1 ->
//                set<nat1, Position>(selector = { r: nat1 ->
//                    Position(r,
//                        c)
//                }, S)
//            }, S),
//            set<VSet<Position>>(set<nat1, Position>(selector = { x: nat1 -> Position(x, x) }, S)),
//            set<VSet<Position>>(set<nat1, Position>(selector = { x: nat1 -> Position(x, Size.toNat1() - x + 1) }, S)),
//        ))
//
//    @RecordType
//    data class Game(
//        val board: Map<Position, Player>,
//        val order: PlayOrder
//    ) {
//
//        override fun isValid() =
//            (XO.moveCountLeft(this) >= 0) and
//                    forall(order.inds - order.len) { i ->
//                        val current = order[i]
//                        val next = order[i + 1]
//                        XO.movesForPlayer(this, current).card - XO.movesForPlayer(this, next).card in set<nat>(0, 1)
//                    }
//    }
//
//    val hasWon = function(
//        command = { g: Game, p: Player ->
//            val moves = XO.movesForPlayer(g, p)
//            exists(winningLines) { line -> line subset moves }.toBool()
//        }
//    )
//
//    val hasLost = function(
//        command = { g: Game, p: Player ->
//            XO.hasWon(g, p).not
//        }
//    )
//
//    val whoWon = function(
//        command = { g: Game -> iota(Players) { p -> XO.hasWon(g, p).toBoolean() } },
//        pre = { g: Game -> XO.isWon(g).toBoolean() }
//    )
//
//    val isWon = function(
//        command = { g: Game -> exists1(Players) { p -> XO.hasWon(g, p).toBoolean() }.toBool() },
//    )
//
//    val isDraw = function(
//        command = { g: Game -> (not(XO.isWon(g)) and (XO.moveCountLeft(g) == 0).toBool()).toBool() },
//    )
//
//    val isUnfinished = function(
//        command = { g: Game -> (not(XO.isWon(g)) and not(XO.isDraw(g))).toBool() },
//    )
//
//    val movesSoFar = function(
//        command = { g: Game -> g.board.dom }
//    )
//
//    val moveCountSoFar = function(
//        command = { g: Game -> XO.movesSoFar(g).card }
//    )
//
//    val moveCountLeft = function(
//        command = { g: Game -> MaxMoves.toNat() - XO.moveCountSoFar(g) }
//    )
//
//    val movesForPlayer = function(
//        command = { g: Game, p: Player -> (g.board rrt set(p)).dom }
//    )

}

