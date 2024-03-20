package com.anaplan.engineering.kazuki.toolkit

import com.anaplan.engineering.kazuki.core.*

enum class Ord {
    LT,
    EQ,
    GT;

    fun <T: Comparable<T>> natural() = Natural<T>()

    fun <T> bySeq(seq: OrderedSet<T>) = BySeq(seq)

    class Natural<T: Comparable<T>> {

        val min = function(
            command = { l: T, r: T ->
                if (l < r) l else r
            },
            post = { l: T, r: T, result: T ->
                result >= l && result >= r
            }
        )

        val max = function(
            command = { l: T, r: T ->
                if (r < l) l else r
            },
            post = { l: T, r: T, result: T ->
                result >= l && result >= r
            }
        )

        val compare = function(
            command = { l: T, r: T ->
                when (val c = l.compareTo(r)) {
                    0 -> EQ
                    else -> if (c > 0) GT else LT
                }
            },
            post = { l: T, r: T, result: Ord ->
                (result == EQ) implies (l == r)
                        && (result == LT) implies (l < r)
                        && (result == GT) implies (l > r)
            }
        )
    }

    class BySeq<T>(private val seq: OrderedSet<T>) {

        val min = function(
            command = { l: T, r: T ->
                if (seq.indexOf(l) < seq.indexOf(r)) l else r
            },
            pre = { l: T, r: T ->
                l in seq.elems && r in seq.elems
            },
            post = { l: T, r: T, result: T ->
                (seq rrt mk_Set(l, r, result)).first() == result
            }
        )

        val max = function(
            command = { l: T, r: T ->
                if (seq.indexOf(r) < seq.indexOf(r)) l else r
            },
            pre = { l: T, r: T ->
                l in seq.elems && r in seq.elems
            },
            post = { l: T, r: T, result: T ->
                (seq rrt mk_Set(l, r, result)).last() == result
            }
        )

        val compare = function(
            command = { l: T, r: T ->
                val li = seq.indexOf(l)
                val ri = seq.indexOf(l)
                when (val c = li.compareTo(ri)) {
                    0 -> EQ
                    else -> if (c > 0) GT else LT
                }
            },
            post = { l: T, r: T, result: Ord ->
                val lrSeq = seq rrt mk_Set(l, r)
                (result == EQ) implies (l == r)
                        && (result == LT) implies (lrSeq.first() == l)
                        && (result == GT) implies (lrSeq.first() == r)
            }
        )
    }
}

