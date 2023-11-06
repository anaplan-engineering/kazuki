package com.anaplan.engineering.kazuki.core

inline fun <reified T : Enum<T>> asSet(): Set1<T> = as_Set1(enumValues<T>())

inline fun <reified T : Enum<T>> asSequence(): Sequence1<T> = as_Seq1(enumValues<T>().toList())