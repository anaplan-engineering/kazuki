package com.anaplan.engineering.kazuki.gameoflife.test

import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Point
import com.anaplan.engineering.kazuki.gameoflife.Conway_Module.mk_Population


class TestConway {

    // TODO Add in general tests

    val block = mk_Population(mk_Point(0, 0), mk_Point(-1, 0), mk_Point(0, -1), mk_Point(-1, -1))

    val blinker = mk_Population(mk_Point(-1, 0), mk_Point(0, 0), mk_Point(1, 0))

    val toad = blinker union mk_Population(mk_Point(0,-1), mk_Point(-1,-1), mk_Point(-2,-1))

    val beacon = mk_Population(mk_Point(-2,0), mk_Point(-2,1), mk_Point(-1,1), mk_Point(0,-2),
        mk_Point(1,-2), mk_Point(1,-1 ))

    // TODO work out the logic here
    val pulsar =

    val diehard = mk_Population(mk_Point(0,1),mk_Point(1,1),mk_Point(1,0),
        mk_Point(0,5),mk_Point(0,6),mk_Point(0,7),mk_Point(2,6))

    val glider = mk_Population(mk_Point(1,0), mk_Point(2,0), mk_Point(3,0), mk_Point(3,1), mk_Point(2,2))

    val gosper_glider_gun =

    /*
        PULSAR = let quadrant = { mk_Point(2,1), mk_Point(3,1), mk_Point(3,2),
            mk_Point(1,2), mk_Point(1,3), mk_Point(2,3),
            mk_Point(5,2), mk_Point(5,3), mk_Point(6,3), mk_Point(7,3),
            mk_Point(2,5), mk_Point(3,5), mk_Point(3,6), mk_Point(3,7) }
        in
        quadrant union
        { mk_Point(-x, y)| mk_Point(x, y) in set quadrant } union
        { mk_Point(x, -y)| mk_Point(x, y) in set quadrant } union
        { mk_Point(-x, -y)| mk_Point(x, y) in set quadrant };

        DIEHARD = {mk_Point(0,1),mk_Point(1,1),mk_Point(1,0),
            mk_Point(0,5),mk_Point(0,6),mk_Point(0,7),mk_Point(2,6)};

        GLIDER = { mk_Point(1,0), mk_Point(2,0), mk_Point(3,0), mk_Point(3,1), mk_Point(2,2) };

        GOSPER_GLIDER_GUN = { mk_Point(2,0), mk_Point(2,1), mk_Point(2,2), mk_Point(3,0), mk_Point(3,1),
            mk_Point(3,2), mk_Point(4,-1), mk_Point(4,3), mk_Point(6,-2), mk_Point(6,-1),
            mk_Point(6,3), mk_Point(6,4), mk_Point(16,1), mk_Point(16,2), mk_Point(17,1),
            mk_Point(17,2), mk_Point(-1,-1), mk_Point(-2,-2), mk_Point(-2,-1), mk_Point(-2,0),
            mk_Point(-3,-3), mk_Point(-3,1), mk_Point(-4,-1), mk_Point(-5,-4), mk_Point(-5,2),
            mk_Point(-6,-4), mk_Point(-6,2), mk_Point(-7,-3), mk_Point(-7,1), mk_Point(-8,-2),
            mk_Point(-8,-1), mk_Point(-8,0), mk_Point(-17,-1), mk_Point(-17,0), mk_Point(-18,-1),
            mk_Point(-18,0)};

    */
}