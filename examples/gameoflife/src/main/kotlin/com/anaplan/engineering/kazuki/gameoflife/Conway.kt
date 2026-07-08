package com.anaplan.engineering.kazuki.gameoflife

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.as_Population
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Point

/**
 * A Kazuki specification inspired by https://github.com/overturetool/overturetool.github.io/blob/master/download/examples/VDMSL/ConwayGameLifeSL/index.md,
 * which was originally developed by Nick Battle, Peter Gorm Larsen and Claus Ballegaard Nielsen.
 */
@Module
object Conway {

    private const val Generate = 3uL // Number of neighbours to cause generation
    private val Survive = mk_Set(2uL, 3uL) // Numbers of neighbours to ensure survival, else death
    private const val MaxNeighbours = 8uL

    interface Point {
        val x: integer
        val y: integer
    }

    interface Population : Set<Point>

    val around: (Point) -> Population = function(
        command = { p: Point ->
            val A: Set<integer> = mk_Set(-1, 0, 1) // Adjacencies
            as_Population(
                dunion(
                set(A) { x: integer -> set(A) { y: integer -> mk_Point(p.x + x, p.y + y) } }
            ).minus(p))
        },
        postCommand = { _, result -> result.card <= MaxNeighbours }
    )

    val neighbourCount: (Population, Point) -> nat = function(
        command = { pop: Population, p: Point ->
            (around(p) inter pop).card
        },
        postCommand = { _, _, result -> result <= MaxNeighbours },
    )

    val newCells: (Population) -> Population = function(
        command = { pop: Population ->
            as_Population(
                dunion(
                set(pop) { p: Point ->
                    set(around(p) minus pop, filter = {
                        neighbourCount(pop, it) == Generate
                    }) { q -> q }
                }
            ))
        },
        postCommand = { pop, result -> (result inter pop).isEmpty() }
    )

    val deadCells: (Population) -> Population = function(
        command = { pop: Population ->
            as_Population(
                set(pop, filter = { neighbourCount(pop, it) !in Survive }) { it }
            )
        },
        postCommand = { pop, result -> (result inter pop) == result }
    )

    internal val generation: (Population) -> Population = function(
        command = { pop: Population ->
            (pop - deadCells(pop)) + newCells(pop)
        }
    )

    val generations: (nat1, Population) -> Population by lazy {
        function(
            command = { n: nat1, pop: Population ->
                val newP = generation(pop)
                if (n == 1uL) {
                    newP
                } else {
                    generations(n - 1u, newP)
                }
            },
            measure = { n, _ -> n },
        )
    }
}
