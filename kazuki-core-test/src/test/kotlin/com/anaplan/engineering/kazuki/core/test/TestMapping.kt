package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMapping {

    @Test
    fun card() {
        assertEquals(0, mk_Mapping<Int, Int>().card)
        assertEquals(1, mk_Mapping(mk_(1, 1)).card)
        assertEquals(2, mk_Mapping(mk_(1, 1), mk_(2, 1)).card)
        // TODO - should be disallowed?
        assertEquals(1, mk_Mapping(mk_(1, 1), mk_(1, 2)).card)
    }

    @Test
    fun dom() {
        assertEquals(mk_Set(), mk_Mapping<Int, Int>().dom)
        assertEquals(mk_Set(1), mk_Mapping(mk_(1, 1)).dom)
        assertEquals(mk_Set(1, 2), mk_Mapping(mk_(1, 1), mk_(2, 1)).dom)
        assertEquals(mk_Set(1, 2), mk_Mapping(mk_(1, 1), mk_(2, 2)).dom)
    }

    @Test
    fun rng() {
        assertEquals(mk_Set(), mk_Mapping<Int, Int>().rng)
        assertEquals(mk_Set(1), mk_Mapping(mk_(1, 1)).rng)
        assertEquals(mk_Set(1), mk_Mapping(mk_(1, 1), mk_(2, 1)).rng)
        assertEquals(mk_Set(1, 2), mk_Mapping(mk_(1, 1), mk_(2, 2)).rng)
    }

    @Test
    fun domRestrictTo() {
        assertEquals(mk_Mapping(), mk_Mapping<Int, Int>() domRestrictTo mk_Set())
        assertEquals(mk_Mapping(), mk_Mapping<Int, Int>() domRestrictTo mk_Set(1))
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping(mk_(1, 2)) domRestrictTo mk_Set(1))
        assertEquals(mk_Mapping(), mk_Mapping(mk_(1, 2)) domRestrictTo mk_Set())
        assertEquals(mk_Mapping(), mk_Mapping(mk_(1, 2)) domRestrictTo mk_Set(2))
        assertEquals(
            mk_Mapping(mk_(1, 2), mk_(3, 4)),
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domRestrictTo mk_Set(1, 3)
        )
        assertEquals(
            mk_Mapping(mk_(3, 4)),
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domRestrictTo mk_Set(4, 3)
        )
    }

    @Test
    fun domSubtract() {
        assertEquals(mk_Mapping(), mk_Mapping<Int, Int>() domSubtract mk_Set())
        assertEquals(mk_Mapping(), mk_Mapping<Int, Int>() domSubtract mk_Set(1))
        assertEquals(mk_Mapping(), mk_Mapping(mk_(1, 2)) domSubtract mk_Set(1))
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping(mk_(1, 2)) domSubtract mk_Set())
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping(mk_(1, 2)) domSubtract mk_Set(2))
        assertEquals(
            mk_Mapping(mk_(2, 3)),
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domSubtract mk_Set(1, 3)
        )
        assertEquals(
            mk_Mapping(mk_(1, 2), mk_(2, 3)),
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4)) domSubtract mk_Set(3)
        )
    }

    @Test
    fun rngRestrictTo() {
        assertEquals(mk_Mapping(), mk_Mapping<Int, Int>() rngRestrictTo mk_Set())
        assertEquals(mk_Mapping(), mk_Mapping<Int, Int>() rngRestrictTo mk_Set(1))
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping(mk_(1, 2)) rngRestrictTo mk_Set(2))
        assertEquals(mk_Mapping(), mk_Mapping(mk_(1, 2)) rngRestrictTo mk_Set())
        assertEquals(mk_Mapping(), mk_Mapping(mk_(1, 2)) rngRestrictTo mk_Set(1))
        assertEquals(
            mk_Mapping(mk_(1, 2), mk_(3, 4)),
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngRestrictTo mk_Set(2, 4)
        )
        assertEquals(
            mk_Mapping(mk_(3, 4)),
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngRestrictTo mk_Set(4, 6)
        )
    }

    @Test
    fun rngSubtract() {
        assertEquals(mk_Mapping(), mk_Mapping<Int, Int>() rngSubtract mk_Set())
        assertEquals(mk_Mapping(), mk_Mapping<Int, Int>() rngSubtract mk_Set(1))
        assertEquals(mk_Mapping(), mk_Mapping(mk_(1, 2)) rngSubtract mk_Set(2))
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping(mk_(1, 2)) rngSubtract mk_Set())
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping(mk_(1, 2)) rngSubtract mk_Set(1))
        assertEquals(
            mk_Mapping(mk_(2, 3)),
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngSubtract mk_Set(2, 4)
        )
        assertEquals(
            mk_Mapping(mk_(1, 2), mk_(2, 3)),
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4)) rngSubtract mk_Set(4, 6)
        )
    }

    @Test
    fun in_pos() {
        assertEquals(false, mk_(1, 1) in mk_Mapping<Int, Int>())
        assertEquals(true, mk_(1, 1) in mk_Mapping(mk_(1, 1)))
        assertEquals(false, mk_(1, 2) in mk_Mapping(mk_(1, 1), mk_(2, 1)))
        assertEquals(true, mk_(2, 1) in mk_Mapping(mk_(1, 1), mk_(2, 1)))
    }

    @Test
    fun in_neg() {
        assertEquals(true, mk_(1, 1) !in mk_Mapping<Int, Int>())
        assertEquals(false, mk_(1, 1) !in mk_Mapping(mk_(1, 1)))
        assertEquals(true, mk_(1, 2) !in mk_Mapping(mk_(1, 1), mk_(2, 1)))
        assertEquals(false, mk_(2, 1) !in mk_Mapping(mk_(1, 1), mk_(2, 1)))
    }

    @Test
    fun equals() {
        assertEquals(mk_Mapping<Int, Int>(), mk_Mapping())
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping(mk_(1, 2)))
        assertEquals(mk_Mapping(mk_(1, 2), mk_(2, 3)), mk_Mapping(mk_(2, 3), mk_(1, 2)))
    }

    @Test
    fun plus_singular_ok() {
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping<Int, Int>() + mk_(1, 2))
        assertEquals(mk_Mapping(mk_(1, 2), mk_(2, 3)), mk_Mapping(mk_(1, 2)) + mk_(2, 3))
    }

    @Test
    fun plus_singular_overlap() {
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping(mk_(1, 2)) + mk_(1, 2))
    }

    @Test(expected = PreconditionFailure::class)
    fun plus_singular_conflict() {
        mk_Mapping(mk_(1, 2)) + mk_(1, 3)
    }

    @Test
    fun plus_plural_ok() {
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping<Int, Int>() + mk_Mapping(mk_(1, 2)))
        assertEquals(mk_Mapping(mk_(1, 2), mk_(2, 3)), mk_Mapping(mk_(1, 2)) + mk_Mapping(mk_(2, 3)))
        assertEquals(
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4), mk_(5, 6)),
            mk_Mapping(mk_(1, 2), mk_(3, 4)) + mk_Mapping(mk_(2, 3), mk_(5, 6))
        )
    }

    @Test
    fun plus_plural_intersection() {
        assertEquals(
            mk_Mapping(mk_(1, 2), mk_(5, 6), mk_(3, 4)),
            mk_Mapping(mk_(1, 2), mk_(5, 6)) + mk_Mapping(mk_(3, 4), mk_(1, 2))
        )
    }

    @Test(expected = PreconditionFailure::class)
    fun plus_plural_conflict() {
        mk_Mapping(mk_(1, 2), mk_(5, 6)) + mk_Mapping(mk_(3, 4), mk_(1, 3))
    }

    @Test
    fun times_singular_ok() {
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping<Int, Int>() * mk_(1, 2))
        assertEquals(mk_Mapping(mk_(1, 2), mk_(2, 3)), mk_Mapping(mk_(1, 2)) * mk_(2, 3))
    }

    @Test
    fun times_singular_overlap() {
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping(mk_(1, 2)) * mk_(1, 2))
    }

    @Test
    fun times_singular_conflict() {
        assertEquals(mk_Mapping(mk_(1, 3)), mk_Mapping(mk_(1, 2)) * mk_(1, 3))
    }

    @Test
    fun times_plural_ok() {
        assertEquals(mk_Mapping(mk_(1, 2)), mk_Mapping<Int, Int>() * mk_Mapping(mk_(1, 2)))
        assertEquals(mk_Mapping(mk_(1, 2), mk_(2, 3)), mk_Mapping(mk_(1, 2)) * mk_Mapping(mk_(2, 3)))
        assertEquals(
            mk_Mapping(mk_(1, 2), mk_(2, 3), mk_(3, 4), mk_(5, 6)),
            mk_Mapping(mk_(1, 2), mk_(3, 4)) * mk_Mapping(mk_(2, 3), mk_(5, 6))
        )
    }

    @Test
    fun times_plural_intersection() {
        assertEquals(
            mk_Mapping(mk_(1, 2), mk_(5, 6), mk_(3, 4)),
            mk_Mapping(mk_(1, 2), mk_(5, 6)) * mk_Mapping(mk_(3, 4), mk_(1, 2))
        )
    }

    @Test
    fun times_plural_conflict() {
        assertEquals(
            mk_Mapping(mk_(1, 3), mk_(5, 6), mk_(3, 4)),
            mk_Mapping(mk_(1, 2), mk_(5, 6)) * mk_Mapping(mk_(3, 4), mk_(1, 3))
        )
    }
}