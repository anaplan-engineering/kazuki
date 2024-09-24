package com.anaplan.engineering.kazuki.gameoflife

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Point
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Population

@Module
object Conway {

    private const val Generate = 3 // Number of neighbours to cause generation
    private val Survive = mk_Set(2, 3) // Numbers of neighbours to ensure survival, else death
    private const val maxNeigh = 8

    interface Point {
        val x: int
        val y: int
    }

    interface Population : Set<Point>

    val around: (Point) -> Population = function(
        command = { p: Point ->
            val A: Set<int> = mk_Set(-1, 0, 1) // Adjacencies
            mk_Population(dunion(
                set(A) { x: int -> set(A) { y: int -> mk_Point(p.x + x, p.y + y) } }
            ).minus(p))
        },
        post = { _, result -> result.card <= maxNeigh }
    )

    val neighbourCount: (Population, Point) -> int = function(
        command = { pop: Population, p: Point ->
            mk_Population(around(p) inter pop).card
        },
        post = { _, _, result -> result <= maxNeigh },
    )

    val newCells: (Population) -> Population = function(
        command = { pop: Population ->
            mk_Population(dunion(
                set(pop) { p: Point ->
                    set(around(p) minus pop, filter = {
                        neighbourCount(pop, it) == Generate
                    }) { q -> q }
                }
            ))
        },
        post = { pop, result -> (result inter pop).isEmpty() }
    )

    val deadCells: (Population) -> Population = function(
        command = { pop: Population ->
            mk_Population(dunion(
                set(pop, filter = { neighbourCount(pop, it) !in Survive }) { it }
            ))
        },
        post = { pop, result -> (result inter pop) == result }
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
                if (n == 1) {
                    newP
                } else {
                    generations(n - 1, newP)
                }
            },
            measure = { n, _ -> n },
        )
    }

    internal val offset: (Population, int, int) -> Population = function(
        command = { pop: Population, dx: int, dy: int ->
            mk_Population(dunion(
                set(pop) { point -> mk_Point(point.x + dx, point.y + dy) }
            ))
        }
    )

    internal val isOffset: (Population, Population, nat1) -> bool = function(
        command = { pop1: Population, pop2: Population, max: nat1 ->
            dunion(
                set(-max..max) { dx ->
                    set(-max..max) { dy ->
                        (dx != 0 || dy != 0) && offset(pop1, dx, dy) == pop2
                    }
                }
            ).any { it }
        }
    )

    internal val periodN: (Population, nat1) -> bool = function(
        command = { pop: Population, n: nat1 ->
            generations(n, pop) == pop
        }
    )

    val periodNP: (Population, nat1) -> bool = function(
        command = { pop: Population, n: nat1 ->
            dunion(
                set(1..n, filter = { periodN(pop, it) }) { it }
            ) == mk_Set(n)
        }
    )

    internal val disappearN: (Population, nat1) -> bool = function(
        command = { pop: Population, n: nat1 ->
            generations(n, pop).isEmpty()
        }
    )

    val disappearNP: (Population, nat1) -> bool = function(
        command = { pop: Population, n: nat1 ->
            dunion(
                set(1..n, filter = { disappearN(pop, it) }) { it }
            ) == mk_Set(n)
        }
    )

    internal val gliderN: (Population, nat1, nat1) -> bool = function(
        command = { pop: Population, n: nat1, max: nat1 ->
            isOffset(pop, generations(n, pop), max)
        },
        pre = { pop, _, _ -> pop.card > 0 }
    )

    val gliderNP: (Population, nat1, nat1) -> bool = function(
        command = { pop: Population, n: nat1, max: nat1 ->
            dunion(
                set(1..n, filter = { gliderN(pop, it, max) }) { it }
            ) == mk_Set(n)
        }
    )
}
