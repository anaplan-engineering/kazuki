package com.anaplan.engineering.kazuki.adt

import com.anaplan.engineering.kazuki.adt.A_Module.is_A
import com.anaplan.engineering.kazuki.adt.B_Module.as_B
import com.anaplan.engineering.kazuki.adt.B_Module.mk_B
import com.anaplan.engineering.kazuki.adt.B_Module.pretty
import com.anaplan.engineering.kazuki.adt.C_Module.as_C
import com.anaplan.engineering.kazuki.adt.C_Module.mk_C
import com.anaplan.engineering.kazuki.adt.C_Module.pretty
import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.nat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Test {

    @Test
    fun one() {
        val b = mk_B(10uL)
        val c = mk_C(10uL)

        assertEquals<A>(b,c)
        assertTrue(is_A(b))
        assertTrue(is_A(c))

        val b2 = as_B(c)
        val c2 = as_C(b)
        println("B=" + b2.pretty())
        println("C=" + c2.pretty())

        assertEquals<A>(b2, c2)
    }
}