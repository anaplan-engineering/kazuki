package com.anaplan.engineering.kazuki.toolkit.sequence

import com.anaplan.engineering.kazuki.core.*
import com.anaplan.engineering.kazuki.toolkit.Ord

object SequenceOrd {

    fun <T : Comparable<T>> natural() = Natural<T>()

    fun <T> byFunction(ordFn: (T, T) -> Ord) = ByFunction(ordFn)

    class Natural<T : Comparable<T>> {
        val ascending = function(
            command = { s: Sequence<T> -> forall(s.inds) { i -> s[i] <= s[i + 1] } },
            post = { s, result -> result != descending(s.reverse()) }
        )

        val descending = function(
            command = { s: Sequence<T> -> forall(s.inds) { i -> s[i] >= s[i + 1] } }
            // can't use ascending in post without loop
        )

        // TODO requires construct capability
        val insert: VFunction2<T, Sequence<T>, Sequence<T>> by lazy {
            function(
                command = { t, s ->
                    when {
                        s.isEmpty() -> mk_Seq(t)
                        else -> {
                            val u = s.first()
                            if (t <= u) mk_Seq(t) cat s else mk_Seq(u) cat insert(t, s.drop(1))
                        }
                    }
                },
                pre = { _, s -> ascending(s) },
                post = { t, s, result ->
                    ascending(result) && SequenceFunctions<T>().permutation(mk_Seq(t) cat s, result)
                },
                measure = { _, s -> s.len }
            )
        }
    }

    private val LTE = mk_Set(Ord.LT, Ord.EQ)

    class ByFunction<T>(
        private val ordFn: (T, T) -> Ord
    ) {
        val ascending = function(
            command = { s: Sequence<T> -> forall(s.inds) { i -> ordFn(s[i], s[i + 1]) in LTE } },
            post = { s, result -> result != descending(s.reverse()) }
        )

        val descending = function(
            command = { s: Sequence<T> -> forall(s.inds) { i -> ordFn(s[i + 1], s[i]) in LTE } }
            // can't use ascending in post without loop
        )
    }

}


