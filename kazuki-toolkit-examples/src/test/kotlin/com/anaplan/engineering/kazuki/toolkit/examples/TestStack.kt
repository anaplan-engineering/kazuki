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
        assertEquals(mk_Stack(1), mk_Stack<Int>().functions.push(1))
        assertEquals(mk_Stack(2, 1), mk_Stack(1).functions.push(2))
    }

    @Test
    fun testPop() {
        assertEquals( mk_(2, mk_Stack(1)), mk_Stack(2, 1).functions.pop())
        assertEquals(mk_(1, mk_Stack()), mk_Stack(1).functions.pop())
    }

    @Test
    fun noDuplicatesInvalid() {
        assertEquals(false, is_Stack(mk_Seq(1, 1)))
    }
}