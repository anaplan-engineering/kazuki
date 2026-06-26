package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.Concrete_Module.mk_Concrete
import kotlin.test.Test
import kotlin.test.assertEquals


class TestAbstractionAkaHide {

    @Test
    fun basic() {
        val old = mk_Concrete(2, 10, 1)
        val new = old.functions.addToA(2)
        assertEquals(new.a, 4L)
    }
}