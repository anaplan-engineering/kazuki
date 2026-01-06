package com.anaplan.engineering.kazuki.core

// TODO -- generate this file -- all of the below to suitable size!


fun <I, OD, OR> mapping(
    provider: Iterable<I>,
    filter: (I) -> Boolean,
    selector: (I) -> Tuple2<OD, OR>
) = as_Mapping(provider.filter(filter).map(selector))

fun <I, OD, OR> mapping(
    provider: Iterable<I>,
    selector: (I) -> Tuple2<OD, OR>
) = mapping(provider, { true }, selector)


fun <I1, I2, OD, OR> mapping(
    p1: Iterable<I1>,
    p2: Iterable<I2>,
    filter: (I1, I2) -> Boolean,
    selector: (I1, I2) -> Tuple2<OD, OR>
) = as_Mapping((cartesianProduct(p1, p2)).filter(tupleAdapter(filter)).map(tupleAdapter(selector)))