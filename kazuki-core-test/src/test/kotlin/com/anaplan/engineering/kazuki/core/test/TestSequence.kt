package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.core.Sequence1Extension_Module.mk_Sequence1Extension
import com.anaplan.engineering.kazuki.core.SequenceExtension_Module.mk_SequenceExtension
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test standard sequence operations on implementations of all mapping types
 */
@RunWith(Parameterized::class)
class TestSequence(
    private val allowsEmpty: Boolean,
    private val creator: (Collection<Int>) -> Sequence<Int>
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun creators(): Collection<Array<Any?>> =
            listOf(
                arrayOf(true, { m: Collection<Int> -> mk_Seq(*m.toTypedArray()) }),
                arrayOf(false, { m: Collection<Int> -> mk_Seq1(*m.toTypedArray()) }),
                arrayOf(true, { m: Collection<Int> -> mk_SequenceExtension(*m.toTypedArray()) }),
                arrayOf(false, { m: Collection<Int> -> mk_Sequence1Extension(*m.toTypedArray()) }),
            )
    }

    private fun create(vararg m: Int) = creator.invoke(m.toList())

    @Test
    fun len() {
        if (allowsEmpty) {
            assertEquals(0, create().len)
        }
        assertEquals(1, create(1).len)
        assertEquals(3, create(1, 2, 1).len)
    }

    @Test
    fun subseq() {
        if (allowsEmpty) {
            assertEquals(true, create() subseq create())
            assertEquals(true, create() subseq create(1))
            assertEquals(false, create(1) subseq create())
        }
        assertEquals(false, create(1) subseq create(2, 3, 4))
        assertEquals(true, create(1, 2) subseq create(1, 2, 3, 4))
        assertEquals(true, create(3, 4) subseq create(1, 2, 3, 4))
        assertEquals(false, create(4, 3) subseq create(1, 2, 3, 4))
        assertEquals(true, create(3, 4) subseq create(3, 4, 3, 4))
        assertEquals(true, create(4, 3) subseq create(3, 4, 3, 4))
        assertEquals(true, create(3, 3, 3) subseq create(3, 3, 3, 3))
        assertEquals(true, create(3, 3, 3, 3) subseq create(3, 3, 3, 3))
        assertEquals(false, create(3, 3, 3, 3) subseq create(3, 3, 3))
    }

    @Test
    fun insert() {
        if (allowsEmpty) {
            assertEquals(create(6), create().insert(6, 1))
        }
        assertEquals(create(6, 7, 8), create(7, 8).insert(6, 1))
        assertEquals(create(6, 7, 8), create(7, 8).insert(6, 1))
        assertEquals(create(6, 7, 8), create(7, 8).insert(6, 1))
        assertEquals(create(6, 7, 8), create(7, 8).insert(6, 1))
        assertEquals(create(7, 6, 8), create(7, 8).insert(6, 2))
        assertEquals(create(7, 8, 6), create(7, 8).insert(6, 3))
        causesPreconditionFailure { create(7, 8).insert(6, 0) }
        causesPreconditionFailure { create(7, 8).insert(6, 4) }
        assertEquals(create(7, 7, 8), create(7, 8).insert(7, 1))
    }
}