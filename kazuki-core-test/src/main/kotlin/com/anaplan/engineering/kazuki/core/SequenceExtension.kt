package com.anaplan.engineering.kazuki.core

@Module
interface SequenceExtension<T>: Sequence<T> {
}

@Module
interface PrettySequenceExtension<T>: Sequence<T>, PrettyPrintable {

    override fun pretty() = "[[" + joinToString(", ") { if (it is PrettyPrintable) it.pretty() else it.toString() } + "]]"
}

@Module
interface Sequence1Extension<T>: Sequence1<T> {
}
