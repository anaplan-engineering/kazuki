package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(Parameterized::class)
class TestRelation(
    private val allowsEmpty: Boolean,
    private val creator: (Collection<Tuple2<*, *>>) -> Relation<*, *>
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun creators() =
            listOf(
                arrayOf(true, { m: Collection<Tuple2<Any, Any>> -> mk_Relation(*m.toTypedArray()) }),
//                arrayOf(false, { m: Collection<Tuple2<Any, Any>> -> mk_Relation1(*m.toTypedArray()) }),
            )
    }

    private fun create(vararg m: Tuple2<Any, Any>) = creator.invoke(m.toList())

    @Test
    fun inter() {
        if (allowsEmpty) {
            assertEquals(create(), create() inter create())
            assertEquals(create(), create(mk_(1, 1), mk_(2, 2), mk_(3, 3)) inter create())
            assertEquals(create(), create(mk_(1, 2), mk_(2, 1)) inter create(mk_(2, 2)))
        } else {
            // inter creates a set of the same type as the first input
            causesPreconditionFailure { create(mk_(1, 2)) inter create(mk_(2, 2)) }
        }
        assertEquals(create(mk_(2, 2)), create(mk_(1, 1), mk_(2, 2), mk_(3, 3)) inter create(mk_(2, 2)))
        assertEquals(create(mk_(2, 2)), create(mk_(1, 1), mk_(2, 2), mk_(3, 3)) inter create(mk_(2, 2), mk_(3, 4)))
        assertEquals(
            create(mk_(1, 1), mk_(2, 2), mk_(3, 3)),
            create(mk_(1, 1), mk_(2, 2), mk_(3, 3)) inter create(mk_(2, 2), mk_(3, 4), mk_(3, 3), mk_(1, 1))
        )
    }

}