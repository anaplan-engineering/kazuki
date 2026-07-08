package com.anaplan.engineering.kazuki.core.test

import com.anaplan.engineering.kazuki.core.function
import com.anaplan.engineering.kazuki.core.unreachable
import kotlin.test.Test
import kotlin.test.assertEquals

class TestUnreachable {

    @Test
    fun testUnreachable() {
        val fn = function(
            command = { i: Int ->
                if (i < 0) {
                    unreachable()
                } else {
                    i + 1
                }
            }
        )

        assertEquals(1, fn(0))
        causesUnreachableFailure { fn(-1) }
    }
}


