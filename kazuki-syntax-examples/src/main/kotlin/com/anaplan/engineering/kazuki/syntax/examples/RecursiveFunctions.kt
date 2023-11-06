package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.arbitrary
import com.anaplan.engineering.kazuki.core.card
import com.anaplan.engineering.kazuki.core.function

object RecursiveFunctions {

    val noRecursion = function(
        command = { t: Set<Int> -> t.card }
    )

    val validRecursion: (Set<Int>) -> Int by lazy {
        function(
            command = { t -> if (t.isEmpty()) 0 else 1 + validRecursion(t - t.arbitrary()) },
            measure = { t -> t.card }
        )
    }

    val invalidRecursion: (Set<Int>) -> Int by lazy {
        function(
            command = { t -> 1 + invalidRecursion(if (t.isEmpty()) t else t - t.arbitrary()) },
            measure = { t -> t.card }
        )
    }
}