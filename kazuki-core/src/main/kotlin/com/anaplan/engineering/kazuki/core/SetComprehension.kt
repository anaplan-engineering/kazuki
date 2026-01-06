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
) = as_Set((cartesianProduct(p1, p2)).filter(tupleAdapter(filter)).map(tupleAdapter(selector)))

fun <I1, I2, I3, O> set(
    p1: Iterable<I1>,
    p2: Iterable<I2>,
    p3: Iterable<I3>,
    filter: (I1, I2, I3) -> Boolean,
    selector: (I1, I2, I3) -> O
) = as_Set((cartesianProduct(p1, p2, p3)).filter(tupleAdapter(filter)).map(tupleAdapter(selector)))

fun <I1, I2, O> tupleAdapter(fn: (I1, I2) -> O): (Tuple2<I1, I2>) -> O = { t -> fn(t._1, t._2) }
fun <I1, I2, I3, O> tupleAdapter(fn: (I1, I2, I3) -> O): (Tuple3<I1, I2, I3>) -> O = { t -> fn(t._1, t._2, t._3) }

fun <T1, T2> cartesianProduct(l: Iterable<T1>, r: Iterable<T2>) = l.flatMap { t -> r.map { u -> mk_(t, u) } }
fun <T1, T2, T3> cartesianProduct(i1: Iterable<T1>, i2: Iterable<T2>, i3: Iterable<T3>) =
    i1.flatMap { t1 ->
        i2.flatMap { t2 ->
            i3.map { t3 -> mk_(t1, t2, t3) }
        }
    }

fun <T1, T2, T3, T4, T5> cartesianProduct(
    i1: Iterable<T1>,
    i2: Iterable<T2>,
    i3: Iterable<T3>,
    i4: Iterable<T4>,
    i5: Iterable<T5>
) =
    as_Set(
        i1.flatMap { t1 ->
            i2.flatMap { t2 ->
                i3.flatMap { t3 ->
                    i4.flatMap { t4 ->
                        i5.map { t5 -> mk_(t1, t2, t3, t4, t5) }
                    }
                }
            }
        }
    )

fun <T1, T2, T3, T4, T5, T6, T7> cartesianProduct(
    i1: Iterable<T1>,
    i2: Iterable<T2>,
    i3: Iterable<T3>,
    i4: Iterable<T4>,
    i5: Iterable<T5>,
    i6: Iterable<T6>,
    i7: Iterable<T7>,
) =
    as_Set(
        i1.flatMap { t1 ->
            i2.flatMap { t2 ->
                i3.flatMap { t3 ->
                    i4.flatMap { t4 ->
                        i5.flatMap { t5 ->
                            i6.flatMap { t6 ->
                                i7.map { t7 -> mk_(t1, t2, t3, t4, t5, t6, t7) }
                            }
                        }
                    }
                }
            }
        }
    )




