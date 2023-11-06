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
