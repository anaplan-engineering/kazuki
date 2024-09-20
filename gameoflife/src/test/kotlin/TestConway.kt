package com.anaplan.engineering.kazuki.gameoflife.test

import com.anaplan.engineering.kazuki.core.dunion
import com.anaplan.engineering.kazuki.core.set
import com.anaplan.engineering.kazuki.gameoflife.Conway.around
import com.anaplan.engineering.kazuki.gameoflife.Conway.disappearNP
import com.anaplan.engineering.kazuki.gameoflife.Conway.generation
import com.anaplan.engineering.kazuki.gameoflife.Conway.gliderNP
import com.anaplan.engineering.kazuki.gameoflife.Conway.newCells
import com.anaplan.engineering.kazuki.gameoflife.Conway.periodNP
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Point
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Population
import org.junit.Test
import kotlin.test.assertEquals


class TestConway {

    // TODO Add in general tests

    val block = mk_Population(mk_Point(0, 0), mk_Point(-1, 0), mk_Point(0, -1), mk_Point(-1, -1))

    val blinker = mk_Population(mk_Point(-1, 0), mk_Point(0, 0), mk_Point(1, 0))

    val toad = mk_Population(
        mk_Point(-1, 0),
        mk_Point(0, 0),
        mk_Point(1, 0),
        mk_Point(0, -1),
        mk_Point(-1, -1),
        mk_Point(-2, -1)
    )

    val beacon = mk_Population(
        mk_Point(-2, 0), mk_Point(-2, 1), mk_Point(-1, 1), mk_Point(0, -2),
        mk_Point(1, -2), mk_Point(1, -1)
    )

    private val pQuad = mk_Population(
        mk_Point(2, 1), mk_Point(3, 1), mk_Point(3, 2),
        mk_Point(1, 2), mk_Point(1, 3), mk_Point(2, 3), mk_Point(5, 2),
        mk_Point(5, 3), mk_Point(6, 3), mk_Point(7, 3), mk_Point(2, 5),
        mk_Point(3, 5), mk_Point(3, 6), mk_Point(3, 7)
    )

    val pulsar = mk_Population(dunion(
        set(pQuad) { point -> point },
        set(pQuad) { point -> mk_Point(-point.x, point.y) },
        set(pQuad) { point -> mk_Point(point.x, -point.y) },
        set(pQuad) { point -> mk_Point(-point.x, -point.y) }
    ))

    val diehard = mk_Population(
        mk_Point(0, 1), mk_Point(1, 1), mk_Point(1, 0), mk_Point(0, 5),
        mk_Point(0, 6), mk_Point(0, 7), mk_Point(2, 6)
    )

    val glider = mk_Population(
        mk_Point(1, 0), mk_Point(2, 0), mk_Point(3, 0),
        mk_Point(3, 1), mk_Point(2, 2)
    )

    val gosper_glider_gun = mk_Population(
        mk_Point(2, 0), mk_Point(2, 1), mk_Point(2, 2), mk_Point(3, 0),
        mk_Point(3, 1), mk_Point(3, 2), mk_Point(4, -1), mk_Point(4, 3), mk_Point(6, -2),
        mk_Point(6, -1), mk_Point(6, 3), mk_Point(6, 4), mk_Point(16, 1), mk_Point(16, 2),
        mk_Point(17, 1), mk_Point(17, 2), mk_Point(-1, -1), mk_Point(-2, -2), mk_Point(-2, -1),
        mk_Point(-2, 0), mk_Point(-3, -3), mk_Point(-3, 1), mk_Point(-4, -1), mk_Point(-5, -4),
        mk_Point(-5, 2), mk_Point(-6, -4), mk_Point(-6, 2), mk_Point(-7, -3), mk_Point(-7, 1),
        mk_Point(-8, -2), mk_Point(-8, -1), mk_Point(-8, 0), mk_Point(-17, -1), mk_Point(-17, 0),
        mk_Point(-18, -1), mk_Point(-18, 0)
    )

    @Test
    fun around() {
        assertEquals(
            mk_Population(
                mk_Point(1, 0), mk_Point(1, 1), mk_Point(0, 1), mk_Point(-1, 1),
                mk_Point(-1, 0), mk_Point(-1, -1), mk_Point(0, -1), mk_Point(1, -1)
            ),
            around(mk_Point(0, 0))
        )
    }

    @Test
    fun newCells() {
        assertEquals(
            mk_Population(mk_Point(0, -1), mk_Point(0, 1)),
            newCells(mk_Population(mk_Point(-1, 0), mk_Point(0, 0), mk_Point(1, 0)))
        )
    }

    @Test
    fun deadCells() {
//        assertEquals(mk_Population(mk_Point(-1, 0), deadCells(mk_Point(0,0))))
        assertEquals(
            mk_Population(mk_Point(-1, 0), mk_Point(1, 0)),
            newCells(mk_Population(mk_Point(-1, 0), mk_Point(0, 0), mk_Point(1, 0)))
        )
    }

    @Test
    fun generations() {
        assertEquals(
            mk_Population(mk_Point(0, -1), mk_Point(0, 0), mk_Point(0, 1)),
            generation(mk_Population(mk_Point(-1, 0), mk_Point(0, 0), mk_Point(1, 0)))
        )
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