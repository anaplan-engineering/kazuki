package com.anaplan.engineering.kazuki.core

// TODO -- extend tuple if left already is one (as required)
fun <T1, T2> Iterable<T1>.product(other: Iterable<T2>): Iterable<Tuple2<T1, T2>> =
    flatMap { i1 -> other.map { i2 -> mk_(i1, i2) } }