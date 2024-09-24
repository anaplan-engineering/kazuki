package com.anaplan.engineering.kazuki.gameoflife

import com.anaplan.engineering.kazuki.core.PreconditionFailure
import com.anaplan.engineering.kazuki.core.dunion
import com.anaplan.engineering.kazuki.core.minus
import com.anaplan.engineering.kazuki.core.set
import com.anaplan.engineering.kazuki.gameoflife.Conway.around
import com.anaplan.engineering.kazuki.gameoflife.Conway.deadCells
import com.anaplan.engineering.kazuki.gameoflife.Conway.disappearN
import com.anaplan.engineering.kazuki.gameoflife.Conway.disappearNP
import com.anaplan.engineering.kazuki.gameoflife.Conway.generation
import com.anaplan.engineering.kazuki.gameoflife.Conway.generations
import com.anaplan.engineering.kazuki.gameoflife.Conway.gliderN
import com.anaplan.engineering.kazuki.gameoflife.Conway.gliderNP
import com.anaplan.engineering.kazuki.gameoflife.Conway.isOffset
import com.anaplan.engineering.kazuki.gameoflife.Conway.neighbourCount
import com.anaplan.engineering.kazuki.gameoflife.Conway.newCells
import com.anaplan.engineering.kazuki.gameoflife.Conway.offset
import com.anaplan.engineering.kazuki.gameoflife.Conway.periodN
import com.anaplan.engineering.kazuki.gameoflife.Conway.periodNP
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

    private val pulsar = mk_Population(
        dunion(
            set(pQuad) { point -> point },
            set(pQuad) { point -> mk_Point(-point.x, point.y) },
            set(pQuad) { point -> mk_Point(point.x, -point.y) },
            set(pQuad) { point -> mk_Point(-point.x, -point.y) }
        ))

    private val diehard = mk_Population(
        mk_Point(0, 1), mk_Point(1, 1), mk_Point(1, 0), mk_Point(0, 5),
        mk_Point(0, 6), mk_Point(0, 7), mk_Point(2, 6)
    )

    private val glider = mk_Population(
        mk_Point(1, 0), mk_Point(2, 0), mk_Point(3, 0),
        mk_Point(3, 1), mk_Point(2, 2)
    )

    @Test
    @Ignore
    fun preconditionTests() {
        // They should not allow these 0s to be input, as they should be a nat1 number
        assertFailsWith<PreconditionFailure> { generations(0, block) }
        assertFailsWith<PreconditionFailure> { periodN(block, 0) }
        assertFailsWith<PreconditionFailure> { disappearN(block, 0) }
        assertFailsWith<PreconditionFailure> { disappearNP(block, 0) }
        assertFailsWith<PreconditionFailure> { gliderN(block, 0, 1) }
        assertFailsWith<PreconditionFailure> { gliderNP(block, 0, 1) }
        assertFailsWith<PreconditionFailure> { gliderN(block, 1, 0) }
        assertFailsWith<PreconditionFailure> { gliderNP(block, 1, 0) }
        assertFailsWith<PreconditionFailure> { isOffset(block, block, 0) }
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
        assertEquals(3, neighbourCount(block, mk_Point(0, 0)))
        assertEquals(0, neighbourCount(mk_Population(), mk_Point(0, 0)))
        assertEquals(8, neighbourCount(around(mk_Point(0, 0)), mk_Point(0, 0)))
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
            mk_Population(), generations(
                4, mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                )
            )
        )
        assertEquals(block, generations(1, block))
        assertEquals(block, generations(500, block))
        assertEquals(toad, generations(4, toad))
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
                blinker, mk_Population(mk_Point(0, 1), mk_Point(1, 1), mk_Point(2, 1)), 2
            )
        )
        assertEquals(true, isOffset(mk_Population(), mk_Population(), 1))
        assertEquals(false, isOffset(block, block, 5))
        assertEquals(false, isOffset(blinker, mk_Population(), 2))
        assertEquals(
            false, isOffset(
                block, mk_Population(
                    mk_Point(2, 0), mk_Point(3, 0),
                    mk_Point(2, -1), mk_Point(3, -1)
                ), 2
            )
        )
        assertEquals(
            true, isOffset(
                block, mk_Population(
                    mk_Point(2, 0), mk_Point(3, 0),
                    mk_Point(2, -1), mk_Point(3, -1)
                ), 3
            )
        )
    }

    @Test
    fun periodN() {
        assertEquals(true, periodN(mk_Population(), 1))
        assertEquals(true, periodN(block, 1))
        assertEquals(true, periodN(block, 5))
        assertEquals(true, periodN(pulsar, 15))
        assertEquals(true, periodN(pulsar, 3))
        assertEquals(false, periodN(pulsar, 2))
        assertEquals(false, periodN(blinker, 1))
    }

    @Test
    fun periodNP() {
        assertEquals(true, periodNP(mk_Population(), 1))
        assertEquals(true, periodNP(block, 1))
        assertEquals(false, periodNP(block, 5))
        assertEquals(false, periodNP(pulsar, 15))
        assertEquals(true, periodNP(pulsar, 3))
        assertEquals(false, periodNP(pulsar, 2))
        assertEquals(false, periodNP(blinker, 1))
    }

    @Test
    fun gliderN() {
        assertEquals(false, gliderN(block, 1, 1))
        assertEquals(true, gliderN(glider, 4, 1))
        assertEquals(true, gliderN(glider, 8, 2))
        assertEquals(false, gliderN(glider, 8, 1))
        assertEquals(false, gliderN(glider, 3, 1))
        assertFailsWith<PreconditionFailure> { gliderN(mk_Population(), 1, 1) }
    }

    @Test
    fun gliderNP() {
        assertEquals(false, gliderNP(block, 1, 1))
        assertEquals(true, gliderNP(glider, 4, 1))
        assertEquals(false, gliderNP(glider, 8, 2))
        assertEquals(false, gliderNP(glider, 8, 1))
        assertEquals(false, gliderNP(glider, 3, 1))
        assertFailsWith<PreconditionFailure> { gliderNP(mk_Population(), 1, 1) }
    }


    @Test
    fun disappearN() {
        assertEquals(
            false, disappearN(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 2
            )
        )
        assertEquals(
            true, disappearN(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 4
            )
        )
        assertEquals(
            true, disappearN(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 5
            )
        )
        assertEquals(false, disappearN(diehard, 10))
        assertEquals(true, disappearN(diehard, 130))
        assertEquals(true, disappearN(diehard, 131))
        assertEquals(true, disappearN(mk_Population(), 1))
    }

    @Test
    fun disappearNP() {
        assertEquals(
            false, disappearNP(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 2
            )
        )
        assertEquals(
            true, disappearNP(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 4
            )
        )
        assertEquals(
            false, disappearNP(
                mk_Population(
                    mk_Point(-3, -3), mk_Point(-2, -2), mk_Point(-1, -1),
                    mk_Point(0, 0), mk_Point(1, 1), mk_Point(2, 2), mk_Point(3, 3)
                ), 5
            )
        )
        assertEquals(false, disappearNP(diehard, 10))
        assertEquals(true, disappearNP(diehard, 130))
        assertEquals(false, disappearNP(diehard, 131))
        assertEquals(true, disappearN(mk_Population(), 1))
    }


    @Test
    fun generalTests() {
        assertEquals(true, periodNP(block, 1))
        assertEquals(true, periodNP(blinker, 2))
        assertEquals(true, periodNP(toad, 2))
        assertEquals(true, periodNP(beacon, 2))
        assertEquals(true, periodNP(pulsar, 3))
        assertEquals(true, gliderNP(glider, 4, 1))
        assertEquals(true, disappearNP(diehard, 130))
    }
}