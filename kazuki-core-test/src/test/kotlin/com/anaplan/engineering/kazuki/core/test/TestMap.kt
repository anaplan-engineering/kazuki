package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMap {

    @Test
    fun card() {
        assertEquals(0, mk_Map<Int, Int>().card)
        assertEquals(1, mk_Map(mk_(1, 1)).card)
        assertEquals(2, mk_Map(mk_(1, 1), mk_(2, 1)).card)
        // TODO - should be disallowed?
        assertEquals(1, mk_Map(mk_(1, 1), mk_(1, 2)).card)
    }
}