package com.anaplan.engineering.kazuki.toolkit.sequence

import com.anaplan.engineering.kazuki.core.*

class SequenceFunctions<T> {

    val countOf = function(
        command = { t: T, s: Sequence<T> -> s.count { it == t } },
        post = { t, s, result -> result == (s rrt mk_Set(t)).len }
    )

    val permutation = function(
        command = { s1: Sequence<T>, s2: Sequence<T> ->
            s1.len == s2.len && forall(s1.elems) { e -> countOf(e, s1) == countOf(e, s2) }
        }
    )

}
