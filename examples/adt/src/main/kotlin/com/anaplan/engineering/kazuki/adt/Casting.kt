package com.anaplan.engineering.kazuki.adt

import com.anaplan.engineering.kazuki.core.Module
import com.anaplan.engineering.kazuki.core.nat

@Module(makeable=false)
interface A {
    val x: nat
}

@Module
interface B : A

@Module
interface C: A