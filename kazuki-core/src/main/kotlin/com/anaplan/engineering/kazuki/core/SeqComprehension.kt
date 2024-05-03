package com.anaplan.engineering.kazuki.core


// TODO -- generate this file -- all of the below to suitable size!

fun <I, O> seq(
    provider: Iterable<I>,
    selector: (I) -> O
) = seq(provider, { true }, selector)

fun <I, O> seq(
    provider: Iterable<I>,
    filter: (I) -> Boolean,
    selector: (I) -> O
) = as_Seq(provider.filter(filter).map(selector))

