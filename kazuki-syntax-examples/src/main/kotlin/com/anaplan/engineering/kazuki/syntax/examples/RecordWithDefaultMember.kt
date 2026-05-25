package com.anaplan.engineering.kazuki.syntax.examples

import com.anaplan.engineering.kazuki.core.*

@Module
interface RecordWithDefaultMember {

    val foo: nat
    val bar: nat get() = 42u
}

@Module
interface RecordWithInheritedDefaultMember : RecordWithDefaultMember {

    val xyzzy: nat
}