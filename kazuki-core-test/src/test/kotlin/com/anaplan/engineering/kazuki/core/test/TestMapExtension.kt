package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.MapExtension_Module.mk_MapExtension
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.core.mk_
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
}