package com.anaplan.engineering.kazuki.gameoflife

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.gameoflife.Conway.Population
import com.anaplan.engineering.kazuki.gameoflife.Conway.around
import com.anaplan.engineering.kazuki.gameoflife.Conway.deadCells
import com.anaplan.engineering.kazuki.gameoflife.Conway.generation
import com.anaplan.engineering.kazuki.gameoflife.Conway.generations
import com.anaplan.engineering.kazuki.gameoflife.Conway.neighbourCount
import com.anaplan.engineering.kazuki.gameoflife.Conway.newCells
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.as_Population
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Point
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Population
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class TestConway {

    private val block = mk_Population(mk_Point(0, 0), mk_Point(-1, 0), mk_Point(0, -1), mk_Point(-1, -1))

    private val blinker = mk_Population(mk_Point(-1, 0), mk_Point(0, 0), mk_Point(1, 0))

    private val toad = mk_Population(
        mk_Point(-1, 0), mk_Point(0, 0), mk_Point(1, 0),
        mk_Point(0, -1), mk_Point(-1, -1), mk_Point(-2, -1)
    )

    private val beacon = mk_Population(
        mk_Point(-2, 0), mk_Point(-2, 1), mk_Point(-1, 1), mk_Point(0, -2),
        mk_Point(1, -2), mk_Point(1, -1)
    )

    private val pQuad = mk_Population(
        mk_Point(2, 1), mk_Point(3, 1), mk_Point(3, 2),
        mk_Point(1, 2), mk_Point(1, 3), mk_Point(2, 3), mk_Point(5, 2),
        mk_Point(5, 3), mk_Point(6, 3), mk_Point(7, 3), mk_Point(2, 5),
        mk_Point(3, 5), mk_Point(3, 6), mk_Point(3, 7)
    )


    private val pulsar = as_Population(
        dunion(
            mk_Set(
                set(pQuad) { point -> point },
                set(pQuad) { point -> mk_Point(-point.x, point.y) },
                set(pQuad) { point -> mk_Point(point.x, -point.y) },
                set(pQuad) { point -> mk_Point(-point.x, -point.y) }
            ))
    )

    private val diehard = mk_Population(
        mk_Point(0, 1), mk_Point(1, 1), mk_Point(1, 0), mk_Point(0, 5),
        mk_Point(0, 6), mk_Point(0, 7), mk_Point(2, 6)
    )

    private val glider = mk_Population(
        mk_Point(1, 0), mk_Point(2, 0), mk_Point(3, 0),
        mk_Point(3, 1), mk_Point(2, 2)
    )


    private val offset: (Population, integer, integer) -> Population = function(
        command = { pop: Population, dx: integer, dy: integer ->
            as_Population(
                set(pop) { point -> mk_Point(point.x + dx, point.y + dy) }
            )
        }
    )

    private val isOffset: (Population, Population, nat1) -> bool = function(
        command = { pop1: Population, pop2: Population, max: nat1 ->
            val maxRange = max.toInteger()
            exists(-maxRange..maxRange) { dx ->
                exists(-maxRange..maxRange) { dy ->
                    (dx != 0L || dy != 0L) && offset(pop1, dx, dy) == pop2
                }
            }
        }
    )

    private val periodN: (Population, nat1) -> bool = function(
        command = { pop: Population, n: nat1 ->
            generations(n, pop) == pop
        }
    )

    private val periodNP: (Population, nat1) -> bool = function(
        command = { pop: Population, n: nat1 ->
            set(1uL..n, filter = { periodN(pop, it) }) { it } == mk_Set(n)
        }
    )

    private val disappearN: (Population, nat1) -> bool = function(
        command = { pop: Population, n: nat1 ->
            generations(n, pop).isEmpty()
        }
    )

    private val disappearNP: (Population, nat1) -> bool = function(
        command = { pop: Population, n: nat1 ->
            set(1uL..n, filter = { disappearN(pop, it) }) { it } == mk_Set(n)
        }
    )

    private val gliderN: (Population, nat1, nat1) -> bool = function(
        command = { pop: Population, n: nat1, max: nat1 ->
            isOffset(pop, generations(n, pop), max)
        },
        pre = { pop, _, _ -> pop.card > 0uL }
    )

    private val gliderNP: (Population, nat1, nat1) -> bool = function(
        command = { pop: Population, n: nat1, max: nat1 ->
            set(1uL..n, filter = { gliderN(pop, it, max) }) { it } == mk_Set(n)
        }
    )


    @Test
    // They should not allow these 0s to be input, as they should be a nat1 number
    @Ignore
    fun nat1InputZeroTests() {
        assertFailsWith<PreconditionFailure> { generations(0uL, block) }
        assertFailsWith<PreconditionFailure> { periodN(block, 0uL) }
        assertFailsWith<PreconditionFailure> { disappearN(block, 0uL) }
        assertFailsWith<PreconditionFailure> { disappearNP(block, 0uL) }
        assertFailsWith<PreconditionFailure> { gliderN(block, 0uL, 1uL) }
        assertFailsWith<PreconditionFailure> { gliderNP(block, 0uL, 1uL) }
        assertFailsWith<PreconditionFailure> { gliderN(block, 1uL, 0uL) }
        assertFailsWith<PreconditionFailure> { gliderNP(block, 1uL, 0uL) }
        assertFailsWith<PreconditionFailure> { isOffset(block, block, 0uL) }
    }

    @Test
    fun around() {
        assertEquals(
            mk_Population(
                mk_Point(1, 0), mk_Point(1, 1), mk_Point(0, 1), mk_Point(-1, 1),
                mk_Point(-1, 0), mk_Point(-1, -1), mk_Point(0, -1), mk_Point(1, -1)
            ),
            around(mk_Point(0, 0))
        )
        assertEquals(true, mk_Point(0, 0) !in around(mk_Point(0, 0)))
    }

    @Test
    fun neighbourCount() {
        assertEquals(3uL, neighbourCount(block, mk_Point(0, 0)))
        assertEquals(0uL, neighbourCount(mk_Population(), mk_Point(0, 0)))
        assertEquals(8uL, neighbourCount(around(mk_Point(0, 0)), mk_Point(0, 0)))
    }

    @Test
    fun newCells() {
        assertEquals(mk_Population(mk_Point(0, 0)), newCells(block - mk_Point(0, 0)))
        assertEquals(mk_Population(), newCells(block))
        assertEquals(mk_Population(mk_Point(0, 1), mk_Point(-1, -2), mk_Point(1, -1), mk_Point(-2, 0)), newCells(toad))
    }

    @Test
    fun deadCells() {
        assertEquals(mk_Population(mk_Point(1, 0), mk_Point(-1, 0)), deadCells(blinker))
        assertEquals(mk_Population(), deadCells(block))
        assertEquals(mk_Population(), deadCells(mk_Population()))
        assertEquals(
            mk_Population(mk_Point(0, 0), mk_Point(2, 0), mk_Point(-2, 0)),
            deadCells(mk_Population(mk_Point(0, 0), mk_Point(2, 0), mk_Point(-2, 0)))
        )
    }

    @Test
    fun generation() {
        assertEquals(block, generation(block))
        assertEquals(mk_Population(mk_Point(0, 1), mk_Point(0, 0), mk_Point(0, -1)), generation(blinker))
        assertEquals(mk_Population(), generation(mk_Population()))
    }

    @Test
    fun generations() {
        assertEquals(
            mk_Population(),
            generations(
                4uL, mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                )
            )
        )
        assertEquals(block, generations(1uL, block))
        assertEquals(block, generations(50uL, block))
        assertEquals(toad, generations(4uL, toad))
    }

    @Test
    fun offset() {
        assertEquals(
            mk_Population(mk_Point(0, 0), mk_Point(1, 0), mk_Point(0, -1), mk_Point(1, -1)),
            offset(block, 1, 0)
        )
        assertEquals(mk_Population(), offset(mk_Population(), 3, 2))
        assertEquals(
            mk_Population(mk_Point(9, 10), mk_Point(10, 10), mk_Point(9, 9), mk_Point(10, 9)),
            offset(block, 10, 10)
        )
    }

    @Test
    fun isOffset() {
        assertEquals(
            true, isOffset(
                blinker, mk_Population(mk_Point(0, 1), mk_Point(1, 1), mk_Point(2, 1)), 2uL
            )
        )
        assertEquals(true, isOffset(mk_Population(), mk_Population(), 1uL))
        assertEquals(false, isOffset(block, block, 5uL))
        assertEquals(false, isOffset(blinker, mk_Population(), 2uL))
        assertEquals(
            false,
            isOffset(
                block, mk_Population(
                    mk_Point(2, 0), mk_Point(3, 0),
                    mk_Point(2, -1), mk_Point(3, -1)
                ), 2uL
            )
        )
        assertEquals(
            true,
            isOffset(
                block, mk_Population(
                    mk_Point(2, 0), mk_Point(3, 0),
                    mk_Point(2, -1), mk_Point(3, -1)
                ), 3uL
            )
        )
    }

    @Test
    fun periodN_NP() {
        assertEquals(true, periodN(mk_Population(), 1uL))
        assertEquals(true, periodN(block, 1uL))
        assertEquals(true, periodN(block, 5uL))
        assertEquals(true, periodN(pulsar, 15uL))
        assertEquals(true, periodN(pulsar, 3uL))
        assertEquals(false, periodN(pulsar, 2uL))
        assertEquals(false, periodN(blinker, 1uL))

        assertEquals(true, periodNP(mk_Population(), 1uL))
        assertEquals(true, periodNP(block, 1uL))
        assertEquals(false, periodNP(block, 5uL))
        assertEquals(false, periodNP(pulsar, 15uL))
        assertEquals(true, periodNP(pulsar, 3uL))
        assertEquals(false, periodNP(pulsar, 2uL))
        assertEquals(false, periodNP(blinker, 1uL))
    }

    @Test
    fun gliderN_NP() {
        assertEquals(false, gliderN(block, 1uL, 1uL))
        assertEquals(true, gliderN(glider, 4uL, 1uL))
        assertEquals(true, gliderN(glider, 8uL, 2uL))
        assertEquals(false, gliderN(glider, 8uL, 1uL))
        assertEquals(false, gliderN(glider, 3uL, 1uL))
        assertFailsWith<PreconditionFailure> { gliderN(mk_Population(), 1uL, 1uL) }

        assertEquals(false, gliderNP(block, 1uL, 1uL))
        assertEquals(true, gliderNP(glider, 4uL, 1uL))
        assertEquals(false, gliderNP(glider, 8uL, 2uL))
        assertEquals(false, gliderNP(glider, 8uL, 1uL))
        assertEquals(false, gliderNP(glider, 3uL, 1uL))
        assertFailsWith<PreconditionFailure> { gliderNP(mk_Population(), 1uL, 1uL) }
    }


    @Test
    fun disappearN_NP() {
        assertEquals(
            false,
            disappearN(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 2uL
            )
        )
        assertEquals(
            true,
            disappearN(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 4uL
            )
        )
        assertEquals(
            true,
            disappearN(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 5uL
            )
        )

        assertEquals(
            false,
            disappearNP(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 2uL
            )
        )
        assertEquals(
            true,
            disappearNP(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 4uL
            )
        )
        assertEquals(
            false,
            disappearNP(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 5uL
            )
        )

        assertEquals(false, disappearN(diehard, 10uL))
        assertEquals(false, disappearN(diehard, 129uL))
        assertEquals(true, disappearN(diehard, 130uL))
        assertEquals(true, disappearN(diehard, 131uL))

        assertEquals(false, disappearNP(diehard, 10uL))
        assertEquals(false, disappearNP(diehard, 129uL))
        assertEquals(true, disappearNP(diehard, 130uL))
        assertEquals(false, disappearNP(diehard, 131uL))

        assertEquals(true, disappearN(mk_Population(), 1uL))

        assertEquals(true, disappearNP(mk_Population(), 1uL))

    }

    @Test
    fun generalTests() {
        assertEquals(true, periodNP(block, 1uL))
        assertEquals(true, periodNP(blinker, 2uL))
        assertEquals(true, periodNP(toad, 2uL))
        assertEquals(true, periodNP(beacon, 2uL))
        assertEquals(true, periodNP(pulsar, 3uL))
        assertEquals(true, gliderNP(glider, 4uL, 1uL))
        assertEquals(true, disappearNP(diehard, 130uL))
    }
}