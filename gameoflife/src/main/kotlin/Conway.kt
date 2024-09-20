package com.anaplan.engineering.kazuki.gameoflife

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Point
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Population


@Module
object Conway {

    const val Generate = 3 // Number of neighbours to cause generation
    val Survive = mk_Set(2, 3) // Numbers of neighbours to ensure survival, else death
    const val maxNeigh = 8

    interface Point {
        val x: int
        val y: int
    }

    interface Population : Set<Point>

    val around = function(
        command = { p: Point ->
            val A: Set<int> = mk_Set(-1, 0, 1) // Adjacencies
            dunion(
                set(A) { x: int -> set(A) { y: int -> mk_Point(p.x + x, p.y + y) } }
            ).minus(p)
        },
        post = { _, result -> result.card <= maxNeigh }
    )

    val neighbourCount = function(
        command = { pop: Population, p: Point ->
            mk_Set(
                around(p) inter pop
            ).card
        },
        post = { _, _, result -> result <= maxNeigh },
        pre = {_,_,_ -> mk_Population().card == 0}
    )

    val newCells: (Population) -> Population = function(
        command = { pop: Population ->
            mk_Population().add( dunion(
                set(pop) { p: Point ->
                    set(around(p) minus pop, filter = {
                        neighbourCount(pop, it) == Generate
                    }) { it }
                }))
            )
        },
        post = { pop, result -> (result inter pop).isEmpty() }
    )
/*
    val deadCells: (Population) -> Population = function(
        command = { pop: Population ->
            dunion(
                set(pop, filter = { neighbourCount(pop, it) in Survive })
                { it }
            )
        },
        post = { pop, result ->
            (result inter pop) == result
        }
    )
*/
    val generation = function(
        command = { pop: Population ->
            (pop minus deadCells(pop)) union newCells(pop)
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

    val offset = function(
        command = { pop: Population, dx: int, dy: int ->
            dunion(
                set(pop) { point -> mk_Point(point.x + dx, point.y + dy) }
            )
        }
    )

    val isOffset = function(
        command = { pop1: Population, pop2: Population, max: nat1 ->
            forall(-max..max) { dx ->
                forall(-max..max) { dy ->
                    ((dx != 0) or (dy != 0)) and (offset(pop1, dx, dy) == pop2)
                }
            }
        }
    )

    val periodN = function(
        command = { pop: Population, n: nat1 ->
            generations(n, pop) == pop
        }
    )

    val disappearN = function(
        command = { pop: Population, n: nat1 ->
            generations(n, pop).isEmpty()
        }
    )

    val gliderN = function(
        command = { pop: Population, n: nat1, max: nat1 ->
            isOffset(pop, generations(n, pop), max)
        }
    )

    val periodNP = function(
        command = { pop: Population, n: nat1 ->
            dunion(
                set(1..n, filter = { periodN(pop, it) }) { it }
            ) == mk_Set(n)
        }
    )

    val disappearNP = function(
        command = { pop: Population, n: nat1 ->
            dunion(
                set(1..n, filter = { disappearN(pop, it) }) { it }
            ) == mk_Set(n)
        }
    )

    val gliderNP = function(
        command = { pop: Population, n: nat1, max: nat1 ->
            dunion(
                set(1..n, filter = { gliderN(pop, it, max) }) { it }
            ) == mk_Set(n)
        }
    )
}
