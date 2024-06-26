package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.*

enum class Ord {
    LT,
    EQ,
    GT;

    companion object {
        fun <T : Comparable<T>> natural() = Natural<T>()

        fun <T> bySeq(seq: OrderedSet<T>) = BySeq(seq)

        class Natural<T : Comparable<T>> {

            val min = function(
                command = { l: T, r: T -> if (l < r) l else r },
                post = { l, r, result ->
                    when (result) {
                        l -> r >= l
                        r -> l >= r
                        else -> false
                    }
                }
            )

            val max = function(
                command = { l: T, r: T -> if (r < l) l else r },
                post = { l, r, result ->
                    when (result) {
                        l -> r <= l
                        r -> l <= r
                        else -> false
                    }
                }
            )

            val compare = function(
                command = { l: T, r: T ->
                    when (val c = l.compareTo(r)) {
                        0 -> EQ
                        else -> if (c > 0) GT else LT
                    }
                },
                post = { l, r, result ->
                    when (result) {
                        EQ -> l == r
                        LT -> l < r
                        GT -> l > r
                    }
                }
            )
        }

        class BySeq<T>(private val seq: OrderedSet<T>) {

            val min = function(
                command = { l: T, r: T -> if (seq.indexOf(l) < seq.indexOf(r)) l else r },
                pre = { l, r -> l in seq.elems && r in seq.elems },
                post = { l, r, result -> (seq rrt mk_Set(l, r)).first() == result }
            )

            val max = function(
                command = { l: T, r: T -> if (seq.indexOf(r) < seq.indexOf(l)) l else r },
                pre = { l, r -> l in seq.elems && r in seq.elems },
                post = { l, r, result -> (seq rrt mk_Set(l, r)).last() == result }
            )

            val compare = function(
                command = { l: T, r: T ->
                    val li = seq.indexOf(l)
                    val ri = seq.indexOf(r)
                    when (val c = li.compareTo(ri)) {
                        0 -> EQ
                        else -> {
                            if (c > 0) GT else LT
                        }
                    }
                },
                pre = { l, r -> l in seq.elems && r in seq.elems },
                post = { l, r, result ->
                    val lrSeq = seq rrt mk_Set(l, r)
                    when (result) {
                        EQ -> lrSeq == mk_Seq(l)
                        LT -> lrSeq == mk_Seq(l, r)
                        GT -> lrSeq == mk_Seq(r, l)
                    }
                }
            )
        }
    }
}

