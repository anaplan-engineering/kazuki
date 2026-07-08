package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.condition
import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.integer
import com.anaplan.engineering.kazuki.core.toNat
import com.anaplan.engineering.kazuki.core.unreachable
import kotlin.test.Test
import kotlin.test.assertEquals

class TestAnimationCondition {

    @Test
    fun testAnimationCondition() {
        val fn = function(
            command = { i: integer ->
                fun doComplexOperation(i: integer) = i * -1

                val j = doComplexOperation(i)
                condition { j > 0 }
                j.toNat()
            }
        )

        assertEquals(1uL, fn(-1))
        causesAnimationConditionFailure { fn(1) }
    }
}


