package com.anaplan.engineering.kazuki.core


// TODO -- generate this file -- all of the below to suitable size!

fun <I, O> set(
    provider: Iterable<I>,
    selector: (I) -> O
) = set(provider, { true }, selector)

    fun <I, O> set(
        provider: Iterable<I>,
        filter: (I) -> Boolean,
        selector: (I) -> O
    ) = as_Set(provider.filter(filter).map(selector))

fun <I1, I2, O> set(
    p1: Iterable<I1>,
    p2: Iterable<I2>,
    selector: (I1, I2) -> O
) = set(p1, p2, { _, _ -> true }, selector)

fun <I1, I2, O> set(
    p1: Iterable<I1>,
    p2: Iterable<I2>,
    filter: (I1, I2) -> Boolean,
    selector: (I1, I2) -> O
) = as_Set((cross(p1, p2)).filter(tupleAdapter(filter)).map(tupleAdapter(selector)))

fun <I1, I2, I3, O> set(
    p1: Iterable<I1>,
    p2: Iterable<I2>,
    p3: Iterable<I3>,
    filter: (I1, I2, I3) -> Boolean,
    selector: (I1, I2, I3) -> O
) = as_Set((cross(p1, p2, p3)).filter(tupleAdapter(filter)).map(tupleAdapter(selector)))

fun <I1, I2, O> tupleAdapter(fn: (I1, I2) -> O): (Tuple2<I1, I2>) -> O = { t -> fn(t._1, t._2) }
fun <I1, I2, I3, O> tupleAdapter(fn: (I1, I2, I3) -> O): (Tuple3<I1, I2, I3>) -> O = { t -> fn(t._1, t._2, t._3) }

private fun <T1, T2> cross(l: Iterable<T1>, r: Iterable<T2>) = l.flatMap { t -> r.map { u -> mk_(t, u) } }
private fun <T1, T2, T3> cross(i1: Iterable<T1>, i2: Iterable<T2>, i3: Iterable<T3>) =
    i1.flatMap { t1 ->
        i2.flatMap { t2 ->
            i3.map { t3 -> mk_(t1, t2, t3) }
        }

    }


internal operator fun <T1, T2, U> Iterable<Tuple2<T1, T2>>.times(other: Iterable<U>) =
    as_Set(flatMap { t -> other.map { u -> mk_(t._1, t._2, u) } })

val x = set(mk_Set(1, 2, 3), { it != 2 }) { it * 2 }

val y1 = set(mk_Set(1, 2, 3), mk_Set("a", "b")) { i1, i2 -> i1 }

val y2 = set(mk_Set(1, 2, 3), mk_Set("a", "b"), { i1, i2 -> i1 > 1 }) { i1, i2 -> i1 }

val z = (mk_Set(1, 2, 3) x mk_Set(1, 2, 3)) * mk_Set(1, 2, 3)
