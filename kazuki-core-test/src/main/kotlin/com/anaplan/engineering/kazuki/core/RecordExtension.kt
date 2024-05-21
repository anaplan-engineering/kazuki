package com.anaplan.engineering.kazuki.core

@Module
interface Record {
    val a: Int
}

@Module
interface RecordExtension: Record {
    val b: String
}

@Module
interface RecordInvOnlyExtension: Record {
    @Invariant
    fun notZero() = a != 0
}

@Module
interface OtherRecord {
    val a: Int
}