package com.anaplan.engineering.kazuki.adt

import com.anaplan.engineering.kazuki.core.InvariantFailure
import com.anaplan.engineering.kazuki.core.as_Seq
import com.anaplan.engineering.kazuki.core.as_Set
import com.anaplan.engineering.kazuki.core.mk_Seq
import com.anaplan.engineering.kazuki.core.nat1
import com.anaplan.engineering.kazuki.adt.Stack_Module.mk_Stack
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class TestStack {

    @Test
    fun testStackAPI() {
        val stack = mk_Stack<nat1>(mk_Seq(1u,2u,3u,3u))
        val seq = listOf<nat1>(1u, 2u, 3u)
        val elems = as_Set(seq)
        // Stack properties
        assertFalse(stack.properties.isEmpty)
        assertEquals(4u, stack.properties.size)
        assertEquals(elems, stack.properties.elems)
        // Stack API
        assertTrue(stack.functions.empty().properties.isEmpty)
        var top = stack.functions.top()
        val poppedS = stack.functions.pop()
        assertEquals(3u, top)
        assertEquals(mk_Stack<nat1>(as_Seq(seq)), poppedS)
        val pushedS = poppedS.functions.push(400u).functions.push(500u)
        assertEquals(5u, pushedS.properties.size)
        assertEquals(500u, pushedS.functions.top())
        assertEquals(400u, pushedS.functions.pop().functions.top())
        assertEquals(stack, pushedS.functions.pop().functions.pop().functions.push(3u))
        // Convenience extension constructor
        assertEquals(mk_Stack<nat1>(as_Seq(seq)), mk_Stack<nat1>(1u, 2u, 3u))
    }
}