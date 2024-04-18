package com.anaplan.engineering.kazuki.toolkit.examples

import com.anaplan.engineering.kazuki.core.mk_
import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.toolkit.examples.Stack_Module.is_Stack
import com.anaplan.engineering.kazuki.toolkit.examples.Stack_Module.mk_Stack
import kotlin.test.Test
import kotlin.test.assertEquals

class TestStack {

    @Test
    fun testPush() {
        assertEquals(mk_Stack(1), Stack.Functions<Int>().push(mk_Stack(), 1))
        assertEquals(mk_Stack(2, 1), Stack.Functions<Int>().push(mk_Stack(1), 2))
    }

    @Test
    fun testPop() {
        assertEquals( mk_(2, mk_Stack(1)), Stack.Functions<Int>().pop(mk_Stack(2, 1)))
        assertEquals(mk_(1, mk_Stack()), Stack.Functions<Int>().pop(mk_Stack(1)))
    }

    @Test
    fun noDuplicatesInvalid() {
        assertEquals(false, is_Stack(mk_Seq(1, 1)))
    }
}