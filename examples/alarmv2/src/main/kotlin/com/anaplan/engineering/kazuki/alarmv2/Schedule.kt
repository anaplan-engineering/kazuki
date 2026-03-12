package com.anaplan.engineering.kazuki.alarmv2

import com.anaplan.engineering.kazuki.core.*

typealias Period = Sequence<Char>

@Module
interface Schedule : Mapping<Period, Set1<Expert>> {
    @Invariant
    fun expertIdentifiersUniqueInSet() =
        forall(rng) { exs ->
            forall(exs, exs) {  ex1, ex2 ->
                (ex1 != ex2) implies (ex1.expertId != ex2.expertId)
            }
        }
}