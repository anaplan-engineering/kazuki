package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.MapExtension_Module.mk_MapExtension
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMapExtension {

    @Test
    fun card() {
        assertEquals(0, mk_MapExtension<Int, Int>().card)
        assertEquals(1, mk_MapExtension(mk_(1, 1)).card)
        assertEquals(2, mk_MapExtension(mk_(1, 1), mk_(2, 1)).card)
        // TODO - should be disallowed?
        assertEquals(1, mk_MapExtension(mk_(1, 1), mk_(1, 2)).card)
    }

    @Test
    fun dom() {
        assertEquals(mk_Set(), mk_MapExtension<Int, Int>().dom())
        assertEquals(mk_Set(1), mk_MapExtension(mk_(1, 1)).dom())
        assertEquals(mk_Set(1, 2), mk_MapExtension(mk_(1, 1), mk_(2, 1)).dom())
        assertEquals(mk_Set(1, 2), mk_MapExtension(mk_(1, 1), mk_(2, 2)).dom())
    }

    @Test
    fun rng() {
        assertEquals(mk_Set(), mk_MapExtension<Int, Int>().rng())
        assertEquals(mk_Set(1), mk_MapExtension(mk_(1, 1)).rng())
        assertEquals(mk_Set(1), mk_MapExtension(mk_(1, 1), mk_(2, 1)).rng())
        assertEquals(mk_Set(1, 2), mk_MapExtension(mk_(1, 1), mk_(2, 2)).rng())
    }

    @Test
    fun maplets() {
        assertEquals(mk_Set(), mk_MapExtension<Int, Int>().toRelation())
        assertEquals(mk_Set(mk_(1, 1)), mk_MapExtension(mk_(1, 1)).toRelation())
        assertEquals(mk_Set(mk_(1, 1), mk_(2, 1)), mk_MapExtension(mk_(1, 1), mk_(2, 1)).toRelation())
        assertEquals(mk_Set(mk_(1, 1), mk_(2, 2)), mk_MapExtension(mk_(1, 1), mk_(2, 2)).toRelation())
    }

    @Test
    fun domRestrictTo() {
        assertEquals(mk_MapExtension(), mk_MapExtension<Int, Int>() domRestrictTo mk_Set())
        assertEquals(mk_MapExtension(), mk_MapExtension<Int, Int>() domRestrictTo mk_Set(1))
        assertEquals(mk_MapExtension(mk_(1, 2)), mk_MapExtension(mk_(1, 2)) domRestrictTo mk_Set(1))
        assertEquals(mk_MapExtension(), mk_MapExtension(mk_(1, 2)) domRestrictTo mk_Set())
        assertEquals(mk_MapExtension(), mk_MapExtension(mk_(1, 2)) domRestrictTo mk_Set(2))
        assertEquals(
            mk_MapExtension(mk_(1, 2), mk_(3, 4)),
            mk_MapExtension(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domRestrictTo mk_Set(1, 3)
        )
        assertEquals(
            mk_MapExtension(mk_(3, 4)),
            mk_MapExtension(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domRestrictTo mk_Set(4, 3)
        )
    }

    @Test
    fun domSubtract() {
        assertEquals(mk_MapExtension(), mk_MapExtension<Int, Int>() domSubtract mk_Set())
        assertEquals(mk_MapExtension(), mk_MapExtension<Int, Int>() domSubtract mk_Set(1))
        assertEquals(mk_MapExtension(), mk_MapExtension(mk_(1, 2)) domSubtract mk_Set(1))
        assertEquals(mk_MapExtension(mk_(1, 2)), mk_MapExtension(mk_(1, 2)) domSubtract mk_Set())
        assertEquals(mk_MapExtension(mk_(1, 2)), mk_MapExtension(mk_(1, 2)) domSubtract mk_Set(2))
        assertEquals(
            mk_MapExtension(mk_(2, 3)),
            mk_MapExtension(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domSubtract mk_Set(1, 3)
        )
        assertEquals(
            mk_MapExtension(mk_(1, 2), mk_(2, 3)),
            mk_MapExtension(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domSubtract mk_Set(3)
        )
    }

    @Test
    fun rngRestrictTo() {
        assertEquals(mk_MapExtension(), mk_MapExtension<Int, Int>() rngRestrictTo mk_Set())
        assertEquals(mk_MapExtension(), mk_MapExtension<Int, Int>() rngRestrictTo mk_Set(1))
        assertEquals(mk_MapExtension(mk_(1, 2)), mk_MapExtension(mk_(1, 2)) rngRestrictTo mk_Set(2))
        assertEquals(mk_MapExtension(), mk_MapExtension(mk_(1, 2)) rngRestrictTo mk_Set())
        assertEquals(mk_MapExtension(), mk_MapExtension(mk_(1, 2)) rngRestrictTo mk_Set(1))
        assertEquals(
            mk_MapExtension(mk_(1, 2), mk_(3, 4)),
            mk_MapExtension(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngRestrictTo mk_Set(2, 4)
        )
        assertEquals(
            mk_MapExtension(mk_(3, 4)),
            mk_MapExtension(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngRestrictTo mk_Set(4, 6)
        )
    }

    @Test
    fun rngSubtract() {
        assertEquals(mk_MapExtension(), mk_MapExtension<Int, Int>() rngSubtract mk_Set())
        assertEquals(mk_MapExtension(), mk_MapExtension<Int, Int>() rngSubtract mk_Set(1))
        assertEquals(mk_MapExtension(), mk_MapExtension(mk_(1, 2)) rngSubtract mk_Set(2))
        assertEquals(mk_MapExtension(mk_(1, 2)), mk_MapExtension(mk_(1, 2)) rngSubtract mk_Set())
        assertEquals(mk_MapExtension(mk_(1, 2)), mk_MapExtension(mk_(1, 2)) rngSubtract mk_Set(1))
        assertEquals(
            mk_MapExtension(mk_(2, 3)),
            mk_MapExtension(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngSubtract mk_Set(2, 4)
        )
        assertEquals(
            mk_MapExtension(mk_(1, 2), mk_(2, 3)),
            mk_MapExtension(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngSubtract mk_Set(4, 6)
        )
    }

    @Test
    fun in_pos() {
        assertEquals(false, mk_(1, 1) in mk_MapExtension<Int, Int>())
        assertEquals(true, mk_(1, 1) in mk_MapExtension(mk_(1, 1)))
        assertEquals(false, mk_(1, 2) in mk_MapExtension(mk_(1, 1), mk_(2, 1)))
        assertEquals(true, mk_(2, 1) in mk_MapExtension(mk_(1, 1), mk_(2, 1)))
    }

    @Test
    fun in_neg() {
        assertEquals(true, mk_(1, 1) !in mk_MapExtension<Int, Int>())
        assertEquals(false, mk_(1, 1) !in mk_MapExtension(mk_(1, 1)))
        assertEquals(true, mk_(1, 2) !in mk_MapExtension(mk_(1, 1), mk_(2, 1)))
        assertEquals(false, mk_(2, 1) !in mk_MapExtension(mk_(1, 1), mk_(2, 1)))
    }
}