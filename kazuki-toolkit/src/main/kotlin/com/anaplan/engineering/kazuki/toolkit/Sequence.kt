package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.*

object SequenceOrd {

    fun <T : Comparable<T>> natural() = Natural<T>()

    fun <T> byFunction(ordFn: (T, T) -> Ord) = ByFunction(ordFn)

    class Functions<T> {
        val countOf = function(
            command = { t: T, s: Sequence<T> -> s.count { it == t } },
            post = { t: T, s: Sequence<T>, result: Int ->
                result == (s rrt mk_Set(t)).len
            }
        )

        val permutation = function(
            command = { s1: Sequence<T>, s2: Sequence<T> ->
                s1.len == s2.len && forall(s1.elems) { e -> countOf(e, s1) == countOf(e, s2) }
            }
        )
    }

    class Natural<T : Comparable<T>> {
        val ascending = function(
            command = { s: Sequence<T> -> forall(s.inds) { i -> s[i] <= s[i + 1] } },
            post = { s: Sequence<T>, result: Boolean -> result != descending(s.reverse()) }
        )

        val descending = function(
            command = { s: Sequence<T> -> forall(s.inds) { i -> s[i] >= s[i + 1] } }
            // can't use ascending in post without loop
        )

        // TODO requires construct capability
        // TODO measure
        val insert by lazy {
            fun command(t: T, s: Sequence<T>): Sequence<T> =
                when {
                    s.isEmpty() -> mk_Seq(t)
                    s.len == 1 -> {
                        val u = s.single()
                        if (t <= u) mk_Seq(t, u) else mk_Seq(u, t)
                    }

                    else -> {
                        val u = s.first()
                        if (t <= u) mk_Seq(t) cat s else mk_Seq(u) cat command(t, s.drop(1))
                    }
                }
            function(
                command = ::command,
                pre = { _: T, s: Sequence<T> -> ascending(s) },
                post = { t: T, s: Sequence<T>, result: Sequence<T> ->
                    ascending(result) && Functions<T>().permutation(mk_Seq(t) cat s, result)
                }
            )
        }
    }

    private val LTE = mk_Set(Ord.LT, Ord.EQ)

    class ByFunction<T>(
        private val ordFn: (T, T) -> Ord
    ) {
        val ascending = function(
            command = { s: Sequence<T> -> forall(s.inds) { i -> ordFn(s[i], s[i + 1]) in LTE } },
            post = { s: Sequence<T>, result: Boolean -> result != descending(s.reverse()) }
        )

        val descending = function(
            command = { s: Sequence<T> -> forall(s.inds) { i -> ordFn(s[i + 1], s[i]) in LTE } }
            // can't use ascending in post without loop
        )
    }

}


