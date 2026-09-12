package com.anaplan.engineering.kazuki.core

@Module(makeable = false)
@ImplementedBy(EmptyAdtConcrete::class)
interface EmptyAdt

@Module
internal interface EmptyAdtConcrete : EmptyAdt {
    val value: Int
}
